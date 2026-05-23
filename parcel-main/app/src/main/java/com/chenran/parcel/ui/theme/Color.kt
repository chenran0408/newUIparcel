package com.chenran.parcel.ui.theme

import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF7C3AED)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF8B5CF6)
val OnPrimaryContainer = Color(0xFFF5F3FF)

val Secondary = Color(0xFF6366F1)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFE0E7FF)
val OnSecondaryContainer = Color(0xFF312E81)

val Tertiary = Color(0xFF06B6D4)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFCFFAFE)
val OnTertiaryContainer = Color(0xFF164E63)

val Error = Color(0xFFF43F5E)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFE4E6)
val OnErrorContainer = Color(0xFF881337)

val BackgroundLight = Color(0xFFF8FAFC)
val OnBackgroundLight = Color(0xFF1E1B4B)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF1E1B4B)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnSurfaceVariantLight = Color(0xFF475569)
val OutlineLight = Color(0xFFCBD5E1)
val OutlineVariantLight = Color(0xFFE2E8F0)

val BackgroundDark = Color(0xFF0C0A1D)
val OnBackgroundDark = Color(0xFFF1F5F9)
val SurfaceDark = Color(0xFF1E1B4B)
val OnSurfaceDark = Color(0xFFF1F5F9)
val SurfaceVariantDark = Color(0xFF312E81)
val OnSurfaceVariantDark = Color(0xFFC7D2FE)
val OutlineDark = Color(0xFF475569)
val OutlineVariantDark = Color(0xFF334155)

val SuccessGreen = Color(0xFF34D399)
val FailPink = Color(0xFFF472B6)

val GradientStartLight = Color(0xFF667EEA)
val GradientMidLight = Color(0xFF764BA2)
val GradientEndLight = Color(0xFF06B6D4)

val GradientStartDark = Color(0xFF1E1B4B)
val GradientMidDark = Color(0xFF0F172A)
val GradientEndDark = Color(0xFF042F2E)

val PickupAmber = Color(0xFFFBBF24)
val PickupBlue = Color(0xFF60A5FA)
val PickupCyan = Color(0xFF22D3EE)
val PickupEmerald = Color(0xFF34D399)
val PickupFuchsia = Color(0xFFE879F9)
val PickupIndigo = Color(0xFF818CF8)
val PickupOrange = Color(0xFFFB923C)
val PickupPink = Color(0xFFF472B6)
val PickupRed = Color(0xFFF87171)
val PickupRose = Color(0xFFFB7185)
val PickupTeal = Color(0xFF2DD4BF)
val PickupViolet = Color(0xFFA78BFA)

private val PickupColorPalette = listOf(
    PickupBlue,
    PickupEmerald,
    PickupAmber,
    PickupFuchsia,
    PickupCyan,
    PickupOrange,
    PickupViolet,
    PickupTeal,
    PickupRose,
    PickupIndigo,
    PickupPink,
    PickupRed,
)

fun pickupCodeColor(code: String): Color {
    if (code.isBlank()) return PickupBlue
    val hash = code.hashCode().let { if (it < 0) -it else it }
    return PickupColorPalette[hash % PickupColorPalette.size]
}

fun pickupCodeBackgroundColor(code: String): Color {
    val base = pickupCodeColor(code)
    return base.copy(alpha = 0.15f)
}

private val PickupDarkColorPalette = listOf(
    Color(0xFF93C5FD),
    Color(0xFF6EE7B7),
    Color(0xFFFDE047),
    Color(0xFFF0ABFC),
    Color(0xFF67E8F9),
    Color(0xFFFDBA74),
    Color(0xFFC4B5FD),
    Color(0xFF5EEAD4),
    Color(0xFFFDA4AF),
    Color(0xFFA5B4FC),
    Color(0xFFF9A8D4),
    Color(0xFFFCA5A5),
)

fun pickupCodeColorDark(code: String): Color {
    if (code.isBlank()) return PickupDarkColorPalette[0]
    val hash = code.hashCode().let { if (it < 0) -it else it }
    return PickupDarkColorPalette[hash % PickupDarkColorPalette.size]
}

fun pickupCodeBackgroundColorDark(code: String): Color {
    val base = pickupCodeColorDark(code)
    return base.copy(alpha = 0.2f)
}
