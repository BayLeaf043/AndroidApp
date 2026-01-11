package com.example.project.ui.schedule

import androidx.recyclerview.widget.RecyclerView
import com.example.project.data.model.DayUi
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.project.R

class DaysAdapter (
    private var items: List<DayUi>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<DaysAdapter.VH>() {

    var selectedPosition: Int = 0
        set(value) {
            val old = field
            field = value
            notifyItemChanged(old)
            notifyItemChanged(value)
        }

    fun submitList(newItems: List<DayUi>) {
        items = newItems
        selectedPosition = 0
        notifyDataSetChanged()
    }

    inner class VH(val view: View) : RecyclerView.ViewHolder(view) {
        val card: androidx.cardview.widget.CardView = view.findViewById(R.id.cardRoot) // дай id!
        val tvName: TextView = view.findViewById(R.id.tvDayName)
        val tvNum: TextView = view.findViewById(R.id.tvDayNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_days, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvName.text = item.dayNameShort
        holder.tvNum.text = item.dayNumber.toString()

        val isSelected = position == selectedPosition
        holder.card.setCardBackgroundColor(
            if (isSelected) 0xFFFF9800.toInt() else 0xFF3A3A3A.toInt()
        )

        holder.itemView.setOnClickListener { onClick(position) }
    }

    override fun getItemCount(): Int = items.size
}

