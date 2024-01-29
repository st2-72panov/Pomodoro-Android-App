package com.example.pomodoroapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CloseNotificationReseiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val service = MasterNotificationService(context)
        service.deleteNotification()
    }
}
