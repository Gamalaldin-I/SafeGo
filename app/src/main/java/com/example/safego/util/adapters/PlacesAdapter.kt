package com.example.safego.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.safego.R
import com.example.safego.databinding.PlaceCardBinding
import com.example.safego.domain.useCaseModel.NearbyPlace

class PlacesAdapter(private val data: ArrayList<NearbyPlace>,private var onItemClick: (NearbyPlace) -> Unit ={}) :
    RecyclerView.Adapter<PlacesAdapter.PlaceHolder>() {

    // Create ViewHolder class
    class PlaceHolder( val binding: PlaceCardBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val binding = PlaceCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceHolder(binding)
    }

    // Bind data to the ViewHolder
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        val item = data[position]
        val binding = holder.binding

        binding.placeName.text = if (item.name.length > 25) {
            item.name.substring(0, 22) + "..."
        } else {
            item.name
        }
        binding.placeClass.text = item.type
        binding.placeDistance.text = "${item.distance} m"
        binding.placeImage.setImageResource(item.image)
        binding.root.setOnClickListener {
            onItemClick(item)
        }

    }

    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
}