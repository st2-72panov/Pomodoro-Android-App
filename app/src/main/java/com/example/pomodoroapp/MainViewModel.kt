package com.example.pomodoroapp

import android.app.NotificationManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodoroapp.sys_functions.DND
import com.example.pomodoroapp.util.TimerType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.math.max

class MainViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _isOff = MutableStateFlow(true)
    private val _isPaused = MutableStateFlow(false)
    private val _timerType: TimerType

    private val _secondsPassed = MutableStateFlow(0)
    private val _startTime = MutableStateFlow(0L)
    private val _endTime = MutableStateFlow(0L)
    private val _currentTime = MutableStateFlow(0L)
    private val _whenPaused = MutableStateFlow(0L)

    var interruptionFilterBeforePomodoro = 0

    val isOff = _isOff.asStateFlow()
    val isPaused = _isPaused.asStateFlow()
    val isWorking = combine(isOff, isPaused) { isOff, isPaused ->
        !isOff && !isPaused
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val timerType = _timerType.asStateFlow()

    val secondsPassed = _secondsPassed.asStateFlow()
    val startTime = _startTime.asStateFlow()
    val endTime = _endTime.asStateFlow()
    val currentTime = _currentTime.asStateFlow()
//    val whenPaused = _whenPaused.asStateFlow()


//    var isPaused by mutableStateOf(false)
//    var isOff by mutableStateOf(true)
//    var timerType by mutableStateOf(WORK)
//    private var timersDurations = mutableMapOf(
//        WORK to 30,
//        REST to 7
//    )
//    var interruptionFilterBeforePomodoro = 0
//
//    var secondsPassed by mutableIntStateOf(0)  // progress bar`s variable
//    var startTime by mutableLongStateOf(0) // (seconds) determined inside onClick function
//    var endTime by mutableLongStateOf(0)  // (seconds) determined inside onClick function
//    var currentTime by mutableLongStateOf(0)  // (seconds)
//    var whenPaused = 0L




    fun setNextTimerType() {
        _timerType.value = getNextTimerType()
    }

    fun renewSecondsPassed() {
        _currentTime.value = System.currentTimeMillis() / 1000
        _secondsPassed.value = (
                (currentTime.value - startTime.value) // / 60
                ).toInt()
    }

    fun startTimer(nm: NotificationManager) {
        if (timerType.value == WORK)
            setDNDMode(nm, saveCurrentFilter_getDNDFilter(nm))
        launchTimer()
        _isOff.value = false
    }

    fun launchTimer() {
        _secondsPassed.value = 0
        _currentTime.value = System.currentTimeMillis() / 1000
        _startTime.value = currentTime.value
        _endTime.value = startTime.value + getCurrentTimerDuration()
    }


    fun resetTimer(nm: NotificationManager) {
        if (isPaused.value) {
            _isPaused.value = false
            setDNDMode(nm, saveCurrentFilter_getDNDFilter(nm))
        }
        launchTimer()
    }

    fun breakTimer(nm: NotificationManager) {
        _isOff.value = true
        _isPaused.value = false

        if (timerType.value == WORK)
            setDNDMode(nm, interruptionFilterBeforePomodoro)
    }

    fun finishTimer(nm: NotificationManager) {
        breakTimer(nm)
        if (WHETHER_CHANGE_TIMER_TYPE_ON_FINISH)
            setNextTimerType()
        if (timerType.value == WORK && WHETHER_START_REST_BY_POMODORO_FINISH)
            launchTimer()
    }

    fun pauseTimer(nm: NotificationManager) {
        _isPaused.value = true
        _whenPaused.value = System.currentTimeMillis() / 1000
        setDNDMode(nm, saveCurrentFilter_getDNDFilter(nm))
    }

    fun resumeTimer(nm: NotificationManager) {
        _isPaused.value = false
        val delta = System.currentTimeMillis() - _whenPaused.value
        _startTime.value += delta
        _endTime.value += delta

        val dnd = when (timerType.value == WORK) {
            true -> saveCurrentFilter_getDNDFilter(nm)
            false -> interruptionFilterBeforePomodoro
        }
        setDNDMode(nm, dnd)
    }

    fun setDNDMode(
        notificationManager: NotificationManager,
        filter: Int = DND.INTERRUPTION_FILTER
    ) {
        if (WHETHER_SET_DND_MODE)
            DND.setDNDMode(notificationManager, filter)
    }

    fun saveCurrentFilter_getDNDFilter(notificationManager: NotificationManager): Int {
        interruptionFilterBeforePomodoro =
            notificationManager.currentInterruptionFilter
        return max(
            interruptionFilterBeforePomodoro,
            DND.INTERRUPTION_FILTER
        )
    }
}