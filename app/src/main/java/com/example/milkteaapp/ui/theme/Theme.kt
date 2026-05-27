package com.example.milkteaapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light Color Scheme ────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary              = Primary,
    onPrimary            = OnPrimary,
    primaryContainer     = PrimaryContainer,
    onPrimaryContainer   = OnPrimaryContainer,

    secondary            = Secondary,
    onSecondary          = OnSecondary,
    secondaryContainer   = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary             = Tertiary,
    onTertiary           = OnTertiary,
    tertiaryContainer    = TertiaryContainer,
    onTertiaryContainer  = OnTertiaryContainer,

    background           = Background,
    onBackground         = OnBackground,
    surface              = Surface,
    onSurface            = OnSurface,
    surfaceVariant       = SurfaceVariant,
    onSurfaceVariant     = OnSurfaceVariant,

    error                = Error,
    onError              = OnError,
    errorContainer       = ErrorContainer,
    onErrorContainer     = OnErrorContainer,

    outline              = Outline,
    outlineVariant       = OutlineVariant
)

// ── Dark Color Scheme (tối giản – dùng invert của palette nâu) ───────────────

private val DarkColorScheme = darkColorScheme(
    primary              = MauKem,
    onPrimary            = MauNauDam,
    primaryContainer     = MauNau,
    onPrimaryContainer   = MauNauNhat,

    secondary            = Color(0xFF80CBC4),
    onSecondary          = MauNauDam,
    secondaryContainer   = MauXanh,
    onSecondaryContainer = Color.White,

    tertiary             = MauVang,
    onTertiary           = MauNauDam,

    background           = Color(0xFF1C1410),
    onBackground         = MauNauNhat,
    surface              = Color(0xFF2A1F1B),
    onSurface            = MauNauNhat,
    surfaceVariant       = Color(0xFF3E2E29),
    onSurfaceVariant     = MauKem,

    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),

    outline              = Color(0xFF8D6E63),
    outlineVariant       = Color(0xFF5D4037)
)

// ── MilkteaappTheme ───────────────────────────────────────────────────────────

/**
 * Theme chính của app Trà Sữa NL.
 *
 * - Dynamic color bị tắt để giữ đúng palette nâu thương hiệu
 * - Status bar được tô màu [MauNauDam] để đồng bộ với header các màn hình
 */
@Composable
fun MilkteaappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Tô màu status bar khớp với header
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = MauNauDam.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}