package com.example.pomodoroapp.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.pomodoroapp.MainActivity
import com.example.pomodoroapp.R

class PolicyAccessNotificationService(
    private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification() {
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification =
            NotificationCompat.Builder(context, PolicyAccessNotificationService.CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_app_settings_alt_24)
            .setContentTitle(
                "Grant the notification policy access"
            )
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Grant the notification policy access to allow this app " +
                    "to enable Do Not Disturb mode while running Pomodoro"))
            .setSilent(true)
            .setShowWhen(false)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(PolicyAccessNotificationService.NOTIFICATION_ID, notification)
    }
    fun deleteNotification() { notificationManager.cancel(NOTIFICATION_ID) }

    companion object {
        const val CHANNEL_ID = "policy_access_channel"
        const val NOTIFICATION_ID = 2
    }
}