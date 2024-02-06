package com.example.pomodoroapp.util
data class TimerType(val name: String, val duration: Int)

object TimerPreferences {
    var CHANGE_TIMER_TYPE_ON_FINISH = true
    var AUTOSTART_REST_BY_POMODORO_FINISH = true
    var workTimerType = TimerType("Pomodoro", 30)
    var restTimerType = TimerType("Rest", 7)
}

object NotificationPreferences {
    var ENABLE_STATUS_NOTIFICATION = true
    var DND_WHILE_WORKING = true
    var NOTIFY_AFTER_FINISH = true
}