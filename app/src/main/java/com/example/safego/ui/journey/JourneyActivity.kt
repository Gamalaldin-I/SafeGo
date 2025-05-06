package com.example.safego.ui.journey

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import com.example.safego.databinding.ActivityJourneyBinding
import com.example.safego.ui.journey.MapFragment.Companion.newInstance
import com.example.safego.util.adapters.FragmentAdapter
import com.example.safego.util.helpers.LocationHelper
import com.example.safego.util.helpers.singlton.DialogBuilder
import com.example.safego.util.helpers.TensorFlowLiteHelper
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.tabs.TabLayoutMediator


class JourneyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJourneyBinding
    private lateinit var statusTextView: TextView
    private lateinit var tflite: TensorFlowLiteHelper
    private lateinit var mapFragment: MapFragment
    private lateinit var nearbyPlacesFragment: NearbyPlacesFragment
    private val viewModel: JourneyActivityViewModel by viewModels()

    // Journey Data
    private lateinit var duration: String
    private lateinit var distance: String
    private lateinit var polyline: String
    private lateinit var destinationLocation: Pair<Double, Double>
    private lateinit var currentLocation: Pair<Double, Double>

    @SuppressLint("MissingInflatedId", "ResourceType", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJourneyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views and data
        statusTextView = binding.statusTextView
        tflite = TensorFlowLiteHelper(this)

        // Check permissions and start camera if granted
        if (allPermissionsGranted()) {
            DialogBuilder.showLoadingDialog(this, "Starting Camera ....")
            viewModel.startCamera(binding.viewFinder, tflite, this)
            getDataFromIntent()
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
            journeyViewModel = viewModel,
            polyline = polyline
        )

        // Create info fragment
        val infoFragment = InfoFragment().newInstance(
            distance = distance,
            speed = "0 km/h",
            predictionTime = duration,
            journeyViewModel = viewModel,
            currentLocation = LocationHelper.getCurrentPlaceName(
                LatLng(currentLocation.first, currentLocation.second), this
            ).split(",")[1],
            destination = LocationHelper.getCurrentPlaceName(
                LatLng(destinationLocation.first, destinationLocation.second), this
            ).split(",")[1]
        )

        nearbyPlacesFragment = NearbyPlacesFragment()

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
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                mapFragment.updateCurrentLocation()
                Log.i("TimerE", "الكود تنفذ")
                handler.postDelayed(this, 60000)
            }
        }
        handler.post(runnable)
    }


}
