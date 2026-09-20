package com.techapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.techapp.R
import com.techapp.databinding.FragmentDashboardBinding
import com.techapp.utils.SessionManager

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvWelcome.text = "Ciao, ${viewModel.getUserName()}!"
        binding.tvDate.text = viewModel.todayDate

        if (viewModel.isAdmin()) {
            binding.tvRoleBadge.text = "👑 Amministratore"
            binding.tvSubtitleHeader.text = "⚡ Gestione e Assegnazione Lavori"
            binding.tvOverviewTitle.text = "Panoramica Aziendale"
        } else {
            val dept = viewModel.getUserDepartment()
            val icon = when (dept) {
                "Computer" -> "💻"
                "Telefoni" -> "📱"
                "Montaggio Lavagne" -> "🛠️"
                else -> "🔧"
            }
            binding.tvRoleBadge.text = "$icon Tecnico • $dept"
            binding.tvSubtitleHeader.text = "⚡ I tuoi lavori assegnati nel reparto $dept"
            binding.tvOverviewTitle.text = "Il Mio Carico di Lavoro"
        }

        viewModel.todayAppointmentCount.observe(viewLifecycleOwner) { count ->
            binding.tvAppointmentCount.text = count.toString()
        }

        viewModel.openInterventionCount.observe(viewLifecycleOwner) { count ->
            binding.tvInterventionCount.text = count.toString()
        }

        // Navigazione rapida dai card
        binding.cardAppointments.setOnClickListener {
            findNavController().navigate(R.id.appointmentsFragment)
        }

        binding.cardInterventions.setOnClickListener {
            findNavController().navigate(R.id.interventionsFragment)
        }

        binding.cardClients.setOnClickListener {
            findNavController().navigate(R.id.clientsFragment)
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            SessionManager(requireContext()).clearSession()
            findNavController().navigate(R.id.action_dashboard_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
