package com.example.pomodoroapp
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.pomodoroapp.service.TimerService
import com.example.pomodoroapp.service.TimerServiceHelper.triggerTimerService
import com.example.pomodoroapp.ui.theme.PomodoroAppTheme

class MainActivity : ComponentActivity() {

    private var isBound by mutableStateOf(false)
    private var timerService by mutableStateOf(null as TimerService?)
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.TimerServiceBinder
            timerService = binder.getService()
            triggerTimerService(applicationContext, TimerService.Actions.Show)
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, TimerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        isBound = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PomodoroAppTheme {
                if (isBound) MainUI(timerService!!)
            }
        }
    }
}
