package com.pocketremote.ui.theme

import android.content.Context
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 深浅色与主题色偏好。 */
enum class AppearanceMode { System, Light, Dark }

/** 系统壁纸取色，或固定种子色。 */
enum class PaletteKey {
    Dynamic,
    Blue,
    Green,
    Orange,
    Purple,
    Teal,
}

/** 持久化外观：跟随系统 / 深浅色 / 动态色或自选种子。 */
class ThemeStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("pocketremote_theme", Context.MODE_PRIVATE)
    private val _appearance = MutableStateFlow(loadAppearance())
    private val _palette = MutableStateFlow(loadPalette())
    val appearance: StateFlow<AppearanceMode> = _appearance.asStateFlow()
    val palette: StateFlow<PaletteKey> = _palette.asStateFlow()

    fun setAppearance(mode: AppearanceMode) {
        prefs.edit().putString(KEY_APPEARANCE, mode.name).apply()
        _appearance.value = mode
    }

    fun setPalette(key: PaletteKey) {
        prefs.edit().putString(KEY_PALETTE, key.name).apply()
        _palette.value = key
    }

    private fun loadAppearance(): AppearanceMode {
        return runCatching { AppearanceMode.valueOf(prefs.getString(KEY_APPEARANCE, "") ?: "") }
            .getOrDefault(AppearanceMode.System)
    }

    private fun loadPalette(): PaletteKey {
        val raw = prefs.getString(KEY_PALETTE, null)
        if (raw != null) {
            return runCatching { PaletteKey.valueOf(raw) }.getOrDefault(defaultPalette())
        }
        return defaultPalette()
    }

    private fun defaultPalette(): PaletteKey {
        return if (Build.VERSION.SDK_INT >= 31) PaletteKey.Dynamic else PaletteKey.Blue
    }

    companion object {
        private const val KEY_APPEARANCE = "appearance"
        private const val KEY_PALETTE = "palette"
    }
}
