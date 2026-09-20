package com.techapp.ui.clients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.techapp.databinding.FragmentClientDetailBinding
import com.techapp.ui.appointments.AppointmentAdapter
import com.techapp.ui.appointments.AppointmentViewModel
import com.techapp.ui.interventions.InterventionAdapter
import com.techapp.ui.interventions.InterventionViewModel

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
            binding.tvClientNotes.text = client.notes.ifBlank { "—" }
        }

        // Appuntamenti del cliente
        val appointmentAdapter = AppointmentAdapter(
            onItemClick = { /* dettaglio appuntamento */ },
            onStatusClick = { appointment ->
                appointmentViewModel.updateStatus(appointment.id, com.techapp.data.model.Appointment.STATUS_COMPLETED)
            }
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
            }
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
