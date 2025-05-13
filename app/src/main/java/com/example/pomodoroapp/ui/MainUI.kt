package com.example.pomodoroapp.ui

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pomodoroapp.DataStoreManager
import com.example.pomodoroapp.R
import com.example.pomodoroapp.service.PomodoroTimer
import com.example.pomodoroapp.service.TimerService
import com.example.pomodoroapp.service.TimerServiceHelper
import com.example.pomodoroapp.ui.theme.indent

@Composable
fun MainUI(
    timerService: TimerService, navController: NavController, dataStoreManager: DataStoreManager
) {
    val context = LocalContext.current
    val appPreferences = dataStoreManager.appPreferences
    val timerDuration =  // since typeId — mutableIntStateOf, there is no problems with plain "="
        if (timerService.timer.typeId == R.string.work)
            appPreferences!!.workDuration
        else
            appPreferences!!.restDuration

    val isOff = when (timerService.timer.state) {
        PomodoroTimer.States.IDLE, PomodoroTimer.States.COMPLETED -> true
        else -> false
    }
    Column {
        // Upper buttons
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(indent, indent)
        ) {
            IconButton({ navController.navigate("SettingsUI") }, enabled = isOff) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = when (timerService.timer.state) {
                        PomodoroTimer.States.RUNNING -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
        }

        // Main area
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (isOff)  // Buttons: Play
                IconButton(onClick = {
                    TimerServiceHelper.triggerTimerService(
                        context, TimerService.Actions.LAUNCH
                    )
                }) {
                    Icon(
                        painterResource(R.drawable.baseline_play_arrow_48),
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            else PomodoroProgressBar(timerService, dataStoreManager)

            Spacer(Modifier.size(12.dp))

            // <Timer name>
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.baseline_swap_vert_16),
                    null,
                    tint = MaterialTheme.colorScheme.tertiary
                )

                Spacer(modifier = Modifier.size(2.dp))

                ClickableText(
                    text = AnnotatedString(context.resources.getString(timerService.timer.typeId)),
                    style = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                ) { _ ->
                    if (isOff) {
                        TimerServiceHelper.triggerTimerService(
                            context, TimerService.Actions.CHANGE_TIMER_TYPE
                        )
                    }
                }

                Spacer(Modifier.size(4.dp))

                Text(
                    AnnotatedString(
                        (timerDuration / 60).toString() + " min"
                    ), style = TextStyle(
                        fontFamily = FontFamily.SansSerif, fontSize = 14.sp
                    ), color = MaterialTheme.colorScheme.tertiary
                )
            }
            // </Timer name>

            // <Control buttons>
            if (!isOff) {
                Spacer(Modifier.size(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        TimerServiceHelper.triggerTimerService(
                            context, TimerService.Actions.STOP
                        )
                    }) {
                        Icon(
                            painterResource(R.drawable.baseline_stop_48),
                            null,
                            Modifier.size(36.dp),
                            MaterialTheme.colorScheme.primary
                        )
                    }

                    // Suspend / Resume
                    val modifier = Modifier.size(36.dp)
                    val color = MaterialTheme.colorScheme.primary
                    if (timerService.timer.state == PomodoroTimer.States.PAUSED) {
                        IconButton(onClick = {
                            TimerServiceHelper.triggerTimerService(
                                context, TimerService.Actions.RESUME
                            )
                        }) {
                            Icon(
                                painterResource(R.drawable.baseline_play_arrow_48),
                                null,
                                modifier,
                                color
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            TimerServiceHelper.triggerTimerService(
                                context, TimerService.Actions.PAUSE
                            )
                        }) {
                            Icon(
                                painterResource(R.drawable.baseline_pause_24), null, modifier, color
                            )
                        }
                    }
                }
            }
            // </Control buttons>
        }
    }
}


@Composable
fun PomodoroProgressBar(
    timerService: TimerService, dataStoreManager: DataStoreManager
) {
    val context = LocalContext.current
    val appPreferences = dataStoreManager.appPreferences
    val timerDuration =  // since ↓typeId↓ — mutableIntStateOf, there is no problems with plain "="
        if (timerService.timer.typeId == R.string.work)
            appPreferences!!.workDuration
        else
            appPreferences!!.restDuration

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
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(5.dp)
                ),
            RoundedCornerShape(5.dp),
            MaterialTheme.colorScheme.secondaryContainer
        ) {
            val padding = borderWidth + 7
            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .padding(padding.dp)
            ) {
                val maxWidth = this.maxWidth
                Row(
                    Modifier.fillMaxSize(), Arrangement.Start, Alignment.CenterVertically
                ) {
                    val spacing = 4
                    val modifier = Modifier
                        .fillMaxHeight()
                        .width(
                            ((maxWidth.value - 9 * spacing) / 10).dp
                        )
                        .background(
                            if (timerService.timer.state == PomodoroTimer.States.RUNNING) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.secondary
                        )
                    val quantityOfBoxes: Int = 10 * timerService.timer.passed / timerDuration
                    for (i in 0..<quantityOfBoxes - 1) {
                        Box(modifier)
                        Spacer(Modifier.width(spacing.dp))
                    }
                    if (quantityOfBoxes != 0) Box(modifier)
                }
            }
        }

        // Start over
        IconButton(onClick = {
            TimerServiceHelper.triggerTimerService(
                context, TimerService.Actions.RESTART
            )
        }) {
            Icon(
                painterResource(R.drawable.rounded_refresh_30),
                null,
                Modifier.size(30.dp),
                MaterialTheme.colorScheme.secondary
            )
        }
    }
}