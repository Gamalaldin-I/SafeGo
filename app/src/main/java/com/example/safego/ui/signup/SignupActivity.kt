package com.example.safego.ui.signup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.safego.util.adapters.FragmentAdapter
import com.example.safego.databinding.ActivitySignupBinding
import com.example.safego.util.helpers.singlton.Animator
import com.example.safego.dataSource.local.sharedPrefrences.SharedPref
import com.example.safego.ui.main.MainActivity
import com.example.safego.ui.login.LoginActivity

class SignupActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignupBinding
    private lateinit var firstInfoFragment: FirstInfoFragment
    private lateinit var secondInfoFragment: SecondInfoFragment
    private lateinit var thirdInfoFragment: ThirdInfoFragment
    private lateinit var fragments :ArrayList<Fragment>
    private lateinit var pref: SharedPref
    private var name = ""
    private var ssn = ""
    private var phone = ""
    private var email = ""
    private var password =""
    private var imageUri =""
    private var position = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setView()
        setContentView(binding.root)
        setControllers()
        pref = SharedPref(this)

    }

    private fun  setView(){
        setUpFragmentAdapter()
    }


    private fun setUpFragmentAdapter(){
        firstInfoFragment = FirstInfoFragment()
        secondInfoFragment = SecondInfoFragment()
        thirdInfoFragment = ThirdInfoFragment()
        fragments = arrayListOf(
            firstInfoFragment
            ,secondInfoFragment
            ,thirdInfoFragment
        )
        val fragmentAdapter = FragmentAdapter(this,fragments)
        binding.viewPager2.adapter = fragmentAdapter
        binding.indicator.attachTo(binding.viewPager2)
    }


    private fun setControllers(){
        binding.NextSignBtn.setOnClickListener{
            if(binding.NextSignBtn.text =="Next"){
                onNextBtn()
            }
            else {
                onSignBtn()
            }
        }
        binding.viewPager2.registerOnPageChangeCallback( object :
            ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                this@SignupActivity.position = position
                binding.NextSignBtn.text = if(fragments.lastIndex==position) "Signup" else "Next"
            }
            })

        binding.loginBtn.setOnClickListener{
            Animator.animateTxt(binding.loginBtn){
                startActivity(Intent(this,LoginActivity::class.java))
                finish()
            }
        }
    }
    private fun onSignBtn(){
        if(!firstInfoFragment.allDone()){
            binding.viewPager2.setCurrentItem(0,true)
            firstInfoFragment.showErrors()
        }
        else if(!secondInfoFragment.allDone()){
            binding.viewPager2.setCurrentItem(1,true)
            secondInfoFragment.showError()
        }
        else if (thirdInfoFragment.allDone()){
            Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
            setData()
            sendDataToApi()
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
        else Toast.makeText(this, "Check all fields and Pick image", Toast.LENGTH_SHORT).show()
    }
    private fun onNextBtn(){
        when(position){
             0->{
                if(firstInfoFragment.allDone()){
                    position+=1
                    binding.viewPager2.setCurrentItem(position,true)
                }
                else firstInfoFragment.showErrors()
            }
            1->{
                if(secondInfoFragment.allDone()){
                    position+=1
                    binding.viewPager2.setCurrentItem(position,true)
                }
                else secondInfoFragment.showError()
            }
        }
    }
    private fun setData(){
        name = firstInfoFragment.getData().first.trim()
        ssn = firstInfoFragment.getData().second
        phone = firstInfoFragment.getData().third
        email = secondInfoFragment.getData().first.trim()
        password = secondInfoFragment.getData().second
        pref.saveProfileData(name,ssn,phone,email,password,true)
        Toast.makeText(this, "Account has been created successfully", Toast.LENGTH_SHORT).show()
    }
    private fun sendDataToApi(){


    }
}