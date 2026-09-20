package com.techapp.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.techapp.R
import com.techapp.data.api.SyncManager
import com.techapp.data.api.SyncStatus
import com.techapp.data.model.Appointment
import com.techapp.data.model.Intervention
import com.techapp.databinding.FragmentDashboardBinding
import com.techapp.ui.common.ServerConfigDialog
import com.techapp.utils.DepartmentHelper
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

        // Cloud Server Configuration & Status
        binding.btnServerConfig.setOnClickListener {
            ServerConfigDialog.show(requireContext()) {
                viewModel.syncData()
            }
        }

        binding.btnServerStatus.setOnClickListener {
            ServerConfigDialog.show(requireContext()) {
                viewModel.syncData()
            }
        }

        viewModel.syncStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                SyncStatus.ONLINE -> {
                    binding.ivSyncDot.setColorFilter(Color.parseColor("#10B981"))
                    binding.tvSyncLabel.text = "Cloud Connesso"
                    binding.swipeRefresh.isRefreshing = false
                }
                SyncStatus.SYNCING -> {
                    binding.ivSyncDot.setColorFilter(Color.parseColor("#F59E0B"))
                    binding.tvSyncLabel.text = "Sincronizzazione..."
                }
                SyncStatus.OFFLINE, null -> {
                    binding.ivSyncDot.setColorFilter(Color.parseColor("#94A3B8"))
                    binding.tvSyncLabel.text = "Modalità Locale"
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }

        // Pull to refresh
        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.syncData()
        }

        // Trigger initial background sync
        viewModel.syncData()

        val isAdmin = viewModel.isAdmin()
        if (isAdmin) {
            binding.ivRoleIcon.setImageResource(R.drawable.ic_shield_admin)
            binding.tvRoleBadge.text = "Amministratore"
            binding.tvSubtitleHeader.text = "Gestione e Assegnazione Lavori Aziendali"
            binding.tvOverviewTitle.text = "Panoramica Aziendale"
            binding.tvActionTitle.text = "+"
            binding.tvActionSubtitle.text = "Nuovo Lavoro"
            binding.cardQuickAction.setOnClickListener {
                findNavController().navigate(R.id.action_dashboard_to_newAppointment)
            }
        } else {
            val dept = viewModel.getUserDepartment()
            binding.ivRoleIcon.setImageResource(R.drawable.ic_precision_tech)
            binding.tvRoleBadge.text = "Tecnico • $dept"
            binding.tvSubtitleHeader.text = "I tuoi lavori assegnati per $dept"
            binding.tvOverviewTitle.text = "Il Mio Carico di Lavoro"
            binding.tvActionTitle.text = "Agenda"
            binding.tvActionSubtitle.text = "Lavori Assegnati"
            binding.cardQuickAction.setOnClickListener {
                findNavController().navigate(R.id.action_dashboard_to_appointments)
            }
        }

        // Stat 1: Appuntamenti oggi
        viewModel.todayAppointmentCount.observe(viewLifecycleOwner) { count ->
            binding.tvAppointmentCount.text = count.toString()
        }

        // Stat 2: Interventi aperti
        viewModel.openInterventionCount.observe(viewLifecycleOwner) { count ->
            binding.tvInterventionCount.text = count.toString()
        }

        // Stat 3: Rubrica Clienti
        viewModel.totalClientsCount.observe(viewLifecycleOwner) { count ->
            binding.tvClientCount.text = count.toString()
        }

        // Anteprima Prossimi Lavori
        var latestAppointments = emptyList<Appointment>()
        var latestInterventions = emptyList<Intervention>()

        fun updateTaskPreview() {
            val hasTasks = latestAppointments.isNotEmpty() || latestInterventions.isNotEmpty()
            if (!hasTasks) {
                binding.tvPreviewEmpty.visibility = View.VISIBLE
                binding.previewSlot1.visibility = View.GONE
                binding.previewSlot2.visibility = View.GONE
                binding.dividerPreview.visibility = View.GONE
                return
            }

            binding.tvPreviewEmpty.visibility = View.GONE

            // Popola Slot 1
            if (latestAppointments.isNotEmpty()) {
                val appt = latestAppointments.first()
                binding.previewSlot1.visibility = View.VISIBLE
                binding.tvP1Client.text = appt.clientName
                binding.ivP1DeptIcon.setImageResource(DepartmentHelper.getIconRes(appt.department))
                binding.tvP1Dept.text = appt.department
                binding.tvP1Time.text = "${appt.date} ${appt.time}"
                binding.tvP1Desc.text = appt.description.ifBlank { "Nessuna descrizione" }
                binding.tvP1Status.text = if (appt.status == Appointment.STATUS_COMPLETED) "Completato" else "Programmato"
                binding.previewSlot1.setOnClickListener {
                    findNavController().navigate(R.id.action_dashboard_to_appointments)
                }
            } else if (latestInterventions.isNotEmpty()) {
                val interv = latestInterventions.first()
                binding.previewSlot1.visibility = View.VISIBLE
                binding.tvP1Client.text = interv.clientName
                binding.ivP1DeptIcon.setImageResource(DepartmentHelper.getIconRes(interv.department))
                binding.tvP1Dept.text = interv.department
                binding.tvP1Time.text = interv.date
                binding.tvP1Desc.text = interv.description.ifBlank { "Nessuna descrizione" }
                binding.tvP1Status.text = "In corso"
                binding.previewSlot1.setOnClickListener {
                    findNavController().navigate(R.id.action_dashboard_to_interventions)
                }
            }

            // Popola Slot 2
            val secondItem = when {
                latestAppointments.size > 1 -> Pair("appt", latestAppointments[1])
                latestInterventions.isNotEmpty() && latestAppointments.isNotEmpty() -> Pair("interv", latestInterventions.first())
                latestInterventions.size > 1 -> Pair("interv", latestInterventions[1])
                else -> null
            }

            if (secondItem != null) {
                binding.dividerPreview.visibility = View.VISIBLE
                binding.previewSlot2.visibility = View.VISIBLE
                if (secondItem.first == "appt") {
                    val appt = secondItem.second as Appointment
                    binding.tvP2Client.text = appt.clientName
                    binding.ivP2DeptIcon.setImageResource(DepartmentHelper.getIconRes(appt.department))
                    binding.tvP2Dept.text = appt.department
                    binding.tvP2Time.text = "${appt.date} ${appt.time}"
                    binding.tvP2Desc.text = appt.description.ifBlank { "Nessuna descrizione" }
                    binding.tvP2Status.text = "Programmato"
                    binding.previewSlot2.setOnClickListener {
                        findNavController().navigate(R.id.action_dashboard_to_appointments)
                    }
                } else {
                    val interv = secondItem.second as Intervention
                    binding.tvP2Client.text = interv.clientName
                    binding.ivP2DeptIcon.setImageResource(DepartmentHelper.getIconRes(interv.department))
                    binding.tvP2Dept.text = interv.department
                    binding.tvP2Time.text = interv.date
                    binding.tvP2Desc.text = interv.description.ifBlank { "Nessuna descrizione" }
                    binding.tvP2Status.text = "In corso"
                    binding.previewSlot2.setOnClickListener {
                        findNavController().navigate(R.id.action_dashboard_to_interventions)
                    }
                }
            } else {
                binding.dividerPreview.visibility = View.GONE
                binding.previewSlot2.visibility = View.GONE
            }
        }

        viewModel.todayAppointments.observe(viewLifecycleOwner) { appts ->
            latestAppointments = appts
            updateTaskPreview()
        }

        viewModel.openInterventions.observe(viewLifecycleOwner) { intervs ->
            latestInterventions = intervs
            updateTaskPreview()
        }

        // Navigazione Rapida
        binding.cardAppointments.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_appointments)
        }

        binding.cardInterventions.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_interventions)
        }

        binding.cardClients.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_clients)
        }

        binding.tvSeeAllTasks.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_appointments)
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
