package com.example.safego.ui.signup

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.safego.util.helpers.personalDataValidaion.DataValidator
import com.example.safego.util.helpers.personalDataValidaion.ErrorHints

class SignupViewModel : ViewModel() {



    fun signup(email:String,password:String,confirmPassword:String,name:String,phone:String,ssn:String,context: Context){
        if(DataValidator.validEmail(email) && DataValidator.validPassword(password) && DataValidator.validPasswordMatch(password,confirmPassword)
            && DataValidator.isValidName(name) && DataValidator.validPhone(phone) && DataValidator.validSSN(ssn)){
            Toast.makeText(context, "success", Toast.LENGTH_SHORT).show()
            //setData()
        }
        else{
            if(!DataValidator.validEmail(email)){
                Toast.makeText(context, ErrorHints.showEmailError(), Toast.LENGTH_SHORT).show()
            }
            if(!DataValidator.validPasswordLength(password)){
                Toast.makeText(context, ErrorHints.showPasswordLengthError(), Toast.LENGTH_SHORT).show()
            }
            if(!DataValidator.validPasswordMatch(password,confirmPassword)){
                Toast.makeText(context, ErrorHints.showPasswordMatchError(), Toast.LENGTH_SHORT).show()
            }
            if(!DataValidator.isValidName(name)){
                Toast.makeText(context, ErrorHints.showNameError(), Toast.LENGTH_SHORT).show()
            }
            if(!DataValidator.validPhone(phone)){
                Toast.makeText(context, ErrorHints.showPhoneError(), Toast.LENGTH_SHORT).show()
            }
            if(!DataValidator.validSSN(ssn)){
                Toast.makeText(context, ErrorHints.showSSNError(), Toast.LENGTH_SHORT).show()
            }

        }
    }

}