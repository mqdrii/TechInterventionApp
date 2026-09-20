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

            val (colorRes, label) = when (appointment.status) {
                Appointment.STATUS_COMPLETED -> Pair(R.color.status_completed, "Completato")
                Appointment.STATUS_CANCELLED -> Pair(R.color.status_cancelled, "Annullato")
                else -> Pair(R.color.status_scheduled, "Programmato")
            }
            binding.tvStatus.text = label
            binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, colorRes))

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
