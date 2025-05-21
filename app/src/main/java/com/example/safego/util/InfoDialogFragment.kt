package com.example.safego.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.example.safego.R
import com.example.safego.databinding.BottomSheetDialogBinding
import com.example.safego.domain.useCases.googleMapFeatures.MapDirector
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class InfoDialogFragment : BottomSheetDialogFragment(), OnMapReadyCallback {

    private lateinit var binding: BottomSheetDialogBinding
    private lateinit var map: GoogleMap

    private lateinit var sensorManager: SensorManager
    private val mapDirector = MapDirector()

    // Data fields for place information
    private var lat = 0.0
    private var lng = 0.0
    private var name = ""
    private var type = ""
    private var distance = ""
    private var image = 0
    private var status = ""
    private var rating = ""
    private var time = ""

    companion object {
        fun getInstance(): InfoDialogFragment {
            return InfoDialogFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetDialogBinding.inflate(inflater, container, false)
        sensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager
        setupMap()
        binding.exit.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    // Update the bottom sheet data and map marker
    fun updateTheData(
        lat: Double,
        lng: Double,
        name: String,
        type: String,
        distance: String,
        image: Int,
        status: String,
        rating: String,
        time: String
    ) {
        this.lat = lat
        this.lng = lng
        this.name = name
        this.type = type
        this.distance = distance
        this.image = image
        this.status = status
        this.rating = rating
        this.time = time

        // Update map marker
        updateMapLocation()

        // Update UI elements
        binding.apply {
            this.distance.text = distance
            this.PlaceImage.setImageResource(image)
            this.placeName.text = name
            this.type.text = type
            handleStatusAndOpeningTimes(
                status,
                time,
                rating
            )
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        try {
            map = p0
            val success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_dark))
            if (!success) Log.e("MapStyle", "Style parsing failed.")
        } catch (e: Exception) {
            Log.e("MapStyle", "Can't find style. Error: ", e)
        }
        updateMapLocation()
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mMap) as? SupportMapFragment
        mapFragment?.getMapAsync(this) ?: run {
            Log.e("InfoDialogFragment", "Failed to load the map fragment.")
        }
    }

    private fun updateMapLocation() {
        if (::map.isInitialized) {
            val location = LatLng(lat, lng)
            val icon = BitmapFactory.decodeResource(resources, R.drawable.back_arrow)
            markTheCurrentLocOnMapAndManageRotation(
                requireContext(),
                requireActivity(),
                null,
                map,
                icon,
                sensorManager
                ,location = location
            )
            map.addMarker(MarkerOptions().position(location).title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
    private fun markTheCurrentLocOnMapAndManageRotation(
        context: Context,
        contextA: FragmentActivity,
        myMarker: Marker?,
        map: GoogleMap,
        icon: Bitmap?,
        sensorManager: SensorManager,
        location:LatLng
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                contextA,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                context,
                "Require location permission for tracking current Location",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        map.isMyLocationEnabled = true

        mapDirector.markLastLocationOnMap(
            context,
            contextA,
            myMarker, map,
            icon,
            location = location
        )

        mapDirector.startOrientationListener(
            sensorManager, myMarker
        )
    }
    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun handleStatusAndOpeningTimes(status: String, openingHours: String, rating: String) {
        val currentTime = LocalTime.now()
        val formattedTime = DateTimeFormatter.ofPattern("HH:mm")
        val hour = currentTime.format(formattedTime).split(":")[0].toInt()

        val isOpen = hour < 24 // This condition may need revision, it currently always returns true for valid hours.

        // Update status text
        binding.status.text = if (status.isEmpty() && !isOpen) "Closed" else "Open"
        if (binding.status.text=="Closed") {
            binding.status.setTextColor(resources.getColor(R.color.emergency))
        }else{
            binding.status.setTextColor(resources.getColor(R.color.safe))
        }

        // Update opening hours text
        binding.openingHours.text = if (openingHours.isEmpty() && !isOpen) "No information" else "opening hours: $openingHours"

        // Update rating text
        binding.rating.text = rating.ifEmpty { "1/5" }
    }


}