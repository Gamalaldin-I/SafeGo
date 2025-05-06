package com.example.safego.util.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.safego.databinding.LocationItemBinding
import com.example.safego.domain.useCaseModel.LocationAddress

class LocationsAdapter(
    private val data: ArrayList<LocationAddress>,
    private val onItemClick : (query: String) -> Unit
) : RecyclerView.Adapter<LocationsAdapter.LocHolder>() {

    // Create ViewHolder class
    class LocHolder(val binding: LocationItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocHolder {
        val binding =
            LocationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: LocHolder, position: Int) {
        holder.binding.locationName.text = data[position].name
        holder.binding.root.setOnClickListener {
            onItemClick(data[position].name)
        }
    }

    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
}