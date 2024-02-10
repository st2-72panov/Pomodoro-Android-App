package com.example.pomodoroapp
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
import com.example.pomodoroapp.service.PomodoroTimer
import com.example.pomodoroapp.service.TimerService
import com.example.pomodoroapp.service.TimerServiceHelper
import com.example.pomodoroapp.ui.theme.Gray
import com.example.pomodoroapp.ui.theme.LightGray
import com.example.pomodoroapp.ui.theme.indent

@Composable
fun MainUI(timerService: TimerService) {
    val context = LocalContext.current
    val isOff = when (timerService.timer.state) {
        PomodoroTimer.States.Idle, PomodoroTimer.States.Completed -> true
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
                IconButton(onClick = {
                    TimerServiceHelper.triggerTimerService(
                        context, TimerService.Actions.Launch
                    )
                }) {
                    Icon(
                        painterResource(R.drawable.baseline_play_arrow_48),
                        null,
                        tint = Color.DarkGray
                    )
                }
            else PomodoroProgressBar(timerService)

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
                    text = AnnotatedString(timerService.timer.type.name),
                    style = TextStyle(fontSize = 16.sp)
                ) { _ ->
                    if (isOff) {
                        TimerServiceHelper.triggerTimerService(
                            context,
                            TimerService.Actions.ChangeTimerType
                        )
                    }
                }

                Spacer(Modifier.size(4.dp))

                Text(
                    AnnotatedString(
                        (timerService.timer.type.duration / 60).toString() + " min"
                    ), style = TextStyle(
                        fontFamily = FontFamily.SansSerif, fontSize = 14.sp
                    ), color = Gray
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
                            context, TimerService.Actions.Stop
                        )
                    }) {
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
                    if (timerService.timer.state == PomodoroTimer.States.Paused) {
                        IconButton(onClick = {
                            TimerServiceHelper.triggerTimerService(
                                context, TimerService.Actions.Resume
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
                                context, TimerService.Actions.Pause
                            )
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


@Composable
fun PomodoroProgressBar(timerService: TimerService) {
    val context = LocalContext.current

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
                    borderWidth.dp, Color.DarkGray,
//                                            Color(0xFF585858),
                    RoundedCornerShape(5.dp)
                ), RoundedCornerShape(5.dp), Gray
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
                            if (timerService.timer.state == PomodoroTimer.States.Running) Color.White
                            else Color.Gray
                        )
                    val quantityOfBoxes: Int =
                        10 * timerService.timer.passed / timerService.timer.type.duration
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
                context, TimerService.Actions.Restart
            )
        }) {
            Icon(
                painterResource(R.drawable.rounded_refresh_30),
                null,
                Modifier.size(30.dp),
                Color.Gray
            )
        }
    }
}