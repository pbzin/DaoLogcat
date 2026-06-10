package com.pluscubed.logcat.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pluscubed.logcat.ui.theme.MatLogTheme

class SettingsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MatLogTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Settings") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { padding ->
                    SettingsList(Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
fun SettingsList(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            SettingItem("Text Size", "Medium")
        }
        item {
            SettingItem("Display Limit", "10000 lines")
        }
        item {
            SettingItem("Omit sensitive info", "Enabled")
        }
        item {
            SettingItem("Show Pid & Timestamp", "Enabled")
        }
    }
}

@Composable
fun SettingItem(title: String, summary: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO */ }
            .padding(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
