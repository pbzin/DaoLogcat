package com.pluscubed.logcat.ui

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.pluscubed.logcat.helper.PreferenceHelper
import com.pluscubed.logcat.ui.theme.DaoLogcatTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogcatActivity : ComponentActivity() {
    private val viewModel: LogcatViewModel by viewModels()
    private var refreshTheme: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            var themeMode by remember { mutableStateOf(PreferenceHelper.getThemeMode(this)) }
            refreshTheme = { themeMode = PreferenceHelper.getThemeMode(this) }
            DaoLogcatTheme(themeMode = themeMode) {
                LogcatScreen(
                    viewModel = viewModel,
                    onSettingsClick = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onSaveClick = {
                        saveVisibleLog()
                    },
                    onFilterClick = {
                        // TODO: Implement filter dialog
                    }
                )
            }
        }

        viewModel.startLogcat()
    }

    override fun onResume() {
        super.onResume()
        refreshTheme?.invoke()
    }

    override fun onDestroy() {
        refreshTheme = null
        super.onDestroy()
    }

    private fun saveVisibleLog() {
        val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(Date())
        val filename = "DaoLogcat-$timestamp.txt"
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/DaoLogcat")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        if (uri == null) {
            Log.e(TAG, "MediaStore returned null URI for $filename")
            Toast.makeText(this, "Unable to save log", Toast.LENGTH_LONG).show()
            return
        }

        runCatching {
            contentResolver.openOutputStream(uri)?.use { output ->
                output.write(viewModel.exportVisibleLogText().toByteArray(Charsets.UTF_8))
            } ?: error("Missing output stream")
            contentResolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) },
                null,
                null
            )
        }.onSuccess {
            Log.i(TAG, "Saved log snapshot to $uri")
            Toast.makeText(this, "Saved to Downloads/DaoLogcat/$filename", Toast.LENGTH_LONG).show()
        }.onFailure {
            Log.e(TAG, "Unable to save log snapshot", it)
            contentResolver.delete(uri, null, null)
            Toast.makeText(this, "Unable to save log: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val TAG = "DaoLogcat"
    }
}
