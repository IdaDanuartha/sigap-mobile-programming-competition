package id.sigap.app.feature.home

import android.text.format.DateUtils
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.composables.icons.lucide.Activity
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.ExternalLink
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.composables.icons.lucide.MessageCircle
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.ScanEye
import com.composables.icons.lucide.Send
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.User
import com.composables.icons.lucide.Users
import id.sigap.app.R
import id.sigap.app.SigapApp
import id.sigap.app.core.designsystem.EmptyState
import id.sigap.app.core.designsystem.SigapCard
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.Spacing
import id.sigap.app.core.util.PermissionChecks
import id.sigap.app.core.util.sourceLabelRes
import id.sigap.app.core.util.stringRes
import id.sigap.app.data.local.seeded.SeededTrends
import id.sigap.app.domain.checkup.CheckIds
import id.sigap.app.domain.checkup.CheckKind
import id.sigap.app.domain.checkup.CheckupItem
import id.sigap.app.domain.checkup.CheckupScorer
import id.sigap.app.domain.learn.TrendArticle
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScamCategory
import id.sigap.app.domain.model.ScanResult
import id.sigap.app.domain.model.SenderKind
import id.sigap.app.domain.model.TodayStats
import id.sigap.app.feature.activity.ActivityScreen
import id.sigap.app.feature.family.FamilyScreen
import id.sigap.app.feature.learn.LearnScreen
import id.sigap.app.feature.settings.SettingsScreen
import java.util.Calendar

private enum class Tab(val icon: ImageVector, @StringRes val label: Int) {
    Home(Lucide.House, R.string.tab_home),
    Activity(Lucide.Activity, R.string.tab_activity),
    Family(Lucide.Users, R.string.tab_family),
    Learn(Lucide.BookOpen, R.string.tab_learn),
    Settings(Lucide.Settings, R.string.tab_settings),
}

/** Main shell with modern clean bottom bar matching design reference. */
@Composable
fun MainScreen(
    onFixProtection: () -> Unit,
    onOpenAlert: (Long) -> Unit,
    onOpenCheck: () -> Unit,
    onOpenEmergency: () -> Unit,
    onOpenCheckup: () -> Unit,
    onOpenArticle: (String) -> Unit,
    onOpenSimulation: () -> Unit,
    onOpenQuiz: () -> Unit,
) {
    var selected by rememberSaveable { mutableStateOf(Tab.Home) }
    var showNotificationCenter by remember { mutableStateOf(false) }

    if (showNotificationCenter) {
        NotificationCenterSheet(
            onDismiss = { showNotificationCenter = false },
            onOpenAlertDetail = { id -> onOpenAlert(id) },
            onOpenQuiz = onOpenQuiz,
            onOpenSimulation = onOpenSimulation,
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            SigapBottomBar(
                selectedTab = selected,
                onTabSelected = { selected = it },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (selected) {
                Tab.Home -> HomeScreen(
                    onFixProtection = onFixProtection,
                    onOpenAlert = onOpenAlert,
                    onOpenCheck = onOpenCheck,
                    onOpenEmergency = onOpenEmergency,
                    onOpenCheckup = onOpenCheckup,
                    onOpenArticle = onOpenArticle,
                    onSeeAllAlerts = { selected = Tab.Activity },
                    onOpenNotifications = { showNotificationCenter = true },
                    onOpenSettings = { selected = Tab.Settings },
                )
                Tab.Activity -> ActivityScreen(onOpenAlert)
                Tab.Family -> FamilyScreen(
                    onOpenNotifications = { showNotificationCenter = true },
                    onOpenProfile = { selected = Tab.Settings },
                )
                Tab.Learn -> LearnScreen(
                    onOpenArticle = onOpenArticle,
                    onOpenQuiz = onOpenQuiz,
                    onOpenNotifications = { showNotificationCenter = true },
                    onOpenProfile = { selected = Tab.Settings },
                )
                Tab.Settings -> SettingsScreen(
                    onOpenSimulation = onOpenSimulation,
                    onOpenNotifications = { showNotificationCenter = true },
                    onOpenPermissions = onFixProtection,
                )
            }
        }
    }
}

/**
 * Modern floating island bottom navigation bar matching the user's reference design.
 * Features:
 * - Floating rounded card with soft elevation and border
 * - Active tab smoothly expands into a pill container [ Icon  Label ]
 * - Unselected tabs remain clean, muted standalone icons
 * - Ample space so text and icons are never clipped or crowded
 * - Integrated navigation bars padding to float gracefully above the system gesture bar
 */
@Composable
private fun SigapBottomBar(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Tab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab

                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onTabSelected(tab) },
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = if (isSelected) 12.dp else 8.dp,
                                vertical = 7.dp,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = stringResource(tab.label),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else SigapTheme.colors.textMuted,
                                modifier = Modifier.size(20.dp),
                            )
                            if (isSelected) {
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = stringResource(tab.label),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    onFixProtection: () -> Unit,
    onOpenAlert: (Long) -> Unit,
    onOpenCheck: () -> Unit,
    onOpenEmergency: () -> Unit,
    onOpenCheckup: () -> Unit,
    onOpenArticle: (String) -> Unit,
    onSeeAllAlerts: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val container = (context.applicationContext as SigapApp).container
    val viewModel: HomeViewModel = viewModel(factory = viewModelFactory { initializer { HomeViewModel(container.scanHistory) } })
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val userSettings by container.settingsState.collectAsStateWithLifecycle()
    var systemActive by remember { mutableStateOf(PermissionChecks.isNotificationListenerEnabled(context)) }
    var userPaused by rememberSaveable { mutableStateOf(false) }
    var resumeTick by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                systemActive = PermissionChecks.isNotificationListenerEnabled(context) && PermissionChecks.canPostNotifications(context)
                resumeTick++
            }
        }
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    val isEffectivelyActive = systemActive && !userPaused

    val checkupScore = remember(resumeTick, userSettings.confirmedChecks) {
        val auto = container.checkupSource.autoChecks()
        val unknown = container.checkupSource.installedMonitoredApps().map { pkg ->
            val id = CheckIds.UNKNOWN_SOURCES_PREFIX + pkg
            CheckupItem(id, CheckKind.SELF, CheckIds.WEIGHT_UNKNOWN_SOURCES, id in userSettings.confirmedChecks, packageName = pkg)
        }
        val protect = CheckupItem(CheckIds.PLAY_PROTECT, CheckKind.SELF, CheckIds.WEIGHT_PLAY_PROTECT, CheckIds.PLAY_PROTECT in userSettings.confirmedChecks)
        CheckupScorer.score(auto + unknown + protect)
    }

    val featuredTrend = SeededTrends.ARTICLES.first()
    val isFirstDayEmpty = state.stats.scanned == 0 && state.latestAlerts.isEmpty() && state.weeklyBlocked == 0
    val disabled = userSettings.disabledPackages
    val monitoredChannels = listOf(
        "com.whatsapp",
        "com.whatsapp.w4b",
        "org.telegram.messenger",
        "com.google.android.apps.messaging",
        "android.sms.default",
    ).count { it !in disabled }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 1. Editorial Header: "Sigap" wordmark, greeting, bell & settings (48dp touch targets)
        EditorialHomeHeader(
            userName = userSettings.userName,
            onOpenNotifications = onOpenNotifications,
            onOpenSettings = onOpenSettings,
        )

        // 2. Compact Status Strip (~52dp) OR Expanded Attention Card
        if (isEffectivelyActive) {
            EditorialStatusStrip(onManage = onOpenSettings)
        } else {
            EditorialAttentionCard(
                isPaused = userPaused,
                onActivate = {
                    if (!systemActive) onFixProtection() else userPaused = false
                },
            )
        }

        // 3. Dominant Bento Card (Solid Ink Light / Terracotta-Tinted Dark) OR First-Day Onboarding
        if (isFirstDayEmpty) {
            EmptyOnboardingCard()
        } else {
            DominantThreatsCard(
                blockedCount = state.weeklyBlocked,
                dangerCount = state.weeklyDangerCount,
                cautionCount = state.weeklyCautionCount,
                bars = state.weeklyBars,
                isPausedOrInactive = !isEffectivelyActive,
            )
        }

        // 4. Two Small Bento Cards (NO border, subtle surface tone only)
        SmallBentoStatsRow(
            scannedToday = if (isEffectivelyActive) state.stats.scanned else 0,
            monitoredAppsCount = if (isEffectivelyActive) monitoredChannels else 0,
        )

        // 4b. Quick Actions Bento (Periksa Nomor/Rekening/SS/QR, Darurat, Cek Keamanan)
        QuickActionsBentoSection(
            checkupScore = checkupScore,
            onOpenCheck = onOpenCheck,
            onOpenEmergency = onOpenEmergency,
            onOpenCheckup = onOpenCheckup,
        )

        // 5. Peringatan Terbaru (Clean faint severity tint, no vertical left line)
        LatestAlertsSection(
            alerts = state.latestAlerts,
            onOpenAlert = onOpenAlert,
            onSeeAllAlerts = onSeeAllAlerts,
        )

        // 6. Modus Minggu Ini (Clean native editorial card)
        ScamOfTheWeekCard(
            trend = featuredTrend,
            onOpenArticle = { onOpenArticle(featuredTrend.id) },
        )

        Spacer(Modifier.height(Spacing.Section))
    }
}

/** 1. Editorial Header: Typographic "Sigap" wordmark, time-based greeting, bell & settings (48dp targets). */
@Composable
private fun EditorialHomeHeader(
    userName: String,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val isEn = LocalConfiguration.current.locales[0]?.language == "en"
    val greetingPrefix = remember(isEn) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (isEn) {
            when (hour) {
                in 4..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                in 17..20 -> "Good evening"
                else -> "Good night"
            }
        } else {
            when (hour) {
                in 4..10 -> "Selamat pagi"
                in 11..14 -> "Selamat siang"
                in 15..18 -> "Selamat sore"
                else -> "Selamat malam"
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(Color(0xFFF9F1E4), RoundedCornerShape(10.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_mark),
                    contentDescription = "Logo Sigap",
                    modifier = Modifier.size(26.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "Sigap",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "$greetingPrefix, $userName",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    fontWeight = FontWeight.Medium,
                    color = SigapTheme.colors.textMuted,
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenNotifications),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Lucide.Bell,
                    contentDescription = if (isEn) "Notification Center" else "Pusat Pemberitahuan",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(20.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenSettings),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Lucide.Settings,
                    contentDescription = if (isEn) "Settings" else "Pengaturan",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/** 2a. Compact Status Strip (~52dp, single row, no border, calm pulsing dot + Atur link). */
@Composable
private fun EditorialStatusStrip(onManage: () -> Unit) {
    val isEn = LocalConfiguration.current.locales[0]?.language == "en"
    val infiniteTransition = rememberInfiniteTransition(label = "calm_status_dot")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot_scale",
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                        .background(SigapTheme.colors.safe, CircleShape),
                )
                Text(
                    text = if (isEn) "Protection active" else "Perlindungan aktif",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onManage)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = if (isEn) "Manage" else "Atur",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/** 2b. Attention Needed Card (expands from status strip when notification access is missing or paused). */
@Composable
private fun EditorialAttentionCard(
    isPaused: Boolean,
    onActivate: () -> Unit,
) {
    val isEn = LocalConfiguration.current.locales[0]?.language == "en"
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SigapTheme.colors.attentionCardBg,
        border = BorderStroke(1.dp, SigapTheme.colors.caution.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    Lucide.TriangleAlert,
                    contentDescription = null,
                    tint = SigapTheme.colors.caution,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(18.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = when {
                            isPaused && isEn -> "Protection is paused"
                            isPaused -> "Perlindungan sedang dijeda"
                            isEn -> "Notification access inactive"
                            else -> "Izin notifikasi belum aktif"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = when {
                            isPaused && isEn -> "Resume protection so Sigap automatically checks incoming messages."
                            isPaused -> "Aktifkan kembali agar Sigap memeriksa pesan masuk secara otomatis."
                            isEn -> "Enable notification access so Sigap can automatically check incoming messages."
                            else -> "Aktifkan akses notifikasi agar Sigap bisa memeriksa pesan masuk secara otomatis."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }
            }

            Button(
                onClick = onActivate,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (isEn) "Activate now" else "Aktifkan sekarang",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        Lucide.ArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/** 3a. Dominant Bento Card: Solid deep ink (Light) / Terracotta-tinted dark (Dark), 24dp radius, oversized numeral + real 7-day bars. */
@Composable
private fun DominantThreatsCard(
    blockedCount: Int,
    dangerCount: Int,
    cautionCount: Int,
    bars: List<DayThreatBar>,
    isPausedOrInactive: Boolean,
) {
    val animatedCount by animateIntAsState(
        targetValue = blockedCount,
        animationSpec = tween(durationMillis = 450),
        label = "blocked_count",
    )

    val isEn = LocalConfiguration.current.locales[0]?.language == "en"
    val subtitle = when {
        isPausedOrInactive && isEn -> "New monitoring is currently paused"
        isPausedOrInactive -> "Pemantauan baru sedang tertunda"
        (dangerCount > 0 || cautionCount > 0) && isEn -> "$dangerCount dangerous and $cautionCount suspicious"
        dangerCount > 0 || cautionCount > 0 -> "$dangerCount berbahaya dan $cautionCount mencurigakan"
        isEn -> "All threats successfully filtered"
        else -> "Semua ancaman berhasil disaring"
    }

    val maxBarCount = (bars.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = SigapTheme.colors.dominantSurface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = animatedCount.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 48.sp,
                        lineHeight = 48.sp,
                        letterSpacing = (-1.5).sp,
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = SigapTheme.colors.onDominant,
                )
                Text(
                    text = if (isEn) "Threats blocked this week" else "Ancaman dicegah minggu ini",
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = SigapTheme.colors.onDominant,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                    color = SigapTheme.colors.onDominantMuted,
                )
            }

            if (bars.isNotEmpty()) {
                Spacer(Modifier.width(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    bars.forEach { bar ->
                        val heightDp = if (bar.count == 0) {
                            6
                        } else {
                            (10 + (bar.count.toFloat() / maxBarCount) * 28).toInt().coerceIn(10, 38)
                        }
                        val isAccent = bar.count > 0 && (!isPausedOrInactive || !bar.isToday)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(7.dp)
                                    .height(heightDp.dp)
                                    .background(
                                        color = if (isAccent) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.White.copy(alpha = 0.24f)
                                        },
                                        shape = RoundedCornerShape(4.dp),
                                    ),
                            )
                            Text(
                                text = bar.dayInitial,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = if (bar.isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (bar.isToday) SigapTheme.colors.onDominant else SigapTheme.colors.onDominantMuted,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 3b. Empty First-Day Onboarding Replacement for Dominant Card. */
@Composable
private fun EmptyOnboardingCard() {
    val isEn = LocalConfiguration.current.locales[0]?.language == "en"
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = SigapTheme.colors.dominantSurface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (isEn) "FIRST DAY" else "HARI PERTAMA",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 13.sp,
                    letterSpacing = 0.6.sp,
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = if (isEn) "Sigap works quietly in the background" else "Sigap bekerja diam-diam di latar belakang",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp, lineHeight = 24.sp),
                fontWeight = FontWeight.ExtraBold,
                color = SigapTheme.colors.onDominant,
            )
            Text(
                text = if (isEn) {
                    "Incoming messages and notifications are automatically checked for fake links, APK files, and unknown senders."
                } else {
                    "Pesan dan notifikasi masuk otomatis diperiksa dari tautan palsu, berkas APK, serta pengirim tak dikenal."
                },
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 19.sp),
                color = SigapTheme.colors.onDominantMuted,
            )
        }
    }
}

/** 4. Two Small Bento Cards: Strictly NO border, subtle surface tone only, 14dp radius. */
@Composable
private fun SmallBentoStatsRow(
    scannedToday: Int,
    monitoredAppsCount: Int,
) {
    val isEn = LocalConfiguration.current.locales[0]?.language == "en"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 82.dp),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = scannedToday.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (scannedToday == 0) SigapTheme.colors.textMuted else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (isEn) "Messages checked today" else "Pesan dicek hari ini",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 82.dp),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = monitoredAppsCount.toString(),
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = if (monitoredAppsCount == 0) SigapTheme.colors.textMuted else MaterialTheme.colorScheme.onSurface,
                    )
                    if (monitoredAppsCount > 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                            listOf(Lucide.MessageCircle, Lucide.Mail, Lucide.Send).forEach { icon ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                                        .border(1.5.dp, MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(12.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (isEn) "Apps monitored" else "Aplikasi dipantau",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }
        }
    }
}

/** 4b. Editorial Bento Quick Actions: Manual Check (Number, Bank, SS, QR, Link), Emergency & Security Checkup. */
@Composable
private fun QuickActionsBentoSection(
    checkupScore: Int,
    onOpenCheck: () -> Unit,
    onOpenEmergency: () -> Unit,
    onOpenCheckup: () -> Unit,
) {
    val isEn = LocalConfiguration.current.locales[0]?.language == "en"
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onOpenCheck),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Lucide.ScanEye,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = if (isEn) "Manual Check" else "Periksa Manual",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (isEn) {
                            "Check numbers, bank accounts, screenshots, QR, & links"
                        } else {
                            "Cek nomor, rekening, screenshot, QR, & tautan"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 17.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }
                Icon(
                    Lucide.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 68.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenEmergency),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        Lucide.ShieldAlert,
                        contentDescription = null,
                        tint = SigapTheme.colors.danger,
                        modifier = Modifier.size(18.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = if (isEn) "Emergency" else "Darurat",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (isEn) "First-aid steps" else "Langkah cepat",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = SigapTheme.colors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 68.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenCheckup),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        Lucide.ShieldCheck,
                        contentDescription = null,
                        tint = SigapTheme.colors.safe,
                        modifier = Modifier.size(18.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = if (isEn) "Security Audit" else "Cek Keamanan",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (isEn) "Score $checkupScore%" else "Skor $checkupScore%",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = SigapTheme.colors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

/** 5. Peringatan Terbaru: 4dp left severity edge + faint tinted background + distinct source label & icon. */
@Composable
private fun LatestAlertsSection(
    alerts: List<ScanResult>,
    onOpenAlert: (Long) -> Unit,
    onSeeAllAlerts: () -> Unit,
) {
    val isEn = LocalConfiguration.current.locales[0]?.language == "en"
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isEn) "Recent alerts" else "Peringatan terbaru",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (alerts.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onSeeAllAlerts)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = if (isEn) "See all" else "Lihat semua",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        Lucide.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }

        if (alerts.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = if (isEn) "No threats blocked yet" else "Belum ada ancaman yang dicegah",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (isEn) {
                            "You don't need to copy messages. When a suspicious notification arrives, its summary appears here automatically."
                        } else {
                            "Anda tidak perlu menyalin pesan. Bila ada notifikasi mencurigakan masuk, ringkasan alasannya otomatis tampil di sini."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 19.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }
            }
        } else {
            val visibleAlerts = alerts.take(4)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    visibleAlerts.forEachIndexed { index, alert ->
                        EditorialAlertRow(alert = alert, onClick = { onOpenAlert(alert.id) })
                        if (index < visibleAlerts.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorialAlertRow(alert: ScanResult, onClick: () -> Unit) {
    val isEn = LocalConfiguration.current.locales[0]?.language == "en"
    val isDanger = alert.level == RiskLevel.DANGER
    val riskLabel = when {
        isDanger && isEn -> "Danger"
        isDanger -> "Berbahaya"
        isEn -> "Suspicious"
        else -> "Mencurigakan"
    }
    val riskTextColor = if (isDanger) SigapTheme.colors.danger else SigapTheme.colors.onCaution
    val iconBg = if (isDanger) SigapTheme.colors.dangerContainer else SigapTheme.colors.cautionContainer
    val dotColor = if (isDanger) SigapTheme.colors.danger else SigapTheme.colors.caution

    val sourceName = stringResource(sourceLabelRes(alert.sourcePackage))
    val sourceIcon = when {
        alert.sourcePackage.contains("whatsapp", ignoreCase = true) -> Lucide.MessageCircle
        alert.sourcePackage.contains("telegram", ignoreCase = true) -> Lucide.Send
        alert.sourcePackage.contains("messaging", ignoreCase = true) -> Lucide.Mail
        else -> Lucide.Bell
    }
    val senderTitle = alert.senderKey?.takeIf { it.isNotBlank() } ?: when {
        isDanger && isEn -> "Unknown Number"
        isDanger -> "Nomor Tak Dikenal"
        else -> sourceName
    }
    val timeAgo = DateUtils.getRelativeTimeSpanString(
        alert.timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                sourceIcon,
                contentDescription = sourceName,
                tint = riskTextColor,
                modifier = Modifier.size(18.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = senderTitle,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }

            Text(
                text = alert.redactedSnippet,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color = SigapTheme.colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = sourceName,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    fontWeight = FontWeight.Medium,
                    color = SigapTheme.colors.textMuted,
                )
                Text(
                    text = "·",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    color = SigapTheme.colors.textMuted,
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(dotColor, CircleShape),
                )
                Text(
                    text = riskLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = riskTextColor,
                )
            }
        }

        Icon(
            Lucide.ChevronRight,
            contentDescription = null,
            tint = SigapTheme.colors.textMuted,
            modifier = Modifier.size(16.dp),
        )
    }
}

/** 6. Modus Minggu Ini: Native editorial card with clean typography and compact spacing. */
@Composable
private fun ScamOfTheWeekCard(
    trend: TrendArticle,
    onOpenArticle: () -> Unit,
) {
    val isEn = LocalConfiguration.current.locales[0]?.language == "en"
    val thumbnailRes = when (trend.id) {
        "online_shopping_marketplace",
        "traffic_ticket_authority",
        "part_time_job_commission",
        -> R.drawable.sample_phishing_proof
        else -> R.drawable.trend_apk_scam
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenArticle),
    ) {
        Column {
            Image(
                painter = painterResource(thumbnailRes),
                contentDescription = if (isEn) trend.titleEn else trend.titleId,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp),
            )
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Lucide.BookOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    text = if (isEn) "Scam of the week" else "Modus minggu ini",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = if (isEn) trend.titleEn else trend.titleId,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 20.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (isEn) trend.summaryEn else trend.summaryId,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color = SigapTheme.colors.textMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (isEn) "${trend.readMinutes} min read" else "Baca ${trend.readMinutes} menit",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                    color = SigapTheme.colors.textMuted,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = if (isEn) "Check the signs" else "Cek ciri-cirinya",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        Lucide.ArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
            }
        }
    }
}
