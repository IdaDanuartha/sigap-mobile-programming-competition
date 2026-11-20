package id.sigap.app.feature.activity

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.composables.icons.lucide.Activity
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.User
import id.sigap.app.R
import id.sigap.app.SigapApp
import id.sigap.app.core.designsystem.EmptyState
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.Spacing
import id.sigap.app.core.util.sourceLabelRes
import id.sigap.app.core.util.stringRes
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScanResult
import kotlinx.coroutines.launch

/** S04: Activity log with 7-Day Integrity bar, filters, grouped history, and swipe-to-delete. */
@Composable
fun ActivityScreen(onOpenAlert: (Long) -> Unit) {
    val context = LocalContext.current
    val container = (context.applicationContext as SigapApp).container
    val viewModel: ActivityViewModel = viewModel(factory = viewModelFactory { initializer { ActivityViewModel(container.scanHistory) } })
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by container.settingsState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.activity_deleted)
    val undoLabel = stringResource(R.string.action_undo)

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header: Dot + "Aktivitas", Notification bell & Profile Avatar
            ActivityTopBar()

            // Search input field with search icon
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                placeholder = {
                    Text(
                        "Cari riwayat ancaman atau kata kunci",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SigapTheme.colors.textMuted,
                    )
                },
                leadingIcon = {
                    Icon(
                        Lucide.Search,
                        contentDescription = null,
                        tint = SigapTheme.colors.textMuted,
                        modifier = Modifier.size(18.dp),
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            // Filter Chips: [Semua 5] [• Berbahaya] [• Waspada] [• Aman]
            ActivityFilterRow(
                selected = state.filter,
                onSelect = viewModel::setFilter,
                totalCount = state.groups.sumOf { it.items.size },
            )

            // Integritas 7 Hari Terakhir Banner
            IntegrityWeeklyBanner()

            // Grouped History List (Hari ini / Kemarin)
            if (state.groups.isEmpty()) {
                EmptyState(
                    icon = Lucide.Activity,
                    title = stringResource(R.string.activity_empty_title),
                    body = stringResource(R.string.activity_empty_body),
                    modifier = Modifier.padding(top = 48.dp),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                ) {
                    state.groups.forEach { group ->
                        item(key = "header_${group.title}") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(group.title),
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "${group.items.size} Intersepsi",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = SigapTheme.colors.textMuted,
                                )
                            }
                        }
                        items(group.items, key = { it.id }) { result ->
                            ActivitySwipeRow(
                                result = result,
                                onOpen = { onOpenAlert(result.id) },
                                onDelete = {
                                    viewModel.delete(result)
                                    scope.launch {
                                        val outcome = snackbar.showSnackbar(
                                            deletedMessage,
                                            undoLabel,
                                            duration = androidx.compose.material3.SnackbarDuration.Short,
                                        )
                                        if (outcome == SnackbarResult.ActionPerformed) viewModel.undoDelete()
                                    }
                                },
                            )
                        }
                    }

                    // Retention & Security Footer Note
                    item(key = "footer_retention") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    Lucide.Clock,
                                    contentDescription = null,
                                    tint = SigapTheme.colors.textMuted,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = "Riwayat disimpan ${settings.retentionDays} hari. Atur di Privasi dan data.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = SigapTheme.colors.textMuted,
                                )
                            }
                            Text(
                                text = "Enkripsi lokal terproteksi • Sesuai standar UU PDP",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = SigapTheme.colors.textMuted.copy(alpha = 0.8f),
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun ActivityTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val isEn = LocalConfiguration.current.locales[0]?.language == "en"
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
            Text(
                text = if (isEn) "Activity" else "Aktivitas",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Lucide.Bell,
                    contentDescription = "Notifikasi",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
            Box(
                Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Lucide.User,
                    contentDescription = "Profil",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun ActivityFilterRow(
    selected: ActivityFilter,
    onSelect: (ActivityFilter) -> Unit,
    totalCount: Int,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        item {
            FilterChip(
                selected = selected == ActivityFilter.ALL,
                onClick = { onSelect(ActivityFilter.ALL) },
                shape = RoundedCornerShape(12.dp),
                label = { Text("Semua  $totalCount", fontWeight = FontWeight.SemiBold) },
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected == ActivityFilter.ALL,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    selectedBorderWidth = 1.dp,
                ),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
        item {
            FilterChip(
                selected = selected == ActivityFilter.DANGER,
                onClick = { onSelect(ActivityFilter.DANGER) },
                shape = RoundedCornerShape(12.dp),
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(Modifier.size(6.dp).background(SigapTheme.colors.danger, CircleShape))
                        Text("Berbahaya", fontWeight = FontWeight.Medium)
                    }
                },
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected == ActivityFilter.DANGER,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = SigapTheme.colors.danger.copy(alpha = 0.5f),
                    selectedBorderWidth = 1.dp,
                ),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SigapTheme.colors.dangerContainer,
                    selectedLabelColor = SigapTheme.colors.danger,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
        item {
            FilterChip(
                selected = selected == ActivityFilter.CAUTION,
                onClick = { onSelect(ActivityFilter.CAUTION) },
                shape = RoundedCornerShape(12.dp),
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(Modifier.size(6.dp).background(SigapTheme.colors.caution, CircleShape))
                        Text("Waspada", fontWeight = FontWeight.Medium)
                    }
                },
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected == ActivityFilter.CAUTION,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = SigapTheme.colors.caution.copy(alpha = 0.55f),
                    selectedBorderWidth = 1.dp,
                ),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SigapTheme.colors.cautionContainer,
                    selectedLabelColor = SigapTheme.colors.onCaution,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
        item {
            FilterChip(
                selected = selected == ActivityFilter.SAFE,
                onClick = { onSelect(ActivityFilter.SAFE) },
                shape = RoundedCornerShape(12.dp),
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(Modifier.size(6.dp).background(SigapTheme.colors.safe, CircleShape))
                        Text("Aman", fontWeight = FontWeight.Medium)
                    }
                },
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected == ActivityFilter.SAFE,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = SigapTheme.colors.safe.copy(alpha = 0.5f),
                    selectedBorderWidth = 1.dp,
                ),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SigapTheme.colors.safeContainer,
                    selectedLabelColor = SigapTheme.colors.safe,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}

/** 7-Day Integrity Banner: Bar chart showing clean days & 94% Bersih */
@Composable
private fun IntegrityWeeklyBanner() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Lucide.ShieldCheck,
                        contentDescription = null,
                        tint = SigapTheme.colors.safe,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Integritas 7 Hari Terakhir",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = SigapTheme.colors.safeContainer,
                ) {
                    Text(
                        text = "94% Bersih",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.safe,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }

            // 7 Days Bar Blocks
            val days = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Hari Ini")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                days.forEachIndexed { idx, day ->
                    val isToday = idx == 6
                    val isWarningDay = idx == 4
                    val isDangerDay = idx == 5
                    val barColor = when {
                        isToday -> MaterialTheme.colorScheme.primary
                        isDangerDay -> SigapTheme.colors.dangerContainer
                        isWarningDay -> SigapTheme.colors.cautionContainer
                        else -> SigapTheme.colors.safeContainer
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor),
                        )
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (isToday) MaterialTheme.colorScheme.primary else SigapTheme.colors.textMuted,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

/** Individual Activity item styled exactly like reference image */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ActivitySwipeRow(result: ScanResult, onOpen: () -> Unit, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onDelete()
            value == SwipeToDismissBoxValue.EndToStart
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(SigapTheme.colors.dangerContainer, RoundedCornerShape(16.dp))
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Lucide.Trash2,
                    contentDescription = "Hapus",
                    tint = SigapTheme.colors.danger,
                )
            }
        },
    ) {
        DesignedActivityCard(result = result, onClick = onOpen)
    }
}

@Composable
private fun DesignedActivityCard(result: ScanResult, onClick: () -> Unit) {
    val isDanger = result.level == RiskLevel.DANGER
    val isSafe = result.level == RiskLevel.SAFE
    val isCaution = result.level == RiskLevel.CAUTION

    val tagBg = when {
        isDanger -> SigapTheme.colors.dangerContainer
        isCaution -> SigapTheme.colors.cautionContainer
        else -> SigapTheme.colors.safeContainer
    }
    val tagFg = when {
        isDanger -> SigapTheme.colors.danger
        isCaution -> SigapTheme.colors.onCaution
        else -> SigapTheme.colors.safe
    }
    val tagLabel = when {
        isDanger -> "Berbahaya"
        isCaution -> "Waspada"
        else -> "Aman"
    }

    val sourceAppName = stringResource(sourceLabelRes(result.sourcePackage))

    val senderSubtitle = when {
        !result.senderKey.isNullOrBlank() -> "$sourceAppName • ${result.senderKey}"
        isDanger -> "$sourceAppName • Kontak Tak Dikenal"
        isSafe -> "$sourceAppName • Terverifikasi Aman"
        else -> "$sourceAppName • Pengirim Tidak Dikenal"
    }

    val categoryPill = stringResource(result.category.stringRes())

    val timeAgo = DateUtils.getRelativeTimeSpanString(
        result.timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header row: Level Badge + Time Ago
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tagBg,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            if (isDanger) Lucide.ShieldAlert else if (isCaution) Lucide.TriangleAlert else Lucide.ShieldCheck,
                            contentDescription = null,
                            tint = tagFg,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = tagLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = tagFg,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }

            // Message text & sender with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (isDanger) Lucide.ShieldAlert else if (isCaution) Lucide.TriangleAlert else Lucide.ShieldCheck,
                        contentDescription = null,
                        tint = tagFg,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = senderSubtitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = result.redactedSnippet,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Footer row: Category Pill + "Rincian >"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Text(
                        text = categoryPill,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Rincian",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Icon(
                        Lucide.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}
