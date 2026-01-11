package com.example.project.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.model.ScheduleUi
import com.google.android.material.card.MaterialCardView
import java.util.Calendar

class ScheduleAdapter (
    private var items: List<ScheduleUi> = emptyList(),
    private val onItemClick: (ScheduleUi) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    fun submitList(newItems: List<ScheduleUi>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardTraining)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvTrainer: TextView = view.findViewById(R.id.tvTrainer)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        val tvPassed: TextView = view.findViewById(R.id.tvPassed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_training, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val item = items[position]

        holder.tvTitle.text = item.title
        holder.tvTime.text = formatTime(item.startAt)
        holder.tvTrainer.text = item.trainerName
        holder.tvDuration.text = "${calcDuration(item.startAt, item.endAt)} хв"

        if (item.isPassed) {
            holder.card.alpha = 0.45f
            holder.tvPassed.visibility = View.VISIBLE
        } else {
            holder.card.alpha = 1f
            holder.tvPassed.visibility = View.GONE
        }

        holder.card.isEnabled = !item.isPassed
        holder.card.setOnClickListener { if (!item.isPassed) onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    private fun formatTime(millis: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return String.format("%02d:%02d",
            c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE)
        )
    }

    private fun calcDuration(start: Long, end: Long): Int {
        if (end <= start) return 60
        return ((end - start) / 60000L).toInt()
    }

}