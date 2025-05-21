package com.example.safego.util.helpers.singlton
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.example.safego.dataSource.local.sharedPrefrences.SharedPref
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object AppManager {

    fun saveOrUpdateImageFile(context: Context, imageUri: Uri, fileName: String = "my_image.jpg"): File? {
        try {
            val filesDir = context.filesDir
            val imageFile = File(filesDir, fileName)

            // لو الملف موجود، نحذفه الأول
            if (imageFile.exists()) {
                imageFile.delete()
            }

            // نعمل ملف جديد وننسخ الصورة الجديدة فيه
            val inputStream = context.contentResolver.openInputStream(imageUri)
            if (inputStream != null) {
                val outputStream = FileOutputStream(imageFile)
                inputStream.copyTo(outputStream)
                outputStream.close()
                inputStream.close()

                // احفظ المسار في SharedPref لو بتستخدم
                val pref = SharedPref(context)
                pref.saveProfileImage(imageFile.absolutePath)

                return imageFile
            }
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    fun viewTheProfileInImageView(context: Context,imageView: ImageView){
        val pref = SharedPref(context)
        val imagePath = pref.getProfileData().imagePath
       // val imageFile = File(imagePath)
        val bitmab = BitmapFactory.decodeFile(imagePath)
        imageView.setImageBitmap(bitmab)
    }


}