package com.example.service

import android.util.Log
import com.example.data.network.NetworkClient
import com.example.domain.notification.NotificationHelper
import com.example.domain.notification.NotificationPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReveFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Device Registration Token generated: $token")
        serviceScope.launch {
            try {
                // Register token with backend
                NetworkClient.api.registerDeviceToken(
                    mapOf(
                        "token" to token,
                        "platform" to "android"
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to register FCM token with backend: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        NotificationPreferences.init(applicationContext)

        val data = remoteMessage.data
        val type = data["type"] ?: "general"
        val matchId = data["match_id"]
        val title = data["title"] ?: remoteMessage.notification?.title ?: "REVE Live Sports"
        val body = data["body"] ?: remoteMessage.notification?.body ?: "Live match score update"

        Log.d(TAG, "FCM message received of type: $type for match: $matchId")

        // Check local notification preferences
        if (!NotificationPreferences.shouldShowAlert(type)) {
            Log.d(TAG, "Notification of type $type suppressed by user preferences")
            return
        }

        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            body = body,
            matchId = matchId,
            type = type
        )
    }

    companion object {
        private const val TAG = "ReveFCMService"
    }
}
