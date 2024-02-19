package com.example.pomodoroapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.pomodoroapp.service.PomodoroTimer
import com.example.pomodoroapp.service.TimerService
import com.example.pomodoroapp.ui.theme.indent

@Composable
fun SettingsUI(
    timerService: TimerService,
    navController: NavController
) {
    Column {
        // Upper buttons
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(indent, indent)
        ) {
            IconButton(onClick = { navController.navigate("MainUI") }) {
                Icon(
                    imageVector = Icons.Default.List,
                    //painter = painterResource(R.drawable.ic_settings),
                    contentDescription = null
                )
            }
        }

        // Main area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (timerService.timer.state == PomodoroTimer.States.Running) {
                Text("Pause the timer to change the settings")
            } else {
                Text("In develop")
            }
        }
    }
}