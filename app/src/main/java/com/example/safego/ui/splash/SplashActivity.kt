package com.example.safego.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.safego.dataSource.local.sharedPrefrences.SharedPref
import com.example.safego.databinding.ActivitySplashBinding
import com.example.safego.ui.login.LoginActivity
import com.example.safego.ui.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private  lateinit var binding: ActivitySplashBinding
    private lateinit var prefs: SharedPref
    private val viewModel = SplashViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = SharedPref(this)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        binding.message.text = viewModel.getRandomDriverSafetyMessage()
        //call functions


        setContentView(binding.root)
        Handler().postDelayed({
            binding.root.animate().apply {
                duration = 0 //1000
                alpha(0f)
                withEndAction {
                    handleGoingToTheNext()
                }
            }
        }, 0 ) //3000


    }
    private fun handleGoingToTheNext(){
        if(!prefs.getProfileData().login){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        else{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}