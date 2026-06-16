package com.pluscubed.logcat.ui.theme
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.pluscubed.logcat.helper.PreferenceHelper

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB4A8),
    onPrimary = Color(0xFF5F160F),
    secondary = Color(0xFF8FD8C2),
    background = Color(0xFF12100F),
    onBackground = Color(0xFFF3E7E2),
    surface = Color(0xFF1D1816),
    onSurface = Color(0xFFF3E7E2),
    surfaceVariant = Color(0xFF302522),
    onSurfaceVariant = Color(0xFFD7C2BB),
    outlineVariant = Color(0xFF5A4842)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF8D2F25),
    onPrimary = Color.White,
    secondary = Color(0xFF006B56),
    background = Color(0xFFF4ECE8),
    onBackground = Color(0xFF251A17),
    surface = Color(0xFFFFF7F2),
    onSurface = Color(0xFF251A17),
    surfaceVariant = Color(0xFFE8D7D0),
    onSurfaceVariant = Color(0xFF55413C),
    outlineVariant = Color(0xFFCDB8B0)
)

@Composable
fun DaoLogcatTheme(
    themeMode: Int? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val selectedMode = themeMode ?: PreferenceHelper.getThemeMode(context)
    val darkTheme = when (selectedMode) {
        PreferenceHelper.THEME_DARK -> true
        PreferenceHelper.THEME_LIGHT -> false
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.surface.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
