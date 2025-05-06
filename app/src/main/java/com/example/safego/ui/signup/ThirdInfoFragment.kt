package com.example.safego.ui.signup

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.app.ActivityCompat
import com.example.safego.databinding.FragmentThirdInfoBinding
import com.example.safego.util.helpers.singlton.Animator

class ThirdInfoFragment : Fragment() {
    private lateinit var binding: FragmentThirdInfoBinding
    private var imageUri: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    companion object {
        const val GALLERY_REQUEST_CODE = 101
        var PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        //inflate
        binding = FragmentThirdInfoBinding.inflate(inflater, container, false)
        setControllers()
        checkAndRequestPermissions()
        return binding.root

    }

    private fun setControllers() {
        binding.profileImage.setOnClickListener {
            Animator.animateTxt(binding.profileImage) {
            pickImage()
            }
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
        // فحص الأذونات دفعة واحدة وطلبها إذا لزم الأمر
        ActivityCompat.requestPermissions(requireActivity(), permissions, PERMISSION_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            if (selectedImageUri != null) {
                // تعيين الصورة في ImageView
                binding.profileImage.setImageURI(selectedImageUri)

                saveProfileImageUri(selectedImageUri.toString())
            }
        }
    }
    private fun saveProfileImageUri(uri: String) {
        imageUri = uri
    }
    fun getData(): String {
        return imageUri!!
    }

    fun allDone(): Boolean {
        return imageUri != null
    }
}












