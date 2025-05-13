package com.example.pomodoroapp.service
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.pomodoroapp.DataStoreManager.AppPreferences
import com.example.pomodoroapp.R
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

class PomodoroTimer(
    private val appPreferences: AppPreferences?,
    private val onTick: () -> Unit,
    private val onComplete: () -> Unit
) {
    private lateinit var timer: Timer
    var state by mutableStateOf(States.IDLE)
        private set
    var typeId by mutableIntStateOf(R.string.work)
        private set

    private var _remaining = -1
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
            ++passed
            updatePublic()

            if (_remaining == 0) {
                stop()
                state = States.COMPLETED
                onComplete()
            }
            onTick()  // performs even ↑if (true)↑
        }
        state = States.RUNNING
    }

    fun restart() {
        if (state == States.RUNNING) stop()
        launch()
    }

    fun pause() {
        timer.cancel()
        state = States.PAUSED
    }

    fun stop() {
        if (state == States.RUNNING) timer.cancel()
        state = States.IDLE
    }

    fun changeType() {
        typeId = if (typeId == R.string.work) R.string.rest else R.string.work
        state = States.IDLE
    }

    private fun setDuration() {
        _remaining =
            if (typeId == R.string.work) appPreferences!!.workDuration else appPreferences!!.restDuration
        passed = 0
    }

    private fun updatePublic() {
        uiRemainingTime = String.format(Locale.getDefault(), "%02d:%02d", _remaining / 60, _remaining % 60)
    }


    enum class States {
        IDLE, RUNNING, PAUSED, COMPLETED
    }
}