package com.example.pomodoroapp.sys_functions

import android.app.NotificationManager

object DND {
    public val INTERRUPTION_FILTER = NotificationManager.INTERRUPTION_FILTER_PRIORITY
    public fun setDNDMode(
        notificationManager : NotificationManager,
        filter : Int = INTERRUPTION_FILTER
    ) {
        if (!notificationManager.isNotificationPolicyAccessGranted)
            return
        notificationManager.setInterruptionFilter(filter)
    }
}
