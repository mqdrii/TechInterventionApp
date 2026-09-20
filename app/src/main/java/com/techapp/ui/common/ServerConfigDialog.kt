package com.techapp.ui.common

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Toast
import com.techapp.R
import com.techapp.data.api.ApiClient
import com.techapp.data.api.SyncManager
import com.techapp.databinding.DialogServerConfigBinding
import com.techapp.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object ServerConfigDialog {

    fun show(context: Context, onSaved: (() -> Unit)? = null) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = DialogServerConfigBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.92).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val sessionManager = SessionManager(context)
        binding.etServerUrl.setText(sessionManager.getServerUrl())

        binding.btnPresetCloud.setOnClickListener {
            binding.etServerUrl.setText(SessionManager.DEFAULT_SERVER_URL)
        }

        binding.btnPresetLocal.setOnClickListener {
            binding.etServerUrl.setText("http://192.168.1.50:3000")
        }

        binding.btnTestConnection.setOnClickListener {
            val url = binding.etServerUrl.text.toString().trim()
            if (url.isBlank()) {
                Toast.makeText(context, "Inserisci un URL valido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.layoutPingResult.visibility = View.VISIBLE
            binding.tvPingStatus.text = "Verifica connessione in corso..."
            binding.tvPingStatus.setTextColor(context.getColor(R.color.text_secondary))

            CoroutineScope(Dispatchers.IO).launch {
                val start = System.currentTimeMillis()
                val healthUrl = if (url.endsWith("/")) "${url}api/health" else "$url/api/health"

                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(4, TimeUnit.SECONDS)
                    .readTimeout(4, TimeUnit.SECONDS)
                    .build()

                var isSuccess = false
                var message = ""
                try {
                    val request = Request.Builder().url(healthUrl).build()
                    val response = okHttpClient.newCall(request).execute()
                    val latency = System.currentTimeMillis() - start
                    if (response.isSuccessful) {
                        isSuccess = true
                        message = "Connesso! Latenza: ${latency}ms"
                    } else {
                        message = "Errore server (${response.code})"
                    }
                } catch (e: Exception) {
                    message = "Non raggiungibile (${e.localizedMessage ?: "timeout"})"
                }

                withContext(Dispatchers.Main) {
                    binding.tvPingStatus.text = message
                    if (isSuccess) {
                        binding.tvPingStatus.setTextColor(context.getColor(R.color.badge_completed_text))
                    } else {
                        binding.tvPingStatus.setTextColor(context.getColor(R.color.badge_cancelled_text))
                    }
                }
            }
        }

        binding.btnCancel.setOnClickListener { dialog.dismiss() }

        binding.btnSaveServer.setOnClickListener {
            val url = binding.etServerUrl.text.toString().trim()
            if (url.isNotBlank()) {
                sessionManager.saveServerUrl(url)
                ApiClient.reset()
                Toast.makeText(context, "Server aggiornato. Sincronizzazione avviata...", Toast.LENGTH_SHORT).show()
                CoroutineScope(Dispatchers.IO).launch {
                    SyncManager.sync(context)
                }
                onSaved?.invoke()
                dialog.dismiss()
            } else {
                Toast.makeText(context, "L'URL del server non può essere vuoto", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}
