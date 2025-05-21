package com.example.safego.ui.profile

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.example.safego.R
import com.example.safego.dataSource.local.model.User
import com.example.safego.dataSource.local.sharedPrefrences.SharedPref
import com.example.safego.databinding.ActivityProfileBinding
import com.example.safego.domain.useCaseModel.ProfileData
import com.example.safego.util.helpers.singlton.AppManager
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputLayout

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var binding: ActivityProfileBinding
    private lateinit var pref: SharedPref
    private var imagePath: String? = null
    private lateinit var profileIv : ShapeableImageView
    private lateinit var userNameLayout : TextInputLayout
    private lateinit var ssnLayout : TextInputLayout
    private lateinit var emailLayout : TextInputLayout
    private lateinit var passwordLayout : TextInputLayout
    private lateinit var phoneLayout : TextInputLayout
    private lateinit var editDialog: Dialog

    companion object{
        const val GALLERY_REQUEST_CODE = 101
        var PERMISSION_REQUEST_CODE = 100
    }



    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = SharedPref(this)

        checkAndRequestPermissions()
        setControllers()
        viewModel.setProfileData(pref)
        viewModel.profileData.observe(this) {
            setView(it)
        }


        binding.backArrow.setOnClickListener {
            finish()
        }
    }

    private fun setView(data: ProfileData) {
        binding.name.text = data.name
        binding.government.text = data.government
        binding.birthdate.text = data.birthDay
        binding.gender.text = data.gender
        binding.ssn.text = data.ssn
        binding.phoneNum.text = data.phone
        binding.email.text = data.email
        binding.profileImage.setImageURI(data.imagePath.toUri())
    }

    private fun setControllers() {
        binding.editBtn.setOnClickListener {
            val user = viewModel.getUserDataFromSharedPref(pref)
            showUpdateProfileDetailsDialog(this, user)
            binding.editBtn.isEnabled = false
            Handler().postDelayed({
                binding.editBtn.isEnabled = true
            }, 1000)
        }
    }

    @SuppressLint("IntentReset")
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }


    private fun checkAndRequestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(READ_MEDIA_IMAGES)
        } else {
            arrayOf(READ_EXTERNAL_STORAGE)
        }
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            if (selectedImageUri != null) {
                profileIv.setImageURI(selectedImageUri)
                saveProfileImageUri(selectedImageUri.toString())
            }
        }
    }

    private fun saveProfileImageUri(uri: String) {
        imagePath = uri
    }

    private fun showUpdateProfileDetailsDialog(context: Context, user: User) {
        editDialog = Dialog(context)
        editDialog.setContentView(R.layout.edit_info_dialog)
        editDialog.setCancelable(true)
        editDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        profileIv = editDialog.findViewById(R.id.profileIv)
        userNameLayout = editDialog.findViewById(R.id.userNameLayout)
        ssnLayout = editDialog.findViewById(R.id.ssnLayout)
        phoneLayout = editDialog.findViewById(R.id.phoneLayout)
        emailLayout = editDialog.findViewById(R.id.emailLayout)
        passwordLayout = editDialog.findViewById(R.id.passwordLayout)
        val confirmBtn = editDialog.findViewById<Button>(R.id.confirmBtn)

        profileIv.setImageURI(user.imagePath.toUri())
        userNameLayout.editText?.setText(user.name)
        ssnLayout.editText?.setText(user.ssn)
        phoneLayout.editText?.setText(user.phone)
        emailLayout.editText?.setText(user.email)
        passwordLayout.editText?.setText(user.password)

        profileIv.setOnClickListener {
            pickImage()
        }

        confirmBtn.setOnClickListener {
            val oldPic = user.imagePath.toUri()
            val imagePath = imagePath?.toUri()?:oldPic
            if(viewModel.updateProfileData(imagePath.toString(),
                    emailLayout,passwordLayout,
                    userNameLayout,phoneLayout,
                    ssnLayout,this)) {
                editDialog.dismiss()
                viewModel.setProfileData(pref)
                viewModel.profileData.observe(this) {
                    setView(it)
                    binding.profileImage.setImageURI(imagePath)
                    AppManager.saveOrUpdateImageFile(this,imagePath)
                }
            }
        }
        editDialog.show()
    }

}