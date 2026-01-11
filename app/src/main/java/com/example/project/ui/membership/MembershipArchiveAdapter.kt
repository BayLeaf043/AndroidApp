package com.example.project.ui.membership

import com.example.project.data.model.MyMembershipUi
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.example.project.R
import android.view.ViewGroup
import android.view.LayoutInflater
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MembershipArchiveAdapter(
    private var items: List<MyMembershipUi> = emptyList()
) : RecyclerView.Adapter<MembershipArchiveAdapter.VH>() {

    fun submitList(newItems: List<MyMembershipUi>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvMeta: TextView = view.findViewById(R.id.tvMeta)
        val tvPeriod: TextView = view.findViewById(R.id.tvPeriod)
        val tvVisits: TextView = view.findViewById(R.id.tvVisits)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_membership_archive, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.tvTitle.text = item.title.ifBlank { "Абонемент" }
        holder.tvStatus.text = "Закінчений"

        val type = item.trainingType.ifBlank { "-" }.uppercase()
        val level = item.level.ifBlank { "-" }
        holder.tvMeta.text = "$type • $level • ${item.ageGroup.ifBlank { "-" }}"

        val startText = formatDate(item.startAtMillis)
        val endText = formatDate(item.endAtMillis)
        holder.tvPeriod.text = "$startText — $endText"

        // Показуємо як у тебе: left/total
        holder.tvVisits.text = "Занять: ${item.visitsLeft}/${item.visitsTotal}"
    }

    override fun getItemCount(): Int = items.size

    private fun formatDate(ms: Long): String {
        if (ms <= 0L) return "-"
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date(ms))
    }
}