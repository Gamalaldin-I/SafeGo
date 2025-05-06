package com.example.safego.util.helpers.personalDataValidaion

object DataValidator {


    private const val STR_WORD = "Strong"

    fun validEmail(email:String):Boolean{
        return SignChecker.isValidEmail(email)
    }
    fun validPasswordLength(password:String):Boolean{
        return SignChecker.passwordMoreThan8(password)
    }
    fun validPasswordMatch(password:String,confirmPassword:String):Boolean{
        return (password==confirmPassword)
    }
    fun validStrongPassword(password:String):String{
        return SignChecker.howStrongPassword(password)
    }
    fun isValidName(name:String):Boolean{
        return SignChecker.isValidName(name)
    }
    fun validPhone(phone:String):Boolean{
        return SignChecker.validPhoneNumber(phone)
    }
    fun validSSN(ssn:String):Boolean {
        return SignChecker.validSSN(ssn)
    }
    fun strongPassword(password:String):Boolean{
        return (validStrongPassword(password) == STR_WORD || validStrongPassword(password) =="Medium")
    }
    fun validPassword(password:String):Boolean{
        return validPasswordLength(password) && strongPassword(password)
    }
}