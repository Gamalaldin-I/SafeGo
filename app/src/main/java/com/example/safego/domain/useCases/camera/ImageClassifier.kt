package com.example.safego.domain.useCases.camera

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.safego.util.helpers.singlton.ImageProcessor
import com.example.safego.util.helpers.TensorFlowLiteHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageClassifier {

    @SuppressLint("UnsafeOptInUsageError")
    suspend fun processImage(imageProxy: ImageProxy, tflite: TensorFlowLiteHelper): Float {
        return try {
            val bitmap = imageProxy.toBitmap()
            val inputBuffer = ImageProcessor.preprocessImage(bitmap)

            // استخدام withContext مع Dispatchers.Default لمعالجة الصورة في خيط منفصل
            withContext(Dispatchers.Default) {
                tflite.runInference(inputBuffer)
            }
        } catch (e: Exception) {
            Log.e("ImageProcessing", "Error processing image", e)
            0f // إرجاع قيمة افتراضية إذا فشل المعالج
        } finally {
            imageProxy.close() // إغلاق ImageProxy بعد التحليل
        }
    }
}

