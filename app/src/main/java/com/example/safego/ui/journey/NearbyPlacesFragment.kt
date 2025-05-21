package com.example.safego.ui.journey

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safego.databinding.FragmentNearbyPlacesBinding
import com.example.safego.domain.useCaseModel.NearbyPlace
import com.example.safego.util.adapters.PlacesAdapter
import com.example.safego.util.helpers.singlton.Animator.animateFadeIn
import com.example.safego.util.helpers.singlton.Animator.animateFadeOut
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NearbyPlacesFragment : Fragment() {
    private val journeyViewModel: JourneyActivityViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
    }
    private lateinit var listener: NearbyClickListener
    companion object{
    fun getInstance(
        currentLocation:LatLng,
        listener: NearbyClickListener
    ): NearbyPlacesFragment {
        val fragment = NearbyPlacesFragment()
        val args = Bundle()
        args.putDouble("lat", currentLocation.latitude)
        args.putDouble("long", currentLocation.longitude)
        fragment.arguments = args
        fragment.listener = listener
        return fragment
    }
    }
    private lateinit var binding: FragmentNearbyPlacesBinding
    private  var lat : Double = 0.0
    private var long :Double = 0.0



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentNearbyPlacesBinding.inflate(inflater, container, false)
        lat = arguments?.getDouble("lat")!!
        long = arguments?.getDouble("long")!!
        binding.placesRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeNearbyPlaces()
    }

    @SuppressLint("SetTextI18n")
     fun observeNearbyPlaces() {
            journeyViewModel.nearbyPlaces.observe(viewLifecycleOwner) { places ->
                journeyViewModel.saveCurrentLocation(lat.toString(), long.toString(), requireContext())
                setupRecycler(places)
        }
     }

    @SuppressLint("SetTextI18n")
    private fun setupRecycler(listOfPlaces: ArrayList<NearbyPlace>) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                if (listOfPlaces.isEmpty()) {
                    animateFadeIn(binding.message)
                    binding.message.text = "No places found"
                    animateFadeOut(binding.placesRecycler)
                } else {
                    Toast.makeText(requireContext(), "get places", Toast.LENGTH_SHORT).show()
                    animateFadeOut(binding.message)
                    binding.placesRecycler.adapter = PlacesAdapter(listOfPlaces){
                        listener.onClick(it)
                    }
                    animateFadeIn(binding.placesRecycler)
                }
            }
        }}

    private fun <T> LiveData<T>.observeOnce(
        lifecycleOwner: LifecycleOwner,
        observer: Observer<T>
    ) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this)
                observer.onChanged(value)
            }
        })
    }


}