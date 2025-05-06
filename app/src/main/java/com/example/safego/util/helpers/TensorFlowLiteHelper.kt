package com.example.safego.util.helpers

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TensorFlowLiteHelper(context: Context) {
     private val tflite: Interpreter

        init {
            tflite = Interpreter(loadModelFile(context), Interpreter.Options())
        }

        private fun loadModelFile(context: Context): ByteBuffer {
            val fileDescriptor = context.assets.openFd("model.tflite")
            val inputStream = fileDescriptor.createInputStream()
            val modelBuffer = inputStream.readBytes().toByteBuffer()
            inputStream.close()
            return modelBuffer
        }
    private fun ByteArray.toByteBuffer(): ByteBuffer {
        return ByteBuffer.allocateDirect(this.size).apply {
            order(ByteOrder.nativeOrder())
            put(this@toByteBuffer)
        }
    }


    fun runInference(inputBuffer: ByteBuffer): Float {
            val output = Array(1) { FloatArray(1) }
            tflite.run(inputBuffer, output)
            return output[0][0] * 100
        }
    }

