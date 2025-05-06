package com.example.safego.util.helpers.personalDataValidaion

object ErrorHints {
    fun showPasswordLengthError():String{
        return "Password must be more than 8 characters"
    }
    fun showPasswordMatchError():String{
        return "Password must be match"
    }
    fun showEmailError():String{
        return "Email is not valid"
    }
    fun showStrongPasswordError():String{
        return "Password is not strong enough use special characters and numbers"
    }
    fun showNameError():String{
        return "Name is not valid"
    }
    fun showPhoneError():String{
        return "Phone is not valid"
    }
    fun showSSNError():String{
        return "SSN is not valid"
    }
}