package en.entouche.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

// Dark Color Scheme - Primary theme for Entouche
private val DarkColorScheme = darkColorScheme(
    // Primary colors
    primary = TealWave,
    onPrimary = TextOnTeal,
    primaryContainer = OceanBlue,
    onPrimaryContainer = AquaMist,

    // Secondary colors
    secondary = Seafoam,
    onSecondary = DeepOcean,
    secondaryContainer = TealDeep,
    onSecondaryContainer = AquaMist,

    // Tertiary colors
    tertiary = AquaMist,
    onTertiary = DeepOcean,
    tertiaryContainer = TealDark,
    onTertiaryContainer = PaleTeal,

    // Background and surface
    background = DeepSurface,
    onBackground = TextPrimary,
    surface = GlassSurface,
    onSurface = TextPrimary,
    surfaceVariant = FrostedGlass,
    onSurfaceVariant = TextSecondary,

    // Container colors
    surfaceContainerLowest = DeepOcean,
    surfaceContainerLow = DeepSurface,
    surfaceContainer = GlassSurface,
    surfaceContainerHigh = FrostedGlass,
    surfaceContainerHighest = SlateGray,

    // Error colors
    error = Error,
    onError = SoftWhite,
    errorContainer = ErrorContainer,
    onErrorContainer = Error,

    // Other
    outline = MutedSlate,
    outlineVariant = SlateGray,
    inverseSurface = SoftWhite,
    inverseOnSurface = DeepOcean,
    inversePrimary = TealDeep,
    scrim = DeepOcean
)

// Light Color Scheme - Alternative theme
private val LightColorScheme = lightColorScheme(
    // Primary colors
    primary = TealDeep,
    onPrimary = SoftWhite,
    primaryContainer = PaleTeal,
    onPrimaryContainer = DeepOcean,

    // Secondary colors
    secondary = TealDark,
    onSecondary = SoftWhite,
    secondaryContainer = AquaMist,
    onSecondaryContainer = DeepOcean,

    // Tertiary colors
    tertiary = OceanBlue,
    onTertiary = SoftWhite,
    tertiaryContainer = LightSlate,
    onTertiaryContainer = DeepOcean,

    // Background and surface
    background = LightMist,
    onBackground = TextPrimaryLight,
    surface = SoftWhite,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSlate,
    onSurfaceVariant = TextSecondaryLight,

    // Container colors
    surfaceContainerLowest = SoftWhite,
    surfaceContainerLow = LightMist,
    surfaceContainer = PaleTeal,
    surfaceContainerHigh = LightSlate,
    surfaceContainerHighest = MutedSlate,

    // Error colors
    error = Error,
    onError = SoftWhite,
    errorContainer = Error.copy(alpha = 0.1f),
    onErrorContainer = ErrorContainer,

    // Other
    outline = MutedSlate,
    outlineVariant = LightSlate,
    inverseSurface = DeepSurface,
    inverseOnSurface = TextPrimary,
    inversePrimary = TealWave,
    scrim = DeepOcean
)

// Extended colors for glassmorphism and custom UI elements
data class ExtendedColors(
    val glassBackground: androidx.compose.ui.graphics.Color,
    val glassBorder: androidx.compose.ui.graphics.Color,
    val glassShadow: androidx.compose.ui.graphics.Color,
    val gradientStart: androidx.compose.ui.graphics.Color,
    val gradientMid: androidx.compose.ui.graphics.Color,
    val gradientEnd: androidx.compose.ui.graphics.Color,
    val success: androidx.compose.ui.graphics.Color,
    val successContainer: androidx.compose.ui.graphics.Color,
    val warning: androidx.compose.ui.graphics.Color,
    val warningContainer: androidx.compose.ui.graphics.Color,
    val info: androidx.compose.ui.graphics.Color,
    val infoContainer: androidx.compose.ui.graphics.Color,
    val textMuted: androidx.compose.ui.graphics.Color
)

private val DarkExtendedColors = ExtendedColors(
    glassBackground = GlassWhite,
    glassBorder = GlassBorder,
    glassShadow = GlassShadow,
    gradientStart = GradientStart,
    gradientMid = GradientMid,
    gradientEnd = GradientEnd,
    success = Success,
    successContainer = SuccessContainer,
    warning = Warning,
    warningContainer = WarningContainer,
    info = Info,
    infoContainer = InfoContainer,
    textMuted = TextMuted
)

private val LightExtendedColors = ExtendedColors(
    glassBackground = GlassSurface.copy(alpha = 0.6f),
    glassBorder = SlateGray.copy(alpha = 0.2f),
    glassShadow = OceanBlue.copy(alpha = 0.1f),
    gradientStart = LightMist,
    gradientMid = PaleTeal,
    gradientEnd = AquaMist.copy(alpha = 0.5f),
    success = Success,
    successContainer = Success.copy(alpha = 0.1f),
    warning = Warning,
    warningContainer = Warning.copy(alpha = 0.1f),
    info = Info,
    infoContainer = Info.copy(alpha = 0.1f),
    textMuted = TextMutedLight
)

val LocalExtendedColors = staticCompositionLocalOf { DarkExtendedColors }

object EntoucheTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}

@Composable
fun EntoucheTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = EntoucheTypography,
            shapes = EntoucheShapes,
            content = content
        )
    }
}
