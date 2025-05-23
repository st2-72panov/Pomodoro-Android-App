package com.example.pomodoroapp.ui
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.pomodoroapp.PreferencesStore
import com.example.pomodoroapp.service.PomodoroTimer
import com.example.pomodoroapp.service.TimerService
import com.example.pomodoroapp.ui.theme.indent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun SettingsUI(
    timerService: TimerService, navController: NavController, preferencesStore: PreferencesStore
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
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // <Main area>
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
        ) {
            if (timerService.timer.state == PomodoroTimer.States.RUNNING) {
                Text(
                    "Pause the timer to change the settings",
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    AnnotatedString("Timers' durations"),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(5.dp))
                Row(horizontalArrangement = Arrangement.Center) {
                    var debounceJob1: Job? = null
                    val scope1 = rememberCoroutineScope()
                    var debounceJob2: Job? = null
                    val scope2 = rememberCoroutineScope()

                    CircularList(
                        preferencesStore.appPreferences!!.workDuration / 60, { minutes ->
                            debounceJob1?.cancel()
                            debounceJob1 = scope1.launch {
                                delay(500)
                                preferencesStore.writeIntData(
                                    minutes * 60, PreferencesStore.PreferenceName.WORK_DURATION
                                )
                            }
                        })
                    CircularList(
                        preferencesStore.appPreferences!!.restDuration / 60, { minutes ->
                            debounceJob2?.cancel()
                            debounceJob2 = scope2.launch {
                                delay(500)
                                preferencesStore.writeIntData(
                                    minutes * 60, PreferencesStore.PreferenceName.REST_DURATION
                                )
                            }
                        })
                }
            }
        }
        // </Main area>

        Box(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            var inDialog by remember {
                mutableStateOf(false)
            }
            TextButton(
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(8.dp),
                onClick = { inDialog = true }
            ) {
                Text("Reset preferences", color = MaterialTheme.colorScheme.secondary)
            }
            if (inDialog)
                DeletionDialog(
                    onConfirmation = {
                        runBlocking { preferencesStore.setDefaultPreferences() }
                        inDialog = false
                        navController.navigate("MainUI")
                    },
                    onDismissRequest = { inDialog = false }
                )
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
    val textColor = MaterialTheme.colorScheme.secondary
    val selectedTextColor = MaterialTheme.colorScheme.primary

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
        items(count = Int.MAX_VALUE, itemContent = { i ->
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
                    }, contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.toString(), style = textStyle, color = if (lastSelectedIndex == i) {
                        selectedTextColor
                    } else {
                        textColor
                    }, fontSize = if (lastSelectedIndex == i) {
                        textStyle.fontSize * itemScaleFact
                    } else {
                        textStyle.fontSize
                    }
                )
            }
        })
    }
}


@Composable
fun DeletionDialog(
    onDismissRequest: () -> Unit, onConfirmation: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Are you sure",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(0.dp, 18.dp, 0.dp, 0.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary  // light
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() }, modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            "Dismiss",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    TextButton(
                        onClick = { onConfirmation() }, modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            "Yes",
                            color = MaterialTheme.colorScheme.primary
                        )  // lightest
                    }
                }
            }
        }
    }
}