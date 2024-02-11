package com.example.pomodoroapp.base

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
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
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_app_settings_alt_24)
                .setContentTitle(context.resources.getString(R.string.policy_access_text))
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(context.resources.getString(R.string.policy_access_text))
                )
                .setSilent(true)
                .setShowWhen(false)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "policy_access_channel"
        const val NOTIFICATION_ID = 2
    }
}
