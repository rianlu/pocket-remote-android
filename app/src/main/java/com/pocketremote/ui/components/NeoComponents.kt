package com.pocketremote.ui.components

import androidx.compose.runtime.CompositionLocalProvider
import com.pocketremote.ui.theme.rememberReducedMotion
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─── Core Design Tokens ──────────────────────────────────────────────────────

/** 新拟物暗色基底 - 始终用 alpha 叠加，不覆盖主题色 */
@Composable
fun neoSurfaceColor() = MaterialTheme.colorScheme.surfaceContainerLow

@Composable
fun neoElevatedColor() = MaterialTheme.colorScheme.surfaceContainer

@Composable
fun neoHighColor() = MaterialTheme.colorScheme.surfaceContainerHigh

// ─── NeoButton ───────────────────────────────────────────────────────────────

/**
 * 带按压凹陷感的新拟物按钮。
 * 颜色跟随 MaterialTheme.colorScheme，始终与主题兼容。
 */
@Composable
fun NeoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color? = null,
    contentColor: Color? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current
    val reduce = rememberReducedMotion()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = if (reduce) snap() else spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "btnScale"
    )

    val defaultSurface = MaterialTheme.colorScheme.surfaceContainer
    val defaultSurfaceHigh = MaterialTheme.colorScheme.surfaceContainerHigh
    val outline = MaterialTheme.colorScheme.outlineVariant
    
    val bgNorm = containerColor ?: defaultSurface
    val bgPress = containerColor ?: defaultSurfaceHigh

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale; alpha = if (enabled) 1f else 0.38f }
            .shadow(if (isPressed) 2.dp else 6.dp, shape)
            .background(if (isPressed) bgPress else bgNorm, shape)
            .border(1.dp, outline.copy(alpha = 0.3f), shape)
            .then(
                if (enabled) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onClick()
                    }
                ) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (contentColor != null) {
                CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides contentColor) {
                    content()
                }
            } else {
                content()
            }
        }
    }
}

// ─── NeoIconButton ────────────────────────────────────────────────────────────

@Composable
fun NeoIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    shape: Shape = RoundedCornerShape(18.dp),
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    NeoButton(onClick = onClick, modifier = modifier.size(size), shape = shape, enabled = enabled) {
        content()
    }
}

// ─── NeoCard ─────────────────────────────────────────────────────────────────

/**
 * 新拟物分组面板，完全使用 colorScheme token。
 */
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val surface = MaterialTheme.colorScheme.surfaceContainerLow
    val outline = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier
            .shadow(4.dp, shape)
            .background(surface, shape)
            .border(1.dp, outline.copy(alpha = 0.4f), shape)
    ) {
        content()
    }
}

// ─── NeoSettingsGroup ─────────────────────────────────────────────────────────

/**
 * 替代 AppleChrome 的 SettingsGroup - 使用主题色、带分割线
 */
@Composable
fun NeoSettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    NeoCard(modifier = modifier.fillMaxWidth(), content = content)
}

// ─── NeoSettingsItem ──────────────────────────────────────────────────────────

@Composable
fun NeoSettingsItem(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    trailingText: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconTint: Color? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val view = LocalView.current
    val clickMod = if (onClick != null) {
        Modifier.clickable {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onClick()
        }
    } else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickMod)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (leadingIcon != null) {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = leadingIconTint ?: MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                headline,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (supporting != null) {
                Text(
                    supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trailingText != null) {
            Text(
                trailingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (trailing != null) trailing()
    }
}

// ─── NeoGroupDivider ──────────────────────────────────────────────────────────

@Composable
fun NeoGroupDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 0.5.dp
    )
}

// ─── NeoSectionLabel ─────────────────────────────────────────────────────────

@Composable
fun NeoSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 4.dp, top = 20.dp, bottom = 8.dp)
    )
}

// ─── NeoItemPanel ────────────────────────────────────────────────────────────

/**
 * 较小的可点击面板，用于列表行/操作行。
 */
@Composable
fun NeoItemPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val surface = MaterialTheme.colorScheme.surfaceContainerLow
    val outline = MaterialTheme.colorScheme.outlineVariant
    val clickMod = if (onClick != null) Modifier.clickable {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        onClick()
    } else Modifier

    Box(
        modifier = modifier
            .shadow(3.dp, shape)
            .background(surface, shape)
            .border(1.dp, outline.copy(alpha = 0.3f), shape)
            .then(clickMod)
            .padding(16.dp)
    ) {
        content()
    }
}

// ─── NeoIconBadge ────────────────────────────────────────────────────────────

/**
 * 图标徽章 - 带颜色背景的圆形图标容器
 */
@Composable
fun NeoIconBadge(
    icon: ImageVector,
    tint: Color,
    background: Color,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}
