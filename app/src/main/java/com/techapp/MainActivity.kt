package com.techapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.techapp.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // Destinazioni che nascondono la bottom nav (schermate auth)
    private val authDestinations = setOf(
        R.id.loginFragment,
        R.id.registerFragment,
        R.id.verifyEmailFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inizializza canale notifiche
        com.techapp.utils.NotificationHelper.initChannel(this)

        // Richiedi permesso notifiche su Android 13+ (API 33)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Collega la BottomNavigationView al NavController
        binding.bottomNavigation.setupWithNavController(navController)

        // Mostra/nascondi la bottom nav in base alla destinazione corrente
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in authDestinations) {
                binding.bottomNavigation.visibility = View.GONE
            } else {
                binding.bottomNavigation.visibility = View.VISIBLE
            }
        }

        // Avvia sincronizzazione periodica in background (ogni 20 secondi) per ricevere notifiche di nuovi lavori
        startPeriodicSync()

        // Schedula WorkManager per eseguire la sincronizzazione e svegliare le notifiche anche ad app chiusa/in sleep
        com.techapp.data.worker.SyncWorker.schedule(applicationContext)

        // Registra token FCM per notifiche push istantanee
        registerFcmToken()
    }

    private fun registerFcmToken() {
        val session = com.techapp.utils.SessionManager(this)
        if (session.isLoggedIn()) {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                val api = com.techapp.data.api.ApiClient.getService(this@MainActivity)
                                api.updateFcmToken(com.techapp.data.api.FcmTokenRequest(token))
                            } catch (_: Exception) {}
                        }
                    }
                }
        }
    }

    private fun startPeriodicSync() {
        lifecycleScope.launch {
            val session = com.techapp.utils.SessionManager(this@MainActivity)
            while (true) {
                if (session.isLoggedIn()) {
                    try {
                        com.techapp.data.api.SyncManager.sync(this@MainActivity)
                    } catch (_: Exception) {}
                }
                delay(20000) // 20s
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
