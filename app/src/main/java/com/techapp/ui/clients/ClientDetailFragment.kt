package com.techapp.ui.clients

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.techapp.databinding.FragmentClientDetailBinding
import com.techapp.ui.appointments.AppointmentAdapter
import com.techapp.ui.appointments.AppointmentViewModel
import com.techapp.ui.interventions.InterventionAdapter
import com.techapp.ui.interventions.InterventionViewModel
import com.techapp.utils.AvatarHelper

class ClientDetailFragment : Fragment() {

    private var _binding: FragmentClientDetailBinding? = null
    private val binding get() = _binding!!

    private val args: ClientDetailFragmentArgs by navArgs()
    private val clientViewModel: ClientViewModel by viewModels()
    private val appointmentViewModel: AppointmentViewModel by viewModels()
    private val interventionViewModel: InterventionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val clientId = args.clientId

        // Osserva i clienti e filtra quello corrente
        clientViewModel.clients.observe(viewLifecycleOwner) { clients ->
            val client = clients.firstOrNull { it.id == clientId } ?: return@observe
            binding.tvClientName.text = client.name
            binding.tvClientPhone.text = client.phone.ifBlank { "N/D" }
            binding.tvClientEmail.text = client.email.ifBlank { "N/D" }
            binding.tvClientAddress.text = client.address.ifBlank { "N/D" }
            binding.tvClientNotes.text = client.notes.ifBlank { "Nessuna nota presente per questo cliente." }

            // Avatar con Iniziali e Colore Deterministico
            binding.tvAvatar.text = AvatarHelper.getInitials(client.name)
            binding.tvAvatar.setTextColor(Color.WHITE)
            binding.tvAvatar.background?.mutate()?.setTint(AvatarHelper.getColorForName(client.name))

            // Azione Chiama
            binding.btnCallClient.setOnClickListener {
                if (client.phone.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}"))
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Nessun recapito telefonico disponibile", Toast.LENGTH_SHORT).show()
                }
            }

            // Azione Navigazione Mappa
            binding.btnMapClient.setOnClickListener {
                if (client.address.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(client.address)}"))
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Nessun indirizzo inserito", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Appuntamenti del cliente
        val appointmentAdapter = AppointmentAdapter(
            onItemClick = { /* dettaglio appuntamento */ },
            onStatusClick = { appointment ->
                appointmentViewModel.updateStatus(appointment.id, com.techapp.data.model.Appointment.STATUS_COMPLETED)
            },
            onDeleteClick = { /* elimina non disponibile nella vista cliente */ }
        )
        binding.rvClientAppointments.adapter = appointmentAdapter
        appointmentViewModel.getAppointmentsByClient(clientId).observe(viewLifecycleOwner) {
            appointmentAdapter.submitList(it)
        }

        // Interventi del cliente
        val interventionAdapter = InterventionAdapter(
            onItemClick = { /* dettaglio intervento */ },
            onStatusClick = { intervention ->
                interventionViewModel.updateStatus(intervention.id, com.techapp.data.model.Intervention.STATUS_CLOSED)
            },
            onDeleteClick = { /* elimina non disponibile nella vista cliente */ }
        )
        binding.rvClientInterventions.adapter = interventionAdapter
        interventionViewModel.getInterventionsByClient(clientId).observe(viewLifecycleOwner) {
            interventionAdapter.submitList(it)
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
