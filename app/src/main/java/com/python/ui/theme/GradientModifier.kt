package com.python.ui.theme

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.autoPyGradientBackground(): Modifier {
    val isDark = isSystemInDarkTheme()
    val colors = if (isDark) {
        listOf(Color(0xFF00141A), Color(0xFF00141A))
    } else {
        listOf(Color(0xFFE5FFFF), Color(0xFFBBDEFB))
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val density = LocalDensity.current
    val widthPx = with(density) { screenWidth.toPx() }
    val heightPx = with(density) { screenHeight.toPx() }

    val brush = remember(colors, widthPx, heightPx) {
        Brush.linearGradient(
            colors = colors,
            start = Offset(0f, 0f),
            end = Offset(widthPx, heightPx)
        )
    }

    return this.background(brush)
}
