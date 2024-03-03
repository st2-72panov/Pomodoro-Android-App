package com.example.pomodoroapp.util
import com.example.pomodoroapp.R

object TimerPreferences {
    var CHANGE_TIMER_TYPE_ON_FINISH = true
    var AUTOSTART_REST_BY_POMODORO_FINISH = true
    var timerTypes = mutableMapOf(
        R.string.work to 30 * 60,
        R.string.rest to 7 * 60
    )
}

object NotificationPreferences {
    var DND_WHILE_WORKING = true
}