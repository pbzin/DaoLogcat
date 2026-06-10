package com.pluscubed.logcat.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.pluscubed.logcat.ui.theme.MatLogTheme

class RecordLogDialogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MatLogTheme {
                Text("Recording Log Dialog")
            }
        }
    }

    companion object {
        const val EXTRA_QUERY_SUGGESTIONS = "suggestions"
    }
}
