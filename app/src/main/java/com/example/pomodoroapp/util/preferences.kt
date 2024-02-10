package com.example.pomodoroapp.util

data class TimerType(val name: String, val duration: Int)

object TimerPreferences {
    var CHANGE_TIMER_TYPE_ON_FINISH = true
    var AUTOSTART_REST_BY_POMODORO_FINISH = true
    var workTimerType = TimerType("Pomodoro", 30 * 60)
    var restTimerType = TimerType("Rest", 7 * 60)
}

object NotificationPreferences {
    var DND_WHILE_WORKING = true
}