package com.example.safego.ui.journey
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.safego.R
import com.example.safego.databinding.FragmentInfoBinding
import com.example.safego.util.helpers.LocationHelper
import com.google.android.gms.maps.model.LatLng

class InfoFragment : Fragment() {
    private lateinit var binding: FragmentInfoBinding
    private var distance: String? = "0 km"
    private var speed:Double? = 0.0
    private var predictionTime: String? = null
    private var currentLocation: String? = null
    private var destination: String? = null
    private var startLocation: String? = null
    private var totalDistance: String? = null
    private var speedLimit: Double? = 0.0
    private val journeyViewModel: JourneyActivityViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
    }

    companion object {
        fun newInstance(
            distance: String,
            speed: Double,
            predictionTime: String,
            currentLocation: String,
            destination: String
        ): InfoFragment {
            return InfoFragment().apply {
                arguments = Bundle().apply {
                    putString("distance", distance)
                    putDouble("speed", speed)
                    putString("predictionTime", predictionTime)
                    putString("currentLocation", currentLocation)
                    putString("destination", destination)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        arguments?.let {
            distance = it.getString("distance")
            totalDistance = it.getString("distance")
            speed = it.getDouble("speed")
            predictionTime = it.getString("predictionTime")
            currentLocation = it.getString("currentLocation")
            startLocation = it.getString("currentLocation")
            destination = it.getString("destination")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentInfoBinding.inflate(inflater, container, false)
        inflateViews()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun inflateViews(){
        binding.distance.text = distance
        binding.speed.speedTo( speed!!.toFloat())
        binding.predictionTime.text = predictionTime
        binding.currentLoc.text =  try {
            currentLocation!!.split(",")[1]
            } catch (e: Exception) {
            currentLocation
        }
        binding.destination.text= "From $startLocation to $destination"
        binding.totalDistance.text = totalDistance
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        observeJourneyDetails()
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun observeJourneyDetails(){
        journeyViewModel.journeyDetailsLiveData.observe(viewLifecycleOwner){
            currentLocation = it.start.getLocationName().split(",")[1]
            destination = it.end.getLocationName()
            updateInfo(it.distance, it.speed, it.duration, currentLocation.orEmpty())
            Toast.makeText(requireContext(), "updated", Toast.LENGTH_SHORT).show()
        }
        journeyViewModel.currentSpeed.observe(viewLifecycleOwner){
            binding.speed.speedTo(it.toFloat())
            if (speed!!<it){
                //if the speed is bigger than the oldSpeed i will check if it is bigger than the roadSpeed
                ifRequireSpeedAlert()
            }
            speed = it
        }

        journeyViewModel.speedLimit.observe(viewLifecycleOwner){
            speedLimit = it
            ifRequireSpeedAlert()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateInfo(
        distance: String,
        speed:Float,
        predictionTime:String,
        currentLocation:String,
        ){
        binding.distance.text=distance
        binding.speed.speedTo(speed)
        binding.predictionTime.text=predictionTime
        binding.currentLoc.text=currentLocation
        updateRemainingDistance(distance)
    }
    private fun LatLng.getLocationName(): String {
        return LocationHelper.getCurrentPlaceName(this, requireContext())
    }
    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateRemainingDistance(currentDistanceText: String) {
        val totalDistanceValue = try {
            totalDistance?.split(" ")?.get(0)?.toFloat() ?: 0f
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error parsing total distance", Toast.LENGTH_SHORT).show()
            return
        }

        val currentDistanceValue = try {
            currentDistanceText.split(" ")[0].toFloat()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error parsing current distance", Toast.LENGTH_SHORT).show()
            return
        }

        val distanceCovered = totalDistanceValue - currentDistanceValue
        val percentageCovered = (distanceCovered / totalDistanceValue) * 100

        binding.percent.text = String.format("%.2f", percentageCovered) + "%"
        binding.progressBar3.progress = percentageCovered.toInt()
    }


    private fun ifRequireSpeedAlert(){
       // if(speedLimit!=0.0){
            if(speed!!>4){
                Toast.makeText(requireContext(), "Speed Limit Reached", Toast.LENGTH_SHORT).show()
                binding.speed.setPointerColor(ContextCompat.getColor(requireContext(), R.color.emergency))
                journeyViewModel.speedReached(true)
            }
            else{
                binding.speed.setPointerColor(ContextCompat.getColor(requireContext(), R.color.primary))
                journeyViewModel.speedReached(false)
            }
        }
       // else{
        //    binding.speed.setPointerColor(ContextCompat.getColor(requireContext(), R.color.primary))
        //    journeyViewModel.speedReached(false)
       // }


}
