package com.example.safego.dataSource.local.sharedPrefrences

import android.content.Context
import com.example.safego.dataSource.local.model.User

class SharedPref(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("profile", Context.MODE_PRIVATE)
    private val currentLocPref = context.getSharedPreferences("currentLoc", Context.MODE_PRIVATE)



    fun saveProfileData(imagePath:String,
                        name:String,
                        ssn:String,
                        phone:String,
                        email: String,
                        password: String,
                        login:Boolean)
    {
        sharedPreferences.edit().putString("image", imagePath).apply()
        sharedPreferences.edit().putString("name", name).apply()
        sharedPreferences.edit().putString("ssn", ssn).apply()
        sharedPreferences.edit().putString("phone", phone).apply()
        sharedPreferences.edit().putString("email", email).apply()
        sharedPreferences.edit().putString("password", password).apply()
        sharedPreferences.edit().putBoolean("login", login).apply()
    }
    fun getProfileData(): User {
        val imagePath = sharedPreferences.getString("image","") ?: ""
        val name = sharedPreferences.getString("name","") ?: ""
        val ssn = sharedPreferences.getString("ssn","") ?: ""
        val phone = sharedPreferences.getString("phone","") ?: ""
        val email = sharedPreferences.getString("email","") ?: ""
        val password = sharedPreferences.getString("password","") ?: ""
        val login = sharedPreferences.getBoolean("login", false)
        return User(imagePath,name, ssn, phone, email, password,login)
    }
    fun saveCurrentLocation(lat:String,lng:String){
        currentLocPref.edit().putString("lat", lat).apply()
        currentLocPref.edit().putString("lng", lng).apply()
    }
    fun getCurrentLocation():Pair<String,String>{
        val lat = currentLocPref.getString("lat","") ?: ""
        val lng = currentLocPref.getString("lng","") ?: ""
        return Pair(lat,lng)
    }
}