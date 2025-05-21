package com.example.safego.ui.signup

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.safego.dataSource.local.sharedPrefrences.SharedPref
import com.example.safego.databinding.FragmentThirdInfoBinding
import com.example.safego.util.helpers.singlton.Animator
import com.example.safego.util.helpers.singlton.AppManager
import com.example.safego.util.helpers.singlton.DialogBuilder
import java.io.File

class ThirdInfoFragment : Fragment() {

    private var _binding: FragmentThirdInfoBinding? = null
    private val binding get() = _binding!!

    private var imageUri: String? = null
    private var cameraImageUri: Uri? = null
    private var pref: SharedPref? = null

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraImageUri != null && pref != null) {
            handelAfterSelectingPhoto(cameraImageUri!!)
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && pref != null) {
          handelAfterSelectingPhoto(uri)
        }
    }
    private fun  handelAfterSelectingPhoto(uri: Uri){
        AppManager.saveOrUpdateImageFile(requireContext(),uri)
        binding.profileImage.setImageURI(uri)
        saveProfileImageUri(uri.toString())
    }
    private fun openCamera() {
        try {
            val imageFile = File.createTempFile("profile_", ".jpg", requireContext().cacheDir)
            cameraImageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                imageFile
            )
            cameraLauncher.launch(cameraImageUri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }



    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (!isCameraPermissionGranted()) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (permissions.isNotEmpty()) {
            requestPermissions(permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThirdInfoBinding.inflate(inflater, container, false)
        pref = SharedPref(requireContext())
        setControllers()
        checkAndRequestPermissions()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setControllers() {
        binding.profileImage.setOnClickListener {
            Animator.animateTxt(binding.profileImage) {
                DialogBuilder.pickImageDialog(
                    requireContext(),
                    onCameraSelected = {
                        if (isCameraPermissionGranted()) {
                            openCamera()
                        }
                    },
                    onGallerySelected = {
                        galleryLauncher.launch("image/*")
                    }
                )
            }
        }
    }

    /*this function to make the imageUri not empty and
    tell the activity all is done after selecting the image.*/
    private fun saveProfileImageUri(uri: String) {
        imageUri = uri
    }

    fun allDone(): Boolean {
        return imageUri != null
    }
}
