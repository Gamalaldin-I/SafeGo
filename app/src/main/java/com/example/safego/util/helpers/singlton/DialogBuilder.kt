package com.example.safego.util.helpers.singlton

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.safego.R
import com.example.safego.dataSource.local.model.Destination
import com.example.safego.util.adapters.SavedLocationsAdapter


object DialogBuilder {
    private var loadingDialog : Dialog? = null
    private var warningDialog : Dialog? = null
    private var dangerAnimation : com.airbnb.lottie.LottieAnimationView? = null
    private var loadingAnimation : com.airbnb.lottie.LottieAnimationView? = null
    private var savedLocationsDialog : Dialog? = null
     @JvmStatic
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
        if (warningDialog!=null){
            dangerAnimation!!.cancelAnimation()
            warningDialog?.dismiss()
            warningDialog =null
        }
    }

    fun showSavedLocationsDialog(onItemClick:(Destination) -> Unit,onDeleteClick:(Destination) -> Unit,context: Context,listOfLocations:ArrayList<Destination>,onClearClick:()->Unit){
        if (listOfLocations.isEmpty()){
            Toast.makeText(context, "The history is empty", Toast.LENGTH_SHORT).show()
        }
        else{
            showLocationsDialog(onItemClick,onDeleteClick,context,listOfLocations,onClearClick)
        }
    }
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun showLocationsDialog(onItemClick:(Destination) -> Unit, onDeleteClick:(Destination) -> Unit, context: Context, listOfLocations:ArrayList<Destination>,onClearClick:()->Unit){
        this.savedLocationsDialog = Dialog(context)
        savedLocationsDialog!!.setContentView(R.layout.saved_locations_dialog)
        savedLocationsDialog!!.setCancelable(true)
        savedLocationsDialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val textView = savedLocationsDialog!!.findViewById<TextView>(R.id.textView3)
        textView.text = "Recent Searches"
        val recyclerView = savedLocationsDialog!!.findViewById<RecyclerView>(R.id.locationsAdapter)
        val clearHistory = savedLocationsDialog!!.findViewById<TextView>(R.id.clearHistory)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        val myList = ArrayList(listOfLocations.reversed())
        recyclerView.adapter = SavedLocationsAdapter(myList,onItemClick) {
            onDeleteClick(it)
            recyclerView.adapter?.notifyItemRemoved(myList.indexOf(it))
            recyclerView.adapter?.notifyItemRangeChanged(myList.indexOf(it), myList.size)
            myList.remove(it)
            if (myList.isEmpty()){
                Toast.makeText(context, "All cleared", Toast.LENGTH_SHORT).show()
                cancelSavedLocationsDialog()
            }
        }
        clearHistory.setOnClickListener {
            showAlertDialog(
                context,"Are you sure you want to clear all history?",
                "Warning","Yes","No",{
                    myList.clear()
                    recyclerView.adapter?.notifyDataSetChanged()
                    Toast.makeText(context, "All cleared", Toast.LENGTH_SHORT).show()
                    onClearClick()
                    cancelSavedLocationsDialog()
                },
                {}
            )
        }
        savedLocationsDialog!!.show()
    }
    fun cancelSavedLocationsDialog(){
        savedLocationsDialog?.dismiss()
        savedLocationsDialog =null
    }
    fun pickImageDialog(context: Context,onCameraSelected:()->Unit,onGallerySelected:()->Unit){
        val options = arrayOf("Open camera","Go to Gallery")
        val selectDialog =AlertDialog.Builder(context)
        selectDialog.setTitle("Select a Profile photo")
            .setItems(options){_,w->
                when(w){
                    0 -> onCameraSelected()
                    1-> onGallerySelected()
                }
            }
        selectDialog.show()
    }



}