package com.example.safego.ui.login

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.safego.util.helpers.personalDataValidaion.SignChecker

class LoginViewModel : ViewModel(){

    fun showPasswordError():String{
        return "Password must be more than 8 characters"
    }
    fun showEmailError():String{
        return "Email is not valid"
    }
     fun validEmail(email:String):Boolean{
        return SignChecker.isValidEmail(email)
    }
     fun validPassword(password:String):Boolean{
        return SignChecker.passwordMoreThan8(password)
    }
    fun login(email:String,password:String,context: Context){
        if(validEmail(email) && validPassword(password)){
        Toast.makeText(context, "success", Toast.LENGTH_SHORT).show()
        }
    }
}