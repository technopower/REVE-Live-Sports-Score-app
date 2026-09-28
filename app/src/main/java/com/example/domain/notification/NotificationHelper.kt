package com.example.domain.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val LIVE_MATCHES_CHANNEL_ID = "reve_live_matches"
    const val GENERAL_ALERTS_CHANNEL_ID = "reve_general_alerts"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val liveChannel = NotificationChannel(
                LIVE_MATCHES_CHANNEL_ID,
                "Live Match Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Live scores, goals, cards, and match event alerts"
                enableVibration(true)
                setShowBadge(true)
            }

            val generalChannel = NotificationChannel(
                GENERAL_ALERTS_CHANNEL_ID,
                "General Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Favorite team news and upcoming match reminders"
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(liveChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        body: String,
        matchId: String? = null,
        type: String = "general"
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!matchId.isNullOrEmpty()) {
                putExtra("EXTRA_MATCH_ID", matchId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (type in listOf("goal", "red_card", "match_starting", "half_time", "full_time")) {
            LIVE_MATCHES_CHANNEL_ID
        } else {
            GENERAL_ALERTS_CHANNEL_ID
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }
}
