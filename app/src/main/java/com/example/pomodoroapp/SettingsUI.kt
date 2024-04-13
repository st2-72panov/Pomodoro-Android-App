package com.example.pomodoroapp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pomodoroapp.service.PomodoroTimer
import com.example.pomodoroapp.service.TimerService
import com.example.pomodoroapp.ui.theme.indent
import com.example.pomodoroapp.util.Preferences.timerTypes

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
                Text(AnnotatedString("Timers' durations"))
                Row(horizontalArrangement = Arrangement.Center) {
                    CircularList(
                        timerTypes[R.string.work]!! / 60,
                        {minutes ->
                            timerTypes[R.string.work] = minutes * 60
                            timerService.timer.setDuration()
                        }
                    )
                    CircularList(
                        timerTypes[R.string.rest]!! / 60,
                        {minutes ->
                            timerTypes[R.string.rest] = minutes * 60
                            timerService.timer.setDuration()
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CircularList(
    initialItem: Int,
    onItemSelected: (number: Int) -> Unit,
    numberOfDisplayedItems: Int = 3,
    itemScaleFact: Float = 1.5f,
) {
    val width = 50.dp
    val itemHeight = 30.dp
    val items = (1..120).toList()
    val textStyle = TextStyle(fontSize = 11.sp)
    val textColor = Color.LightGray
    val selectedTextColor = Color.Black

    val itemHalfHeight = LocalDensity.current.run { itemHeight.toPx() / 2f }
    val scrollState = rememberLazyListState(0)
    var lastSelectedIndex by remember {
        mutableIntStateOf(0)
    }
    var itemsState by remember {
        mutableStateOf(items)
    }
    LaunchedEffect(items) {
        var targetIndex = items.indexOf(initialItem) - 1
        targetIndex += ((Int.MAX_VALUE / 2) / items.size) * items.size
        itemsState = items
        lastSelectedIndex = targetIndex
        scrollState.scrollToItem(targetIndex)
    }
    LazyColumn(
        modifier = Modifier
            .width(width)
            .height(itemHeight * numberOfDisplayedItems),
        state = scrollState,
        flingBehavior = rememberSnapFlingBehavior(
            lazyListState = scrollState
        )
    ) {
        items(
            count = Int.MAX_VALUE,
            itemContent = { i ->
                val item = itemsState[i % itemsState.size]
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            val y = coordinates.positionInParent().y - itemHalfHeight
                            val parentHalfHeight =
                                (coordinates.parentCoordinates?.size?.height ?: 0) / 2f
                            val isSelected =
                                (y > parentHalfHeight - itemHalfHeight && y < parentHalfHeight + itemHalfHeight)
                            if (isSelected && lastSelectedIndex != i) {
                                onItemSelected(item)
                                lastSelectedIndex = i
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.toString(),
                        style = textStyle,
                        color = if (lastSelectedIndex == i) {
                            selectedTextColor
                        } else {
                            textColor
                        },
                        fontSize = if (lastSelectedIndex == i) {
                            textStyle.fontSize * itemScaleFact
                        } else {
                            textStyle.fontSize
                        }
                    )
                }
            }
        )
    }
}