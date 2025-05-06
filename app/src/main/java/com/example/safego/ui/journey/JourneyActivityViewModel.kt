package com.example.safego.ui.journey

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safego.domain.useCaseModel.JourneyDetails
import com.example.safego.domain.useCases.camera.ImageClassifier
import com.example.safego.domain.useCases.currentLocation.CurrentLocationProvider
import com.example.safego.util.helpers.TensorFlowLiteHelper
import com.example.safego.util.helpers.singlton.Alarm
import com.example.safego.util.helpers.singlton.DialogBuilder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class JourneyActivityViewModel : ViewModel() {
    private val _confidence = MutableLiveData<Float>()
    val confidence: LiveData<Float> get() = _confidence

    private val imageClassifier = ImageClassifier()
    private var eyeSleepingTime = 0L
    private var lastDrowsyTime = 0L
    private var isSleeping = false


    private val _journeyDetails = MutableLiveData<JourneyDetails>()
    val journeyDetailsLiveData: LiveData<JourneyDetails> get() = _journeyDetails

    fun startCamera(
        viewFinder: PreviewView,
        tflite: TensorFlowLiteHelper,
        lifecycleOwner: LifecycleOwner
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(viewFinder.context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = createPreview(viewFinder)
                val imageAnalysis = createImageAnalysis(viewFinder, tflite, lifecycleOwner)

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                DialogBuilder.cancelLoadingDialog()

            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(viewFinder.context))
    }

    private fun createPreview(viewFinder: PreviewView): Preview {
        return Preview.Builder().build().also {
            it.setSurfaceProvider(viewFinder.surfaceProvider)
        }
    }

    private fun createImageAnalysis(
        viewFinder: PreviewView,
        tflite: TensorFlowLiteHelper,
        lifecycleOwner: LifecycleOwner
    ): ImageAnalysis {
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        // استخدام Executor لتحليل الصورة
        val executor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(executor) { imageProxy ->
            viewModelScope.launch(Dispatchers.Main) {
                val confidenceValue = imageClassifier.processImage(imageProxy, tflite)
                _confidence.postValue(confidenceValue)
                eyeState(confidenceValue, lifecycleOwner as Context)
            }
        }

        return imageAnalysis
    }

    private fun eyeState(confidence: Float, context: Context) {
        val currentTime = System.currentTimeMillis()

        // Check if eye is closed based on confidence
        if (confidence >= 50) {
            if (lastDrowsyTime == 0L) lastDrowsyTime = currentTime
            eyeSleepingTime = currentTime - lastDrowsyTime
        } else {
            stopAlert()
        }

        // Trigger alert if eye is closed for more than 3 seconds
        if (eyeSleepingTime >= 3000 && !isSleeping) {
            isSleeping = true
            sendSleepAlert(context)
            Alarm.playAlarm(context)
        }
    }

    private fun stopAlert() {
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

     suspend fun getUserLocationPair(context: Context, activity: FragmentActivity): LatLng =
     CurrentLocationProvider(context, activity).getPairOfCurrentLoc()

    fun getJourneyDetails(duration: String, distance: String, start: LatLng, end: LatLng) {
        _journeyDetails.value = JourneyDetails(
            duration = duration,
            distance = distance,
            start = start,
            end = end
        )
    }

}
