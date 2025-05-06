package com.example.safego.ui.journey

import android.content.Context.SENSOR_SERVICE
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.safego.R
import com.example.safego.databinding.FragmentMapBinding
import com.example.safego.ui.map.MapViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentMapBinding
    private lateinit var mMap: GoogleMap
    private val mapViewModel: MapViewModel by viewModels()
    private lateinit var journeyViewModel: JourneyActivityViewModel

    private var myMarker: Marker? = null
    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLoc: LatLng = LatLng(0.0, 0.0)
    private var destinationLoc: LatLng = LatLng(0.0, 0.0)
    private var polyline: String = ""
    private var counter = 0

    companion object {
        fun newInstance(
            destinationLat: Double,
            destinationLong: Double,
            currentLocLat: Double,
            currentLocLong: Double,
            polyline: String,
            journeyViewModel: JourneyActivityViewModel
        ): MapFragment {
            val fragment = MapFragment()
            val bundle = Bundle().apply {
                putDouble("destinationLat", destinationLat)
                putDouble("destinationLong", destinationLong)
                putDouble("currentLocLat", currentLocLat)
                putDouble("currentLocLong", currentLocLong)
                putString("polyline", polyline)
            }
            fragment.journeyViewModel = journeyViewModel
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        arguments?.let {
            currentLoc = LatLng(it.getDouble("currentLocLat"), it.getDouble("currentLocLong"))
            destinationLoc = LatLng(it.getDouble("destinationLat"), it.getDouble("destinationLong"))
            polyline = it.getString("polyline") ?: ""
        }

        sensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapF) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_dark)
            )
            if (!success) Log.e("MapStyle", "Style parsing failed.")
        } catch (e: Resources.NotFoundException) {
            Log.e("MapStyle", "Can't find style. Error: ", e)
        }

        markCurrentLocation()
        drawRoute()
    }

    private fun markCurrentLocation() {
        val icon = BitmapFactory.decodeResource(resources, R.drawable.back_arrow)
        mapViewModel.markTheCurrentLocOnMapAndManageRotation(requireContext(), requireActivity(), myMarker, mMap, icon, sensorManager)
    }

    private fun drawRoute() {
        mapViewModel.drawRoute(polyline, mMap, requireContext())
    }

    private suspend fun updateTrack(start: LatLng, end: LatLng) {
        counter++
        val (duration, distance, polyline) = mapViewModel.getDurationAndDistanceOfDestination1(
            start, end, requireContext()
        )
        this.polyline = polyline
        withContext(Dispatchers.Main) {
            drawRoute()
            journeyViewModel.getJourneyDetails(duration, "$distance counter:$counter", start, end)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun updateCurrentLocation() {
        GlobalScope.launch {
            currentLoc = journeyViewModel.getUserLocationPair(requireContext(), requireActivity())
            updateTrack(currentLoc, destinationLoc)
        }
    }

}


