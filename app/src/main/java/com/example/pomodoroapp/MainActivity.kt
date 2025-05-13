package com.example.pomodoroapp
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pomodoroapp.service.TimerService
import com.example.pomodoroapp.service.TimerServiceHelper.sendPreferencesToTimerService
import com.example.pomodoroapp.service.TimerServiceHelper.triggerTimerService
import com.example.pomodoroapp.ui.MainUI
import com.example.pomodoroapp.ui.SettingsUI
import com.example.pomodoroapp.ui.theme.PomodoroAppTheme
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private var isBound by mutableStateOf(false)
    private var timerService by mutableStateOf(null as TimerService?)
    private var dataStoreManager by mutableStateOf(null as DataStoreManager?)
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.TimerServiceBinder
            timerService = binder.getService()

            val appPreferences = runBlocking { dataStoreManager!!.appPreferences!! }
            sendPreferencesToTimerService(applicationContext, appPreferences)
            triggerTimerService(applicationContext, TimerService.Actions.SHOW)

            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, TimerService::class.java).also { intent ->
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        isBound = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataStoreManager = DataStoreManager(applicationContext)
        runBlocking { dataStoreManager!!.setValuesForFirstLaunch() }

        setContent {
            PomodoroAppTheme(
                darkTheme = when {
                    dataStoreManager!!.appPreferences!!.alwaysDarkTheme -> true
                    dataStoreManager!!.appPreferences!!.alwaysLightTheme -> false
                    else -> isSystemInDarkTheme()
                }
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "MainUI") {
                        composable("MainUI") {
                            if (isBound) MainUI(timerService!!, navController, dataStoreManager!!)
                        }
                        composable("SettingsUI") {
                            if (isBound) SettingsUI(timerService!!, navController, dataStoreManager!!)
                        }
                    }
                }
            }
        }
    }
}
