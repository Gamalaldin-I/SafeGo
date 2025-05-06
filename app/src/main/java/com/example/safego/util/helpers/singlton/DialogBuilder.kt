package com.example.safego.util.helpers.singlton

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.widget.TextView
import com.example.safego.R


object DialogBuilder {
    private var loadingDialog : Dialog? = null
    private var warningDialog : Dialog? = null
    private var  dangerAnimation : com.airbnb.lottie.LottieAnimationView? = null
    private var loadingAnimation : com.airbnb.lottie.LottieAnimationView? = null
    fun showAlertDialog(
        context: Context,
        message: String,
        title: String,
        positiveButton: String,
        negativeButton: String,
        onConfirm: () -> Unit,
        onCancel: () -> Unit,
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setTitle(title)

        builder.setPositiveButton(positiveButton) { dialog, _ ->
            onConfirm()
            dialog.dismiss()
        }
        builder.setNegativeButton(negativeButton) { dialog, _ ->
            onCancel()
            dialog.dismiss()

        }
        val dialog = builder.create()

        dialog.show()

    }

    fun showLoadingDialog(context: Context,message: String){
        loadingDialog = Dialog(context)
        loadingDialog!!.setContentView(R.layout.loading_dialog)
        loadingDialog!!.setCancelable(false)
        loadingDialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
        loadingAnimation = loadingDialog!!.findViewById(R.id.progressBar)
        loadingAnimation!!.playAnimation()
        val textView = loadingDialog!!.findViewById<TextView>(R.id.message)
        textView.text = message
        loadingDialog!!.show()
    }
    fun cancelLoadingDialog(){
        loadingAnimation!!.cancelAnimation()
        loadingDialog?.dismiss()
        loadingDialog =null
    }


    fun showWarningDialog(context: Context){
        warningDialog = Dialog(context)
        warningDialog!!.setContentView(R.layout.warninig_dialog)
        warningDialog!!.setCancelable(false)
        warningDialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dangerAnimation = warningDialog!!.findViewById(R.id.dangerAnimation)
        dangerAnimation!!.playAnimation()
        warningDialog!!.show()
    }
    fun cancelWarningDialog(){
        dangerAnimation!!.cancelAnimation()
        warningDialog?.dismiss()
        warningDialog =null
    }


}