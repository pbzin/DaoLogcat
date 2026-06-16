package com.pluscubed.logcat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import android.widget.Toast
import android.content.ClipData
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import com.pluscubed.logcat.data.LogLine
import com.pluscubed.logcat.ui.components.LogLineItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogcatScreen(
    viewModel: LogcatViewModel,
    onSettingsClick: () -> Unit,
    onSaveClick: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    val logLines by viewModel.logLines.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val status by viewModel.status.collectAsState()
    val filterQuery by viewModel.filterQuery.collectAsState()
    val minLogLevel by viewModel.minLogLevel.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    var isPaused by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var currentTab by remember { mutableIntStateOf(0) }

    val hasFilter = filterQuery.isNotEmpty() || minLogLevel > 0
    val listState = rememberLazyListState()
    val visibleLines = remember(logLines) { logLines.asReversed() }
    val expandedLogs = remember { mutableStateMapOf<Long, Boolean>() }
    val scope = rememberCoroutineScope()
    var followLatest by remember { mutableStateOf(true) }
    val showJumpToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 3 }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            Triple(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
                listState.isScrollInProgress
            )
        }.collect { (index, offset, scrolling) ->
            if (scrolling) {
                followLatest = index <= 1 && offset < 120
            }
        }
    }

    LaunchedEffect(filterQuery, minLogLevel) {
        followLatest = true
        if (visibleLines.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(visibleLines.firstOrNull()?.stableId) {
        if (followLatest && visibleLines.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DaoLogcat")
                        if (currentTab == 0) {
                            Text(
                                text = if (isPaused) "Paused" else status,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (currentTab == 0) {
                        IconButton(onClick = {
                            onFilterClick()
                            showFilterDialog = true
                        }) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (hasFilter) MaterialTheme.colorScheme.primary
                                else LocalContentColor.current
                            )
                        }
                        IconButton(onClick = { showSaveDialog = true }) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                    
                    IconButton(onClick = { currentTab = 0 }) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "Logs",
                            tint = if (currentTab == 0) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { currentTab = 1 }) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = "Saved Logs",
                            tint = if (currentTab == 1) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }

                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentTab == 0) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (showJumpToTop) {
                        SmallFloatingActionButton(
                            onClick = {
                                followLatest = true
                                scope.launch { listState.scrollToItem(0) }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Ir para logs recentes"
                            )
                        }
                    }
                    FloatingActionButton(
                        onClick = {
                            isPaused = !isPaused
                            viewModel.togglePause()
                        }
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause"
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (currentTab == 0) {
                BottomAppBar(
                    actions = {
                        IconButton(onClick = { viewModel.clearLogs() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear")
                        }
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        if (hasFilter) {
                            AssistChip(
                                onClick = { viewModel.clearFilter() },
                                label = {
                                    Text(
                                        text = activeFilterLabel(filterQuery, minLogLevel),
                                        maxLines = 1
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Close, contentDescription = "Clear filter")
                                }
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (currentTab == 0) {
                if (isLoading) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (visibleLines.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No logs yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { viewModel.startLogcat() }) {
                            Text("Retry")
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = visibleLines,
                                key = { it.stableId }
                            ) { logLine ->
                                val logKey = logLine.stableId
                                val expanded = expandedLogs[logKey] == true
                                LogLineItem(
                                    logLine = logLine,
                                    expanded = expanded,
                                    onToggleExpanded = {
                                        val next = !expanded
                                        expandedLogs[logKey] = next
                                        logLine.isExpanded = next
                                    },
                                    onCopyClick = {
                                        scope.launch {
                                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Logcat", logLine.getOriginalLine())))
                                        }
                                        Toast.makeText(context, "Log copiado", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 0.5.dp
                                )
                            }
                        }
                        LogScrollbar(
                            listState = listState,
                            itemCount = visibleLines.size,
                            onManualScroll = { targetIndex ->
                                followLatest = targetIndex <= 1
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
            } else {
                SavedLogsScreen()
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            currentQuery = filterQuery,
            currentMinLevel = minLogLevel,
            onDismiss = { showFilterDialog = false },
            onApply = { query, minLevel ->
                viewModel.setFilter(query, minLevel)
                showFilterDialog = false
            },
            onClear = {
                viewModel.clearFilter()
                showFilterDialog = false
            }
        )
    }

    if (showSaveDialog) {
        SaveLogDialog(
            onDismiss = { showSaveDialog = false },
            onConfirm = { name ->
                onSaveClick(name)
                showSaveDialog = false
            }
        )
    }
}

@Composable
fun SaveLogDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val defaultName = remember {
        val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(Date())
        "DaoLogcat-$timestamp"
    }
    var fileName by remember { mutableStateOf(defaultName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Log") },
        text = {
            Column {
                Text("Enter a name for the log file:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Filename") },
                    suffix = { Text(".txt") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalName = if (fileName.isBlank()) defaultName else fileName
                    onConfirm(finalName)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun LogScrollbar(
    listState: LazyListState,
    itemCount: Int,
    onManualScroll: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (itemCount <= 0) {
        return
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(24.dp)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val visibleItems = listState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1)
        val maxIndex = (itemCount - visibleItems).coerceAtLeast(1)
        val progress = (listState.firstVisibleItemIndex.toFloat() / maxIndex).coerceIn(0f, 1f)
        val thumbHeight = 48.dp
        val thumbOffset = (maxHeight - thumbHeight) * progress
        val scope = rememberCoroutineScope()
        fun scrollToPosition(y: Float) {
            val trackHeight = constraints.maxHeight.toFloat().coerceAtLeast(1f)
            val targetProgress = (y / trackHeight).coerceIn(0f, 1f)
            val targetIndex = (targetProgress * maxIndex).roundToInt().coerceIn(0, maxIndex)
            onManualScroll(targetIndex)
            scope.launch { listState.scrollToItem(targetIndex) }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(itemCount, maxIndex) {
                    detectVerticalDragGestures(
                        onDragStart = { offset -> scrollToPosition(offset.y) },
                        onVerticalDrag = { change, _ -> scrollToPosition(change.position.y) }
                    )
                }
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(3.dp)
                .background(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Box(
            modifier = Modifier
                .padding(top = thumbOffset)
                .height(thumbHeight)
                .width(10.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(5.dp)
                )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterDialog(
    currentQuery: String,
    currentMinLevel: Int,
    onDismiss: () -> Unit,
    onApply: (String, Int) -> Unit,
    onClear: () -> Unit
) {
    var query by remember(currentQuery) { mutableStateOf(currentQuery) }
    var minLevel by remember(currentMinLevel) { mutableStateOf(currentMinLevel) }
    val levels = listOf(
        "Todos" to 0,
        "Verbose" to 2,
        "Debug" to 3,
        "Info" to 4,
        "Avisos" to 5,
        "Erros" to 6
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar logs") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Texto, tag, app ou categoria") },
                    singleLine = true
                )
                Text(
                    text = "Mostrar a partir de",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    levels.forEach { (label, value) ->
                        FilterChip(
                            selected = minLevel == value,
                            onClick = { minLevel = value },
                            label = { Text(label, maxLines = 1) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(query, minLevel) }) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onClear) {
                Text("Limpar")
            }
        }
    )
}

private fun activeFilterLabel(query: String, minLevel: Int): String {
    val level = when (minLevel) {
        2 -> "Verbose+"
        3 -> "Debug+"
        4 -> "Info+"
        5 -> "Avisos+"
        6 -> "Erros"
        else -> "Todos"
    }
    return if (query.isEmpty()) level else "$level: $query"
}
