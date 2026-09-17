package com.pockettv.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pockettv.phone.ui.PhoneApp
import com.pockettv.phone.ui.PhoneViewModel
import com.pockettv.phone.ui.theme.PocketTvTheme

/** 口袋遥控入口。 */
class MainActivity : ComponentActivity() {
    private val viewModel: PhoneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PocketTvTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PhoneApp(viewModel)
                }
            }
        }
        viewModel.scan()
    }
}
