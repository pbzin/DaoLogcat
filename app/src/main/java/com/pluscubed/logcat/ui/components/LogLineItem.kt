package com.pluscubed.logcat.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pluscubed.logcat.data.LogLine
import com.pluscubed.logcat.util.LogLineAdapterUtil

@Composable
fun LogLineItem(logLine: LogLine, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val indicatorColor = Color(LogLineAdapterUtil.getBackgroundColorForLogLevel(context, logLine.logLevel))
    val tagColor = Color(LogLineAdapterUtil.getOrCreateTagColor(context, logLine.tag))

    Box(modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(4.dp).height(40.dp).align(Alignment.CenterVertically).background(indicatorColor))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = logLine.getProcessIdText(), style = MaterialTheme.typography.labelLarge, color = indicatorColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = logLine.tag ?: "", style = MaterialTheme.typography.titleSmall, color = tagColor, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = logLine.processId.toString(), style = MaterialTheme.typography.labelSmall)
                        Text(text = logLine.timestamp ?: "", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Text(text = logLine.logOutput ?: "", style = MaterialTheme.typography.bodyMedium, maxLines = if (logLine.isExpanded) Int.MAX_VALUE else 1)
            }
        }
    }
}
