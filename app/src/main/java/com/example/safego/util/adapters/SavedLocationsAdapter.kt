package com.example.safego.util.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.safego.dataSource.local.model.Destination
import com.example.safego.databinding.SavedDestinationItemBinding

class SavedLocationsAdapter(private val data: ArrayList<Destination>,
    private val onItemClick: (Destination) -> Unit,
    private val onDeleteClick: (Destination) -> Unit

) :
    RecyclerView.Adapter<SavedLocationsAdapter.DehHolder>() {

    // Create ViewHolder class
    class DehHolder(val binding: SavedDestinationItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Create ViewHolder and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DehHolder {
        val binding =
            SavedDestinationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DehHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: DehHolder, position: Int) {
        val item = data[position]
        holder.binding.locationName.text = item.name
        holder.binding.deleteBtn.setOnClickListener {
            onDeleteClick(item)
        }
        holder.binding.root.setOnClickListener {
            onItemClick(item)
        }
    }

    // Return the size of the data list
    override fun getItemCount(): Int {
        return data.size
    }
}