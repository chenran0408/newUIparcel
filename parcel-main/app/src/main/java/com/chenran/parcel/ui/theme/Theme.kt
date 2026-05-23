package com.chenran.parcel.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.chenran.parcel.util.WallpaperSettings

val LocalIsDarkTheme = compositionLocalOf { false }

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC4B5FD),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFA5B4FC),
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = Color(0xFF67E8F9),
    onTertiary = Color(0xFF042F2E),
    tertiaryContainer = Color(0xFF164E63),
    onTertiaryContainer = Color(0xFFCFFAFE),
    error = Color(0xFFFDA4AF),
    onError = Color(0xFF881337),
    errorContainer = Color(0xFF881337),
    onErrorContainer = Color(0xFFFFE4E6),
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
)

val LightGradientBrush = Brush.linearGradient(
    colors = listOf(
        GradientStartLight,
        GradientMidLight,
        GradientEndLight,
    ),
    start = androidx.compose.ui.geometry.Offset(0f, 0f),
    end = androidx.compose.ui.geometry.Offset(1000f, 1800f),
)

val DarkGradientBrush = Brush.linearGradient(
    colors = listOf(
        GradientStartDark,
        GradientMidDark,
        GradientEndDark,
    ),
    start = androidx.compose.ui.geometry.Offset(0f, 0f),
    end = androidx.compose.ui.geometry.Offset(1000f, 1800f),
)

@Composable
fun ParcelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isSeniorMode: Boolean = false,
    homeWallpaperUri: String = "",
    wallpaperSettings: WallpaperSettings = WallpaperSettings(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val backgroundGradient = if (darkTheme) DarkGradientBrush else LightGradientBrush
    val context = LocalContext.current
    val hasWallpaper = homeWallpaperUri.isNotBlank()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(isSeniorMode),
        shapes = Shapes,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (hasWallpaper) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(homeWallpaperUri)
                            .crossfade(300)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = BiasAlignment(
                             wallpaperSettings.alignmentX,
                             wallpaperSettings.alignmentY
                         ),
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (wallpaperSettings.blurRadius > 0f)
                                    Modifier.blur(wallpaperSettings.blurRadius.dp)
                                else Modifier
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = if (darkTheme) 0.30f + wallpaperSettings.overlayAlpha else wallpaperSettings.overlayAlpha))
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (hasWallpaper) {
                                Modifier.background(
                                    brush = backgroundGradient,
                                    alpha = wallpaperSettings.gradientAlpha
                                )
                            } else {
                                Modifier.background(brush = backgroundGradient)
                            }
                        )
                ) {
                    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
                        content()
                    }
                }
            }
        }
    )
}
