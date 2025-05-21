package com.example.safego.ui.map

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safego.R
import com.example.safego.dataSource.local.model.Destination
import com.example.safego.dataSource.local.repo.LocalRepoImp
import com.example.safego.databinding.ActivityMapBinding
import com.example.safego.domain.useCaseModel.LocationAddress
import com.example.safego.ui.journey.JourneyActivity
import com.example.safego.util.adapters.LocationsAdapter
import com.example.safego.util.helpers.LocationHelper
import com.example.safego.util.helpers.singlton.Animator
import com.example.safego.util.helpers.singlton.DialogBuilder
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Suppress("DEPRECATION")
class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding
    private lateinit var mMap: GoogleMap
    private var canClick = true
    private val viewModel: MapViewModel by viewModels()

    // Location related properties
    private lateinit var currentLoc: LatLng
    private lateinit var destinationLoc: LatLng
    private var myMarker: Marker? = null

    // Places related properties
    private lateinit var placesClient: PlacesClient
    private lateinit var placesAdapter: LocationsAdapter
    private val placesList = ArrayList<LocationAddress>()

    // Database
    private lateinit var db: LocalRepoImp
    private var listOfLocations = ArrayList<Destination>()

    // Route information
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
        setupUI()
        observeViewModel()
    }

    private fun initializeComponents() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
        db = LocalRepoImp(this)
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        applyMapStyle()
        setupMapListeners()
        viewModel.getUserLocationPair(this, this)
        binding.map.visibility = VISIBLE
    }

    private fun applyMapStyle() {
        try {
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark)
            )
            if (!success) Log.e("MapStyle", "Style parsing failed.")
        } catch (e: Resources.NotFoundException) {
            Log.e("MapStyle", "Can't find style. Error: ", e)
        }
    }

    private fun setupMapListeners() {
        markCurrentLocation()
        onMapClick()
    }

    private fun updateDestinationMarker(locationName: String, latLng: LatLng) {
        myMarker?.remove()
        myMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(locationName)
        )
        destinationLoc = latLng
    }

    private fun markCurrentLocation() {
        val icon = BitmapFactory.decodeResource(resources, R.drawable.back_arrow)
        viewModel.markTheCurrentLocOnMapAndManageRotation(
            this,
            this,
            myMarker,
            mMap,
            icon,
            getSystemService(Context.SENSOR_SERVICE) as SensorManager
        )
    }

    private fun setupUI() {
        setupSearchComponents()
        setupClickListeners()
    }

    private fun setupSearchComponents() {
        placesAdapter = LocationsAdapter(placesList) { place ->
            handlePlaceSelection(place)
        }

        binding.locationsAdapter.layoutManager = LinearLayoutManager(this)
        binding.locationsAdapter.adapter = placesAdapter

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { handleSearchQuery(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.isEmpty()) {
                        binding.locationsAdapter.visibility = GONE
                    } else {
                        viewModel.showSuggestions(
                            placesClient,
                            it,
                            this@MapActivity,
                            placesAdapter,
                            placesList,
                            binding
                        )
                    }
                }
                return true
            }
        })
    }

    private fun setupClickListeners() = with(binding) {
        searchTxt.setOnClickListener { showSearchView() }
        searchLocIcon.setOnClickListener { searchLocationFromText() }
        mic.setOnClickListener { startVoiceSearch() }
        backArrow.setOnClickListener { handleBackNavigation() }
        delete.setOnClickListener { clearSearch() }
        directionBtn.setOnClickListener { showRoute() }
        hideBtn.setOnClickListener { hideDetails() }
        lastSearch.setOnClickListener { showSavedLocations() }
        startJourney.setOnClickListener { startJourney() }
    }

    private fun showSearchView() = with(binding) {
        val text = searchTxt.text.toString()
        searchPageView.visibility = VISIBLE
        mainView.visibility = GONE

        if (text != DEFAULT_SEARCH_HINT) {
            searchBar.setQuery(text, false)
        }
        Animator.animateSearchbar(searchBar, backArrow)
    }

    private fun searchLocationFromText() {
        val query = binding.searchTxt.text.toString()
        if (query.isNotEmpty() && query != DEFAULT_SEARCH_HINT) {
            viewModel.searchAndGoToLocation(query, this, mMap)
        }
    }

    private fun startVoiceSearch() {
        Animator.animateTxt(binding.mic) {
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
    }

    private fun handleBackNavigation() = with(binding) {
        showMapView()
        Animator.deAnimateSearchBar(searchBar, backArrow)

        val query = searchBar.query.toString()
        if (query.isEmpty()) {
            resetSearchUI()
        } else {
            handleSearchQuery(query)
        }
    }

    private fun showRoute() {
        binding.directionBtn.animate().rotationBy(180f).setDuration(200).start()
        viewModel.drawRoute(polyline, mMap, this)
        Animator.animateDetailsCard(binding.DestinationDetailsCard)
        canClick = false
    }

    private fun showSavedLocations() {
        lifecycleScope.launch {
            listOfLocations = db.getAllDestinations() as ArrayList<Destination>
            withContext(Dispatchers.Main) {
                DialogBuilder.showSavedLocationsDialog(
                    onItemClick = { destination ->
                        handleSavedLocationSelection(destination)
                        DialogBuilder.cancelSavedLocationsDialog()
                    },
                    onDeleteClick = { destination ->
                            viewModel.deleteDestination(destination, db)
                    },
                    context = this@MapActivity,
                    listOfLocations = listOfLocations
                ){
                    viewModel.deleteAllDestinations(db)
                }
            }
        }
    }

    private fun handleSavedLocationSelection(destination: Destination) {
        destinationLoc = LatLng(destination.latitude, destination.longitude)
        updateDestinationUI(destination.name, destinationLoc, true)
        viewModel.goToSavedLocation(destination, this, mMap)
    }

    private fun startJourney() {
        val intent = Intent(this, JourneyActivity::class.java).apply {
            putExtras(getJourneyExtras())
        }
        startActivity(intent)
        finish()
    }

    private fun getJourneyExtras(): Bundle {
        return Bundle().apply {
            putString("duration", durationV)
            putString("distance", distanceV)
            putString("polyline", polyline)
            putDouble("destinationLat", destinationLoc.latitude)
            putDouble("destinationLong", destinationLoc.longitude)
            putDouble("currentLocLat", currentLoc.latitude)
            putDouble("currentLocLong", currentLoc.longitude)
            putString("vehicleType", getSelectedVehicleType())
        }
    }

    private fun getSelectedVehicleType(): String {
        return when (binding.radioGroup.checkedRadioButtonId) {
            R.id.carRB -> "car"
            R.id.busRB -> "bus"
            R.id.truckRB -> "Truck"
            else -> "Bike"
        }
    }

    private fun clearSearch() = with(binding) {
        mic.visibility = VISIBLE
        searchLocIcon.visibility = VISIBLE
        delete.visibility = GONE
        resetSearchUI()
    }

    private fun resetSearchUI() {
        updateDestinationUI(DEFAULT_SEARCH_HINT, LatLng(0.0, 0.0))
    }

    private fun showMapView() = with(binding) {
        searchPageView.visibility = GONE
        mainView.visibility = VISIBLE
    }

    private fun hideDetails() {
        Animator.hideDetailsCard(binding.DestinationDetailsCard)
        canClick = true
    }

    @Deprecated("Use Activity Result APIs instead")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.let { spokenText ->
                    handleVoiceSearchResult(spokenText)
                }
        }
    }

    private fun handleVoiceSearchResult(spokenText: String) {
        destinationLoc = viewModel.getPairOfLoc(spokenText, this)
        updateDestinationUI(spokenText, destinationLoc)
        handleMicSearch(spokenText)
    }

    private fun handleMicSearch(query: String) = with(binding) {
        mic.visibility = GONE
        delete.visibility = VISIBLE
        viewModel.searchAndGoToLocation(query, this@MapActivity, mMap)
    }

    private fun handleSearchQuery(query: String) {
        viewModel.searchAndGoToLocation(query, this, mMap)
        showMapView()
        destinationLoc = viewModel.getPairOfLoc(query, this)
        updateDestinationUI(query, destinationLoc)
    }

    private fun handlePlaceSelection(place: String) {
        binding.searchBar.setQuery(place, false)
        handleSearchQuery(place)
    }

    private fun handleLocationSelection(locationName: String, latLng: LatLng) {
        updateDestinationUI(locationName, latLng)
        afterSelectingLocation()
        handleMicSearch(locationName)
    }

    private fun updateDestinationUI(locationName: String, location: LatLng, isSaved: Boolean = false) {
        binding.searchTxt.text = if (locationName != DEFAULT_SEARCH_HINT) locationName else DEFAULT_SEARCH_HINT

        if (locationName != DEFAULT_SEARCH_HINT) {
            showDestinationDetails(location, locationName)

            animateDirectionButton()

            if (!isSaved) {
                saveDestination(locationName, location)
            }
        } else {
            hideDetails()
            binding.directionBtn.visibility = GONE
        }
    }

    private fun showDestinationDetails(destination: LatLng, destinationName: String) = with(binding) {
        if (::currentLoc.isInitialized && destinationName != DEFAULT_SEARCH_HINT && destination != LatLng(0.0, 0.0)) {
            viewModel.getDurationAndDistanceOfDestination(
                LatLng(currentLoc.latitude, currentLoc.longitude),
                destination,
                this@MapActivity
            )
        } else {
            hideDetails()
            Toast.makeText(this@MapActivity, "Error while getting location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun animateDirectionButton() = with(binding) {
        directionBtn.visibility = VISIBLE
        directionBtn.animate()
            .alpha(0f)
            .setDuration(0)
            .withEndAction {
                directionBtn.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .start()
            }
    }

    private fun saveDestination(name: String, location: LatLng) {
        viewModel.insertNewDestination(name, location.latitude, location.longitude, db)
    }

    private fun afterSelectingLocation() = with(binding) {
        mic.visibility = GONE
        delete.visibility = VISIBLE
    }

    private fun observeViewModel() {
        viewModel.currentLocationPair.observe(this) { currentLoc = it }

        viewModel.desInformation.observe(this) { (duration, distance, polylineData) ->
            durationV = duration
            distanceV = distance
            polyline = polylineData

            with(binding) {
                binding.distance.text = distanceV
                binding.duration.text = durationV
                distination.text = binding.searchTxt.text
            }
        }
    }

    private fun onMapClick(){
            mMap.setOnMapClickListener { latLng ->
                if (canClick) {
                val locationName = LocationHelper.getCurrentPlaceName(latLng, this)
                updateDestinationMarker(locationName, latLng)
                handleLocationSelection(locationName, latLng)
                }

        }
    }
}