package com.pockettv.phone.ui.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pockettv.phone.ui.PhoneViewModel
import com.pockettv.phone.ui.Route
import com.pockettv.phone.ui.UiState

/** 电视已装应用：打开或卸载。 */
@Composable
fun AppsScreen(state: UiState, viewModel: PhoneViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("电视应用", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { viewModel.go(Route.Remote) }) { Text("返回遥控板") }
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.apps, key = { it.pkg }) { app ->
                ListItem(
                    headlineContent = { Text(app.name) },
                    supportingContent = { Text(app.pkg + if (app.system) " · 系统" else "") },
                    trailingContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { viewModel.openApp(app.pkg) }) { Text("打开") }
                            if (!app.system) {
                                TextButton(onClick = { viewModel.uninstallApp(app.pkg) }) { Text("卸载") }
                            }
                        }
                    },
                )
            }
        }
    }
}
