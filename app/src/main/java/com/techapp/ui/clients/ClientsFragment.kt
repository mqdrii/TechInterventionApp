package com.techapp.ui.clients

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.techapp.R
import com.techapp.databinding.FragmentClientsBinding
import com.techapp.utils.SessionManager

class ClientsFragment : Fragment() {

    private var _binding: FragmentClientsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClientViewModel by viewModels()
    private lateinit var adapter: ClientAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        val isAdmin = sessionManager.isAdmin()

        // Restrizione: i tecnici NON possono aggiungere contatti
        binding.fabAddClient.visibility = if (isAdmin) View.VISIBLE else View.GONE

        adapter = ClientAdapter(
            isAdmin = isAdmin,
            onItemClick = { client ->
                val action = ClientsFragmentDirections.actionClientsToClientDetail(client.id)
                findNavController().navigate(action)
            },
            onCallClick = { client ->
                if (client.phone.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}"))
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Nessun recapito telefonico disponibile", Toast.LENGTH_SHORT).show()
                }
            },
            onMapClick = { client ->
                if (client.address.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(client.address)}"))
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Nessun indirizzo inserito", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { client ->
                if (isAdmin) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Elimina cliente")
                        .setMessage("Sei sicuro di voler eliminare ${client.name}?")
                        .setPositiveButton("Elimina") { _, _ -> viewModel.deleteClient(client) }
                        .setNegativeButton("Annulla", null)
                        .show()
                }
            }
        )

        binding.rvClients.adapter = adapter

        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary)
        binding.swipeRefresh.setOnRefreshListener {
            androidx.lifecycle.lifecycleScope.launchWhenStarted {
                com.techapp.data.api.SyncManager.sync(requireContext())
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewModel.clients.observe(viewLifecycleOwner) { clients ->
            adapter.submitList(clients)
            binding.tvEmpty.visibility = if (clients.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText ?: "")
                return true
            }
        })

        if (isAdmin) {
            binding.fabAddClient.setOnClickListener {
                showAddClientDialog()
            }
        }
    }

    private fun showAddClientDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_client, null)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nuovo cliente")
            .setView(dialogView)
            .setPositiveButton("Salva") { _, _ ->
                viewModel.insertClient(
                    name = dialogView.findViewById<TextInputEditText>(R.id.et_name).text.toString(),
                    address = dialogView.findViewById<TextInputEditText>(R.id.et_address).text.toString(),
                    phone = dialogView.findViewById<TextInputEditText>(R.id.et_phone).text.toString(),
                    email = dialogView.findViewById<TextInputEditText>(R.id.et_email).text.toString(),
                    notes = dialogView.findViewById<TextInputEditText>(R.id.et_notes).text.toString()
                )
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
