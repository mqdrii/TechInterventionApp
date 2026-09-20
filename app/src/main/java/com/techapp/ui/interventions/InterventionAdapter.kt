package com.techapp.ui.interventions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.techapp.R
import com.techapp.data.model.Intervention
import com.techapp.databinding.ItemInterventionBinding

class InterventionAdapter(
    private val onItemClick: (Intervention) -> Unit,
    private val onStatusClick: (Intervention) -> Unit
) : ListAdapter<Intervention, InterventionAdapter.InterventionViewHolder>(DiffCallback) {

    inner class InterventionViewHolder(private val binding: ItemInterventionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(intervention: Intervention) {
            binding.tvClientName.text = intervention.clientName
            binding.tvDate.text = intervention.date
            binding.tvDescription.text = intervention.description

            val (colorRes, label) = when (intervention.status) {
                Intervention.STATUS_CLOSED -> Pair(R.color.status_completed, "Chiuso")
                Intervention.STATUS_IN_PROGRESS -> Pair(R.color.status_scheduled, "In corso")
                else -> Pair(R.color.status_cancelled, "Aperto")
            }
            binding.tvStatus.text = label
            binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, colorRes))

            binding.root.setOnClickListener { onItemClick(intervention) }
            binding.tvStatus.setOnClickListener {
                if (intervention.status != Intervention.STATUS_CLOSED) onStatusClick(intervention)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterventionViewHolder {
        val binding = ItemInterventionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InterventionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InterventionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Intervention>() {
        override fun areItemsTheSame(oldItem: Intervention, newItem: Intervention) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Intervention, newItem: Intervention) = oldItem == newItem
    }
}
