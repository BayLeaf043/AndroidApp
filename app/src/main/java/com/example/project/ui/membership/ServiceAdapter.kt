package com.example.project.ui.membership

import com.example.project.data.model.Service
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.example.project.R
import android.view.ViewGroup
import android.view.LayoutInflater

class ServiceAdapter(
    private var items: List<Service> = emptyList(),
    private val onClick: (Service) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    fun submitList(newItems: List<Service>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_membership, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val item = items[position]

        holder.tvTitle.text = item.title

        val type = item.trainingType.uppercase()
        val level = item.level.lowercase().replaceFirstChar { it.uppercase() }

        holder.tvSubtitle.text = if (item.sessionsCount > 0) {
            "$type $level · ${item.sessionsCount} занять"
        } else {
            "$type $level"
        }

        holder.tvPrice.text = "${item.price} грн"
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size
}