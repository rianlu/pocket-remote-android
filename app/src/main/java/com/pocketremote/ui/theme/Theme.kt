package com.pocketremote.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme

val LocalThemeStore = staticCompositionLocalOf<ThemeStore> {
    error("ThemeStore 未提供")
}

private val MdShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

fun PaletteKey.swatchColor(): Color {
    return when (this) {
        PaletteKey.Dynamic -> Color(0xFF5F6368)
        PaletteKey.Blue -> Color(0xFF1A73E8)
        PaletteKey.Green -> Color(0xFF188038)
        PaletteKey.Orange -> Color(0xFFE8710A)
        PaletteKey.Purple -> Color(0xFF6750A4)
        PaletteKey.Teal -> Color(0xFF00897B)
    }
}

private fun PaletteKey.seedColor(): Color {
    return if (this == PaletteKey.Dynamic) PaletteKey.Blue.swatchColor() else swatchColor()
}

private fun seedScheme(key: PaletteKey, dark: Boolean): ColorScheme {
    return dynamicColorScheme(
        seedColor = key.seedColor(),
        isDark = dark,
        isAmoled = false,
        style = PaletteStyle.TonalSpot,
    )
}

/** Material 3：跟随系统深浅色、壁纸动态色、或自选种子色。 */
@Composable
fun PocketRemoteTheme(
    appearance: AppearanceMode,
    palette: PaletteKey,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (appearance) {
        AppearanceMode.System -> systemDark
        AppearanceMode.Light -> false
        AppearanceMode.Dark -> true
    }
    val context = LocalContext.current
    val colorScheme = if (palette == PaletteKey.Dynamic && Build.VERSION.SDK_INT >= 31) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        seedScheme(if (palette == PaletteKey.Dynamic) PaletteKey.Blue else palette, dark)
    }
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = MdShapes,
        content = content,
    )
}

fun PaletteKey.label(): String {
    return when (this) {
        PaletteKey.Dynamic -> "壁纸取色"
        PaletteKey.Blue -> "蓝"
        PaletteKey.Green -> "绿"
        PaletteKey.Orange -> "橙"
        PaletteKey.Purple -> "紫"
        PaletteKey.Teal -> "青"
    }
}

fun AppearanceMode.label(): String {
    return when (this) {
        AppearanceMode.System -> "跟随系统"
        AppearanceMode.Light -> "浅色"
        AppearanceMode.Dark -> "深色"
    }
}
