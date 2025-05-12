package com.example.pomodoroapp.ui.theme
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat

private fun Color.darken(): Color {
    val hsv = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hsv)
    hsv[2] = 1 - hsv[2]  // lightness = 100% - lightness
    return Color(ColorUtils.HSLToColor(hsv))
}

private val LightColorScheme = lightColorScheme(
    primary = Color.DarkGray,
    primaryContainer = Color(0xFFEDE9EC),
    onPrimaryContainer = Color.White,
    secondary = Color.Gray,
    secondaryContainer = SlightlyDarkerGray,
    onSecondaryContainer = Color.White,
    tertiary = SlightlyLighterGray,  // Not that tertiary (it's ~2.5-ary)
    background = LightBackgroundColor,

)

private val DarkColorScheme = darkColorScheme(
    primary = LightColorScheme.primary.darken(),
    primaryContainer = LightColorScheme.primaryContainer.darken(),
    secondary = LightColorScheme.secondary.darken(),
    secondaryContainer = LightColorScheme.secondaryContainer.darken(),
    onSecondaryContainer = Color.White,
    tertiary = LightColorScheme.tertiary.darken(),
    background = Color(0xff141313)
)

@Composable
fun PomodoroAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}