package com.example.safego.ui.main

import android.content.Context
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safego.dataSource.local.Pics.Backgrounds
import com.example.safego.dataSource.local.sharedPrefrences.SharedPref
import com.example.safego.domain.useCaseModel.NearbyPlace
import com.example.safego.domain.useCases.currentLocation.CurrentLocationProvider
import com.example.safego.domain.useCases.nearbyPlaces.NearByPlacesService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private var pref: SharedPref? = null

    private val _currentLocationName = MutableLiveData<String>()
    val currentLocationName: LiveData<String> = _currentLocationName

    private val _nearbyPlaces = MutableLiveData<ArrayList<NearbyPlace>>()
    val nearbyPlaces: LiveData<ArrayList<NearbyPlace>> = _nearbyPlaces

    private val _currentLocationPair = MutableLiveData<LatLng>()
    val currentLocationPair: LiveData<LatLng> = _currentLocationPair

    private var nearbyPlacesService: NearByPlacesService? = null

    fun getLocationName(context: Context, activity: FragmentActivity) {
        viewModelScope.launch {
            val locationName = CurrentLocationProvider(context, activity).getNameOfCurrentLoc()
            _currentLocationName.postValue(locationName.split(",").getOrNull(1) ?: locationName)
        }
    }

    fun getUserLocationPair(context: Context, activity: FragmentActivity) {
        viewModelScope.launch {
            val locationPair = CurrentLocationProvider(context, activity).getPairOfCurrentLoc()
            _currentLocationPair.postValue(locationPair)
        }
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
        apiKey: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            nearbyPlacesService = NearByPlacesService(
                currentLocationLat,
                currentLocationLng,
                context,
                activity,
                apiKey = apiKey
            )
            val result = nearbyPlacesService?.getNearByPlaces()
            _nearbyPlaces.postValue(result ?: arrayListOf())
        }
    }

    fun saveCurrentLocation(lat: String, lng: String, context: Context) {
        if (pref == null) pref = SharedPref(context)
        pref?.saveCurrentLocation(lat, lng)
    }
}

