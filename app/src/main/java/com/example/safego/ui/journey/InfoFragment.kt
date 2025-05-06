package com.example.safego.ui.journey

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.safego.databinding.FragmentInfoBinding
import com.example.safego.util.helpers.LocationHelper
import com.google.android.gms.maps.model.LatLng

class InfoFragment : Fragment() {
    private lateinit var binding: FragmentInfoBinding
    private lateinit var journeyViewModel: JourneyActivityViewModel

    private var distance: String? = null
    private var speed: String? = null
    private var predictionTime: String? = null
    private var currentLocation: String? = null
    private var destination: String? = null

    fun newInstance(
        distance: String,
        speed: String,
        predictionTime: String,
        currentLocation: String,
        destination: String,
        journeyViewModel: JourneyActivityViewModel
    ): InfoFragment {
        return InfoFragment().apply {
            arguments = Bundle().apply {
                putString("distance", distance)
                putString("speed", speed)
                putString("predictionTime", predictionTime)
                putString("currentLocation", currentLocation)
                putString("destination", destination)
            }
            this.journeyViewModel = journeyViewModel
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            distance = it.getString("distance")
            speed = it.getString("speed")
            predictionTime = it.getString("predictionTime")
            currentLocation = it.getString("currentLocation")
            destination = it.getString("destination")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoBinding.inflate(inflater, container, false)
        inflateViews()
        return binding.root
    }

    private fun inflateViews() {
        binding.distance.text = distance
        binding.speed.text = speed
        binding.predictionTime.text = predictionTime
        binding.currentLoc.text = currentLocation
        binding.destination.text = destination
    }

    override fun onResume() {
        super.onResume()
        observeJourneyDetails()
    }

    private fun observeJourneyDetails() {
        journeyViewModel.journeyDetailsLiveData.observe(viewLifecycleOwner) {
            currentLocation = it.start.getLocationName()
            destination = it.end.getLocationName()
            updateInfo(it.distance, it.speed, it.duration, currentLocation.orEmpty(), destination.orEmpty())
            Toast.makeText(requireContext(), "updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateInfo(
        distance: String,
        speed: String,
        predictionTime: String,
        currentLocation: String,
        destination: String
    ) {
        binding.distance.text = distance
        binding.speed.text = speed
        binding.predictionTime.text = predictionTime
        binding.currentLoc.text = currentLocation
        binding.destination.text = destination
    }

    private fun LatLng.getLocationName(): String {
        return LocationHelper.getCurrentPlaceName(this, requireContext())
    }
}
