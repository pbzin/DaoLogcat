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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pluscubed.logcat.helper.PreferenceHelper
import com.pluscubed.logcat.ui.theme.DaoLogcatTheme

class SettingsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var themeMode by remember { mutableStateOf(PreferenceHelper.getThemeMode(this)) }
            DaoLogcatTheme(themeMode = themeMode) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Configuracoes") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                                }
                            }
                        )
                    }
                ) { padding ->
                    SettingsList(
                        themeMode = themeMode,
                        onThemeModeChange = { mode ->
                            PreferenceHelper.setThemeMode(this, mode)
                            themeMode = mode
                        },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsList(
    themeMode: Int,
    onThemeModeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            SectionTitle("Aparencia")
            ThemeModeSetting(
                selectedMode = themeMode,
                onModeSelected = onThemeModeChange
            )
        }
        item {
            SectionTitle("Logs")
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
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun ThemeModeSetting(
    selectedMode: Int,
    onModeSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = "Tema", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Escolha o visual do DaoLogcat.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeChip(
                label = "Sistema",
                selected = selectedMode == PreferenceHelper.THEME_SYSTEM,
                onClick = { onModeSelected(PreferenceHelper.THEME_SYSTEM) }
            )
            ThemeChip(
                label = "Escuro",
                selected = selectedMode == PreferenceHelper.THEME_DARK,
                onClick = { onModeSelected(PreferenceHelper.THEME_DARK) }
            )
            ThemeChip(
                label = "Claro suave",
                selected = selectedMode == PreferenceHelper.THEME_LIGHT,
                onClick = { onModeSelected(PreferenceHelper.THEME_LIGHT) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1) }
    )
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
