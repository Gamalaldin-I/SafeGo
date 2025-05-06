package com.example.safego.domain.useCases.nearbyPlaces

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.example.safego.R
import com.example.safego.domain.useCaseModel.NearbyPlace
import com.example.safego.util.helpers.singlton.PlacesClientProvider
import com.example.safego.domain.useCases.distance.DistanceMeasure.calculateDistance
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("DEPRECATION")
class NearByPlacesService(
    private val currentLocationLat: String,
    private val currentLocationLng: String,
    private val context: Context,
    private val contextA:FragmentActivity,
    apiKey: String
){
    private val placesClient = PlacesClientProvider.getClient(context, apiKey)


    suspend fun getNearByPlaces(): ArrayList<NearbyPlace> {
        val placeFields = listOf(
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS
        )
        val request = FindCurrentPlaceRequest.newInstance(placeFields)

        // Check permissions
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
            Toast.makeText(context, "Give permission", Toast.LENGTH_SHORT).show()
            return arrayListOf()
        }

        // Wait for the result using suspendCoroutine
        return try{withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { cont ->
                val task = placesClient.findCurrentPlace(request)
                task.addOnSuccessListener { response ->
                    val result = arrayListOf<NearbyPlace>()
                    for (placeLikelihood in response.placeLikelihoods) {
                        result.add(assignNearByPlace(placeLikelihood))
                        Log.i("Place", "Place Name: ${placeLikelihood.place.name}")
                    }
                    cont.resume(result)
                }.addOnFailureListener { exception ->
                    if (exception is ApiException) {
                        Log.e("Place", "Place not found: ${exception.statusCode}")
                    }
                    cont.resumeWithException(exception)
                }
            }
        }
    }catch (e: Exception) {
            Log.e("Place", "GGL Exception: ${e.message}")
            return arrayListOf()
    }
    }


    private fun assignNearByPlace(place: PlaceLikelihood?):NearbyPlace{
        val p = place!!.place
        val nearbyPlace =
            NearbyPlace(
                name = p.name?:"unKnown",
                type = p.types?.toString()?:tryWithNameToGetImage(p.name?:"unknown").second,
                distance = handleDistance(place),
                image = handeImage(place),
                status = p.businessStatus?.toString()?:"",
                openingHours = p.openingHours?.toString()?:"",
                rating = p.rating?.toString()?:"",
                time = ""
            )
        return nearbyPlace

    }



    @SuppressLint("DefaultLocale")
    private fun handleDistance(place: PlaceLikelihood):String{
        val distance = calculateDistance(
            currentLocationLat.toDouble(),
            currentLocationLng.toDouble(),
            place.place.latLng!!.latitude,
            place.place.latLng!!.longitude)
        val disInTenMeters = String.format("%.1f", distance)
        return disInTenMeters
    }



    private fun handeImage(place: PlaceLikelihood):Int{
       val type = place.place.types?.get(0).toString()
        val name = place.place.name?.toString()
        return when(type){
            "restaurant" -> R.drawable.restaurant
            "cafe" -> R.drawable.cafe
            "gas_station" -> R.drawable.gas
            "مسجد","جامع","زاوية","الجامع","المسجد","Mosque" -> R.drawable.mosque
            "دكان" ,"store" ,"محل" -> R.drawable.store
            "جراج", "garage","الورشة","ورشة" -> R.drawable.mech
            "مصنع", "معمل","مصانع", "factory" -> R.drawable.factory
            "وحدة", "مستوصف", "إسعافة", "مستشفي" ->R.drawable.hospital
            "صيدلية", "أجزخانة" -> R.drawable.pharmacy
            "قهوة", "كوفي", "كافيه", "مقهي", "غرزة","استراحة" -> R.drawable.cafe
            "مطعم" -> R.drawable.restaurant
            "بلايستيشن" -> R.drawable.cafe
            "نادي" -> R.drawable.cafe
            "كنيسة" -> R.drawable.cafe
            "كلية", "جامعة", "معهد" -> R.drawable.university
            "مدرسة", "حضانة" -> R.drawable.school
            "سوبرماركت","ماركت" -> R.drawable.market
            else -> tryWithNameToGetImage(name!!).first
        }
    }
    private fun tryWithNameToGetImage(name:String):Pair<Int,String>{
        return when(name.split(" ")[0]){
            "gas_station" ->    Pair(R.drawable.gas,"gas station")
            "مسجد","جامع","زاوية","الجامع","المسجد","Mosque" -> Pair(R.drawable.mosque,"mosque")
            "دكان" ,"store" ,"محل" -> Pair(R.drawable.store,"store")
            "جراج", "garage","الورشة","ورشة","ورشه","الورشه" -> Pair(R.drawable.mech,"Service")
            "مصنع", "معمل","مصانع", "factory" -> Pair(R.drawable.factory,"factory")
            "وحدة", "مستوصف", "إسعافة", "مستشفي" -> Pair(R.drawable.hospital,"hospital")
            "صيدلية", "أجزخانة" -> Pair(R.drawable.pharmacy,"pharmacy")
            "قهوة", "كوفي", "كافيه", "مقهي", "غرزة","استراحة" -> Pair(R.drawable.cafe,"cafe")
            "مطعم" -> Pair(R.drawable.restaurant,"restaurant")
            "بلايستيشن" -> Pair(R.drawable.cafe,"play station")
            "نادي" -> Pair(R.drawable.cafe,"club")
            "كنيسة" -> Pair(R.drawable.cafe,"crest")
            "كلية", "جامعة", "معهد" -> Pair(R.drawable.university,"university")
            "مدرسة", "حضانة" -> Pair(R.drawable.school,"school")
            "سوبرماركت","ماركت" -> Pair(R.drawable.market,"market")


            else -> Pair(R.drawable.unknown,"unknown")
        }
    }


}