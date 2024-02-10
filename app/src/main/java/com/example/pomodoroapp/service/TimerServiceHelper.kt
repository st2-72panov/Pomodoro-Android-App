package com.example.pomodoroapp.service
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.pomodoroapp.MainActivity
import com.example.pomodoroapp.R

private typealias Flags = PomodoroTimer.States

object TimerServiceHelper {
    private const val flag = PendingIntent.FLAG_IMMUTABLE
    const val SERVICE_CHANNEL_ID = "master_channel"
    const val SERVICE_NOTIFICATION_ID = 1

    fun triggerTimerService(context: Context, action: TimerService.Actions) {
        Intent(context, TimerService::class.java).apply {
            this.action = action.name
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
            context, action.ordinal, intent, flag
        )
    }

    fun provideNotification(
        context: Context,
        flag: Flags,
        timerType: String,
        uiRemainingTime: String?,
    ): Notification {
        val notification = NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    1,
                    Intent(context, MainActivity::class.java),
                    TimerServiceHelper.flag
                )
            )
            .setAutoCancel(false)
            .setOngoing(true)
            .setSilent(flag != Flags.Completed)
            .setShowWhen(flag == Flags.Completed)
            .setContentTitle(
                "Pomodoro Timer ($timerType)" + when (flag) {
                    Flags.Running -> ": $uiRemainingTime"
                    Flags.Paused -> ": $uiRemainingTime - paused"
                    else -> ""
                }
            )

        when (flag) {
            Flags.Idle, Flags.Completed -> {
                notification.addAction(
                    R.drawable.baseline_play_arrow_48,
                    "Launch",
                    getPendingIntent(context, TimerService.Actions.Launch)
                )
            }

            Flags.Running, Flags.Paused -> {
                if (flag == Flags.Running)
                    notification.addAction(
                        R.drawable.baseline_pause_24,
                        "Pause",
                        getPendingIntent(context, TimerService.Actions.Pause)
                    )
                else
                    notification.addAction(
                        R.drawable.sharp_resume_24,
                        "Resume",
                        getPendingIntent(context, TimerService.Actions.Resume)
                    )
                notification
                    .addAction(
                        R.drawable.baseline_stop_48,
                        "Stop",
                        getPendingIntent(context, TimerService.Actions.Stop)
                    ).addAction(
                        R.drawable.rounded_refresh_30,
                        "Restart",
                        getPendingIntent(context, TimerService.Actions.Restart)
                    )
            }
        }

        notification
            .setOngoing(true)
            .addAction(
                0,
                "Cancel",
                getPendingIntent(context, TimerService.Actions.Cancel)
            )
        return notification.build()
    }
}