package com.example.pomodoroapp.service
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.pomodoroapp.util.TimerPreferences.restTimerType
import com.example.pomodoroapp.util.TimerPreferences.workTimerType
import androidx.compose.runtime.mutableIntStateOf
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

class PomodoroTimer(
    private val onTick: () -> Unit,
    private val onComplete: () -> Unit
) {
    private lateinit var timer: Timer
    var state by mutableStateOf(States.Idle)
        private set
    var type by mutableStateOf(workTimerType)
        private set

    private var _remaining = 0
    var passed by mutableIntStateOf(0)
        private set
    var uiRemainingTime: String? = null
        private set

    fun launch() {
        setDuration()
        updatePublic()
        resume()
    }

    fun resume() {
        timer = fixedRateTimer(initialDelay = 500L, period = 1000L) {
            --_remaining
            passed++
            updatePublic()

            if (_remaining == 0) {
                stop()
                state = States.Completed
                onComplete()
            }
            onTick()  // performs even ↑if (true)↑
        }
        state = States.Running
    }

    fun restart() {
        if (state == States.Running)
            stop()
        launch()
    }

    fun pause() {
        timer.cancel()
        state = States.Paused
    }

    fun stop() {
        if (state == States.Running)
            timer.cancel()
        state = States.Idle
    }

    fun changeType() {
        type = if (type == workTimerType) restTimerType else workTimerType
        state = States.Idle
    }

    private fun setDuration() {
        _remaining = type.duration
        passed = 0
    }

    private fun updatePublic() {
        uiRemainingTime = String.format("%02d:%02d", _remaining / 60, _remaining % 60)
    }


    enum class States {
        Idle, Running, Paused, Completed
    }
}