package com.chenran.parcel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chenran.parcel.ui.theme.LocalIsDarkTheme
import com.chenran.parcel.util.BackgroundManager
import com.chenran.parcel.util.WallpaperSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperAdjustSheet(
    wallpaperUri: String,
    initialSettings: WallpaperSettings,
    onDismiss: () -> Unit,
    onApply: (WallpaperSettings) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current

    var blurRadius by remember { mutableFloatStateOf(initialSettings.blurRadius) }
    var overlayAlpha by remember { mutableFloatStateOf(initialSettings.overlayAlpha) }
    var gradientAlpha by remember { mutableFloatStateOf(initialSettings.gradientAlpha) }
    var alignmentX by remember { mutableFloatStateOf(initialSettings.alignmentX) }
    var alignmentY by remember { mutableFloatStateOf(initialSettings.alignmentY) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1B4B).copy(alpha = 0.95f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "背景图调整",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(wallpaperUri)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = BiasAlignment(
                        alignmentX,
                        alignmentY
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (blurRadius > 0f) Modifier.blur(blurRadius.dp) else Modifier)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = if (isDark) 0.30f + overlayAlpha else overlayAlpha))
                )
            }

            Spacer(Modifier.height(20.dp))

            SliderRow("模糊度", blurRadius, 0f, 40f) { blurRadius = it }
            SliderRow("遮罩", overlayAlpha, 0f, 0.5f) { overlayAlpha = it }
            SliderRow("渐变透明度", gradientAlpha, 0.1f, 0.8f) { gradientAlpha = it }
            SliderRow("水平位置", alignmentX, -1f, 1f) { alignmentX = it }
            SliderRow("垂直位置", alignmentY, -1f, 1f) { alignmentY = it }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val settings = WallpaperSettings(
                            blurRadius = 0f,
                            overlayAlpha = 0.12f,
                            gradientAlpha = 0.35f,
                            alignmentX = 0f,
                            alignmentY = 0f,
                        )
                        BackgroundManager.setWallpaperSettings(context, settings)
                        onApply(settings)
                    },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("恢复默认", color = Color.White)
                }

                Button(
                    onClick = {
                        val settings = WallpaperSettings(
                            blurRadius = blurRadius,
                            overlayAlpha = overlayAlpha,
                            gradientAlpha = gradientAlpha,
                            alignmentX = alignmentX,
                            alignmentY = alignmentY,
                        )
                        BackgroundManager.setWallpaperSettings(context, settings)
                        onApply(settings)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("应用")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    rangeStart: Float,
    rangeEnd: Float,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            Text(
                text = "%.0f".format(value),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = rangeStart..rangeEnd,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF7C3AED),
                inactiveTrackColor = Color.White.copy(alpha = 0.2f),
            ),
        )
    }
}
