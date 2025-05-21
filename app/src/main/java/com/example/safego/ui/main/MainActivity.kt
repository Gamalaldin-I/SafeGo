package com.example.safego.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.safego.R
import com.example.safego.dataSource.local.sharedPrefrences.SharedPref
import com.example.safego.databinding.ActivityMainBinding
import com.example.safego.domain.useCaseModel.NearbyPlace
import com.example.safego.ui.map.MapActivity
import com.example.safego.ui.profile.ProfileActivity
import com.example.safego.util.InfoDialogFragment
import com.example.safego.util.adapters.PlacesAdapter
import com.example.safego.util.helpers.InternetConnectionListener
import com.example.safego.util.helpers.singlton.Animator
import com.example.safego.util.helpers.singlton.AppManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pref: SharedPref
    private lateinit var networkReceiver: InternetConnectionListener.NetworkChangeReceiver
    private val viewModel: MainViewModel by viewModels()
    private lateinit var mMap: GoogleMap
    private var bottomSheetDialog: InfoDialogFragment? = null
    private var hasFetchedPlaces = false
    private var currentLocationLat: Double = 0.0
    private var currentLocationLng: Double = 0.0
    private var myMarker: Marker? = null
    private lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        initializeViews()
        setupMap()
        setupObservers()
        setupListeners()
    }

    private fun initializeViews() {
        pref = SharedPref(this)
        binding.name.text = pref.getProfileData().name
        binding.placesRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        setupBottomSheet()


    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupObservers() {
        viewModel.getLocationName(this, this)
        viewModel.currentLocationName.observe(this) {
            binding.currentLoc.text = it
        }
    }

    private fun setupListeners() {
        binding.setDestinationBtn.setOnClickListener { onSetDestinationClick() }
        binding.header.setOnClickListener { onHeaderClick() }
    }

    private fun onSetDestinationClick() {
        Animator.animateButtonClick(binding.setDestinationBtn) {
            startActivity(Intent(this, MapActivity::class.java))
        }
    }

    private fun onHeaderClick() {
        Animator.animateButtonClick(binding.header) {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun observeLocation() {
        viewModel.getUserLocationPair(this, this)
        viewModel.currentLocationPair.observeOnce(this) {
            currentLocationLat = it.latitude
            currentLocationLng = it.longitude
            updateMapLocation()
            fetchNearbyPlaces()
        }
    }

    private fun updateMapLocation() {
        if (::mMap.isInitialized) {
            val userLocation = LatLng(currentLocationLat, currentLocationLng)
            markCurrentLocation()
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10f))
        }
    }
    private fun markCurrentLocation() {
        val icon = BitmapFactory.decodeResource(resources, R.drawable.back_arrow)
        viewModel.markTheCurrentLocOnMapAndManageRotation(this,this, myMarker, mMap, icon, sensorManager)
    }


    private fun fetchNearbyPlaces() {
        viewModel.getPlaces(currentLocationLat.toString(), currentLocationLng.toString(), this, this)
        viewModel.nearbyPlaces.observeOnce(this) { places ->
            viewModel.saveCurrentLocation(currentLocationLat.toString(), currentLocationLng.toString(), this)
            setupRecyclerView(places)
            hideSplashView()
        }
    }

    private fun setupRecyclerView(places: ArrayList<NearbyPlace>) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                if (places.isEmpty()) {
                    showNoPlacesFound()
                } else {
                    showPlaces(places)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showNoPlacesFound() {
        Animator.animateFadeIn(binding.message)
        binding.message.text = "No places found"
        Animator.animateFadeOut(binding.placesRecycler)
    }

    private fun showPlaces(places: ArrayList<NearbyPlace>) {
        Animator.animateFadeOut(binding.message)
        binding.placesRecycler.adapter = PlacesAdapter(places) { place ->
            showPlaceDetails(place)
        }
        Animator.animateFadeIn(binding.placesRecycler)
    }

    private fun showPlaceDetails(place: NearbyPlace) {
        if (bottomSheetDialog == null) setupBottomSheet()
        if (bottomSheetDialog?.isAdded == false) {
            bottomSheetDialog?.show(supportFragmentManager, "bottomSheet")
            Handler(Looper.getMainLooper()).postDelayed({
                bottomSheetDialog?.updateTheData(
                    place.latLng.latitude,
                    place.latLng.longitude,
                    place.name,
                    place.type,
                    place.distance,
                    place.image,
                    place.status,
                    place.rating,
                    place.openingHours
                )
            }, 100)
        }
    }


    private fun setupBottomSheet() {
        bottomSheetDialog = InfoDialogFragment.getInstance(
        )
        bottomSheetDialog?.isCancelable = false
    }

    private fun hideSplashView() {
        Animator.animateConstraintSplashView(binding.splash) {
            binding.splash.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        AppManager.viewTheProfileInImageView(this,binding.profileImage)


        viewModel.setBg(binding.bg)
        networkReceiver = InternetConnectionListener.NetworkChangeReceiver { isConnected ->
            if (isConnected) onConnectedToInternet() else onNotConnectedToInternet()
        }
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkReceiver)

        bottomSheetDialog?.let { dialog ->
            if (dialog.isAdded && !dialog.isRemoving) {
                dialog.dismiss()
            }
        }
        bottomSheetDialog = null
    }

    private fun onConnectedToInternet() {
        if (!hasFetchedPlaces){
            //observeLocation()
            hideSplashView()
            hasFetchedPlaces=true}
        Animator.animateFadeOut(binding.noInternet)
        Animator.animateFadeIn(binding.mainView)
    }
    private fun onNotConnectedToInternet() {
        hasFetchedPlaces=false
        Toast.makeText(this, "Not connected to the internet", Toast.LENGTH_SHORT).show()
        hideSplashView()
        Animator.animateFadeIn(binding.noInternet)
        Animator.animateFadeOut(binding.mainView)
    }
    // Extension Function to observe LiveData once
    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this)
                observer.onChanged(value)
            }
        })
    }

    override fun onMapReady(map: GoogleMap) {
        try {
            mMap = map
            val success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark))
            if (!success) Log.e("MapStyle", "Style parsing failed.")
        } catch (e: Exception) {
            Log.e("MapStyle", "Can't find style. Error: ", e)
        }
    }

}

