package com.example.pomodoroapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pomodoroapp.ui.theme.PomodoroAppTheme
import kotlinx.coroutines.delay

const val WORK = "Pomodoro"
const val REST = "Rest"

class MainActivity : ComponentActivity() {
    private fun getNextTimerType(t: String): String {
        return if (t == WORK) REST else WORK
    }

    @SuppressLint("MutableCollectionMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PomodoroAppTheme {
                var timerDurations = remember {
                    mutableStateMapOf(
                        WORK to 10,
                        REST to 5
                    )
                }
                var timerType by remember { mutableStateOf(WORK) }
                var isWorking by remember { mutableStateOf(false) }
                var timePassed by remember { mutableLongStateOf(0L) }

                MenuButton(isWorking = isWorking)
                //
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (!isWorking && timePassed == 0L)  // Draw button
                        IconButton(onClick = { isWorking = true }) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        }
                    else {  // Draw timer
                        val totalTime: Long = (
                                timerDurations.getOrDefault(timerType, 1) * 1000
                                ).toLong()
                        var value by remember { mutableLongStateOf(0L) }
                        LaunchedEffect(key1 = isWorking, key2 = timePassed) {
                            if (!isWorking) {
                            } else if (timePassed < totalTime) {
                                delay(100L)
                                timePassed += 100L
                                value = timePassed * 100 / totalTime
                            } else {
                                // Rest starts automatically after Pomodoro ends
                                // Pomodoro does not
                                if (timerType == REST)
                                    isWorking = false
                                timerType = getNextTimerType(timerType)
                                timePassed = 0L
                            }
                        }
                        Text("$value%")
                    }
                    //
                    ClickableText(text = AnnotatedString(timerType)) { offset ->
                        if (!isWorking) {
                            timerType = getNextTimerType(timerType)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuButton(isWorking: Boolean, indentation: Dp = 20.dp) {
    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier
            .padding(horizontal = indentation, vertical = indentation)
            .fillMaxWidth()
    ) {
        IconButton({ /*TODO*/ }, enabled = !isWorking) {
            Icon(
                imageVector = Icons.Default.List,
                //painter = painterResource(R.drawable.ic_settings),
                contentDescription = null
            )
        }
    }
}