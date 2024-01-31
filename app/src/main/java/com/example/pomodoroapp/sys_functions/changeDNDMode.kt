package com.example.pomodoroapp.sys_functions

import android.app.NotificationManager

public fun changeDNDMode(notificationManager: NotificationManager) {
    if (!notificationManager.isNotificationPolicyAccessGranted)
        return
    val interruptionFilter = when (
        notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL
    ) {
        true -> NotificationManager.INTERRUPTION_FILTER_PRIORITY
        false -> NotificationManager.INTERRUPTION_FILTER_ALL
    }
    notificationManager.setInterruptionFilter(interruptionFilter)
}

