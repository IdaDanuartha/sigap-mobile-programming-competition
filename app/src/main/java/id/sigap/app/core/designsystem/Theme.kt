package id.sigap.app.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LightScheme = lightColorScheme(
    primary = SigapPalette.LightPrimary,
    onPrimary = SigapPalette.LightOnPrimary,
    primaryContainer = SigapPalette.LightPrimaryContainer,
    onPrimaryContainer = SigapPalette.LightOnPrimaryContainer,
    secondary = SigapPalette.LightSecondary,
    secondaryContainer = SigapPalette.LightPrimaryContainer,
    onSecondaryContainer = SigapPalette.LightOnPrimaryContainer,
    tertiary = SigapPalette.LightPrimary,
    tertiaryContainer = SigapPalette.LightPrimaryContainer,
    onTertiaryContainer = SigapPalette.LightOnPrimaryContainer,
    background = SigapPalette.LightBackground,
    onBackground = SigapPalette.LightTextPrimary,
    surface = SigapPalette.LightSurface,
    onSurface = SigapPalette.LightTextPrimary,
    surfaceVariant = SigapPalette.LightSurfaceContainer,
    onSurfaceVariant = SigapPalette.LightTextMuted,
    surfaceContainer = SigapPalette.LightSurfaceContainer,
    outline = SigapPalette.LightOutline,
    outlineVariant = SigapPalette.LightOutline,
    error = LightSigapColors.danger,
    errorContainer = LightSigapColors.dangerContainer,
)

private val DarkScheme = darkColorScheme(
    primary = SigapPalette.DarkPrimary,
    onPrimary = SigapPalette.DarkOnPrimary,
    primaryContainer = SigapPalette.DarkPrimaryContainer,
    onPrimaryContainer = SigapPalette.DarkOnPrimaryContainer,
    secondary = SigapPalette.DarkSecondary,
    secondaryContainer = SigapPalette.DarkPrimaryContainer,
    onSecondaryContainer = SigapPalette.DarkOnPrimaryContainer,
    tertiary = SigapPalette.DarkPrimary,
    tertiaryContainer = SigapPalette.DarkPrimaryContainer,
    onTertiaryContainer = SigapPalette.DarkOnPrimaryContainer,
    background = SigapPalette.DarkBackground,
    onBackground = SigapPalette.DarkTextPrimary,
    surface = SigapPalette.DarkSurface,
    onSurface = SigapPalette.DarkTextPrimary,
    surfaceVariant = SigapPalette.DarkSurfaceContainer,
    onSurfaceVariant = SigapPalette.DarkTextMuted,
    surfaceContainer = SigapPalette.DarkSurfaceContainer,
    outline = SigapPalette.DarkOutline,
    outlineVariant = SigapPalette.DarkOutline,
    error = DarkSigapColors.danger,
    errorContainer = DarkSigapColors.dangerContainer,
)

private val SigapShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val LocalSigapColors = staticCompositionLocalOf { LightSigapColors }

/** Access to the semantic colours outside the Material scheme. */
object SigapTheme {
    val colors: SigapColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSigapColors.current
}

/** App theme. Dynamic colour is intentionally disabled: the brand palette is fixed. */
@Composable
fun SigapTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSigapColors provides if (darkTheme) DarkSigapColors else LightSigapColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = SigapTypography,
            shapes = SigapShapes,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ) {
                content()
            }
        }
    }
}

