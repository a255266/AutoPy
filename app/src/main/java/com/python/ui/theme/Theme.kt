package com.python.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

//
//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)
//
//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40
//
//    /* Other default colors to override
//    background = Color(0xFFFFFBFE),
//    surface = Color(0xFFFFFBFE),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onTertiary = Color.White,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),
//    */
//)

// Theme.kt

private const val l = 0xFF0D47A1

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),         // 蓝色主色
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF03A9F4),
    onSecondary = Color.Black,

//    background = Color(0xFFCCE6FF),
    background = Color(0xFFF1F8FF),

    onBackground = Color.Black,
    surface = Color(0xFFADDCF5),
    onSurface = Color.Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF4FC3F7),
    onSecondary = Color.White,

    background = Color(0xFF121212),
//    background = Color(0xFF00284D),

    onBackground = Color.White,
    surface = Color(0xFF002733),
    onSurface = Color(0xFFD9D9D9),
)


@Composable
fun AutoPyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

//    val colorScheme = if (darkTheme) {
//        DarkColorScheme // 你自定义的 dark colors
//    } else {
//        LightColorScheme // 你自定义的 light colors
//    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

