package com.pluscubed.logcat.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pluscubed.logcat.data.LogClassification
import com.pluscubed.logcat.data.LogLine
import com.pluscubed.logcat.data.ProcessInfo
import com.pluscubed.logcat.helper.ProcessInfoResolver
import com.pluscubed.logcat.util.LogLineAdapterUtil

@Composable
fun LogLineItem(
    logLine: LogLine,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val indicatorColor = Color(LogLineAdapterUtil.getBackgroundColorForLogLevel(context, logLine.logLevel))
    val tagColor = Color(LogLineAdapterUtil.getOrCreateTagColor(context, logLine.tag))
    val processInfo = ProcessInfoResolver.getInstance().resolve(context, logLine.processId)
    val classification = LogClassification.from(logLine, processInfo)
    val source = if (processInfo.isKnown()) processInfo.getCompactName() else ""

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleExpanded() }
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(if (expanded) 48.dp else 26.dp)
                    .background(indicatorColor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(5.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = logLine.getProcessIdText(),
                            style = MaterialTheme.typography.labelSmall,
                            color = indicatorColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = logLine.tag.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = tagColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 4.dp)) {
                        Text(text = logLine.processId.toString(), style = MaterialTheme.typography.labelSmall)
                        Text(text = logLine.timestamp ?: "", style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (expanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onCopyClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy log",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copiar log")
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    if (classification.hasLabel()) {
                        LogBadge(
                            text = classification.label,
                            color = Color(classification.color),
                            modifier = Modifier.widthIn(max = 104.dp)
                        )
                    }
                    if (source.isNotEmpty()) {
                        LogBadge(
                            text = source,
                            color = sourceColor(processInfo),
                            modifier = Modifier.widthIn(max = 132.dp)
                        )
                    }
                    Text(
                        text = logLine.logOutput.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (expanded && classification.hasLabel()) {
                    Text(
                        text = classification.summary,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun LogBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .background(color, RoundedCornerShape(5.dp))
            .padding(horizontal = 4.dp, vertical = 0.dp)
    )
}

private fun sourceColor(processInfo: ProcessInfo): Color {
    return when {
        processInfo.isAppProcess() -> Color(0xff455a64)
        processInfo.isVendorProcess() -> Color(0xff33691e)
        processInfo.isSystemProcess() -> Color(0xff0d47a1)
        else -> Color(0xff424242)
    }
}
