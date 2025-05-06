package com.example.safego.util.helpers.singlton

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ImageProcessor {
    fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val inputBuffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val intValues = IntArray(224 * 224)
        resizedBitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)

        for (pixelValue in intValues) {
            val r = (pixelValue shr 16 and 0xFF) / 255.0f
            val g = (pixelValue shr 8 and 0xFF) / 255.0f
            val b = (pixelValue and 0xFF) / 255.0f

            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }
        return inputBuffer
    }
}