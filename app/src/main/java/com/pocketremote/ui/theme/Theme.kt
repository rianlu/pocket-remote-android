package com.pocketremote.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
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

/** 全局深色模式：支持 Android 12+ 壁纸取色或任意自定义主题色 */
@Composable
fun PocketRemoteTheme(
    useDynamic: Boolean,
    seedColor: Color,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    
    val colorScheme = if (useDynamic && Build.VERSION.SDK_INT >= 31) {
        dynamicDarkColorScheme(context)
    } else {
        dynamicColorScheme(
            seedColor = seedColor,
            isDark = true,
            isAmoled = false,
            style = PaletteStyle.TonalSpot,
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = MdShapes,
        content = content,
    )
}
