package com.example.pomodoroapp

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pomodoroapp.notifications.MasterNotificationService
import com.example.pomodoroapp.notifications.PolicyAccessNotificationService
import com.example.pomodoroapp.ui.theme.Gray
import com.example.pomodoroapp.ui.theme.LightGray
import com.example.pomodoroapp.ui.theme.PomodoroAppTheme
import com.example.pomodoroapp.ui.theme.indent
import kotlinx.coroutines.delay

const val WORK = "Pomodoro"
const val REST = "Rest"

/*TODO:
   1) fix bugs
   2) settings page
   3) master notification
*/

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val nm =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val masterNotificationService = MasterNotificationService(applicationContext)
            PomodoroAppTheme {
                val vm = viewModel<MainViewModel>()

                val isOff by vm.isOff.collectAsState()
                val isPaused by vm.isPaused.collectAsState()
                val isWorking by vm.isWorking.collectAsState()
                val timerType by vm.timerType.collectAsState()

                val currentTime by vm.currentTime.collectAsState()
                val endTime by vm.endTime.collectAsState()
                val secondsPassed by vm.secondsPassed.collectAsState()

                Column {
                    // Upper buttons
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(indent, indent)
                    ) {
                        IconButton(
                            onClick = {
                                MasterNotificationService.isOn = true
                                masterNotificationService.showNotification()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null
                            )
                        }
                        IconButton({ /*TODO: make settings screen*/ }, enabled = isOff) {
                            Icon(
                                imageVector = Icons.Default.List,
                                //painter = painterResource(R.drawable.ic_settings),
                                contentDescription = null
                            )
                        }
                    }

                    // Main
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (isOff)  // Buttons: Play
                            IconButton(onClick = { vm.startTimer(nm) }) {
                                Icon(
                                    painterResource(R.drawable.baseline_play_arrow_48),
                                    null,
                                    tint = Color.DarkGray
                                )
                            }
                        else {
                            LaunchedEffect(key1 = isPaused, key2 = currentTime) {
                                if (isPaused) {
                                } else if (currentTime < endTime) {
                                    delay(100L)
                                    vm.renewSecondsPassed()
                                } else {
                                    vm.finishTimer(nm)
                                }
                            }

                            // <Progress bar>
                            val barWidth = 200
                            val barHeight = 45
                            val borderWidth = 6
                            Row {
                                // Compensation Icon
                                IconButton(onClick = {}, enabled = false) {
                                    Icon(
                                        painterResource(R.drawable.baseline_refresh_48),
                                        null,
                                        Modifier.size(30.dp),
                                        Color.Transparent
                                    )
                                }

                                Surface(
                                    Modifier
                                        .size(barWidth.dp, barHeight.dp)
                                        .border(
                                            borderWidth.dp,
                                            Color.DarkGray,
//                                            Color(0xFF585858),
                                            RoundedCornerShape(5.dp)
                                        ),
                                    RoundedCornerShape(5.dp),
                                    Gray
                                ) {
                                    val padding = borderWidth + 7
                                    BoxWithConstraints(
                                        Modifier
                                            .fillMaxSize()
                                            .padding(padding.dp)
                                    ) {
                                        val maxWidth = this.maxWidth
                                        Row(
                                            Modifier.fillMaxSize(),
                                            Arrangement.Start,
                                            Alignment.CenterVertically
                                        ) {
                                            val spacing = 4
                                            val modifier = Modifier
                                                .fillMaxHeight()
                                                .width(
                                                    ((maxWidth.value - 9 * spacing) / 10).dp
                                                )
                                                .background(
                                                    if (isWorking) Color.White else Color.Gray
                                                )
                                            val quantityOfBoxes: Int =
                                                10 * secondsPassed / (vm.getCurrentTimerDuration())//TODO: * 60)
                                            for (i in 0..<quantityOfBoxes - 1) {
                                                Box(modifier)
                                                Spacer(Modifier.width(spacing.dp))
                                            }
                                            if (quantityOfBoxes != 0)
                                                Box(modifier)
                                        }
                                    }
                                }

                                // Start over
                                IconButton(onClick = { vm.resetTimer(nm) }) {
                                    Icon(
                                        painterResource(R.drawable.rounded_refresh_30),
                                        null,
                                        Modifier.size(30.dp),
                                        Color.Gray
                                    )
                                }
                            }
                            // </Progress bar>
                        }

                        Spacer(Modifier.size(12.dp))

                        // <Timer name>
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(R.drawable.baseline_swap_vert_16),
                                null,
                                tint = LightGray
                            )

                            Spacer(modifier = Modifier.size(2.dp))

                            ClickableText(
                                text = AnnotatedString(timerType),
                                style = TextStyle(fontSize = 16.sp)
                            ) { _ ->
                                if (isOff) {
                                    vm.setNextTimerType()
                                }
                            }

                            Spacer(Modifier.size(4.dp))

                            Text(
                                AnnotatedString((vm.getCurrentTimerDuration()).toString() + " min"),
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 14.sp
                                ),
                                color = Gray
                            )
                        }
                        // </Timer name>

                        // <Control buttons>
                        if (!isOff) {
                            Spacer(Modifier.size(20.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { vm.breakTimer(nm) }) {
                                    Icon(
                                        painterResource(R.drawable.baseline_stop_48),
                                        null,
                                        Modifier.size(36.dp),
                                        Color.DarkGray
                                    )
                                }

                                // Suspend / Resume
                                val modifier = Modifier.size(36.dp)
                                val color = Color.DarkGray
                                if (isPaused) {
                                    IconButton(onClick = { vm.resumeTimer(nm) }) {
                                        Icon(
                                            painterResource(R.drawable.baseline_play_arrow_48),
                                            null,
                                            modifier,
                                            color
                                        )
                                    }
                                } else {
                                    IconButton(onClick = {
                                        vm.pauseTimer(nm)
                                    }) {
                                        Icon(
                                            painterResource(R.drawable.baseline_pause_24),
                                            null,
                                            modifier,
                                            color
                                        )
                                    }
                                }
                            }
                        }
                        // </Control buttons>
                    }
                }
            }
            // basic notifications
            if (MasterNotificationService.isOn)
                MasterNotificationService(applicationContext).showNotification()
            if (!nm.isNotificationPolicyAccessGranted)
                PolicyAccessNotificationService(applicationContext).showNotification()
        }
    }
}
