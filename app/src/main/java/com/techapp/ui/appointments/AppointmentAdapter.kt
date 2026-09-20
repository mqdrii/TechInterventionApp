package com.techapp.ui.appointments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.techapp.R
import com.techapp.data.model.Appointment
import com.techapp.databinding.ItemAppointmentBinding

class AppointmentAdapter(
    private val onItemClick: (Appointment) -> Unit,
    private val onStatusClick: (Appointment) -> Unit
) : ListAdapter<Appointment, AppointmentAdapter.AppointmentViewHolder>(DiffCallback) {

    inner class AppointmentViewHolder(private val binding: ItemAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: Appointment) {
            binding.tvClientName.text = appointment.clientName
            binding.tvDate.text = "${appointment.date} ${appointment.time}"
            binding.tvDescription.text = appointment.description

            // Reparto con icona
            val deptIcon = when (appointment.department) {
                "Computer" -> "💻"
                "Telefoni" -> "📱"
                "Montaggio Lavagne" -> "🛠️"
                else -> "⚙️"
            }
            binding.tvDepartment.text = "$deptIcon ${appointment.department}"

            // Tecnico assegnato
            if (appointment.assignedUserName.isNotBlank()) {
                binding.tvAssignedTo.text = "👤 ${appointment.assignedUserName}"
                binding.tvAssignedTo.visibility = android.view.View.VISIBLE
            } else {
                binding.tvAssignedTo.text = "👤 Non assegnato"
                binding.tvAssignedTo.visibility = android.view.View.VISIBLE
            }

            val (bgRes, textColorRes, label) = when (appointment.status) {
                Appointment.STATUS_COMPLETED -> Triple(R.drawable.badge_status_completed, R.color.badge_completed_text, "Completato")
                Appointment.STATUS_CANCELLED -> Triple(R.drawable.badge_status_cancelled, R.color.badge_cancelled_text, "Annullato")
                else -> Triple(R.drawable.badge_status_scheduled, R.color.badge_scheduled_text, "Programmato")
            }
            binding.tvStatus.text = label
            binding.tvStatus.setBackgroundResource(bgRes)
            binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, textColorRes))

            binding.root.setOnClickListener { onItemClick(appointment) }
            binding.tvStatus.setOnClickListener {
                if (appointment.status == Appointment.STATUS_SCHEDULED) onStatusClick(appointment)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Appointment>() {
        override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment) = oldItem == newItem
    }
}
