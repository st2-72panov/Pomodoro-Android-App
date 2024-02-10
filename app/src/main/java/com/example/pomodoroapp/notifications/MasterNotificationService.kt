package com.example.pomodoroapp.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.pomodoroapp.MainActivity
import com.example.pomodoroapp.R

class MasterNotificationService(
    private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(flag: Int = 0) {
        val activityIntent = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(activityIntent)
            .setAutoCancel(false)
            .setSilent(flag != FLAG_COMPLETED)
            .setShowWhen(flag == FLAG_COMPLETED)
            .setContentTitle(
                "Pomodoro Timer" + when (flag) {
                    FLAG_RUNNING -> ": running"
                    FLAG_PAUSED -> ": paused"
                    else -> ""
                }
            )

        when (flag) {
            FLAG_PLANE, FLAG_COMPLETED -> {
                notification.addAction(
                    R.drawable.baseline_play_arrow_48,
                    "launch",
                    PendingIntent.getBroadcast(
                        context,
                        3,
                        Intent(
                            context,
                            object : BroadcastReceiver() {
                                override fun onReceive(context: Context, intent: Intent?) {
                                }
                            }::class.java
                        ),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }

            FLAG_RUNNING, FLAG_PAUSED -> {
                if (flag == FLAG_RUNNING)
                    notification.addAction(
                        R.drawable.baseline_pause_24,
                        "pause",
                        PendingIntent.getBroadcast(
                            context,
                            4,
                            Intent(
                                context,
                                object : BroadcastReceiver() {
                                    override fun onReceive(context: Context, intent: Intent?) {
                                    }
                                }::class.java
                            ),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                else
                    notification.addAction(
                        R.drawable.baseline_play_arrow_48,
                        "resume",
                        PendingIntent.getBroadcast(
                            context,
                            4,
                            Intent(
                                context,
                                object : BroadcastReceiver() {
                                    override fun onReceive(context: Context, intent: Intent?) {
                                    }
                                }::class.java
                            ),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                notification.addAction(
                    R.drawable.baseline_stop_48,
                    "break",
                    PendingIntent.getBroadcast(
                        context,
                        5,
                        Intent(
                            context,
                            object : BroadcastReceiver() {
                                override fun onReceive(context: Context, intent: Intent?) {
                                }
                            }::class.java
                        ),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                ).addAction(
                    R.drawable.rounded_refresh_30,
                    "reset",
                    PendingIntent.getBroadcast(
                        context,
                        6,
                        Intent(
                            context,
                            object : BroadcastReceiver() {
                                override fun onReceive(context: Context, intent: Intent?) {
                                }
                            }::class.java
                        ),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }
        }

        val intent = Intent(
            context,
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent?) {
                    MasterNotificationService(context).deleteNotification()
                }
            }::class.java
        )
        notification
            .setOngoing(true)
            .addAction(
                R.drawable.ic_launcher_background,  // is not used
                "Close",
                PendingIntent.getBroadcast(
                    context,
                    2,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
//        if (flag == FLAG_COMPLETED) Todo
//            notificationManager.cancel(NOTIFICATION_ID)
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    fun deleteNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
        isOn = false
    }

    companion object {
        const val CHANNEL_ID = "master_channel"
        const val NOTIFICATION_ID = 2
        public var isOn = true
    }
}

enum class Flags {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}
