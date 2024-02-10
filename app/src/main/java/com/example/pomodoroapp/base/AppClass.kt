package com.example.pomodoroapp.base

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.pomodoroapp.notifications.MasterNotificationService
import com.example.pomodoroapp.notifications.PolicyAccessNotificationService

class AppClass : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val masterChannel = NotificationChannel(
            MasterNotificationService.CHANNEL_ID,
            "Master channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        masterChannel.description = "Pomodoro timer"
        notificationManager.createNotificationChannel(masterChannel)

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