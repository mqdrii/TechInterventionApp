package com.techapp.ui.interventions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.techapp.R
import com.techapp.data.model.Intervention
import com.techapp.databinding.FragmentInterventionsBinding

class InterventionsFragment : Fragment() {

    private var _binding: FragmentInterventionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InterventionViewModel by viewModels()
    private lateinit var adapter: InterventionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInterventionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = InterventionAdapter(
            onItemClick = { /* dettaglio futuro */ },
            onStatusClick = { intervention ->
                val options = arrayOf("In corso", "Chiuso")
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Aggiorna stato")
                    .setItems(options) { _, which ->
                        val status = if (which == 0) Intervention.STATUS_IN_PROGRESS
                                     else Intervention.STATUS_CLOSED
                        viewModel.updateStatus(intervention.id, status)
                    }
                    .show()
            }
        )

        binding.rvInterventions.adapter = adapter

        // Toggle: tutti / solo aperti
        var showAll = true
        viewModel.allInterventions.observe(viewLifecycleOwner) { list ->
            if (showAll) {
                adapter.submitList(list)
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        viewModel.openInterventions.observe(viewLifecycleOwner) { list ->
            if (!showAll) {
                adapter.submitList(list)
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.chipAll.setOnClickListener {
            showAll = true
            viewModel.allInterventions.value?.let { adapter.submitList(it) }
        }

        binding.chipOpen.setOnClickListener {
            showAll = false
            viewModel.openInterventions.value?.let { adapter.submitList(it) }
        }

        binding.fabAddIntervention.setOnClickListener {
            findNavController().navigate(R.id.action_interventions_to_newIntervention)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
