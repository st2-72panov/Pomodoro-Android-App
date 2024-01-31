package com.example.pomodoroapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoroapp.notifications.MasterNotificationService
import com.example.pomodoroapp.notifications.PolicyAccessNotificationService
import com.example.pomodoroapp.ui.theme.Gray
import com.example.pomodoroapp.ui.theme.LightGray
import com.example.pomodoroapp.ui.theme.PomodoroAppTheme
import com.example.pomodoroapp.ui.theme.PurpleGrey80
import com.example.pomodoroapp.ui.theme.indent
import kotlinx.coroutines.delay

const val WORK = "Pomodoro"
const val REST = "Rest"

class MainActivity : ComponentActivity() {
    private fun getNextTimerType(t: String): String { return if (t == WORK) REST else WORK }

    @SuppressLint("MutableCollectionMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val masterNotificationService = MasterNotificationService(applicationContext)

        setContent {
            PomodoroAppTheme {
                val timerDurations = remember {
                    mutableStateMapOf(
                        WORK to 30,
                        REST to 7
                    )
                }
                var timerType by remember { mutableStateOf(WORK) }
                var isWorking by remember { mutableStateOf(false) }  // if paused also false
                var timePassed by remember { mutableLongStateOf(0L) }
                val isOff = !isWorking && timePassed == 0L

                Column {
                    // Upper buttons
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(indent, indent)
                    ) {
                        IconButton(
                            onClick = { masterNotificationService.showNotification() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null
                            )
                        }
                        MenuButton(isWorking = isWorking)
                    }

                    // Main
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (isOff)  // Buttons: Play
                            IconButton(onClick = { isWorking = true }) {
                                Icon(
                                    painterResource(R.drawable.baseline_play_arrow_48),
                                    null,
                                    tint = Color.DarkGray
                                )
                            }
                        else {
                            // <Timer>
                            val totalTime =
                                (timerDurations.getOrDefault(timerType, 1) * 1000).toLong()
                            var value by remember { mutableIntStateOf(0) }

                            LaunchedEffect(key1 = isWorking, key2 = timePassed) {
                                if (!isWorking) {   // Buttons: Resume Start over Stop
                                } else if (timePassed < totalTime) { // Buttons: Suspend Start over Stop
                                    delay(100L)
                                    timePassed += 100L
                                    value = (timePassed * 10 / totalTime).toInt()
                                } else {
                                    // Rest starts automatically after Pomodoro ends
                                    // Pomodoro does not
                                    if (timerType == REST)
                                        isWorking = false
                                    timerType = getNextTimerType(timerType)
                                    timePassed = 0L
                                }
                            }
                            // </Timer>

                            // <Progress bar>
                            val barWidth = 200
                            val barHeight = 45
                            val borderWidth = 6
                            Row() {
                                // Compensation Icon
                                IconButton(onClick = {}, enabled = false) {
                                    Icon(
                                        painterResource(R.drawable.baseline_refresh_48),
                                        null,
                                        Modifier.size(36.dp),
                                        Color.Transparent
                                    )
                                }

                                Surface(
                                    Modifier
                                        .size(barWidth.dp, barHeight.dp)
                                        .border(
                                            borderWidth.dp,
                                            Color(0xFF585858),
                                            CutCornerShape(5.dp)
                                        ),
                                    CutCornerShape(5.dp),
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
                                            for (i in 0..<value - 1) {
                                                Box(modifier)
                                                Spacer(Modifier.width(spacing.dp))
                                            }
                                            if (value != 0)
                                                Box(modifier)
                                        }
                                    }
                                }

                                // Start over
                                IconButton(onClick = {
                                    timePassed = 0L
                                    isWorking = true
                                }) {
                                    Icon(
                                        painterResource(R.drawable.baseline_refresh_48),
                                        null,
                                        Modifier.size(36.dp),
                                        Color.Gray
                                    )
                                }
                            }
                            // </Progress bar>
                        }

                        Spacer(Modifier.size(12.dp))

                        // <Timer name>
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier.border(1.dp, LightGray, RoundedCornerShape(10)).padding(5.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.baseline_swap_vert_16),
                                null,
                                tint = LightGray
                            )

                            Spacer(modifier = Modifier.size(2.dp))

                            ClickableText(
                                text = AnnotatedString(timerType),
                                style = TextStyle(fontSize = 16.sp)
                            ) { offset ->
                                if (isOff) {
                                    timerType = getNextTimerType(timerType)
                                }
                            }

                            Spacer(Modifier.size(4.dp))

                            Text(
                                AnnotatedString(timerDurations[timerType].toString() + " min"),
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

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Stop
                                IconButton(onClick = {
                                    timePassed = 0L
                                    isWorking = false
                                }) {
                                    Icon(
                                        painterResource(R.drawable.baseline_stop_48),
                                        null,
                                        Modifier.size(36.dp),
                                        Color.DarkGray
                                    )
                                }

                                // Suspend / Resume
//                                Spacer(Modifier.size(10.dp))
                                val painter = if (!isWorking)
                                    painterResource(R.drawable.baseline_play_arrow_48)
                                else
                                    painterResource(R.drawable.baseline_pause_24)
                                IconButton(onClick = { isWorking = !isWorking }) {
                                    Icon(painter, null, Modifier.size(36.dp), Color.DarkGray)
                                }
                            }
                        }
                        // </Control buttons>
                    }
                }
            }
        }
        masterNotificationService.showNotification()
        PolicyAccessNotificationService(applicationContext).showNotification()
    }
}

@Composable
fun MenuButton(isWorking: Boolean) {
    IconButton({ /*TODO: make settings screen*/ }, enabled = !isWorking) {
        Icon(
            imageVector = Icons.Default.List,
            //painter = painterResource(R.drawable.ic_settings),
            contentDescription = null
        )
    }
}
