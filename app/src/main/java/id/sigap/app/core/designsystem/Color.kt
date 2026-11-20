package id.sigap.app.core.designsystem

import androidx.compose.ui.graphics.Color

/** Brand and risk colour tokens — Editorial Bento System (Light & Dark designed together). */
internal object SigapPalette {
    val LightBackground = Color(0xFFF5F1EA)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceContainer = Color(0xFFF7F4EE)
    val LightOutline = Color(0xFFDDD7CC)
    val LightPrimary = Color(0xFFC85A32)
    val LightOnPrimary = Color(0xFFFFFFFF)
    val LightPrimaryContainer = Color(0xFFF6E6DF)
    val LightOnPrimaryContainer = Color(0xFF8A3314)
    val LightSecondary = Color(0xFF3B424F)
    val LightTextPrimary = Color(0xFF14181F)
    val LightTextMuted = Color(0xFF3B424F)

    val DarkBackground = Color(0xFF10141B)
    val DarkSurface = Color(0xFF181E28)
    val DarkSurfaceContainer = Color(0xFF1B222E)
    val DarkOutline = Color(0xFF28303E)
    val DarkPrimary = Color(0xFFE57A52)
    val DarkOnPrimary = Color(0xFFFFFFFF)
    val DarkPrimaryContainer = Color(0xFF261B18)
    val DarkOnPrimaryContainer = Color(0xFFF59D7C)
    val DarkSecondary = Color(0xFFBDC6D6)
    val DarkTextPrimary = Color(0xFFF0F3F7)
    val DarkTextMuted = Color(0xFFBDC6D6)
}

/** Semantic colours that Material 3 does not model: risk scale, dominant bento surface, and muted text. */
data class SigapColors(
    val textMuted: Color,
    val safe: Color,
    val safeContainer: Color,
    val caution: Color,
    val cautionContainer: Color,
    val onCaution: Color,
    val danger: Color,
    val dangerContainer: Color,
    val dominantSurface: Color,
    val onDominant: Color,
    val onDominantMuted: Color,
    val dangerRowTint: Color,
    val cautionRowTint: Color,
    val attentionCardBg: Color,
)

internal val LightSigapColors = SigapColors(
    textMuted = SigapPalette.LightTextMuted,
    safe = Color(0xFF1E7A4C),
    safeContainer = Color(0xFFD8F0E3),
    caution = Color(0xFFD98218),
    cautionContainer = Color(0xFFFEF4E6),
    onCaution = Color(0xFF8A4D04),
    danger = Color(0xFFE5484D),
    dangerContainer = Color(0xFFFDECEE),
    dominantSurface = Color(0xFF14181F),
    onDominant = Color(0xFFF7F5F0),
    onDominantMuted = Color(0xFFD1D7E2),
    dangerRowTint = Color(0xFFFDF2F1),
    cautionRowTint = Color(0xFFFEF7EC),
    attentionCardBg = Color(0xFFFCECD8),
)

internal val DarkSigapColors = SigapColors(
    textMuted = SigapPalette.DarkTextMuted,
    safe = Color(0xFF4FC084),
    safeContainer = Color(0xFF123828),
    caution = Color(0xFFF5B04C),
    cautionContainer = Color(0xFF2E2212),
    onCaution = Color(0xFFFFE2B5),
    danger = Color(0xFFFF6B6F),
    dangerContainer = Color(0xFF32191D),
    dominantSurface = Color(0xFF34221D),
    onDominant = Color(0xFFFAF6F3),
    onDominantMuted = Color(0xFFE6CFC6),
    dangerRowTint = Color(0xFF26181A),
    cautionRowTint = Color(0xFF262016),
    attentionCardBg = Color(0xFF2E2315),
)
