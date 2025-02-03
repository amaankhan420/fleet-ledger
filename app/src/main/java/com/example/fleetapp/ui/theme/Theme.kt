package com.example.fleetapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = White,
    secondary = Black,
    background = DarkGray,
    error = ErrorColor,
    errorContainer = DeleteContainerColor,
    onSecondary = LightGray
)


private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    secondary = Black,
    background = White,
    error = ErrorColor,
    errorContainer = DeleteContainerColor,
    onSecondary = LightGray
)

@Composable
fun FleetAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
