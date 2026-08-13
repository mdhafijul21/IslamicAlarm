package com.hafij.islamicalarm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hafij.islamicalarm.data.AlarmItem
import com.hafij.islamicalarm.databinding.ItemAlarmBinding

class AlarmAdapter(
    private val onToggle: (AlarmItem, Boolean) -> Unit,
    private val onDelete: (AlarmItem) -> Unit,
    private val onClick: (AlarmItem) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.VH>() {

    private var items: List<AlarmItem> = emptyList()

    fun submitList(newItems: List<AlarmItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemAlarmBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.timeText.text = item.timeText()
        holder.binding.detailText.text = holder.itemView.context.getString(
            R.string.duration_label_format, item.lockDurationMinutes, item.label
        )
        holder.binding.enableSwitch.setOnCheckedChangeListener(null)
        holder.binding.enableSwitch.isChecked = item.enabled
        holder.binding.enableSwitch.setOnCheckedChangeListener { _, checked -> onToggle(item, checked) }
        holder.binding.deleteButton.setOnClickListener { onDelete(item) }
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
