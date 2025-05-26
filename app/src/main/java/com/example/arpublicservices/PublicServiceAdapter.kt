package com.example.arpublicservices

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PublicServiceAdapter(private val services: List<PublicService>) : 
    RecyclerView.Adapter<PublicServiceAdapter.ServiceViewHolder>() {
    
    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.service_name)
        val typeTextView: TextView = itemView.findViewById(R.id.service_type)
        val descriptionTextView: TextView = itemView.findViewById(R.id.service_description)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        holder.nameTextView.text = service.name
        holder.typeTextView.text = service.type.replaceFirstChar { it.uppercase() }
        holder.descriptionTextView.text = service.description
    }
    
    override fun getItemCount() = services.size
}
