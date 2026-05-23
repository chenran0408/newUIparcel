package com.chenran.parcel.util

import android.content.Context
import androidx.core.content.edit

data class WallpaperSettings(
    val blurRadius: Float = 0f,
    val overlayAlpha: Float = 0.12f,
    val gradientAlpha: Float = 0.35f,
    val alignmentX: Float = 0f,
    val alignmentY: Float = 0f,
)

object BackgroundManager {
    private const val PREFS_NAME = "parcel_prefs"
    private const val KEY_HOME_WALLPAPER_URI = "home_wallpaper_uri"
    private const val KEY_WP_BLUR = "wp_blur"
    private const val KEY_WP_OVERLAY = "wp_overlay"
    private const val KEY_WP_GRADIENT = "wp_gradient"
    private const val KEY_WP_ALIGN_X = "wp_align_x"
    private const val KEY_WP_ALIGN_Y = "wp_align_y"

    fun getHomeWallpaperUri(context: Context): String {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_HOME_WALLPAPER_URI, "") ?: ""
        } catch (_: Exception) { "" }
    }

    fun setHomeWallpaperUri(context: Context, uri: String) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit { putString(KEY_HOME_WALLPAPER_URI, uri) }
        } catch (_: Exception) { }
    }

    fun getWallpaperSettings(context: Context): WallpaperSettings {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            WallpaperSettings(
                blurRadius = prefs.getFloat(KEY_WP_BLUR, 0f),
                overlayAlpha = prefs.getFloat(KEY_WP_OVERLAY, 0.12f),
                gradientAlpha = prefs.getFloat(KEY_WP_GRADIENT, 0.35f),
                alignmentX = prefs.getFloat(KEY_WP_ALIGN_X, 0f),
                alignmentY = prefs.getFloat(KEY_WP_ALIGN_Y, 0f),
            )
        } catch (_: Exception) { WallpaperSettings() }
    }

    fun setWallpaperSettings(context: Context, settings: WallpaperSettings) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
                putFloat(KEY_WP_BLUR, settings.blurRadius)
                putFloat(KEY_WP_OVERLAY, settings.overlayAlpha)
                putFloat(KEY_WP_GRADIENT, settings.gradientAlpha)
                putFloat(KEY_WP_ALIGN_X, settings.alignmentX)
                putFloat(KEY_WP_ALIGN_Y, settings.alignmentY)
            }
        } catch (_: Exception) { }
    }

    fun clearHomeWallpaper(context: Context) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
                remove(KEY_HOME_WALLPAPER_URI)
            }
        } catch (_: Exception) { }
    }
}
