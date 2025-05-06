package com.example.safego.util.helpers

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.util.Locale

object LocationHelper {
    fun getCurrentPlaceName(location: LatLng,context: Context): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0)
            } else {
                "UnknownN"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            "Unknown"
        }
    }
}