package com.pluscubed.logcat.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.pluscubed.logcat.ui.theme.DaoLogcatTheme

class RecordLogDialogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DaoLogcatTheme {
                Text("Recording Log Dialog")
            }
        }
    }

    companion object {
        const val EXTRA_QUERY_SUGGESTIONS = "suggestions"
    }
}
