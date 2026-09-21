package com.techapp.ui.auth

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.techapp.R
import com.techapp.databinding.FragmentVerifyEmailBinding

class VerifyEmailFragment : Fragment() {

    private var _binding: FragmentVerifyEmailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    private var userId: Long = 0L
    private var resendTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = arguments?.getLong("userId", 0L) ?: 0L
        val customMessage = arguments?.getString("message")
        if (!customMessage.isNullOrBlank()) {
            binding.tvVerifySubtitle.text = customMessage
        }

        binding.btnVerify.setOnClickListener {
            val otp = binding.etOtp.text?.toString()?.trim() ?: ""
            if (otp.length != 6) {
                binding.tvError.text = "Inserisci il codice a 6 cifre completo"
                binding.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            binding.tvError.visibility = View.GONE
            binding.btnVerify.isEnabled = false
            viewModel.verifyEmail(userId, otp)
        }

        binding.tvResend.setOnClickListener {
            viewModel.resendOtp(userId)
            startResendCooldown()
        }

        viewModel.verifyResult.observe(viewLifecycleOwner) { result ->
            binding.btnVerify.isEnabled = true
            when (result) {
                is AuthViewModel.VerifyResult.Success -> {
                    Snackbar.make(binding.root, "Account verificato con successo! Benvenuto in Xelta.", Snackbar.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_verifyEmail_to_dashboard)
                }
                is AuthViewModel.VerifyResult.Error -> {
                    binding.tvError.text = result.message
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }

        viewModel.resendResult.observe(viewLifecycleOwner) { msg ->
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
        }

        startResendCooldown()
    }

    private fun startResendCooldown() {
        binding.tvResend.isEnabled = false
        binding.tvResend.alpha = 0.5f
        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                _binding?.tvTimer?.text = "Puoi richiedere un nuovo codice tra ${sec}s"
            }

            override fun onFinish() {
                _binding?.let { b ->
                    b.tvResend.isEnabled = true
                    b.tvResend.alpha = 1.0f
                    b.tvTimer.text = ""
                }
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resendTimer?.cancel()
        resendTimer = null
        _binding = null
    }
}
