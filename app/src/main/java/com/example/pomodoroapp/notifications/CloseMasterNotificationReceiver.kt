package com.example.pomodoroapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CloseMasterNotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        MasterNotificationService(context).deleteNotification()
    }
}
