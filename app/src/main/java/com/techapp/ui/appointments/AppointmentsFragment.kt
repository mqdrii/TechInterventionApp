package com.techapp.ui.appointments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.techapp.R
import com.techapp.databinding.FragmentAppointmentsBinding
import kotlinx.coroutines.launch

class AppointmentsFragment : Fragment() {

    private var _binding: FragmentAppointmentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppointmentViewModel by viewModels()
    private lateinit var adapter: AppointmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AppointmentAdapter(
            onItemClick = { /* dettaglio futuro */ },
            onStatusClick = { appointment ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Aggiorna stato")
                    .setItems(arrayOf("Completato", "Annullato")) { _, which ->
                        val status = if (which == 0) com.techapp.data.model.Appointment.STATUS_COMPLETED
                                     else com.techapp.data.model.Appointment.STATUS_CANCELLED
                        viewModel.updateStatus(appointment.id, status)
                    }
                    .show()
            }
        )

        binding.rvAppointments.adapter = adapter

        viewModel.allAppointments.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary)
        binding.swipeRefresh.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch {
                com.techapp.data.api.SyncManager.sync(requireContext())
                binding.swipeRefresh.isRefreshing = false
            }
        }

        val sessionManager = com.techapp.utils.SessionManager(requireContext())
        binding.fabAddAppointment.visibility = if (sessionManager.isAdmin()) View.VISIBLE else View.GONE

        binding.fabAddAppointment.setOnClickListener {
            findNavController().navigate(R.id.action_appointments_to_newAppointment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
