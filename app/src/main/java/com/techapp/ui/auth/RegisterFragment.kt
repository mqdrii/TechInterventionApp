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
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            viewModel.register(
                firstName = binding.etFirstName.text.toString(),
                lastName = binding.etLastName.text.toString(),
                email = binding.etEmail.text.toString(),
                password = binding.etPassword.text.toString(),
                confirmPassword = binding.etConfirmPassword.text.toString()
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
