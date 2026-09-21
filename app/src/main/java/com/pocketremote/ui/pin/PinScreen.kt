package com.pocketremote.ui.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketremote.ui.PhoneViewModel
import com.pocketremote.ui.UiState
import com.pocketremote.ui.components.AppScaffold

/** 输入电视上的 6 位码，满 6 位自动提交。 */
@Composable
fun PinScreen(state: UiState, viewModel: PhoneViewModel) {
    var pin by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    LaunchedEffect(state.pinNonce) { submitted = "" }
    LaunchedEffect(pin) {
        if (pin.length == 6 && pin != submitted) {
            submitted = pin
            viewModel.submitPin(pin)
        }
    }

    AppScaffold(
        title = "输入配对码",
        navigationIcon = {
            IconButton(onClick = { viewModel.disconnectToList() }) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
            }
        },
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            if (state.message.isNotBlank()) state.message else "查看电视屏幕上显示的数字",
            style = MaterialTheme.typography.bodyMedium,
            color = if (state.message.contains("错误") || state.message.contains("失败"))
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(40.dp))

        BasicTextField(
            value = pin,
            onValueChange = { pin = it.filter { c -> c.isDigit() }.take(6) },
            modifier = Modifier.fillMaxWidth().focusRequester(focus),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(fontSize = 1.sp, color = MaterialTheme.colorScheme.surface),
            decorationBox = {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(6) { index ->
                        val ch = pin.getOrNull(index)?.toString().orEmpty()
                        val active = index == pin.length
                        val filled = ch.isNotEmpty()

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .shadow(
                                    if (active) 6.dp else 2.dp,
                                    RoundedCornerShape(14.dp)
                                )
                                .background(
                                    if (filled) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerLow,
                                    RoundedCornerShape(14.dp)
                                )
                                .border(
                                    width = if (active) 2.dp else 1.dp,
                                    color = if (active) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                        ) {
                            Text(
                                if (filled) ch else if (active) "│" else "·",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = if (filled) FontWeight.Bold else FontWeight.Normal,
                                color = if (filled) MaterialTheme.colorScheme.onPrimaryContainer
                                        else if (active) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            },
        )

        Spacer(Modifier.height(16.dp))
        Text(
            "满 6 位自动提交",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
