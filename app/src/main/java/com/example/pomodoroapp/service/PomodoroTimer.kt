package com.example.pomodoroapp.service

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.pomodoroapp.util.TimerPreferences.restTimerType
import com.example.pomodoroapp.util.TimerPreferences.workTimerType
import androidx.compose.runtime.mutableIntStateOf
import com.example.pomodoroapp.service.ServicePendingIntents.triggerTimerService
import java.util.Timer
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.seconds

class PomodoroTimer(
    private val owner: TimerService
) {
    private lateinit var timer: Timer
    private var timerState by mutableStateOf(States.Idle)
    private var _remaining = 0.seconds
    var remainingSeconds by mutableIntStateOf(0)
    var uiRemainingSeconds = "00:00"
    var timerType by mutableStateOf(workTimerType)
        private set

    fun launch() {
        setDuration()
        updatePublic()
        resume()
    }

    fun resume() {
        timer = fixedRateTimer(initialDelay = 300L, period = 1000L) {
            _remaining.minus(1.seconds)
            updatePublic()
            if (remainingSeconds == 0) finish()
        }
        timerState = States.Running
    }

    fun restart() {
        if (timerState == States.Running)
            stop()
        launch()
    }

    fun pause() {
        timer.cancel()
        timerState = States.Paused
    }

    fun stop() {
        if (timerState == States.Running)
            timer.cancel()
        timerState = States.Idle
    }

    private fun finish() {
        stop()
        triggerTimerService(owner, TimerService.Actions.Finish.name)
    }

    fun changeType() {
        timerType = if (timerType == workTimerType) restTimerType else workTimerType
    }

    private fun setDuration() {
        _remaining = (timerType.duration * 60).seconds
    }

    private fun updatePublic() {
        remainingSeconds = _remaining.inWholeSeconds.toInt()
        uiRemainingSeconds = _remaining.toComponents { minutes, seconds, _ ->
            String.format("%02d:%02d", minutes, seconds)
        }
    }


    private enum class States {
        Idle, Running, Paused
    }
}