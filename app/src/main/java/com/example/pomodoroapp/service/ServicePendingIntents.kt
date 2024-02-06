package com.example.pomodoroapp.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.pomodoroapp.MainActivity

object ServicePendingIntents {
    private val flag = PendingIntent.FLAG_IMMUTABLE

    fun getForegroundServiceTriggerPendingIntent(context: Context, action: TimerService.Actions): PendingIntent {
        val cancelIntent = Intent(context, TimerService::class.java).apply {
            putExtra("action", action.name)
        }
        return PendingIntent.getService(
            context, action.ordinal, cancelIntent, flag
        )
    }

    fun triggerForegroundService(context: Context, action: String) {
        Intent(context, TimerService::class.java).apply {
            this.action = action
            context.startService(this)
        }
    }
}