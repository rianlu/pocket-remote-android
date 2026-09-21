package com.pocketremote.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp

/**
 * 共享设置组件 - 使用 NeoComponents 实现，保持向后兼容。
 */

@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) = NeoSettingsGroup(modifier, content)

@Composable
fun SettingsItem(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    trailingText: String? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) = NeoSettingsItem(
    headline = headline,
    modifier = modifier,
    supporting = supporting,
    trailingText = trailingText,
    trailing = trailing ?: leading,
    onClick = onClick,
)

@Composable
fun GroupDivider() = NeoGroupDivider()

@Composable
fun SectionLabel(text: String) = NeoSectionLabel(text)

@Composable
fun CapsuleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: CapsuleTone = CapsuleTone.Primary,
) {
    NeoButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(
            text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

enum class CapsuleTone { Primary, Secondary }
