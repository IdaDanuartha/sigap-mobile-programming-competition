package id.sigap.app.feature.splash

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.sigap.app.R
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.WordmarkFontFamily
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

/**
 * Simple, modern, and clearly visible editorial splash animation (S00).
 * Waits briefly for the Android 12+ system splash handoff, then:
 * 1. Logo mark pops in cleanly with spring scale + fade
 * 2. Single subtle terracotta ring breath
 * 3. Wordmark & tagline rise into place
 * 4. Calm hold, followed by a smooth fade-out transition
 */
@Composable
fun AnimatedLogoSplash(onFinished: () -> Unit) {
    val context = LocalContext.current
    val isEnglish = LocalConfiguration.current.locales[0]?.language == "en"
    val animationsOn = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }

    val screenAlpha = remember { Animatable(1f) }
    val logoAlpha = remember { Animatable(if (animationsOn) 0f else 1f) }
    val logoScale = remember { Animatable(if (animationsOn) 0.72f else 1f) }
    val ringScale = remember { Animatable(1f) }
    val ringAlpha = remember { Animatable(0f) }
    val wordmarkAlpha = remember { Animatable(if (animationsOn) 0f else 1f) }
    val wordmarkOffsetY = remember { Animatable(if (animationsOn) 24f else 0f) }
    val taglineAlpha = remember { Animatable(if (animationsOn) 0f else 1f) }

    LaunchedEffect(Unit) {
        if (!animationsOn) {
            onFinished()
            return@LaunchedEffect
        }

        // Let Android 12+ system splash finish its handoff so the entrance is clearly visible
        delay(160)

        coroutineScope {
            // 1. Logo mark crisp pop-in
            launch {
                logoAlpha.animateTo(1f, tween(280, easing = EmphasizedDecelerate))
            }
            launch {
                logoScale.animateTo(
                    1f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                )
            }

            // 2. Single subtle terracotta ring breath
            launch {
                delay(180)
                ringAlpha.snapTo(0.4f)
                launch { ringScale.animateTo(1.42f, tween(520, easing = EmphasizedDecelerate)) }
                launch { ringAlpha.animateTo(0f, tween(520, easing = EmphasizedDecelerate)) }
            }

            // 3. Wordmark upward settle
            launch {
                delay(240)
                launch { wordmarkAlpha.animateTo(1f, tween(360, easing = EmphasizedDecelerate)) }
                launch { wordmarkOffsetY.animateTo(0f, tween(380, easing = EmphasizedDecelerate)) }
            }

            // 4. Footer tagline fade
            launch {
                delay(380)
                taglineAlpha.animateTo(1f, tween(320, easing = EmphasizedDecelerate))
            }
        }

        // Calm hold, then smooth exit fade
        delay(420)
        screenAlpha.animateTo(0f, tween(240, easing = EmphasizedDecelerate))
        onFinished()
    }

    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .graphicsLayer { alpha = screenAlpha.value },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(128.dp),
            ) {
                // Single subtle terracotta breath ring
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .graphicsLayer {
                            scaleX = ringScale.value
                            scaleY = ringScale.value
                            alpha = ringAlpha.value
                        }
                        .border(
                            border = BorderStroke(1.5.dp, primary),
                            shape = RoundedCornerShape(24.dp),
                        ),
                )

                // Clean editorial squircle mark
                Surface(
                    modifier = Modifier
                        .size(88.dp)
                        .graphicsLayer {
                            alpha = logoAlpha.value
                            scaleX = logoScale.value
                            scaleY = logoScale.value
                        },
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFF9F1E4),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.logo_mark),
                            contentDescription = stringResource(R.string.logo_description),
                            modifier = Modifier.size(62.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.app_name),
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = WordmarkFontFamily,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.6).sp,
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = wordmarkAlpha.value
                    translationY = wordmarkOffsetY.value
                },
            )
        }

        Text(
            text = if (isEnglish) "Protect yourself from digital scams" else "Lindungi diri dari penipuan digital",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = SigapTheme.colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 28.dp)
                .graphicsLayer { alpha = taglineAlpha.value },
        )
    }
}
