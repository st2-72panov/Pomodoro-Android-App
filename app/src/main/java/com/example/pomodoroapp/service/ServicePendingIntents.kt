package com.example.pomodoroapp.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.pomodoroapp.MainActivity

object ServicePendingIntents {
    private const val flag = PendingIntent.FLAG_IMMUTABLE

    fun getTimerServiceTriggerPendingIntent(
        context: Context,
        action: TimerService.Actions
    ): PendingIntent {
        val intent = Intent(context, TimerService::class.java).apply {
            putExtra("action", action.name)
        }
        return PendingIntent.getService(
            context, action.ordinal, intent, flag
        )
    }

    fun triggerTimerService(context: Context, action: TimerService.Actions) {
        Intent(context, TimerService::class.java).apply {
            this.action = action.name
            context.startService(this)
        }
    }
}