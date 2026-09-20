package com.techapp.ui.clients

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.techapp.data.model.Client
import com.techapp.databinding.ItemClientBinding

class ClientAdapter(
    private val onItemClick: (Client) -> Unit,
    private val onDeleteClick: (Client) -> Unit
) : ListAdapter<Client, ClientAdapter.ClientViewHolder>(DiffCallback) {

    inner class ClientViewHolder(private val binding: ItemClientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(client: Client) {
            binding.tvClientName.text = client.name
            binding.tvClientPhone.text = client.phone.ifBlank { "N/D" }
            binding.tvClientAddress.text = client.address.ifBlank { "N/D" }
            binding.root.setOnClickListener { onItemClick(client) }
            binding.btnDelete.setOnClickListener { onDeleteClick(client) }
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
