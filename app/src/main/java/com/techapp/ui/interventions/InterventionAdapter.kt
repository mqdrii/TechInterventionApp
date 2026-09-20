package com.techapp.ui.interventions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.techapp.R
import com.techapp.data.model.Intervention
import com.techapp.databinding.ItemInterventionBinding
import com.techapp.utils.DepartmentHelper

class InterventionAdapter(
    private val onItemClick: (Intervention) -> Unit,
    private val onStatusClick: (Intervention) -> Unit,
    private val onDeleteClick: (Intervention) -> Unit
) : ListAdapter<Intervention, InterventionAdapter.InterventionViewHolder>(DiffCallback) {

    inner class InterventionViewHolder(private val binding: ItemInterventionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(intervention: Intervention) {
            binding.tvClientName.text = intervention.clientName

            // Reparto con icona vettoriale
            binding.ivDeptIcon.setImageResource(DepartmentHelper.getIconRes(intervention.department))
            binding.tvDepartment.text = intervention.department

            // Tecnico assegnato
            val tech = if (intervention.assignedUserName.isNotBlank()) intervention.assignedUserName else "Non assegnato"
            binding.tvAssignedTo.text = tech

            binding.tvDate.text = intervention.date
            binding.tvDescription.text = intervention.description

            val (bgRes, textColorRes, label) = when (intervention.status) {
                Intervention.STATUS_CLOSED -> Triple(R.drawable.badge_status_completed, R.color.badge_completed_text, "Chiuso")
                Intervention.STATUS_IN_PROGRESS -> Triple(R.drawable.badge_status_in_progress, R.color.badge_in_progress_text, "In corso")
                else -> Triple(R.drawable.badge_status_open, R.color.badge_open_text, "Aperto")
            }
            binding.tvStatus.text = label
            binding.tvStatus.setBackgroundResource(bgRes)
            binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, textColorRes))

            binding.btnDelete.setOnClickListener { onDeleteClick(intervention) }

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
