package com.imageforge.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imageforge.app.ui.theme.*

/**
 * Custom Modifier for Forest Liquid Glassmorphism styling.
 * Combines translucent background, dual-gradient specular borders, and soft shadows.
 */
fun Modifier.glass(
    cornerRadius: Dp = 16.dp,
    bgAlpha: Float = 0.22f,
    borderAlpha: Float = 0.35f,
    shadowElevation: Dp = 8.dp
): Modifier = this
    .shadow(
        elevation = shadowElevation,
        shape = RoundedCornerShape(cornerRadius),
        clip = false,
        ambientColor = BlackTranslucent.copy(alpha = 0.3f),
        spotColor = BlackTranslucent.copy(alpha = 0.5f)
    )
    .drawBehind {
        // Draw physical glass background gradient
        val glassBrush = Brush.linearGradient(
            colors = listOf(
                GlassBg.copy(alpha = bgAlpha),
                GlassBgSecondary.copy(alpha = bgAlpha * 0.7f)
            ),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )
        drawRoundRect(
            brush = glassBrush,
            size = size,
            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
        )

        // Draw dynamic inner glow highlights
        val innerGlowBrush = Brush.radialGradient(
            colors = listOf(
                WhiteTranslucent.copy(alpha = 0.05f),
                Color.Transparent
            ),
            center = Offset(0f, 0f),
            radius = size.width * 0.5f
        )
        drawRoundRect(
            brush = innerGlowBrush,
            size = size,
            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
        )
    }
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                GlassBorderLight.copy(alpha = borderAlpha),
                GlassBorderDark.copy(alpha = borderAlpha * 0.4f),
                Color.Transparent,
                WhiteGlassBorder.copy(alpha = 0.05f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
    .clip(RoundedCornerShape(cornerRadius))

/**
 * GlassCard: Standard container representing forest glass surface
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    bgAlpha: Float = 0.22f,
    borderAlpha: Float = 0.35f,
    shadowElevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.glass(cornerRadius, bgAlpha, borderAlpha, shadowElevation),
        content = content
    )
}

/**
 * GlassSegmentedControl: Bottom segmented control with sliding liquid green pill indicator
 */
@Composable
fun GlassSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .glass(cornerRadius = 24.dp, bgAlpha = 0.3f, borderAlpha = 0.4f)
            .height(52.dp)
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidth = maxWidth
        val tabCount = items.size
        val tabWidth = totalWidth / tabCount

        // Slide animation for selected tab pill
        val offsetDp by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = tween(durationMillis = 350),
            label = "LiquidTabSlide"
        )

        // Glow indicator pulsing state
        val pulseAlpha by animateFloatAsState(
            targetValue = 0.85f,
            animationSpec = tween(durationMillis = 500),
            label = "TabPillPulse"
        )

        // Floating jelly pill background
        Box(
            modifier = Modifier
                .offset(x = offsetDp)
                .width(tabWidth)
                .fillMaxHeight()
                .padding(2.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = EmeraldGlow.copy(alpha = 0.5f)
                )
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            EmeraldGlow.copy(alpha = 0.75f),
                            TealMist.copy(alpha = 0.6f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(100f, 100f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.dp,
                    color = LimeAurora.copy(alpha = pulseAlpha),
                    shape = RoundedCornerShape(20.dp)
                )
        )

        // Text Row
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, title ->
                val isSelected = index == selectedIndex
                val textColor = if (isSelected) ForestVoid else WhiteTranslucent
                val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(
                                bounded = true,
                                color = EmeraldGlow
                            ),
                            onClick = { onItemSelected(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = textColor,
                        fontWeight = fontWeight,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Reusable floating vertical GlassToolbar representing controls
 */
@Composable
fun GlassToolbar(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .glass(cornerRadius = 24.dp, bgAlpha = 0.28f, borderAlpha = 0.45f)
            .width(58.dp)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

/**
 * Premium glass toolbar action button matching 48dp guidelines
 */
@Composable
fun GlassToolbarButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null
) {
    Box(
        modifier = modifier
            .size(48.dp) // Exact minimum target requirements
            .clip(CircleShape)
            .background(
                color = if (isActive) EmeraldGlow.copy(alpha = 0.25f) else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = if (isActive) LimeAurora.copy(alpha = 0.6f) else Color.Transparent,
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false, radius = 24.dp, color = EmeraldGlow),
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) LimeAurora else WhiteTranslucent.copy(alpha = 0.8f),
            modifier = Modifier.size(22.dp)
        )

        // Floating active indicator light or badge
        if (isActive && badgeText == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .size(6.dp)
                    .background(LimeAurora, CircleShape)
                    .shadow(4.dp, CircleShape, spotColor = LimeAurora)
            )
        }

        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 4.dp, end = 4.dp)
                    .background(EmeraldGlow, RoundedCornerShape(4.dp))
                    .padding(horizontal = 3.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    color = ForestVoid,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
