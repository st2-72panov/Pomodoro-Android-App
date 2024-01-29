package com.example.pomodoroapp.base

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.pomodoroapp.notifications.MasterNotificationService
import com.example.pomodoroapp.notifications.PolicyAccessNotificationService


class AppClass : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val masterChannel = NotificationChannel(
            MasterNotificationService.CHANNEL_ID,
            "Pomodoro Timer",
            NotificationManager.IMPORTANCE_HIGH
        )
        masterChannel.description = "Pomodoro timer"
        notificationManager.createNotificationChannel(masterChannel)

        if (notificationManager.isNotificationPolicyAccessGranted) { return }
        val policyAccessChannel = NotificationChannel(
            PolicyAccessNotificationService.CHANNEL_ID,
            "Name",
            NotificationManager.IMPORTANCE_HIGH
        )
        policyAccessChannel.description = "Description"
        notificationManager.createNotificationChannel(policyAccessChannel)
    }

    fun setDNDMode(state: Boolean) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val interruptionFilter = when (state) {
            true -> NotificationManager.INTERRUPTION_FILTER_NONE
            false -> NotificationManager.INTERRUPTION_FILTER_ALL
        }
        notificationManager.setInterruptionFilter(interruptionFilter)
    }
}