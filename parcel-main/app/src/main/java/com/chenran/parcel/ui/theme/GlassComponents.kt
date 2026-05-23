package com.chenran.parcel.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

data class GlassStyle(
    val containerAlpha: Float,
    val borderAlpha: Float,
    val highlightAlpha: Float,
    val shadowAlpha: Float,
)

object GlassStyles {
    val CardBlur = GlassStyle(
        containerAlpha = 0.65f,
        borderAlpha = 0.15f,
        highlightAlpha = 0.08f,
        shadowAlpha = 0.06f,
    )
    val PillBlur = GlassStyle(
        containerAlpha = 0.58f,
        borderAlpha = 0.14f,
        highlightAlpha = 0.08f,
        shadowAlpha = 0.0f,
    )
    val ChipBlur = GlassStyle(
        containerAlpha = 0.50f,
        borderAlpha = 0.12f,
        highlightAlpha = 0.06f,
        shadowAlpha = 0.0f,
    )
    val SheetBlur = GlassStyle(
        containerAlpha = 0.85f,
        borderAlpha = 0.10f,
        highlightAlpha = 0.06f,
        shadowAlpha = 0.08f,
    )
}

@Composable
fun glassBaseColor(): Color {
    return if (LocalIsDarkTheme.current) Color(0xFF1E1B4B) else Color.White
}

@Composable
fun glassOnCardColor(): Color {
    return if (LocalIsDarkTheme.current) Color(0xFFF1F5F9) else Color(0xFF1E1B4B)
}

@Composable
fun glassOnCardVariantColor(): Color {
    return if (LocalIsDarkTheme.current) Color(0xFFC7D2FE) else Color(0xFF475569)
}

@Composable
fun glassSheetColor(): Color {
    val base = glassBaseColor()
    return base.copy(alpha = GlassStyles.SheetBlur.containerAlpha)
}

@Composable
fun glassButtonColor(): Color {
    val base = glassBaseColor()
    return base.copy(alpha = 0.55f)
}

@Composable
fun glassChipColor(): Color {
    val base = glassBaseColor()
    return base.copy(alpha = 0.50f)
}

private fun Modifier.glassLayers(
    baseColor: Color,
    style: GlassStyle,
    shape: Shape,
): Modifier {
    val containerColor = baseColor.copy(alpha = style.containerAlpha)
    val borderColor = Color.White.copy(alpha = style.borderAlpha)
    val highlightColor = Color.White.copy(alpha = style.highlightAlpha)

    return this
        .clip(shape)
        .background(containerColor)
        .border(1.dp, borderColor, shape)
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    highlightColor,
                    Color.Transparent,
                ),
                startY = 0f,
                endY = 80f,
            ),
            shape = shape,
        )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    style: GlassStyle = GlassStyles.CardBlur,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val baseColor = glassBaseColor()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassLayers(baseColor, style, shape)
    ) {
        ColumnScopeContent(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            content = content,
        )
    }
}

@Composable
private fun ColumnScopeContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    androidx.compose.foundation.layout.Column(modifier = modifier, content = content)
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    color: Color = Color.Unspecified,
    borderAlpha: Float = 0.15f,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    style: GlassStyle = GlassStyles.PillBlur,
    content: @Composable () -> Unit,
) {
    val resolvedColor = if (color != Color.Unspecified) color else {
        val base = glassBaseColor()
        base.copy(alpha = style.containerAlpha)
    }
    val borderColor = Color.White.copy(alpha = borderAlpha)

    if (onClick != null) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = resolvedColor,
            border = BorderStroke(0.5.dp, borderColor),
            onClick = onClick,
            enabled = enabled,
            content = content,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = resolvedColor,
            border = BorderStroke(0.5.dp, borderColor),
            content = content,
        )
    }
}

@Composable
fun GlassChip(
    modifier: Modifier = Modifier,
    style: GlassStyle = GlassStyles.ChipBlur,
    content: @Composable () -> Unit,
) {
    val base = glassBaseColor()
    val containerColor = base.copy(alpha = style.containerAlpha)
    val borderColor = Color.White.copy(alpha = style.borderAlpha)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(0.5.dp, borderColor),
        content = content,
    )
}
