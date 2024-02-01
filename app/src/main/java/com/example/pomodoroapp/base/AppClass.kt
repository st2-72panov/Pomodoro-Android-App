package com.example.pomodoroapp.base

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.pomodoroapp.activities.MainActivity
import com.example.pomodoroapp.notifications.CompletionNotificationService
import com.example.pomodoroapp.notifications.MasterNotificationService
import com.example.pomodoroapp.notifications.PolicyAccessNotificationService
import com.example.pomodoroapp.sys_functions.DND.setDNDMode

class AppClass : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val soundChannel = NotificationChannel(
            CompletionNotificationService.CHANNEL_ID,
            "Sound channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        soundChannel.description = "Sound channel"
        notificationManager.createNotificationChannel(soundChannel)

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