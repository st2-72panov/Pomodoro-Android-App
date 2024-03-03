package com.example.pomodoroapp.service
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.pomodoroapp.R
import com.example.pomodoroapp.base.PolicyAccessNotificationService
import com.example.pomodoroapp.util.NotificationPreferences.DND_WHILE_WORKING
import com.example.pomodoroapp.util.TimerPreferences.AUTOSTART_REST_BY_POMODORO_FINISH
import com.example.pomodoroapp.util.TimerPreferences.CHANGE_TIMER_TYPE_ON_FINISH
import kotlin.math.max
import kotlin.system.exitProcess

class TimerService : Service() {
    var timer = PomodoroTimer(
        { updateNotifications() },
        { onTimerComplete() }
    )
        private set
    private var interruptionFilterBeforePomodoro = 0

    private val binder = TimerServiceBinder()
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action ?: intent?.getStringExtra("action")) {

            Actions.Launch.name -> launchTimer()
            Actions.Resume.name -> {
                setDND()
                timer.resume()
            }

            Actions.Restart.name -> {
                if (timer.state == PomodoroTimer.States.Paused)
                    setDND()
                timer.restart()
            }

            Actions.Pause.name -> {
                unsetDND()
                timer.pause()
            }

            Actions.Stop.name -> {
                if (timer.state == PomodoroTimer.States.Running)
                    unsetDND()
                timer.stop()
            }

            Actions.ChangeTimerType.name -> timer.changeType()
            Actions.Show.name -> {
                startForeground(
                    TimerServiceHelper.SERVICE_NOTIFICATION_ID,
                    getForegroundNotification()
                )
                if (!notificationManager.isNotificationPolicyAccessGranted)
                    PolicyAccessNotificationService(this).showNotification()
            }

            Actions.Cancel.name -> {
                notificationManager.cancel(TimerServiceHelper.SERVICE_NOTIFICATION_ID)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                exitProcess(0)
            }
        }
        when (intent?.action ?: intent?.getStringExtra("action")) {
            Actions.Show.name, Actions.Cancel.name -> {}
            else -> updateNotifications()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun launchTimer() {
        setDND()
        timer.launch()
    }

    private fun onTimerComplete() {
        unsetDND()
        updateNotifications()
        if (CHANGE_TIMER_TYPE_ON_FINISH)
            timer.changeType()
        if (AUTOSTART_REST_BY_POMODORO_FINISH && timer.typeId == R.string.rest)
            launchTimer()
    }

    private fun updateNotifications() {
        notificationManager.notify(
            TimerServiceHelper.SERVICE_NOTIFICATION_ID,
            getForegroundNotification()
        )
    }

    private fun getForegroundNotification(): Notification {
        return TimerServiceHelper.provideNotification(
            this,
            timer.state,
            this.resources.getString(timer.typeId),
            timer.uiRemainingTime
        )
    }

    private fun areDNDModeChangesForbidden(): Boolean {
        return !DND_WHILE_WORKING ||
                !notificationManager.isNotificationPolicyAccessGranted ||
                timer.typeId != R.string.work
    }

    private fun setDND() {
        if (areDNDModeChangesForbidden())
            return
        interruptionFilterBeforePomodoro =
            notificationManager.currentInterruptionFilter
        notificationManager.setInterruptionFilter(
            max(
                interruptionFilterBeforePomodoro,
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
            )
        )
    }

    private fun unsetDND() {
        if (areDNDModeChangesForbidden())
            return
        notificationManager.setInterruptionFilter(
            interruptionFilterBeforePomodoro
        )
    }

    inner class TimerServiceBinder : Binder() {
        fun getService() = this@TimerService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    enum class Actions {
        Launch,
        Restart,
        Pause,
        Resume,
        Stop,

        ChangeTimerType,
        Show,
        Cancel
    }
}