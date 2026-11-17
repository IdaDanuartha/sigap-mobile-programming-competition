package id.sigap.app.feature.onboarding

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquareWarning
import com.composables.icons.lucide.ShieldCheck
import id.sigap.app.R
import id.sigap.app.core.designsystem.PrimaryButton
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.Spacing
import id.sigap.app.core.designsystem.TextAction
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val badge: String,
    @StringRes val title: Int,
    @StringRes val body: Int,
)

private val pages = listOf(
    OnboardingPage(
        icon = Lucide.MessageSquareWarning,
        badge = "Deteksi Ancaman APK & Phising",
        title = R.string.onboarding_1_title,
        body = R.string.onboarding_1_body,
    ),
    OnboardingPage(
        icon = Lucide.Eye,
        badge = "Pemantauan Pasif Latar Belakang",
        title = R.string.onboarding_2_title,
        body = R.string.onboarding_2_body,
    ),
    OnboardingPage(
        icon = Lucide.Lock,
        badge = "100% On-Device · Privasi Terjamin",
        title = R.string.onboarding_3_title,
        body = R.string.onboarding_3_body,
    ),
)

/** S01: modern three-page introduction with elevated typography, hero badge, and smooth pager. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = Spacing.ScreenHorizontal),
    ) {
        // Top Brand Header: Right-aligned "Lewati" as shown in S01 mockup
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            if (!isLast) {
                TextAction(stringResource(R.string.action_skip), onClick = onFinished)
            } else {
                Spacer(Modifier.height(24.dp))
            }
        }

        // Pager Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { index ->
            PageContent(pages[index])
        }

        // Animated Dots left-aligned as shown in mockup
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PageDots(count = pages.size, current = pagerState.currentPage)
        }

        // Action Button
        PrimaryButton(
            text = stringResource(if (isLast) R.string.action_get_started else R.string.action_next),
            icon = if (isLast) Lucide.ShieldCheck else Lucide.ArrowRight,
            onClick = {
                if (isLast) {
                    onFinished()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            modifier = Modifier.padding(bottom = Spacing.Section),
        )
    }
}

@Composable
private fun PageContent(page: OnboardingPage) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        // Hero Visual Container matching S01 mockup
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 36.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .size(170.dp)
                    .clip(RoundedCornerShape(32.dp)),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Soft green circular backdrop
                    Box(
                        Modifier
                            .size(120.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    )
                    // Card background representation
                    Box(
                        Modifier
                            .size(width = 80.dp, height = 96.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        ) {
                            Box(Modifier.size(width = 36.dp, height = 3.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(2.dp)))
                            Box(Modifier.size(width = 24.dp, height = 3.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                            Spacer(Modifier.height(8.dp))
                            // Shield icon
                            Box(
                                Modifier
                                    .size(46.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    page.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Title (Left aligned)
        Text(
            stringResource(page.title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 25.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Start,
        )

        Spacer(Modifier.height(12.dp))

        // Body (Left aligned)
        Text(
            stringResource(page.body),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 14.sp,
                lineHeight = 22.sp,
            ),
            color = SigapTheme.colors.textMuted,
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
private fun PageDots(count: Int, current: Int) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val isSelected = index == current
            val targetWidth = if (isSelected) 24.dp else 8.dp
            val animatedWidth by animateDpAsState(targetValue = targetWidth, label = "dotWidth")
            val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

            Box(
                Modifier
                    .size(width = animatedWidth, height = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}
