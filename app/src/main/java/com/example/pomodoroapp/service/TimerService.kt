package com.example.pomodoroapp.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.pomodoroapp.util.TimerType
import com.example.pomodoroapp.util.TimerPreferences.workTimerType
import com.example.pomodoroapp.util.TimerPreferences.restTimerType
import java.util.Timer

class TimerService : Service() {
    var timerState by mutableStateOf(States.Idle)
        private set
    var timerType by mutableStateOf(workTimerType)
        private set
    private var interruptionFilterBeforePomodoro = 0

    private val binder = TimerBinder()

    override fun onBind(intent: Intent?) = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action ?: intent?.getStringExtra("action")) {
            Actions.Start.name -> {

            }
            Actions.Restart.name -> {

            }
            Actions.Pause.name -> {

            }
            Actions.Resume.name -> {

            }
            Actions.Stop.name -> {

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTimer() {
        timerState = States.Running
    }


    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    enum class Actions {
        Start,
        Restart,
        Pause,
        Resume,
        Stop
    }

    enum class States {
        Idle,
        Running,
        Paused
    }
}