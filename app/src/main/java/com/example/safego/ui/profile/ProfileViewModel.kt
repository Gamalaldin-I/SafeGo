package com.example.safego.ui.profile

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.safego.dataSource.local.model.User
import com.example.safego.dataSource.local.sharedPrefrences.SharedPref
import com.example.safego.domain.useCaseModel.ProfileData
import com.example.safego.domain.useCases.ssn.SsnDeserializer
import com.example.safego.util.helpers.personalDataValidaion.DataValidator
import com.example.safego.util.helpers.personalDataValidaion.ErrorHints
import com.google.android.material.textfield.TextInputLayout

class ProfileViewModel : ViewModel() {
    private var _profileData = MutableLiveData<ProfileData>()
    val profileData :LiveData<ProfileData> = _profileData

    private val selectedImageUri = MutableLiveData<String>()
    val selectedImageUriLiveData: LiveData<String> get() = selectedImageUri

    fun setSelectedImageUri(uri: String) {
        selectedImageUri.value = uri
    }


    fun setProfileData(sharedPref: SharedPref) {
        // Retrieve user data from SharedPreferences
        val user = getUserDataFromSharedPref(sharedPref)
        //extract data from ssn
        val ssnDeserializer = SsnDeserializer(user)
        val (government, birthDay, gender) = ssnDeserializer.getInformation()
        // Create ProfileData object
        val profileData = ProfileData(
            imagePath = user.imagePath,
            name = user.name,
            government = "From $government",
            birthDay = "Born on $birthDay",
            gender  = "Gender $gender",
            ssn = "SSN ${user.ssn}",
            phone = user.phone,
            email = user.email
        )
        _profileData.value = profileData
    }

    fun getUserDataFromSharedPref(sharedPref: SharedPref): User {
       return sharedPref.getProfileData()
   }
    private fun saveNewUserData( name: String, ssn: String, phone: String, email: String, password: String, context: Context){
        val sharedPref = SharedPref(context)
        sharedPref.saveProfileData(
            name = name,
            ssn = ssn,
            phone = phone,
            email = email,
            password = password,
            login = true
        )

    }


    fun updateProfileData(imagePath: String?,emailE:TextInputLayout,passwordE:TextInputLayout,nameE:TextInputLayout,phoneE:TextInputLayout,ssnE:TextInputLayout,context: Context):Boolean{
        var updated=false
        val name=nameE.editText!!.text.toString()
        val email=emailE.editText!!.text.toString()
        val password=passwordE.editText!!.text.toString()
        val phone=phoneE.editText!!.text.toString()
        val ssn=ssnE.editText!!.text.toString()

        //confirm if the data is valid
        if(DataValidator.validEmail(email) && DataValidator.validPassword(password)&&DataValidator.strongPassword(password)
            && DataValidator.isValidName(name) && DataValidator.validPhone(phone) && DataValidator.validSSN(ssn)
            && imagePath!=null){
            Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
            //save new data
            saveNewUserData(
                name = name,
                ssn = ssn,
                phone = phone,
                email = email,
                password = password,
                context = context
            )
            updated=true
        }
        else{
            if(!DataValidator.validEmail(email)) emailE.error= ErrorHints.showEmailError() else emailE.error=null
            if(!DataValidator.validPasswordLength(password)) passwordE.error= ErrorHints.showPasswordLengthError() else passwordE.error=null
            if(!DataValidator.isValidName(name)) nameE.error= ErrorHints.showNameError() else nameE.error=null
            if(!DataValidator.validPhone(phone)) phoneE.error= ErrorHints.showPhoneError() else phoneE.error=null
            if(!DataValidator.validSSN(ssn)) ssnE.error= ErrorHints.showSSNError() else ssnE.error=null
            if(imagePath==null) Toast.makeText(context, "Please Select Image", Toast.LENGTH_SHORT).show()
            if(!DataValidator.strongPassword(password)) passwordE.error= ErrorHints.showStrongPasswordError() else passwordE.error=null
        }

    return updated
    }


}