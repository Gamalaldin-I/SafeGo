package com.example.safego.util.helpers.singlton

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

object PlacesClientProvider {
    private var placesClient: PlacesClient? = null

    fun getClient(context: Context, apiKey: String): PlacesClient {
        if (placesClient == null) {
            if (!Places.isInitialized()) {
                Places.initialize(context.applicationContext, apiKey)
            }
            placesClient = Places.createClient(context)
        }
        return placesClient!!
    }
}
