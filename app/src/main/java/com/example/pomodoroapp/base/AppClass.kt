package com.example.pomodoroapp.base
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.pomodoroapp.service.TimerServiceHelper

class AppClass : Application() {
    override fun onCreate() {
        super.onCreate()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val foregroundServiceChannel = NotificationChannel(
            TimerServiceHelper.SERVICE_CHANNEL_ID,
            "Foreground service channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        foregroundServiceChannel.description = "Pomodoro timer"
        notificationManager.createNotificationChannel(foregroundServiceChannel)

        if (notificationManager.isNotificationPolicyAccessGranted)
            return
        val policyAccessChannel = NotificationChannel(
            PolicyAccessNotificationService.CHANNEL_ID,
            "Policy Access",
            NotificationManager.IMPORTANCE_HIGH
        )
        policyAccessChannel.description = "Provide notification policy access"
        notificationManager.createNotificationChannel(policyAccessChannel)
    }
}