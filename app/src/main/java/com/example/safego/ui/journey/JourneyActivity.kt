package com.example.safego.ui.journey

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.safego.databinding.ActivityJourneyBinding
import com.example.safego.domain.useCaseModel.NearbyPlace
import com.example.safego.ui.journey.MapFragment.Companion.newInstance
import com.example.safego.util.InfoDialogFragment
import com.example.safego.util.adapters.FragmentAdapter
import com.example.safego.util.helpers.LocationHelper
import com.example.safego.util.helpers.TensorFlowLiteHelper
import com.example.safego.util.helpers.singlton.Alarm
import com.example.safego.util.helpers.singlton.DialogBuilder
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch


class JourneyActivity : AppCompatActivity(),NearbyClickListener {
    private lateinit var binding: ActivityJourneyBinding
    private lateinit var statusTextView: TextView
    private lateinit var tflite: TensorFlowLiteHelper
    private lateinit var mapFragment: MapFragment
    private lateinit var snackBar: Snackbar
    private lateinit var nearbyPlacesFragment: NearbyPlacesFragment
    val viewModel: JourneyActivityViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory(application)
    }

    // Journey Data
    private lateinit var duration: String
    private lateinit var distance: String
    private lateinit var polyline: String
    private lateinit var vehicleType: String
    private lateinit var destinationLocation: Pair<Double, Double>
    private lateinit var currentLocation: Pair<Double, Double>
    private var bottomSheetDialog: InfoDialogFragment? = null

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    @SuppressLint("MissingInflatedId", "ResourceType", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJourneyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        snackBar = Snackbar.make(binding.root, "You are over the speed limit!", Snackbar.LENGTH_INDEFINITE)
            .setAction("Dismiss") {
                snackBar.dismiss()
            }
        snackBar.view.setBackgroundColor(Color.RED)
        snackBar.view.backgroundTintList = ColorStateList.valueOf(Color.RED)
        snackBar.setActionTextColor(Color.WHITE)

        observeSpeedLimit()

        // Initialize views and data
        statusTextView = binding.statusTextView
        tflite = TensorFlowLiteHelper(this)

        // Check permissions and start camera if granted
        if (allPermissionsGranted()) {
           // DialogBuilder.showLoadingDialog(this, "Starting Camera ....")
           // viewModel.startCamera(binding.viewFinder, tflite, this)
            getDataFromIntent()
            Toast.makeText(this, vehicleType, Toast.LENGTH_SHORT).show()
            setupFragments()
        } else {
            requestPermissions()
        }

        // Observe confidence changes for drowsiness status
        viewModel.confidence.observe(this) { confidence ->
            val status = if (confidence > 50) "Drowsy 😴" else "Awake 🚗"
            statusTextView.text = "🔍 $status | Confidence: ${confidence.toInt()}%"
        }
    }

    private fun setupFragments() {
        // Create map fragment with journey details
        mapFragment = newInstance(
            destinationLat = destinationLocation.first,
            destinationLong = destinationLocation.second,
            currentLocLat = currentLocation.first,
            currentLocLong = currentLocation.second,
            polyline = polyline ,
            vehicleType = vehicleType
        )

        // Create info fragment
        val infoFragment = InfoFragment.newInstance(
            distance = distance,
            speed = 0.0,
            predictionTime = duration,
            currentLocation = LocationHelper.getCurrentPlaceName(
                LatLng(currentLocation.first, currentLocation.second), this
            ).split(",")[1],
            destination = LocationHelper.getCurrentPlaceName(
                LatLng(destinationLocation.first, destinationLocation.second), this
            ).split(",")[1]
        )

        nearbyPlacesFragment = NearbyPlacesFragment.getInstance(
            LatLng(currentLocation.first, currentLocation.second),
            this
        )

        // Set up the fragment adapter
        val fragmentList = arrayListOf(mapFragment, infoFragment, nearbyPlacesFragment)
        val fragmentTitleList = listOf("Navigation", "Driving status", "Nearby Places")
        val adapter = FragmentAdapter(this, fragmentList)

        binding.viewPager2.adapter = adapter
        binding.viewPager2.isUserInputEnabled = false

        // Setup TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = fragmentTitleList[position]
        }.attach()
        binding.endMonitor.setOnClickListener {
            finish()
        }

    }

    private fun requestPermissions() {
        requestPermissionsLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.startCamera(binding.viewFinder, tflite, this)
            } else {
                Toast.makeText(this, "يجب منح الإذن للكاميرا!", Toast.LENGTH_SHORT).show()
            }
        }

    private fun getDataFromIntent() {
        duration = intent.getStringExtra("duration").orEmpty()
        distance = intent.getStringExtra("distance").orEmpty()
        polyline = intent.getStringExtra("polyline").orEmpty()
        vehicleType = intent.getStringExtra("vehicleType").orEmpty()
        destinationLocation = Pair(
            intent.getDoubleExtra("destinationLat", 0.0),
            intent.getDoubleExtra("destinationLong", 0.0)
        )
        currentLocation = Pair(
            intent.getDoubleExtra("currentLocLat", 0.0),
            intent.getDoubleExtra("currentLocLong", 0.0)
        )
    }

    override fun onResume() {
            super.onResume()
         handler = Handler(Looper.getMainLooper())
         runnable = object : Runnable {
            override fun run() {
                lifecycleScope.launch {
                mapFragment.updateCurrentLocation()}
                viewModel.getRoadSpeedLimit(this@JourneyActivity,this@JourneyActivity)
                Log.i("TimerE", "الكود تنفذ")
                handler.postDelayed(this, 600000)
            }
        }
        handler.post(runnable)

    }
    override fun onBackPressed() {
        Toast.makeText(this, "You can't go back from here!", Toast.LENGTH_SHORT).show()
    }
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
        viewModel.stopAlert()
        DialogBuilder.cancelWarningDialog()
        if(bottomSheetDialog?.isAdded == true||bottomSheetDialog!=null){
            bottomSheetDialog?.dismiss()
            bottomSheetDialog = null
        }
    }


    private fun setupBottomSheet() {
        bottomSheetDialog = InfoDialogFragment.getInstance(
        )
        bottomSheetDialog?.isCancelable = false
    }

    override fun onClick(nearbyPlace: NearbyPlace) {
        if (bottomSheetDialog == null) setupBottomSheet()
        if (bottomSheetDialog?.isAdded == false) {
            bottomSheetDialog?.show(supportFragmentManager, "bottomSheet")
            Handler(Looper.getMainLooper()).postDelayed({
                bottomSheetDialog?.updateTheData(
                    nearbyPlace.latLng.latitude,
                    nearbyPlace.latLng.longitude,
                    nearbyPlace.name,
                    nearbyPlace.type,
                    nearbyPlace.distance,
                    nearbyPlace.image,
                    nearbyPlace.status,
                    nearbyPlace.rating,
                    nearbyPlace.openingHours
                )
            }, 100)
        }
    }
    private fun observeSpeedLimit(){
        viewModel.speedReached.observe(this){
            if(it){
                Alarm.speedAlert(this)
                snackBar.show()

            }
            else{
                Alarm.stopAlarm()
                snackBar.dismiss()
            }
        }
    }


}
