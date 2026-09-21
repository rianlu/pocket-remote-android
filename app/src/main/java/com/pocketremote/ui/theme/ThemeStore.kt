package com.pocketremote.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("pocketremote_theme", Context.MODE_PRIVATE)
    
    private val _useDynamic = MutableStateFlow(loadDynamic())
    private val _seedColor = MutableStateFlow(loadSeed())
    
    val useDynamic: StateFlow<Boolean> = _useDynamic.asStateFlow()
    val seedColor: StateFlow<Color> = _seedColor.asStateFlow()

    fun setDynamic(dynamic: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC, dynamic).apply()
        _useDynamic.value = dynamic
    }

    fun setSeedColor(color: Color) {
        prefs.edit().putInt(KEY_SEED, color.toArgb()).apply()
        _seedColor.value = color
    }

    private fun loadDynamic(): Boolean {
        val def = Build.VERSION.SDK_INT >= 31
        return prefs.getBoolean(KEY_DYNAMIC, def)
    }

    private fun loadSeed(): Color {
        val argb = prefs.getInt(KEY_SEED, 0xFF00E5FF.toInt())
        return Color(argb)
    }

    companion object {
        private const val KEY_DYNAMIC = "dynamic"
        private const val KEY_SEED = "seed"
    }
}
