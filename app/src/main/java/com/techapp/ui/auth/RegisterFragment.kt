package com.techapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.techapp.R
import com.techapp.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Popola suggerimenti mansioni
        val deptAdapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            com.techapp.utils.DepartmentHelper.SUGGESTED_DEPARTMENTS
        )
        binding.actvDepartment.setAdapter(deptAdapter)

        binding.rgRole.setOnCheckedChangeListener { _, checkedId ->
            binding.layoutDepartment.visibility = if (checkedId == R.id.rb_technician) View.VISIBLE else View.GONE
        }

        binding.btnRegister.setOnClickListener {
            val isAdmin = binding.rbAdmin.isChecked
            val role = if (isAdmin) com.techapp.data.model.User.ROLE_ADMIN else com.techapp.data.model.User.ROLE_TECHNICIAN
            val department = if (isAdmin) {
                com.techapp.data.model.User.DEPARTMENT_ALL
            } else {
                val input = binding.actvDepartment.text.toString().trim()
                if (input.isNotBlank()) input else com.techapp.data.model.User.DEPARTMENT_GENERAL
            }

            viewModel.register(
                firstName = binding.etFirstName.text.toString(),
                lastName = binding.etLastName.text.toString(),
                email = binding.etEmail.text.toString(),
                password = binding.etPassword.text.toString(),
                confirmPassword = binding.etConfirmPassword.text.toString(),
                role = role,
                department = department
            )
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.registerResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthViewModel.RegisterResult.Success -> {
                    Snackbar.make(binding.root, "Account creato! Accedi ora.", Snackbar.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is AuthViewModel.RegisterResult.RequiresVerification -> {
                    val bundle = Bundle().apply {
                        putLong("userId", result.userId)
                        putString("message", result.message)
                    }
                    findNavController().navigate(R.id.action_register_to_verifyEmail, bundle)
                }
                is AuthViewModel.RegisterResult.Error -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
