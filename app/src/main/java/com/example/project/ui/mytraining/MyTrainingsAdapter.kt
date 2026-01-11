package com.example.project.ui.mytraining

import com.example.project.data.model.MyTrainingUi
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import android.view.View
import android.widget.TextView
import com.example.project.R
import android.view.ViewGroup
import android.view.LayoutInflater
import java.util.Calendar

class MyTrainingsAdapter(
    private var items: List<MyTrainingUi> = emptyList()
) : RecyclerView.Adapter<MyTrainingsAdapter.VH>() {

    fun submitList(newItems: List<MyTrainingUi>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardTraining)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvTrainer: TextView = view.findViewById(R.id.tvTrainer)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        val tvPassed: TextView = view.findViewById(R.id.tvPassed)
        val tvSource: TextView = view.findViewById(R.id.tvSource)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_my_training, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.tvTitle.text = item.title
        holder.tvDate.text = formatDate(item.startAt)
        holder.tvTime.text = formatTime(item.startAt)
        holder.tvTrainer.text = if (item.trainerName.isNotBlank()) "Тренер: ${item.trainerName}" else "Тренер: —"
        holder.tvDuration.text = "${calcDuration(item.startAt, item.endAt)} хв"

        holder.tvSource.text = if (item.source.equals("membership", true)) "Абонемент" else "Разове"

        if (item.isPassed) {
            holder.card.alpha = 0.45f
            holder.tvPassed.visibility = View.VISIBLE
        } else {
            holder.card.alpha = 1f
            holder.tvPassed.visibility = View.GONE
        }

        holder.card.isEnabled = false // це “мої тренування”, без кліку або потім додамо
    }

    override fun getItemCount(): Int = items.size

    private fun formatTime(millis: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
    }

    private fun formatDate(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale("uk", "UA"))
        return sdf.format(java.util.Date(millis))
    }

    private fun calcDuration(start: Long, end: Long): Int {
        if (end <= start) return 60
        return ((end - start) / 60000L).toInt()
    }
}