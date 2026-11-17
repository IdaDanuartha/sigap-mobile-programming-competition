package id.sigap.app.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.TriangleAlert
import id.sigap.app.core.util.labelRes
import id.sigap.app.core.util.sourceLabelRes
import id.sigap.app.core.util.stringRes
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScanResult

/** Colours for a risk level. Risk is always shown with colour, icon and label together. */
data class RiskStyle(val accent: Color, val container: Color, val onContainer: Color, val icon: ImageVector)

@Composable
fun RiskLevel.style(): RiskStyle {
    val c = SigapTheme.colors
    return when (this) {
        RiskLevel.DANGER -> RiskStyle(c.danger, c.dangerContainer, c.danger, Lucide.ShieldAlert)
        RiskLevel.CAUTION -> RiskStyle(c.caution, c.cautionContainer, c.onCaution, Lucide.TriangleAlert)
        RiskLevel.SAFE -> RiskStyle(c.safe, c.safeContainer, c.safe, Lucide.ShieldCheck)
    }
}

@Composable
fun RiskBadge(level: RiskLevel, modifier: Modifier = Modifier) {
    val style = level.style()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = style.container,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(style.icon, contentDescription = null, tint = style.onContainer, modifier = Modifier.size(13.dp))
            Text(
                stringResource(level.labelRes()),
                style = MaterialTheme.typography.labelSmall,
                color = style.onContainer,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/** Semi-circle gauge from 0 to 100. */
@Composable
fun RiskGauge(score: Int, level: RiskLevel, modifier: Modifier = Modifier) {
    val style = level.style()
    val track = MaterialTheme.colorScheme.outline
    Box(modifier.size(width = 220.dp, height = 124.dp), contentAlignment = Alignment.BottomCenter) {
        Canvas(Modifier.size(width = 220.dp, height = 124.dp)) {
            val strokeWidth = 16.dp.toPx()
            val inset = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, (size.height - inset) * 2)
            val topLeft = Offset(inset, inset)
            val stroke = Stroke(strokeWidth, cap = StrokeCap.Round)
            drawArc(track, 180f, 180f, false, topLeft, arcSize, style = stroke)
            drawArc(style.accent, 180f, 180f * score.coerceIn(0, 100) / 100f, false, topLeft, arcSize, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(score.toString(), style = MaterialTheme.typography.displayLarge)
            Text(stringResource(level.labelRes()), style = MaterialTheme.typography.labelLarge, color = style.accent)
        }
    }
}

/** Summary card for a scan result. */
@Composable
fun AlertCard(result: ScanResult, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SigapCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RiskBadge(result.level)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Text(
                    stringResource(sourceLabelRes(result.sourcePackage)),
                    style = MaterialTheme.typography.labelSmall,
                    color = SigapTheme.colors.textMuted,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
        Text(
            result.redactedSnippet,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 10.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val ruleText = result.reasons.firstOrNull()?.let { stringResource(it.stringRes()) } ?: ""
            Text(
                ruleText,
                style = MaterialTheme.typography.labelSmall,
                color = SigapTheme.colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Lucide.ChevronRight,
                contentDescription = null,
                tint = SigapTheme.colors.textMuted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
