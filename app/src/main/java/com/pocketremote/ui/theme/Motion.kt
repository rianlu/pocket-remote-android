package com.pocketremote.ui.theme

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** 系统关闭动画时跳过位移/淡入，避免眩晕。 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        fun scale(key: String): Float {
            return try {
                Settings.Global.getFloat(context.contentResolver, key, 1f)
            } catch (_: Exception) {
                1f
            }
        }
        scale(Settings.Global.ANIMATOR_DURATION_SCALE) == 0f ||
            scale(Settings.Global.TRANSITION_ANIMATION_SCALE) == 0f
    }
}
