package com.example.pomodoroapp.service
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.pomodoroapp.MainActivity
import com.example.pomodoroapp.PreferencesStore.AppPreferences
import com.example.pomodoroapp.R
import com.google.gson.Gson

private typealias Flags = PomodoroTimer.States

object TimerServiceHelper {
    private const val FLAG = PendingIntent.FLAG_IMMUTABLE
    const val SERVICE_CHANNEL_ID = "master_channel"
    const val SERVICE_NOTIFICATION_ID = 1

    fun triggerTimerService(context: Context, action: TimerService.Actions) {
        Intent(context, TimerService::class.java).apply {
            this.action = action.name
            context.startService(this)
        }
    }

    fun sendPreferencesToTimerService(
        context: Context,
        preferences: AppPreferences
    ) {
        val jsonPreferences = Gson().toJson(preferences)
        Intent(context, TimerService::class.java).apply {
            this.action = TimerService.Actions.UPDATE_PREFERENCES.name
            this.putExtra("preferences", jsonPreferences)
            context.startService(this)
        }
    }

    private fun getPendingIntent(
        context: Context,
        action: TimerService.Actions
    ): PendingIntent {
        val intent = Intent(context, TimerService::class.java).apply {
            putExtra("action", action.name)
        }
        return PendingIntent.getService(
            context, action.ordinal, intent, FLAG
        )
    }

    fun provideNotification(
        context: Context,
        flag: Flags,
        timerName: String,
        uiRemainingTime: String?,
        makeDetachedCompletionNotification: Boolean
    ): Notification {
        val notification = NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.lasso)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    1,
                    Intent(context, MainActivity::class.java),
                    FLAG
                )
            )
            .setAutoCancel(false)
            .setOngoing(true)
            .setSilent(flag != Flags.COMPLETED && makeDetachedCompletionNotification)
            .setShowWhen(flag == Flags.COMPLETED)
            .setContentTitle(
                "$timerName: " + when (flag) {
                    Flags.RUNNING -> "$uiRemainingTime"
                    Flags.PAUSED -> "$uiRemainingTime - ${context.resources.getString(R.string.state_paused)}"
                    else -> context.resources.getString(R.string.state_idle)
                }
            )

        when (flag) {
            Flags.IDLE, Flags.COMPLETED -> {
                notification
                    .addAction(
                        R.drawable.baseline_play_arrow_48,
                        context.resources.getString(R.string.button_launch),
                        getPendingIntent(context, TimerService.Actions.LAUNCH)
                    )
                    .addAction(
                        R.drawable.baseline_swap_vert_12,
                        context.resources.getString(R.string.button_change_timer),
                        getPendingIntent(context, TimerService.Actions.CHANGE_TIMER_TYPE)
                    )
                    .addAction(
                        0,
                        context.resources.getString(R.string.button_cancel),
                        getPendingIntent(context, TimerService.Actions.CANCEL)
                    )
            }

            Flags.RUNNING, Flags.PAUSED -> {
                if (flag == Flags.RUNNING)
                    notification.addAction(
                        R.drawable.baseline_pause_24,
                        context.resources.getString(R.string.button_pause),
                        getPendingIntent(context, TimerService.Actions.PAUSE)
                    )
                else
                    notification.addAction(
                        R.drawable.sharp_resume_24,
                        context.resources.getString(R.string.button_resume),
                        getPendingIntent(context, TimerService.Actions.RESUME)
                    )
                notification
                    .addAction(
                        R.drawable.baseline_stop_48,
                        context.resources.getString(R.string.button_stop),
                        getPendingIntent(context, TimerService.Actions.STOP)
                    ).addAction(
                        R.drawable.rounded_refresh_30,
                        context.resources.getString(R.string.button_restart),
                        getPendingIntent(context, TimerService.Actions.RESTART)
                    )
            }
        }
        return notification.build()
    }
}


object SoundService {
    private const val FLAG = PendingIntent.FLAG_IMMUTABLE
    const val CHANNEL_ID = "sound_channel"
    const val NOTIFICATION_ID = 2

    fun provideNotification(
        context: Context,
        typeId: Int
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.lasso)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    1,
                    Intent(context, MainActivity::class.java),
                    FLAG
                )
            )
            .setContentTitle(
                context.resources.getString(typeId) + " " + context.resources.getString(R.string.finished)
            )
            .build()
    }
}