package com.pluscubed.logcat.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.pluscubed.logcat.ui.theme.MatLogTheme

class LogcatActivity : ComponentActivity() {
    private val viewModel: LogcatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MatLogTheme {
                LogcatScreen(
                    viewModel = viewModel,
                    onSettingsClick = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onSaveClick = {
                        // TODO: Implement save logic in ViewModel
                    },
                    onFilterClick = {
                        // TODO: Implement filter dialog
                    }
                )
            }
        }

        viewModel.startLogcat()
    }
}
