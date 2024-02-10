package com.example.pomodoroapp.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import androidx.core.app.NotificationCompat
import com.example.pomodoroapp.notifications.MasterNotificationService
import com.example.pomodoroapp.sys_functions.DND
import javax.inject.Inject
import kotlin.math.max

class TimerService : Service() {

    private val binder = TimerServiceBinder()
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager

    private var interruptionFilterBeforePomodoro = 0
    var timer = PomodoroTimer { onTimerFinish() }
        private set


    override fun onBind(intent: Intent?) = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action ?: intent?.getStringExtra("action")) {

            Actions.Launch.name -> {
                setDND()
                timer.launch()
            }
            Actions.Resume.name -> {
                setDND()
                timer.resume()
            }
            Actions.Restart.name -> {
                if (timer.timerState == PomodoroTimer.States.Paused)
                    setDND()
                timer.restart()
            }
            Actions.Pause.name -> {
                unsetDND()
                timer.pause()
            }
            Actions.Stop.name -> {
                if (timer.timerState == PomodoroTimer.States.Running)
                    unsetDND()
                timer.stop()
            }

            Actions.ChangeTimerType.name -> timer.changeType()
            Actions.Show.name ->
                startForeground(
                    MasterNotificationService.NOTIFICATION_ID,
                    notificationBuilder.build()
                )
            Actions.Cancel.name -> {
                notificationManager.cancel(MasterNotificationService.NOTIFICATION_ID)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun onTimerFinish() {

    }

    private fun updateNotifications() {

    }

    private fun setDND() {
        interruptionFilterBeforePomodoro =
            notificationManager.currentInterruptionFilter

        notificationManager.setInterruptionFilter(
            max(
                interruptionFilterBeforePomodoro,
                DND.INTERRUPTION_FILTER
            )
        )
    }

    private fun unsetDND() {
        notificationManager.setInterruptionFilter(
            interruptionFilterBeforePomodoro
        )
    }

    inner class TimerServiceBinder : Binder() {
        fun getService() = this@TimerService
    }

    enum class Actions {
        Show,
        Cancel,

        Launch,
        Restart,
        Pause,
        Resume,
        Stop,

        ChangeTimerType
    }
}