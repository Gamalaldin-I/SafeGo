package com.example.safego.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.hardware.SensorManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safego.R
import com.example.safego.databinding.ActivityMapBinding
import com.example.safego.domain.useCaseModel.LocationAddress
import com.example.safego.ui.journey.JourneyActivity
import com.example.safego.util.adapters.LocationsAdapter
import com.example.safego.util.helpers.LocationHelper
import com.example.safego.util.helpers.singlton.Animator
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.Locale

@Suppress("DEPRECATION")
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private lateinit var mMap: GoogleMap
    private val viewModel: MapViewModel by viewModels()

    private lateinit var placesAdapter: LocationsAdapter
    private val placesList = ArrayList<LocationAddress>()
    private lateinit var placesClient: PlacesClient
    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var myMarker: Marker? = null
    private lateinit var currentLoc: LatLng
    private lateinit var destinationLoc: LatLng

    private var durationV = ""
    private var distanceV = ""
    private var polyline = ""

    companion object {
        private const val SPEECH_REQUEST_CODE = 100
        private const val DEFAULT_SEARCH_HINT = "Search here"
        private const val DEFAULT_ZOOM_LEVEL = 10f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeComponents()
        setupMap()
        setupUIControllers()
        setupSearchBar()

        viewModel.getUserLocationPair(this, this)
        viewModel.currentLocationPair.observe(this) { currentLoc = it }
    }

    private fun initializeComponents() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        moveCameraToInitialLocation()

        try {
            val success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark))
            if (!success) Log.e("MapStyle", "Style parsing failed.")
        } catch (e: Resources.NotFoundException) {
            Log.e("MapStyle", "Can't find style. Error: ", e)
        }

            markCurrentLocation()

        mMap.setOnMapClickListener { latLng ->
            val locationName = LocationHelper.getCurrentPlaceName(latLng, this)
            //mark the place as a new Dist
            myMarker?.remove()
            myMarker = mMap.addMarker(MarkerOptions().position(latLng).title(locationName))
            destinationLoc = latLng
            ableToViewNameTrack(true, locationName, latLng)
            afterSelectingLocation()
            handleMicSearch(locationName)
        }
    }


    private fun moveCameraToInitialLocation() {
        val cairo = LatLng(30.0444, 31.2357)
        mMap.addMarker(MarkerOptions().position(cairo).title("Marker in Cairo"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cairo, DEFAULT_ZOOM_LEVEL))
    }

    private fun markCurrentLocation() {
        val icon = BitmapFactory.decodeResource(resources, R.drawable.back_arrow)
        viewModel.markTheCurrentLocOnMapAndManageRotation(this, this, myMarker, mMap, icon, sensorManager)
    }

    private fun setupUIControllers() = with(binding) {
        searchTxt.setOnClickListener { handleSearchTextClicked() }
        searchLocIcon.setOnClickListener { searchLocationFromText() }
        mic.setOnClickListener { Animator.animateTxt(mic) { startSpeechRecognition() } }
        backArrow.setOnClickListener { handleBackPressed() }
        delete.setOnClickListener { clearSearchField() }
        directionBtn.setOnClickListener { rotateButtonAndDrawTrack() }
        hideBtn.setOnClickListener { hideDetails() }
        startJourney.setOnClickListener {
            val intent = Intent(this@MapActivity, JourneyActivity::class.java)
            intent.putExtra("duration", durationV)
            intent.putExtra("distance", distanceV)
            intent.putExtra("polyline", polyline)
            val lat = destinationLoc.latitude
            val long = destinationLoc.longitude
            intent.putExtra("destinationLat", lat)
            intent.putExtra("destinationLong", long)
            val cLoc = currentLoc.latitude
            val cLong = currentLoc.longitude
            intent.putExtra("currentLocLat", cLoc)
            intent.putExtra("currentLocLong", cLong)
            startActivity(intent)
            finish()
        }
    }

    private fun handleSearchTextClicked() = with(binding) {
        val text = searchTxt.text.toString()
        showSearchView()
        if (text != DEFAULT_SEARCH_HINT) searchBar.setQuery(text, false)
        Animator.animateSearchbar(searchBar, backArrow)
    }

    private fun searchLocationFromText() {
        val query = binding.searchTxt.text.toString()
        if (query.isNotEmpty() && query != DEFAULT_SEARCH_HINT) {
            viewModel.searchAndGoToLocation(query, this, mMap)
        }
    }

    private fun handleBackPressed() = with(binding) {
        showMapView()
        Animator.deAnimateSearchBar(searchBar, backArrow)
        val query = searchBar.query.toString()
        if (query.isEmpty()) {
            ableToViewNameTrack(false, DEFAULT_SEARCH_HINT, LatLng(0.0, 0.0))
        } else {
            destinationLoc = viewModel.getPairOfLoc(query, this@MapActivity)
            ableToViewNameTrack(true, query, LatLng(destinationLoc.latitude, destinationLoc.longitude))
            afterSelectingLocation()
        }
    }

    private fun rotateButtonAndDrawTrack() {
        binding.directionBtn.animate().rotationBy(180f).setDuration(200).start()
        viewModel.drawRoute(polyline, mMap, this)
        Animator.animateDetailsCard(binding.DestinationDetailsCard)
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "What are you looking for?")
        }
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Use Activity Result APIs instead")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (spokenText.isNotEmpty()) {
                destinationLoc = viewModel.getPairOfLoc(spokenText, this)
                ableToViewNameTrack(true, spokenText, LatLng(destinationLoc.latitude, destinationLoc.longitude))
                handleMicSearch(spokenText)
            }
        }
    }

    private fun handleMicSearch(query: String) = with(binding) {
        mic.visibility = GONE
        delete.visibility = VISIBLE
        viewModel.searchAndGoToLocation(query, this@MapActivity, mMap)
    }

    private fun setupSearchBar() {
        placesAdapter = LocationsAdapter(placesList) { place ->
            binding.searchBar.setQuery(place, false)
            viewModel.searchAndGoToLocation(place, this, mMap)
            showMapView()
            destinationLoc = viewModel.getPairOfLoc(place, this)
            ableToViewNameTrack(true, place, LatLng(destinationLoc.latitude, destinationLoc.longitude))
        }
        binding.locationsAdapter.layoutManager = LinearLayoutManager(this)
        binding.locationsAdapter.adapter = placesAdapter

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.searchAndGoToLocation(it, this@MapActivity, mMap)
                    showMapView()
                    destinationLoc = viewModel.getPairOfLoc(it, this@MapActivity)
                    ableToViewNameTrack(true, it, LatLng(destinationLoc.latitude, destinationLoc.longitude))
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return if (newText.isNullOrEmpty()) {
                    binding.locationsAdapter.visibility = GONE
                    true
                } else {
                    viewModel.showSuggestions(placesClient, newText, this@MapActivity, placesAdapter, placesList, binding)
                    true
                }
            }
        })
    }

    private fun clearSearchField() = with(binding) {
        mic.visibility = VISIBLE
        searchLocIcon.visibility = VISIBLE
        delete.visibility = GONE
        ableToViewNameTrack(false, DEFAULT_SEARCH_HINT, LatLng(0.0, 0.0))
    }

    private fun showSearchView() = with(binding) {
        searchPageView.visibility = VISIBLE
        mainView.visibility = GONE
    }

    private fun showMapView() = with(binding) {
        searchPageView.visibility = GONE
        mainView.visibility = VISIBLE
    }

    private fun ableToViewNameTrack(able: Boolean, desName: String, des: LatLng) = with(binding) {
        searchTxt.text = if (able) desName else DEFAULT_SEARCH_HINT
        directionBtn.visibility = if (able) VISIBLE else GONE
        hideDetails()
        if (able) {
            showDetailsCard(des, desName)
        }
    }

    private fun afterSelectingLocation() = with(binding) {
        mic.visibility = GONE
        delete.visibility = VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun showDetailsCard(destination: LatLng, destinationName: String) = with(binding) {
        if (::currentLoc.isInitialized && destinationName != DEFAULT_SEARCH_HINT && destination != LatLng(0.0, 0.0)) {
            val cLoc = LatLng(currentLoc.latitude, currentLoc.longitude)
            viewModel.getDurationAndDistanceOfDestination(cLoc, destination, this@MapActivity)
            viewModel.desInformation.observe(this@MapActivity) {
                durationV = it.first
                distanceV = it.second
                polyline = it.third
                distance.text = distanceV
                duration.text = durationV
                distination.text = destinationName
                directionBtn.animate().alpha(0f).setDuration(0).withEndAction {
                    directionBtn.animate().alpha(1f).setDuration(400).start()
                }
            }
        } else {
            hideDetails()
            Toast.makeText(this@MapActivity, "Error while getting location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideDetails() {
        Animator.hideDetailsCard(binding.DestinationDetailsCard)
    }
}