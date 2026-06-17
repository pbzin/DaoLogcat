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
import androidx.compose.ui.res.stringResource
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
import com.pb.daologcat.R

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
                                text = if (isPaused) stringResource(R.string.paused) else status,
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
                                contentDescription = stringResource(R.string.filter),
                                tint = if (hasFilter) MaterialTheme.colorScheme.primary
                                else LocalContentColor.current
                            )
                        }
                        IconButton(onClick = { showSaveDialog = true }) {
                            Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save))
                        }
                    }
                    
                    IconButton(onClick = { currentTab = 0 }) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = stringResource(R.string.logs),
                            tint = if (currentTab == 0) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { currentTab = 1 }) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = stringResource(R.string.saved_logs),
                            tint = if (currentTab == 1) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }

                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
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
                                contentDescription = stringResource(R.string.jump_to_latest_logs)
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
                            contentDescription = if (isPaused) stringResource(R.string.resume) else stringResource(R.string.pause)
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
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.clear))
                        }
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
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
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_filter))
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
                            text = stringResource(R.string.no_logs_yet),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { viewModel.startLogcat() }) {
                            Text(stringResource(R.string.retry))
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
                                        Toast.makeText(context, context.getString(R.string.log_copied), Toast.LENGTH_SHORT).show()
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
        title = { Text(stringResource(R.string.save_log)) },
        text = {
            Column {
                Text(stringResource(R.string.save_log_message))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text(stringResource(R.string.filename)) },
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
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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
        stringResource(R.string.all_levels) to 0,
        "Verbose" to 2,
        "Debug" to 3,
        "Info" to 4,
        stringResource(R.string.warnings) to 5,
        stringResource(R.string.errors) to 6
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_logs)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.filter_query_hint)) },
                    singleLine = true
                )
                Text(
                    text = stringResource(R.string.minimum_level),
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
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onClear) {
                Text(stringResource(R.string.clear))
            }
        }
    )
}

@Composable
private fun activeFilterLabel(query: String, minLevel: Int): String {
    val level = when (minLevel) {
        2 -> "Verbose+"
        3 -> "Debug+"
        4 -> "Info+"
        5 -> stringResource(R.string.warnings_short)
        6 -> stringResource(R.string.errors_short)
        else -> stringResource(R.string.all_levels)
    }
    return if (query.isEmpty()) level else "$level: $query"
}
