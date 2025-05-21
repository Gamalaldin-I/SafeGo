package com.example.safego.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.SensorManager
import android.location.Geocoder
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safego.dataSource.local.model.Destination
import com.example.safego.dataSource.local.repo.LocalRepoImp
import com.example.safego.databinding.ActivityMapBinding
import com.example.safego.domain.useCaseModel.LocationAddress
import com.example.safego.domain.useCases.currentLocation.GetCurrentLocationUseCase
import com.example.safego.domain.useCases.googleMapFeatures.MapDirector
import com.example.safego.util.adapters.LocationsAdapter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch
import java.util.Locale

class MapViewModel :ViewModel() {

    private val mapDirector = MapDirector()
    private val _currentLocationPair = MutableLiveData<LatLng>()
    val currentLocationPair: LiveData<LatLng> get() = _currentLocationPair
    private val _desInformation = MutableLiveData<Triple<String,String,String>>()
    val desInformation: LiveData<Triple<String,String,String>> = _desInformation
    private val _savedLocations = MutableLiveData<ArrayList<Destination>>()
    val savedLocations: LiveData<ArrayList<Destination>> = _savedLocations

    //Feature1: Mark and go to location after search or get the location from mic
    fun searchAndGoToLocation(
        placeName:String,
        context: Context,
        map: GoogleMap
    ){
        mapDirector.searchAndGoToLocation(
            placeName,context,map
        )
    }
    //Feature2: Mark the current location on the map
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

    fun getUserLocationPair(context: Context, activity: FragmentActivity) {
        viewModelScope.launch {
            val locationPair = GetCurrentLocationUseCase(context, activity).getPairOfCurrentLoc()
            _currentLocationPair.postValue(locationPair)
        }
    }
    //Feature3 : Search for places and show suggestions
    @SuppressLint("NotifyDataSetChanged")
    fun showSuggestions(placesClient:PlacesClient, query: String, context: Context, placesAdapter: LocationsAdapter, placesList: ArrayList<LocationAddress>, binding: ActivityMapBinding) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                placesList.clear()
                response.autocompletePredictions.forEach {
                    placesList.add(LocationAddress(it.getFullText(null).toString()))}
                placesAdapter.notifyDataSetChanged()
                binding.locationsAdapter.visibility = VISIBLE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun getPairOfLoc(locName:String,context: Context):LatLng{
        val geocoder = Geocoder(context, Locale.getDefault())
        val latLng: LatLng
            val addressList = geocoder.getFromLocationName(locName, 1)
            if (!addressList.isNullOrEmpty()) {
                val address = addressList[0]
                 latLng = LatLng(address.latitude, address.longitude)
            }
            else return LatLng(0.0,0.0)
        return latLng

        }

    fun getDurationAndDistanceOfDestination(start:LatLng,end:LatLng,context: Context){
        viewModelScope.launch {
            val result = mapDirector.getRouteAndDurationAndDistance(start,end,context)
            val duration = result.first
            val distance = result.second
            val polyline = result.third
            _desInformation.postValue(Triple(duration,distance,polyline))
        }
    }

    suspend fun getDurationAndDistanceOfDestination1(start:LatLng,end:LatLng,context: Context):Triple<String,String,String> =
              mapDirector.getRouteAndDurationAndDistance(start,end,context)

    fun drawRoute(polyline: String,map: GoogleMap,context: Context){
        if (polyline.isEmpty()) {
            Toast.makeText(context, "No route found", Toast.LENGTH_SHORT).show()
            return
        }
        mapDirector.drawPolyline(polyline =polyline, map = map )
    }
    fun insertNewDestination(name:String,latitude:Double,longitude:Double,localRepo: LocalRepoImp){
        val maximumSavings = 100
        val newDestination = Destination(
            name = name,
            latitude = latitude,
            longitude = longitude
        )
        viewModelScope.launch {
            val lengthOfDestinationList = localRepo.getAllDestinations().size
            if (lengthOfDestinationList>=maximumSavings){
                deleteDestination(localRepo.getAllDestinations()[0],localRepo)
            }
            localRepo.insertDestination(newDestination)
        }
    }
    fun deleteDestination(destination: Destination, localRepo: LocalRepoImp){
        viewModelScope.launch {
            localRepo.deleteDestination(destination.name)
        }
    }
    fun getAllSavedLocations(localRepo: LocalRepoImp){
        viewModelScope.launch {
            _savedLocations.postValue(localRepo.getAllDestinations()as ArrayList<Destination>)
        }
    }
    fun deleteAllDestinations(localRepo: LocalRepoImp){
        viewModelScope.launch {
            localRepo.deleteAllDestinations()
        }
    }

    fun goToSavedLocation(destination:Destination,context: Context,map: GoogleMap){
        mapDirector.goTOLocation(
            LatLng(
                destination.latitude,
                destination.longitude
            ),
            destination.name,context,map
        )
    }





}