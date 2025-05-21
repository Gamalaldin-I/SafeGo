package com.example.safego.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.SensorManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safego.dataSource.local.Pics.Backgrounds
import com.example.safego.dataSource.local.sharedPrefrences.SharedPref
import com.example.safego.domain.useCaseModel.NearbyPlace
import com.example.safego.domain.useCases.currentLocation.GetCurrentLocationUseCase
import com.example.safego.domain.useCases.googleMapFeatures.MapDirector
import com.example.safego.domain.useCases.nearbyPlaces.GetNearByPlacesUseCase
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val mapDirector = MapDirector()
    private var pref: SharedPref? = null
    private val _currentLocationName = MutableLiveData<String>()
    val currentLocationName: LiveData<String> = _currentLocationName

    private val _nearbyPlaces = MutableLiveData<ArrayList<NearbyPlace>>()
    val nearbyPlaces: LiveData<ArrayList<NearbyPlace>> = _nearbyPlaces

    private val _currentLocationPair = MutableLiveData<LatLng>()
    val currentLocationPair: LiveData<LatLng> = _currentLocationPair

    fun getLocationName(context: Context, activity: FragmentActivity) {
        viewModelScope.launch {
            val locationName = GetCurrentLocationUseCase(context, activity).getNameOfCurrentLoc()
            _currentLocationName.postValue(locationName.split(",").getOrNull(1) ?: locationName)
        }
    }

    fun getUserLocationPair(context: Context, activity: FragmentActivity) {
        viewModelScope.launch {
            val locationPair = GetCurrentLocationUseCase(context, activity).getPairOfCurrentLoc()
            _currentLocationPair.postValue(locationPair)
        }
    }
    fun markTheCurrentLocOnMapAndManageRotation(
        context: Context,
        contextA: FragmentActivity,
        myMarker: Marker?,
        map: GoogleMap,
        icon: Bitmap?,
        sensorManager: SensorManager
    ){
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                contextA,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Require location permission for tracking current Location", Toast.LENGTH_SHORT).show()
            return
        }
        map.isMyLocationEnabled = true

        mapDirector.markLastLocationOnMap(
            context,contextA,myMarker,map,icon
        )

        mapDirector.startOrientationListener(
            sensorManager,myMarker
        )
    }

    fun setBg(imageView: ImageView) {
        val list = Backgrounds.listOfMainBackgrounds
        val random = (list.indices).random()
        imageView.setBackgroundResource(list[random])
    }

    fun getPlaces(
        currentLocationLat: String,
        currentLocationLng: String,
        context: Context,
        activity: FragmentActivity,
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            val result = GetNearByPlacesUseCase.getNearByPlaces(
                context,
                activity,
                currentLocationLat,
                currentLocationLng
            )
            _nearbyPlaces.postValue(result)
        }
    }

    fun saveCurrentLocation(lat: String, lng: String, context: Context) {
        if (pref == null) pref = SharedPref(context)
        pref?.saveCurrentLocation(lat, lng)
    }
}

