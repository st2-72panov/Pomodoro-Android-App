package com.example.pomodoroapp.service
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.pomodoroapp.R
import com.example.pomodoroapp.base.PolicyAccessNotificationService
import com.example.pomodoroapp.PreferencesStore.AppPreferences
import com.google.gson.Gson
import kotlin.math.max
import kotlin.system.exitProcess

class TimerService : Service() {
    private var interruptionFilterBeforePomodoro = 0
    private lateinit var appPreferences: AppPreferences
    var timer by mutableStateOf(
        PomodoroTimer(
            null,
            { updateForeground() },
            { onTimerComplete() }
        )
    )

    private val binder = TimerServiceBinder()
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action ?: intent?.getStringExtra("action")) {

            Actions.LAUNCH.name -> launchTimer()
            Actions.RESUME.name -> {
                setDND()
                timer.resume()
            }

            Actions.RESTART.name -> {
                if (timer.state == PomodoroTimer.States.PAUSED)
                    setDND()
                timer.restart()
            }

            Actions.PAUSE.name -> {
                unsetDND()
                timer.pause()
            }

            Actions.STOP.name -> {
                if (timer.state == PomodoroTimer.States.RUNNING)
                    unsetDND()
                timer.stop()
            }

            Actions.CHANGE_TIMER_TYPE.name -> timer.changeType()
            Actions.SHOW.name -> {
                startForeground(
                    TimerServiceHelper.SERVICE_NOTIFICATION_ID,
                    getForegroundNotification()
                )
                if (!notificationManager.isNotificationPolicyAccessGranted)
                    PolicyAccessNotificationService(this).showNotification()
            }

            Actions.CANCEL.name -> {
                notificationManager.cancel(TimerServiceHelper.SERVICE_NOTIFICATION_ID)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                exitProcess(0)
            }

            Actions.UPDATE_PREFERENCES.name -> {
                val jsonPreferences = intent?.getStringExtra("preferences")
                appPreferences = Gson().fromJson(jsonPreferences, AppPreferences::class.java)
                timer = PomodoroTimer(
                    appPreferences,
                    { updateForeground() },
                    { onTimerComplete() }
                )
            }
        }
        when (intent?.action ?: intent?.getStringExtra("action")) {
            Actions.SHOW.name, Actions.CANCEL.name -> {}
            else -> updateForeground()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun launchTimer() {
        setDND()
        timer.launch()
    }

    private fun onTimerComplete() {
        unsetDND()
        if (appPreferences.makeDetachedCompletionNotification)
            notificationManager.notify(
                SoundService.NOTIFICATION_ID,
                SoundService.provideNotification(this, timer.typeId)
            )
        updateForeground()
        if (appPreferences.changeTimerTypeOnFinish)
            timer.changeType()
        if (appPreferences.autostartRestAfterWork && timer.typeId == R.string.rest)
            launchTimer()
    }

    private fun updateForeground() {
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
            timer.uiRemainingTime,
            appPreferences.makeDetachedCompletionNotification
        )
    }

    private fun areDNDModeChangesForbidden(): Boolean {
        return !appPreferences.dndWhileWorking ||
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
        LAUNCH,
        RESTART,
        PAUSE,
        RESUME,
        STOP,

        CHANGE_TIMER_TYPE,
        SHOW,
        CANCEL,

        UPDATE_PREFERENCES
    }
}