package com.example.safego.domain.useCases.Speed

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority

class GetSpeedUpdatesUseCase {
    operator fun invoke(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        onSpeedUpdate: (Double) -> Unit
    ): LocationCallback? {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(1000)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location != null) {
                    val speedInKmh = location.speed * 3.6 // من m/s إلى km/h
                    onSpeedUpdate(speedInKmh)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        return locationCallback
    }

}