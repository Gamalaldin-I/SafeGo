package com.example.safego.ui.main
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safego.dataSource.local.sharedPrefrences.SharedPref
import com.example.safego.databinding.ActivityMainBinding
import com.example.safego.domain.useCaseModel.NearbyPlace
import com.example.safego.ui.journey.JourneyActivity
import com.example.safego.ui.map.MapActivity
import com.example.safego.ui.profile.ProfileActivity
import com.example.safego.util.adapters.PlacesAdapter
import com.example.safego.util.helpers.singlton.Animator
import com.example.safego.util.helpers.singlton.Animator.animateFadeIn
import com.example.safego.util.helpers.singlton.Animator.animateFadeOut
import com.example.safego.util.helpers.InternetConnectionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pref: SharedPref
    private lateinit var currentLocationLat: String
    private lateinit var currentLocationLng: String
    private lateinit var networkReceiver: InternetConnectionListener.NetworkChangeReceiver
    private val viewModel: MainViewModel by viewModels()

    private var hasFetchedPlaces = false

    companion object {
        private const val API_KEY = "AIzaSyDJ8xWvWQTRK7ivBcqFCmlGg9Mp-dpfSQ4"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        observeView()
        setupControllers()
    }

    private fun init() {
        pref = SharedPref(this)
        binding.name.text = pref.getProfileData().name
        binding.placesRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun observeView() {
        observeLocation()
    }

    private fun observeLocation() {
        viewModel.getLocationName(this, this)
        viewModel.currentLocationName.observe(this) {
            binding.currentLoc.text = it
            //observeNearbyPlaces()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeNearbyPlaces() {
        binding.message.text = "Loading nearby places ..."
        viewModel.getUserLocationPair(this, this)
        viewModel.currentLocationPair.observeOnce(this) {
            currentLocationLat = it.latitude.toString()
            currentLocationLng = it.longitude.toString()

            viewModel.getPlaces(
                currentLocationLat,
                currentLocationLng,
                this,
                this,
                API_KEY
            )

            viewModel.nearbyPlaces.observeOnce(this) { places ->
                viewModel.saveCurrentLocation(currentLocationLat, currentLocationLng, this)
                setupRecycler(places)
            }
        }
    }

    private fun setupControllers() {
        binding.setDestinationBtn.setOnClickListener {
            Animator.animateButtonClick(binding.setDestinationBtn) {
                startActivity(Intent(this, MapActivity::class.java))
            }
        }

        binding.startBtn.setOnClickListener {
            Animator.animateButtonClick(binding.startBtn) {
            startActivity(Intent(this, JourneyActivity::class.java))
            }
        }

        binding.header.setOnClickListener {
            Animator.animateButtonClick(binding.header) {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
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
                    animateFadeOut(binding.message)
                    binding.placesRecycler.adapter = PlacesAdapter(listOfPlaces)
                    animateFadeIn(binding.placesRecycler)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.setBg(binding.bg)

        networkReceiver = InternetConnectionListener.NetworkChangeReceiver { isConnected ->
            if (isConnected) {
                onConnectedToInternet()
            } else {
                onNotConnectedToInternet()
            }
        }

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkReceiver)
    }

    private fun onConnectedToInternet() {
        if (!hasFetchedPlaces) {
           // observeNearbyPlaces()
            hasFetchedPlaces = true
        }
        animateFadeOut(binding.noInternet)
        animateFadeIn(binding.mainView)
    }

    private fun onNotConnectedToInternet() {
        hasFetchedPlaces = false
        Toast.makeText(this, "Not connected to the internet", Toast.LENGTH_SHORT).show()
        animateFadeIn(binding.noInternet)
        animateFadeOut(binding.mainView)
    }

    // Extension Function
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
