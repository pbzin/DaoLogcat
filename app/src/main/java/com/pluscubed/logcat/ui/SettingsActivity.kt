package com.pluscubed.logcat.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pluscubed.logcat.helper.PreferenceHelper
import com.pluscubed.logcat.ui.theme.DaoLogcatTheme
import com.pb.daologcat.R

private const val PIX_KEY = "5198a8b3-6b89-4475-aec1-5adcfcfd12cf"
private const val BITCOIN_ADDRESS = "1GkpDZDHYov7WZLs54Nv19f2KUoZPcACs2"
private const val MONERO_ADDRESS =
    "45YtYmxUeXeFdokKPG1KWtMFLByS8nwmtiJjEiZ9LfbkNaSUCvyWWAx3VmtDKKkxPJFdQLSXxodRWMt7EBu5TmA3Qi9dgwT"

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
                            title = { Text(stringResource(R.string.settings_title)) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            SectionTitle(stringResource(R.string.appearance))
            ThemeModeSetting(
                selectedMode = themeMode,
                onModeSelected = onThemeModeChange
            )
        }
        item {
            SectionTitle(stringResource(R.string.logs))
            SettingItem(
                title = stringResource(R.string.display_limit),
                summary = stringResource(R.string.display_limit_summary, displayLimit),
                onClick = { showDisplayLimitDialog = true }
            )
        }
        item {
            SwitchSettingItem(
                title = stringResource(R.string.omit_sensitive_info),
                summary = stringResource(R.string.omit_sensitive_info_summary),
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
                title = stringResource(R.string.show_pid_timestamp),
                summary = stringResource(R.string.show_pid_timestamp_summary),
                checked = showPidTimestamp,
                onCheckedChange = {
                    val sharedPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
                    sharedPrefs.edit().putBoolean(context.getString(com.pb.daologcat.R.string.pref_show_timestamp), it).apply()
                    PreferenceHelper.clearCache()
                    showPidTimestamp = it
                }
            )
        }
        item {
            SectionTitle(stringResource(R.string.support_development))
            DonationSection()
        }
    }

    if (showDisplayLimitDialog) {
        var textValue by remember { mutableStateOf(displayLimit.toString()) }
        AlertDialog(
            onDismissRequest = { showDisplayLimitDialog = false },
            title = { Text(stringResource(R.string.display_limit)) },
            text = {
                Column {
                    Text(stringResource(R.string.display_limit_dialog_message))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { if (it.all { char -> char.isDigit() }) textValue = it },
                        label = { Text(stringResource(R.string.lines)) },
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
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisplayLimitDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun DonationSection() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.support_development_summary),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                modifier = Modifier.fillMaxWidth(),
                onClick = { openUrl(context, "https://buymeacoffee.com/pbzin") },
                label = { Text("Buy Me a Coffee") },
                leadingIcon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                trailingIcon = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null) }
            )
            AssistChip(
                modifier = Modifier.fillMaxWidth(),
                onClick = { openUrl(context, "https://github.com/sponsors/pbzin") },
                label = { Text("GitHub Sponsors") },
                leadingIcon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                trailingIcon = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null) }
            )
        }
        DonationAddress(
            title = "${stringResource(R.string.pix_brazil)} 🇧🇷",
            value = PIX_KEY,
            onCopy = { copyDonationValue(context, "Pix", PIX_KEY) }
        )
        DonationAddress(
            title = "Bitcoin",
            value = BITCOIN_ADDRESS,
            qrResId = R.drawable.bitcoin_qr,
            onCopy = { copyDonationValue(context, "Bitcoin", BITCOIN_ADDRESS) }
        )
        DonationAddress(
            title = "Monero",
            value = MONERO_ADDRESS,
            qrResId = R.drawable.monero_qr,
            onCopy = { copyDonationValue(context, "Monero", MONERO_ADDRESS) }
        )
        TextButton(
            onClick = { openUrl(context, "https://github.com/pbzin") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.developed_by))
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
        }
    }
}

@Composable
fun DonationAddress(
    title: String,
    value: String,
    onCopy: () -> Unit,
    qrResId: Int? = null
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onCopy) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.copy))
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (qrResId != null) {
                Image(
                    painter = painterResource(qrResId),
                    contentDescription = "$title QR code",
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

private fun openUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun copyDonationValue(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
    Toast.makeText(context, context.getString(R.string.copied_value, label), Toast.LENGTH_SHORT).show()
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
        Text(text = stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(R.string.theme_summary),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeChip(
                label = stringResource(R.string.theme_system),
                selected = selectedMode == PreferenceHelper.THEME_SYSTEM,
                onClick = { onModeSelected(PreferenceHelper.THEME_SYSTEM) }
            )
            ThemeChip(
                label = stringResource(R.string.theme_dark),
                selected = selectedMode == PreferenceHelper.THEME_DARK,
                onClick = { onModeSelected(PreferenceHelper.THEME_DARK) }
            )
            ThemeChip(
                label = stringResource(R.string.theme_light_soft),
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
