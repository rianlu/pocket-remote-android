package com.pocketremote

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import com.pocketremote.ui.PhoneApp
import com.pocketremote.ui.PhoneViewModel
import com.pocketremote.ui.theme.AppearanceMode
import com.pocketremote.ui.theme.LocalThemeStore
import com.pocketremote.ui.theme.PocketRemoteTheme

/** 口袋遥控入口。 */
class MainActivity : ComponentActivity() {
    private val viewModel: PhoneViewModel by viewModels()
    private val nearbyWifi = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) {
            viewModel.note("未授权附近的设备，可点「手动添加」填 IP")
        }
        viewModel.scan()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val store = (application as PocketRemoteApp).themeStore
        setContent {
            val appearance by store.appearance.collectAsState()
            val palette by store.palette.collectAsState()
            val dark = when (appearance) {
                AppearanceMode.System -> isSystemInDarkTheme()
                AppearanceMode.Light -> false
                AppearanceMode.Dark -> true
            }
            EdgeToEdge(dark)
            CompositionLocalProvider(LocalThemeStore provides store) {
                PocketRemoteTheme(appearance = appearance, palette = palette) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        PhoneApp(viewModel)
                    }
                }
            }
        }
        startScan()
    }

    private fun startScan() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            nearbyWifi.launch(Manifest.permission.NEARBY_WIFI_DEVICES)
            return
        }
        viewModel.scan()
    }
}

@Composable
private fun EdgeToEdge(dark: Boolean) {
    val view = LocalView.current
    SideEffect {
        val activity = view.context as ComponentActivity
        activity.enableEdgeToEdge(
            statusBarStyle = if (dark) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            },
            navigationBarStyle = if (dark) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            },
        )
    }
}
