package com.example.pomodoroapp.activities

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoroapp.R
import com.example.pomodoroapp.notifications.CompletionNotificationService
import com.example.pomodoroapp.notifications.MasterNotificationService
import com.example.pomodoroapp.notifications.PolicyAccessNotificationService
import com.example.pomodoroapp.sys_functions.DND
import com.example.pomodoroapp.ui.theme.Gray
import com.example.pomodoroapp.ui.theme.LightGray
import com.example.pomodoroapp.ui.theme.PomodoroAppTheme
import com.example.pomodoroapp.ui.theme.indent
import kotlinx.coroutines.delay
import kotlin.math.max

const val WORK = "Pomodoro"
const val REST = "Rest"

class MainActivity : ComponentActivity() {

    companion object {

        lateinit var notificationManager: NotificationManager

        public var interruptionFilterBeforePomodoro: Int = 0

        // <> timer variables
        public var WHETHER_SET_DND_MODE = true
        public var WHETHER_CHANGE_TIMER_TYPE_ON_FINISH = true
        public var WHETHER_START_REST_BY_POMODORO_FINISH = true

        public var timerType by mutableStateOf(WORK)
        public var timerDurations = mutableMapOf(
            WORK to 30,
            REST to 7
        )
        public var isPaused by mutableStateOf(false)
        public var isOff by mutableStateOf(true)

        public var startTime by mutableLongStateOf(0) // (seconds) determined inside onClick function
        public var endTime by mutableLongStateOf(0)  // (seconds) determined inside onClick function
        public var currentTime by mutableLongStateOf(0)  // (seconds)
        public var secondsPassed by mutableIntStateOf(0)  // progress bar`s variable

        public var whenPaused: Long = 0
        // </>


        // Timer state
        fun whetherWorking(): Boolean{
            return !isOff && !isPaused
        }

        fun getCurrentTimerDuration(): Int {  // seconds
            return timerDurations.getOrDefault(timerType, 1)
        }

        fun getNextTimerType(): String {
            return if (timerType == WORK) REST else WORK
        }

        // Manipulations with timer
        fun startTimer() {
            if (timerType == WORK)
                setDNDMode(
                    saveCurrentFilter_getDNDFilter()
                )
            launchTimer()
            isOff = false
        }

        fun launchTimer() {
            secondsPassed = 0
            currentTime = System.currentTimeMillis() / 1000
            startTime = currentTime
            endTime = startTime + getCurrentTimerDuration()
        }


        fun resetTimer() {
            if (isPaused) {
                isPaused = false
                setDNDMode(saveCurrentFilter_getDNDFilter())
            }
            launchTimer()
        }

        fun breakTimer() {
            isPaused = false
            isOff = true
            setDNDMode(interruptionFilterBeforePomodoro)
        }

        fun pauseTimer() {
            isPaused = true
            whenPaused = System.currentTimeMillis() / 1000
            setDNDMode(interruptionFilterBeforePomodoro)
        }

        fun resumeTimer() {
            isPaused = false
            val delta = System.currentTimeMillis() - whenPaused
            startTime += delta
            endTime += delta

            val dnd = when (timerType == WORK) {
                true -> saveCurrentFilter_getDNDFilter()
                false -> interruptionFilterBeforePomodoro
            }
            setDNDMode(dnd)
        }

        private fun setDNDMode(filter: Int = DND.INTERRUPTION_FILTER) {
            if (WHETHER_SET_DND_MODE)
                DND.setDNDMode(notificationManager, filter)
        }

        private fun saveCurrentFilter_getDNDFilter(): Int {
            interruptionFilterBeforePomodoro =
                notificationManager.currentInterruptionFilter
            return max(
                interruptionFilterBeforePomodoro,
                DND.INTERRUPTION_FILTER
            )
        }

        /*Todo: notification*/
//        fun notifyAboutCompletion() {
//            CompletionNotificationService(applicationContext).sendNotification(
//                timerType
//            )
//            delay(5000L)
//            CompletionNotificationService(applicationContext).deleteNotification()
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val masterNotificationService = MasterNotificationService(applicationContext)

        setContent {
            PomodoroAppTheme {
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
                        IconButton({ /*TODO: make settings screen*/ }, enabled = !whetherWorking()) {
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
                            IconButton(onClick = { startTimer() }) {
                                Icon(
                                    painterResource(R.drawable.baseline_play_arrow_48),
                                    null,
                                    tint = Color.DarkGray
                                )
                            }
                        else {
                            // <Timer>
                            LaunchedEffect(key1 = isPaused, key2 = currentTime) {
                                if (isPaused || isOff) {
                                } else if (currentTime < endTime) {
                                    delay(100L)
                                    currentTime = System.currentTimeMillis() / 1000
                                    secondsPassed = (
                                            (currentTime - startTime) // / 60
                                            ).toInt()
                                } else {
                                    isOff = true
                                    isPaused = false

                                    if (timerType == WORK)
                                        setDNDMode(interruptionFilterBeforePomodoro)

                                    if (WHETHER_CHANGE_TIMER_TYPE_ON_FINISH)
                                        timerType = getNextTimerType()
                                    if (WHETHER_START_REST_BY_POMODORO_FINISH && timerType == WORK)
                                        launchTimer()

//                                    notifyAboutCompletion()
                                }
                            }
                            // </Timer>

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
                                                    if (whetherWorking()) Color.White else Color.Gray
                                                )
                                            val quantityOfBoxes: Int =
                                                10 * secondsPassed / (getCurrentTimerDuration())// * 60)
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
                                IconButton(onClick = { resetTimer() }) {
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
                                    timerType = getNextTimerType()
                                }
                            }

                            Spacer(Modifier.size(4.dp))

                            Text(
                                AnnotatedString((getCurrentTimerDuration()).toString() + " min"),
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
                                IconButton(onClick = { breakTimer() }) {
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
                                    IconButton(onClick = { resumeTimer() }) {
                                        Icon(
                                            painterResource(R.drawable.baseline_play_arrow_48),
                                            null,
                                            modifier,
                                            color
                                        )
                                    }
                                } else {
                                    IconButton(onClick = { pauseTimer() }) {
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
            LaunchedEffect(key1 = null) {  // basic notifications
                MasterNotificationService(applicationContext).showNotification()
                if (!notificationManager.isNotificationPolicyAccessGranted)
                    PolicyAccessNotificationService(applicationContext).showNotification()
            }
        }
    }
}
