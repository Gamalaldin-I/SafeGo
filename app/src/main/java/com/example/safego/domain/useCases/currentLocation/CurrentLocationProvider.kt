package com.example.safego.domain.useCases.currentLocation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.example.safego.domain.useCaseModel.CurrentLocation
import com.example.safego.util.helpers.LocationHelper.getCurrentPlaceName
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

class CurrentLocationProvider(
    private val context: Context,
    private val contextA: FragmentActivity
) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

     private suspend fun getCurrentLocation(): CurrentLocation {
         if (ActivityCompat.checkSelfPermission(
                 context,
                 Manifest.permission.ACCESS_FINE_LOCATION
             ) != PackageManager.PERMISSION_GRANTED
         ) {
             ActivityCompat.requestPermissions(
                 contextA,
                 arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                 1001
             )
             return CurrentLocation(0.0, 0.0, "Fci Zagazig")
         }

         val location = suspendCancellableCoroutine { continuation ->
             val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                 .setMaxUpdates(1)
                 .build()

             val locationCallback = object : LocationCallback() {
                 override fun onLocationResult(result: LocationResult) {
                     fusedLocationClient.removeLocationUpdates(this)
                     continuation.resume(result.lastLocation)
                 }
             }

             fusedLocationClient.requestLocationUpdates(
                 locationRequest,
                 locationCallback,
                 context.mainLooper
             )
         }

         return if (location != null) {
             val latLng:LatLng = LatLng(location.latitude, location.longitude)
             val address = getCurrentPlaceName(latLng,context)
             CurrentLocation(location.latitude, location.longitude, address)
         } else {
             CurrentLocation(0.0, 0.0, "Unknown from update")
         }
     }





      suspend fun getNameOfCurrentLoc():String{
          return getCurrentLocation().address
     }
     suspend fun getPairOfCurrentLoc():LatLng{
         return LatLng(getCurrentLocation().latitude,getCurrentLocation().longitude)
     }

}
