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
import com.techapp.databinding.FragmentLoginBinding
import com.techapp.utils.SessionManager

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Se già loggato, vai alla dashboard
        val session = SessionManager(requireContext())
        if (session.isLoggedIn()) {
            findNavController().navigate(R.id.action_login_to_dashboard)
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.btnServerConfig.setOnClickListener {
            com.techapp.ui.common.ServerConfigDialog.show(requireContext())
        }

        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthViewModel.LoginResult.Success -> {
                    findNavController().navigate(R.id.action_login_to_dashboard)
                }
                is AuthViewModel.LoginResult.RequiresVerification -> {
                    val bundle = Bundle().apply {
                        putLong("userId", result.userId)
                        putString("message", "Il tuo account non è ancora verificato. Inserisci il codice inviato via email.")
                    }
                    findNavController().navigate(R.id.action_login_to_verifyEmail, bundle)
                }
                is AuthViewModel.LoginResult.Error -> {
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
