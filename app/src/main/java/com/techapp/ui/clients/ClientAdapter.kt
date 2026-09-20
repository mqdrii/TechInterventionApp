package com.techapp.ui.clients

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.techapp.data.model.Client
import com.techapp.databinding.ItemClientBinding
import com.techapp.utils.AvatarHelper

class ClientAdapter(
    private val isAdmin: Boolean,
    private val onItemClick: (Client) -> Unit,
    private val onCallClick: (Client) -> Unit,
    private val onMapClick: (Client) -> Unit,
    private val onDeleteClick: (Client) -> Unit
) : ListAdapter<Client, ClientAdapter.ClientViewHolder>(DiffCallback) {

    inner class ClientViewHolder(private val binding: ItemClientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(client: Client) {
            binding.tvClientName.text = client.name
            binding.tvClientPhone.text = client.phone.ifBlank { "Nessun telefono" }
            binding.tvClientAddress.text = client.address.ifBlank { "Nessun indirizzo" }

            // Avatar con Iniziali e Colore Dedicato
            binding.tvAvatar.text = AvatarHelper.getInitials(client.name)
            binding.tvAvatar.setTextColor(Color.WHITE)
            binding.tvAvatar.background?.mutate()?.setTint(AvatarHelper.getColorForName(client.name))

            // Azione Chiamata
            if (client.phone.isNotBlank()) {
                binding.btnCall.visibility = View.VISIBLE
                binding.btnCall.setOnClickListener { onCallClick(client) }
            } else {
                binding.btnCall.visibility = View.GONE
            }

            // Azione Mappa
            if (client.address.isNotBlank()) {
                binding.btnMap.visibility = View.VISIBLE
                binding.btnMap.setOnClickListener { onMapClick(client) }
            } else {
                binding.btnMap.visibility = View.GONE
            }

            // Azione Elimina (Solo Admin)
            if (isAdmin) {
                binding.btnDelete.visibility = View.VISIBLE
                binding.btnDelete.setOnClickListener { onDeleteClick(client) }
            } else {
                binding.btnDelete.visibility = View.GONE
            }

            binding.root.setOnClickListener { onItemClick(client) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val binding = ItemClientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Client>() {
        override fun areItemsTheSame(oldItem: Client, newItem: Client) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Client, newItem: Client) = oldItem == newItem
    }
}
