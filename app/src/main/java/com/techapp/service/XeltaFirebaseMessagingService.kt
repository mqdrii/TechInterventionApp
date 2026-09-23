package com.techapp.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.techapp.data.api.ApiClient
import com.techapp.data.api.FcmTokenRequest
import com.techapp.utils.NotificationHelper
import com.techapp.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class XeltaFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuovo token FCM generato: $token")
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Messaggio FCM ricevuto: ${remoteMessage.data}")

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Nuovo Lavoro Xelta!"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Hai una nuova assegnazione."

        val id = (remoteMessage.data["id"]?.toIntOrNull() ?: (System.currentTimeMillis() % 100000)).toInt()

        // Mostra la notifica Popup Heads-Up ad altissima priorità (suono + vibrazione)
        NotificationHelper.showNotification(
            context = applicationContext,
            notificationId = id,
            title = title,
            body = body
        )
    }

    private fun sendTokenToServer(token: String) {
        val session = SessionManager(applicationContext)
        if (session.isLoggedIn()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val api = ApiClient.getApiService(applicationContext)
                    api.updateFcmToken(FcmTokenRequest(token))
                    Log.d("FCM", "Token FCM inviato con successo al server")
                } catch (e: Exception) {
                    Log.e("FCM", "Errore invio token FCM al server: ${e.message}")
                }
            }
        }
    }
}
