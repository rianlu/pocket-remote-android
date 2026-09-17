package com.pockettv.phone.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pockettv.phone.ui.apps.AppsScreen
import com.pockettv.phone.ui.devices.DevicesScreen
import com.pockettv.phone.ui.files.FilesScreen
import com.pockettv.phone.ui.keyboard.KeyboardScreen
import com.pockettv.phone.ui.pin.PinScreen
import com.pockettv.phone.ui.remote.RemoteScreen

/** 按 ViewModel.route 切换页面。 */
@Composable
fun PhoneApp(viewModel: PhoneViewModel) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    when (state.route) {
        Route.Devices -> DevicesScreen(state, viewModel)
        Route.Pin -> PinScreen(state, viewModel)
        Route.Remote -> RemoteScreen(state, viewModel)
        Route.Keyboard -> KeyboardScreen(state, viewModel)
        Route.Files -> FilesScreen(state, viewModel)
        Route.Apps -> AppsScreen(state, viewModel)
    }
}
