package com.pluscubed.logcat.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
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
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
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
    val context = LocalContext.current
    var displayLimit by remember { mutableIntStateOf(PreferenceHelper.getDisplayLimitPreference(context)) }
    var showDisplayLimitDialog by remember { mutableStateOf(false) }
    
    var showPidTimestamp by remember { mutableStateOf(PreferenceHelper.getShowTimestampAndPidPreference(context)) }
    var omitSensitive by remember { mutableStateOf(PreferenceHelper.isScrubberEnabled(context)) }

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
            SettingItem(
                title = "Display Limit",
                summary = "Mostrar apenas as últimas $displayLimit linhas (Padrão: 10.000)",
                onClick = { showDisplayLimitDialog = true }
            )
        }
        item {
            SwitchSettingItem(
                title = "Omitir info sensível",
                summary = "Ocultar URLs, números de telefone e e-mails nos logs",
                checked = omitSensitive,
                onCheckedChange = {
                    val sharedPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
                    sharedPrefs.edit().putBoolean("scrubber", it).apply()
                    omitSensitive = it
                }
            )
        }
        item {
            SwitchSettingItem(
                title = "Mostrar PID e Timestamp",
                summary = "Exibe o ID do processo e data/hora ao expandir logs",
                checked = showPidTimestamp,
                onCheckedChange = {
                    val sharedPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
                    sharedPrefs.edit().putBoolean(context.getString(org.omnirom.logcat.R.string.pref_show_timestamp), it).apply()
                    PreferenceHelper.clearCache()
                    showPidTimestamp = it
                }
            )
        }
    }

    if (showDisplayLimitDialog) {
        var textValue by remember { mutableStateOf(displayLimit.toString()) }
        AlertDialog(
            onDismissRequest = { showDisplayLimitDialog = false },
            title = { Text("Display Limit") },
            text = {
                Column {
                    Text("Defina o número máximo de linhas de log a serem exibidas (padrão: 10.000).")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { if (it.all { char -> char.isDigit() }) textValue = it },
                        label = { Text("Linhas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newLimit = textValue.toIntOrNull() ?: 10000
                        PreferenceHelper.setDisplayLimitPreference(context, newLimit)
                        displayLimit = newLimit
                        showDisplayLimitDialog = false
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisplayLimitDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
fun SwitchSettingItem(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingItem(title: String, summary: String, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
