package com.example.pomodoroapp.util
import com.example.pomodoroapp.R

data class TimerType(val resourceId: Int, val duration: Int)

object TimerPreferences {
    var CHANGE_TIMER_TYPE_ON_FINISH = true
    var AUTOSTART_REST_BY_POMODORO_FINISH = true
    var workTimerType = TimerType(R.string.work, 30 * 60)
    var restTimerType = TimerType(R.string.rest, 7 * 60)
}

object NotificationPreferences {
    var DND_WHILE_WORKING = true
}