package com.pockettv.phone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Surface = Color(0xFF0B1326)
private val OnSurface = Color(0xFFDAE2FD)
private val Primary = Color(0xFF2665FD)
private val Muted = Color(0xFF9AA4C7)

private val Scheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    background = Surface,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Color(0xFF15203A),
    onSurfaceVariant = Muted,
    secondary = Muted,
)

/** 深色遥控风格，与电视端主色一致。 */
@Composable
fun PocketTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
