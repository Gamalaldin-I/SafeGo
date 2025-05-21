package com.example.safego.ui.journey

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.example.safego.dataSource.local.sharedPrefrences.SharedPref
import com.example.safego.domain.useCaseModel.JourneyDetails
import com.example.safego.domain.useCaseModel.NearbyPlace
import com.example.safego.domain.useCases.Speed.GetSpeedUpdatesUseCase
import com.example.safego.domain.useCases.camera.ImageClassifier
import com.example.safego.domain.useCases.currentLocation.GetCurrentLocationUseCase
import com.example.safego.domain.useCases.nearbyPlaces.GetNearByPlacesUseCase
import com.example.safego.domain.useCases.road.GetRoadSpeedUseCase
import com.example.safego.util.helpers.TensorFlowLiteHelper
import com.example.safego.util.helpers.singlton.Alarm
import com.example.safego.util.helpers.singlton.DialogBuilder
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class JourneyActivityViewModel(application: Application) : AndroidViewModel(application) {

    // region - Declarations
    @SuppressLint("StaticFieldLeak")
    private val contextOfApp = application.applicationContext
    private var pref: SharedPref? = null

    // Confidence from model
    private val _confidence = MutableLiveData<Float>()
    val confidence: LiveData<Float> get() = _confidence

    private val imageClassifier = ImageClassifier()

    // Nearby Places
    private val _nearbyPlaces = MutableLiveData<ArrayList<NearbyPlace>>()
    val nearbyPlaces: LiveData<ArrayList<NearbyPlace>> = _nearbyPlaces

    // Journey Details
    private val _journeyDetails = MutableLiveData<JourneyDetails>()
    val journeyDetailsLiveData: LiveData<JourneyDetails> get() = _journeyDetails

    // Speed
    private val getSpeedLimitUseCase = GetRoadSpeedUseCase()
    private val _speedLimit = MutableLiveData(0.0)
    val speedLimit: LiveData<Double> = _speedLimit


    private val _speedReached = MutableLiveData(false)
    val speedReached: LiveData<Boolean> = _speedReached


    private val getSpeedUpdatesUseCase = GetSpeedUpdatesUseCase()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(contextOfApp)
    private val _currentSpeed = MutableLiveData(0.0)
    val currentSpeed: LiveData<Double> = _currentSpeed
    private var locationCallback: LocationCallback? = null

    // Camera & Sleep Detection
    private var lastAnalysisTime = 0L
    private var lastDrowsyTime = 0L
    private var eyeSleepingTime = 0L
    private var isSleeping = false
    // endregion

    // region - Init
    init {
        locationCallback = getSpeedUpdatesUseCase(contextOfApp, fusedLocationClient) { speed ->
            _currentSpeed.postValue(speed)
        }
    }
    // endregion

    // region - Speed
    fun getRoadSpeedLimit(context: Context, activity: FragmentActivity) {
        viewModelScope.launch {
            val latLng = getUserLocationPair(context, activity)
            val limit = getSpeedLimitUseCase.getTheLimitSpeedOfRoad(latLng.latitude, latLng.longitude)
            _speedLimit.postValue(limit)
        }
    }
    fun speedReached(trueOrFalse: Boolean) {
        _speedReached.value = trueOrFalse
    }
    // endregion

    // region - Journey Info
    fun getJourneyDetails(duration: String, distance: String, start: LatLng, end: LatLng, vehicleType: String) {
        _journeyDetails.postValue(
            JourneyDetails(
                duration=duration,
                distance=distance,
                start=start,
                end = end,
                vehicleType=vehicleType)
        )
    }
    // endregion

    // region - Location
    suspend fun getUserLocationPair(context: Context, activity: FragmentActivity): LatLng {
        return GetCurrentLocationUseCase(context, activity).getPairOfCurrentLoc()
    }

    fun saveCurrentLocation(lat: String, lng: String, context: Context) {
        if (pref == null) pref = SharedPref(context)
        pref?.saveCurrentLocation(lat, lng)
    }
    // endregion

    // region - Nearby Places
    fun getPlaces(currentLat: String, currentLng: String, context: Context, activity: FragmentActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = GetNearByPlacesUseCase.getNearByPlaces(context, activity, currentLat, currentLng)
            _nearbyPlaces.postValue(result)
        }
    }
    // endregion

    // region - Camera & Sleep Detection
    fun startCamera(viewFinder: PreviewView, tflite: TensorFlowLiteHelper, lifecycleOwner: LifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(viewFinder.context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = createPreview(viewFinder)
                val imageAnalysis = createImageAnalysis(viewFinder, tflite, lifecycleOwner)

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)

                DialogBuilder.cancelLoadingDialog()
            } catch (e: Exception) {
                Log.e("CameraX", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(viewFinder.context))
    }

    private fun createPreview(viewFinder: PreviewView): Preview {
        return Preview.Builder().build().also {
            it.setSurfaceProvider(viewFinder.surfaceProvider)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun createImageAnalysis(viewFinder: PreviewView, tflite: TensorFlowLiteHelper, lifecycleOwner: LifecycleOwner): ImageAnalysis {
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val executor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(executor) { imageProxy ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastAnalysisTime >= 4000) {
                lastAnalysisTime = currentTime

                GlobalScope.launch(Dispatchers.Main) {
                    try {
                        val confidenceValue = imageClassifier.processImage(imageProxy, tflite)
                        _confidence.postValue(confidenceValue)
                        eyeState(confidenceValue, lifecycleOwner as Context)
                    } catch (e: Exception) {
                        Log.e("Analyzer", "Error analyzing image: ${e.message}")
                    } finally {
                        imageProxy.close()
                    }
                }
            } else {
                imageProxy.close()
            }
        }

        return imageAnalysis
    }

    private fun eyeState(confidence: Float, context: Context) {
        val currentTime = System.currentTimeMillis()

        if (confidence >= 50) {
            if (lastDrowsyTime == 0L) lastDrowsyTime = currentTime
            eyeSleepingTime = currentTime - lastDrowsyTime
        } else {
            stopAlert()
        }

        if (eyeSleepingTime >= 3000 && !isSleeping) {
            isSleeping = true
            sendSleepAlert(context)
            Alarm.drowsyAlert(context)
        }
    }

    fun stopAlert() {
        if (isSleeping) {
            isSleeping = false
            eyeSleepingTime = 0L
            lastDrowsyTime = 0L
            DialogBuilder.cancelWarningDialog()
            Alarm.stopAlarm()
        }
    }

    private fun sendSleepAlert(context: Context) {
        DialogBuilder.showWarningDialog(context)
    }
    // endregion

    // region - Clear
    override fun onCleared() {
        super.onCleared()
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }
    // endregion
}

