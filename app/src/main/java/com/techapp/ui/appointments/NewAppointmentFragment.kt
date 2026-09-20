package com.techapp.ui.appointments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.techapp.R
import com.techapp.databinding.FragmentNewAppointmentBinding
import com.techapp.ui.clients.ClientViewModel
import java.util.*

class NewAppointmentFragment : Fragment() {

    private var _binding: FragmentNewAppointmentBinding? = null
    private val binding get() = _binding!!

    private val appointmentViewModel: AppointmentViewModel by viewModels()
    private val clientViewModel: ClientViewModel by viewModels()

    private var selectedClientId: Long = -1
    private var selectedDate: String = ""
    private var selectedTime: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Popola spinner clienti
        clientViewModel.clients.observe(viewLifecycleOwner) { clients ->
            val names = if (clients.isEmpty()) listOf("Nessun cliente registrato") else clients.map { it.name }
            val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, names)
            adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
            binding.spinnerClient.adapter = adapter
            binding.spinnerClient.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    if (clients.isNotEmpty() && pos < clients.size) {
                        selectedClientId = clients[pos].id
                    }
                }
                override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
            }
        }

        // Popola spinner reparto
        val deptAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner, com.techapp.data.model.User.DEPARTMENTS)
        deptAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinnerDepartment.adapter = deptAdapter

        // Popola spinner tecnici
        var techniciansList: List<com.techapp.data.model.User> = emptyList()
        appointmentViewModel.technicians.observe(viewLifecycleOwner) { techs ->
            techniciansList = techs
            val techNames = mutableListOf("Nessun tecnico specifico")
            techNames.addAll(techs.map { "${it.fullName} (${it.department})" })
            val techAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner, techNames)
            techAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
            binding.spinnerTechnician.adapter = techAdapter
        }

        // Date picker
        binding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = "%04d-%02d-%02d".format(y, m + 1, d)
                binding.tvSelectedDate.text = selectedDate
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Time picker
        binding.btnPickTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, h, min ->
                selectedTime = "%02d:%02d".format(h, min)
                binding.tvSelectedTime.text = selectedTime
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        binding.btnSave.setOnClickListener {
            val techPos = binding.spinnerTechnician.selectedItemPosition
            val assignedUserId = if (techPos > 0 && techPos - 1 < techniciansList.size) techniciansList[techPos - 1].id else 0L
            val assignedUserName = if (techPos > 0 && techPos - 1 < techniciansList.size) techniciansList[techPos - 1].fullName else ""
            val department = binding.spinnerDepartment.selectedItem?.toString() ?: "Generale"

            appointmentViewModel.insertAppointment(
                clientId = selectedClientId,
                clientName = binding.spinnerClient.selectedItem?.toString() ?: "",
                date = selectedDate,
                time = selectedTime,
                description = binding.etDescription.text.toString(),
                department = department,
                assignedUserId = assignedUserId,
                assignedUserName = assignedUserName
            )
        }

        appointmentViewModel.insertResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                findNavController().navigateUp()
            } else {
                Snackbar.make(binding.root, "Compila tutti i campi obbligatori", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener { findNavController().navigateUp() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
