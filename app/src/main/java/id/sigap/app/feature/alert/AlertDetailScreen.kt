package id.sigap.app.feature.alert

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Ban
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquare
import com.composables.icons.lucide.PhoneOff
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Timer
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.User
import com.composables.icons.lucide.UserCheck
import com.composables.icons.lucide.X
import com.composables.icons.lucide.Zap
import id.sigap.app.R
import id.sigap.app.SigapApp
import id.sigap.app.core.designsystem.EmptyState
import id.sigap.app.core.designsystem.RiskGauge
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.Spacing
import id.sigap.app.core.util.sourceLabelRes
import id.sigap.app.domain.model.AiAnalysisResult
import id.sigap.app.domain.model.ReasonCode
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScamCategory
import id.sigap.app.domain.model.ScanResult
import id.sigap.app.core.util.stringRes
import id.sigap.app.feature.report.ReportBottomSheet

/** S05: Detail Ancaman matching reference design with high-fidelity gauge, reasons, message preview, checklist and interactive actions. */
@Composable
fun AlertDetailScreen(id: Long, onBack: () -> Unit, onOpenEmergency: (String) -> Unit) {
    val container = (LocalContext.current.applicationContext as SigapApp).container
    val viewModel: AlertDetailViewModel = viewModel(
        key = "alert_$id",
        factory = viewModelFactory {
            initializer {
                AlertDetailViewModel(id, container.scanHistory, container.trustedSenders, container.hybridAi)
            }
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        // App Top Bar: Back, "Detail Ancaman", More options, Profile Avatar
        val loadedResult = (state as? AlertDetailUiState.Loaded)?.result
        DetailTopBar(
            onBack = onBack,
            result = loadedResult,
            onDelete = { viewModel.delete(); onBack() },
            onOpenEmergency = { loadedResult?.let { onOpenEmergency(it.category.name) } },
        )

        when (val current = state) {
            AlertDetailUiState.Loading -> Unit
            AlertDetailUiState.NotFound -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.ScreenHorizontal, vertical = 32.dp),
            ) {
                EmptyState(
                    icon = Lucide.TriangleAlert,
                    title = stringResource(R.string.detail_not_found),
                    body = "",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            is AlertDetailUiState.Loaded -> DetailContent(
                result = current.result,
                aiState = aiState,
                onRequestAi = { viewModel.requestAiAnalysis(current.result) },
                onOpenEmergency = { onOpenEmergency(current.result.category.name) },
                onTrust = { viewModel.trustSender(current.result); onBack() },
                onDelete = { viewModel.delete(); onBack() },
            )
        }
    }
}

@Composable
private fun DetailTopBar(
    onBack: () -> Unit,
    result: ScanResult? = null,
    onDelete: () -> Unit = {},
    onOpenEmergency: () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Lucide.ArrowLeft,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = "Detail Ancaman",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Lucide.EllipsisVertical,
                        contentDescription = "Opsi Tambahan",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Bagikan Laporan Ancaman") },
                        onClick = {
                            showMenu = false
                            val text = "⚠️ Laporan Deteksi Ancaman Sigap:\n\n" +
                                "Kategori: ${result?.category?.name ?: "Mencurigakan"}\n" +
                                "Tingkat Bahaya: ${result?.level?.name ?: "Berbahaya"}\n" +
                                "Pesan: \"${result?.redactedSnippet.orEmpty()}\"\n\n" +
                                "Diperiksa menggunakan Analisis Keamanan On-Device Sigap."
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(intent, "Bagikan Laporan"))
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Salin Bukt   i Pesan") },
                        onClick = {
                            showMenu = false
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Bukti Pesan", result?.redactedSnippet.orEmpty())
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Teks bukti berhasil disalin", Toast.LENGTH_SHORT).show()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Panduan Darurat Satgas") },
                        onClick = {
                            showMenu = false
                            onOpenEmergency()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus Riwayat Peringatan", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                    )
                }
            }

            Box(
                Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Lucide.User,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailContent(
    result: ScanResult,
    aiState: AiAnalysisUiState,
    onRequestAi: () -> Unit,
    onOpenEmergency: () -> Unit,
    onTrust: () -> Unit,
    onDelete: () -> Unit,
) {
    var showTrustDialog by remember { mutableStateOf(false) }
    var showGuideSheet by remember { mutableStateOf(false) }
    var showReportSheet by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Top Risk Level Tabs: [Berbahaya] [Waspada]
        RiskLevelTabs(currentLevel = result.level)

        // Hero Score Gauge Card (92 / 100 • Berbahaya + Pills)
        HeroGaugeCard(result = result)

        // Section 1: Kenapa ini ditandai (X Temuan)
        WhyFlaggedSection(reasons = result.reasons, category = result.category, snippet = result.redactedSnippet)

        // Section 2: Pratinjau pesan (Terenkripsi badge + Diperiksa di perangkat note)
        MessagePreviewSection(snippet = result.redactedSnippet, sourcePackage = result.sourcePackage)

        // Section 3: Yang perlu dilakukan (Numbered checklist 1, 2, 3)
        WhatToDoSection(category = result.category, level = result.level, reasons = result.reasons)

        // Section 4: Perlindungan Aktif Status Card
        ProtectionActiveBanner()

        // Action Buttons:
        // [Aku terlanjur klik] (Red primary)
        Button(
            onClick = onOpenEmergency,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SigapTheme.colors.danger,
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Lucide.Zap, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = "Aku terlanjur klik",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // [Laporkan ke komunitas] (Outlined white button)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { showReportSheet = true },
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Lucide.Share2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Laporkan ke komunitas",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // Footer Actions: Percayai pengirim • Cara memblokir
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.clickable { showTrustDialog = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Lucide.UserCheck,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "Percayai pengirim",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
            }

            Text(
                text = "  •  ",
                style = MaterialTheme.typography.labelSmall,
                color = SigapTheme.colors.textMuted,
            )

            Row(
                modifier = Modifier.clickable { showGuideSheet = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Lucide.Ban,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "Cara memblokir",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Spacer(Modifier.height(Spacing.Section))
    }

    if (showTrustDialog) {
        Dialog(onDismissRequest = { showTrustDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Lucide.UserCheck,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Percayai Pengirim?",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "Tambahkan ke daftar tepercaya ponsel",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = SigapTheme.colors.textMuted,
                                )
                            }
                        }

                        IconButton(onClick = { showTrustDialog = false }, modifier = Modifier.size(32.dp)) {
                            Icon(Lucide.X, contentDescription = "Tutup", tint = SigapTheme.colors.textMuted, modifier = Modifier.size(18.dp))
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    Text(
                        text = "Pesan berikutnya dari nomor atau pengirim ini tidak akan lagi ditandai atau diperingatkan sebagai ancaman di masa mendatang.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 19.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = { showTrustDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.weight(1f).height(44.dp),
                        ) {
                            Text("Batal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Button(
                            onClick = {
                                showTrustDialog = false
                                onTrust()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1.2f).height(44.dp),
                        ) {
                            Text("Percayai", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }

    if (showReportSheet) {
        ReportBottomSheet(initialCategory = result.category, onDismiss = { showReportSheet = false })
    }

    if (showGuideSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGuideSheet = false },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            Column(
                Modifier
                    .padding(Spacing.ScreenHorizontal)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Cara Memblokir Kontak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("1. WhatsApp: Buka profil kontak > Gulir ke bawah > Pilih 'Blokir Kontak'.", style = MaterialTheme.typography.bodyMedium)
                Text("2. SMS: Buka percakapan > Ketuk titik tiga di pojok kanan > Pilih 'Blokir nomor'.", style = MaterialTheme.typography.bodyMedium)
                Text("3. Telegram: Buka obrolan kontak > Pilih 'Blokir pengguna'.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Level Tabs at top: [Berbahaya] [Waspada] */
@Composable
private fun RiskLevelTabs(currentLevel: RiskLevel) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Berbahaya tab
            val isDanger = currentLevel == RiskLevel.DANGER
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDanger) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (isDanger) 2.dp else 0.dp,
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Lucide.TriangleAlert,
                        contentDescription = null,
                        tint = if (isDanger) SigapTheme.colors.danger else SigapTheme.colors.textMuted,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Berbahaya",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isDanger) FontWeight.Bold else FontWeight.Medium,
                        color = if (isDanger) SigapTheme.colors.danger else SigapTheme.colors.textMuted,
                    )
                }
            }

            // Waspada tab
            val isCaution = currentLevel == RiskLevel.CAUTION
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isCaution) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (isCaution) 2.dp else 0.dp,
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Lucide.Info,
                        contentDescription = null,
                        tint = if (isCaution) SigapTheme.colors.caution else SigapTheme.colors.textMuted,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Waspada",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isCaution) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCaution) SigapTheme.colors.caution else SigapTheme.colors.textMuted,
                    )
                }
            }
        }
    }
}

/** Hero Gauge Card (Risk Gauge + Dynamic Category + Dynamic Source + Dynamic Timestamp) */
@Composable
private fun HeroGaugeCard(result: ScanResult) {
    val categoryName = stringResource(result.category.stringRes())
    val sourceName = stringResource(sourceLabelRes(result.sourcePackage))
    val timeAgo = DateUtils.getRelativeTimeSpanString(
        result.timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()

    val isDanger = result.level == RiskLevel.DANGER
    val categoryBadgeBg = if (isDanger) SigapTheme.colors.dangerContainer.copy(alpha = 0.6f) else SigapTheme.colors.cautionContainer.copy(alpha = 0.6f)
    val categoryBadgeFg = if (isDanger) SigapTheme.colors.danger else SigapTheme.colors.caution

    val categoryIcon = when (result.category) {
        ScamCategory.APK_MALWARE, ScamCategory.FAKE_INVITATION -> Lucide.FileText
        ScamCategory.FAKE_COURIER, ScamCategory.BANK_PHISHING -> Lucide.ShieldAlert
        ScamCategory.AUTHORITY_IMPERSONATION -> Lucide.Shield
        ScamCategory.JOB_SCAM, ScamCategory.INVESTMENT, ScamCategory.FAKE_PRIZE -> Lucide.CircleAlert
        ScamCategory.OTP_THEFT -> Lucide.Lock
        else -> Lucide.TriangleAlert
    }

    val sourceIcon = when {
        result.sourcePackage.contains("whatsapp", ignoreCase = true) -> Lucide.MessageSquare
        result.sourcePackage.contains("telegram", ignoreCase = true) -> Lucide.MessageSquare
        result.sourcePackage.contains("messaging", ignoreCase = true) -> Lucide.MessageSquare
        else -> Lucide.Sparkles
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Semicircle gauge
            val score = if (result.score > 0) result.score else if (isDanger) 92 else 65
            RiskGauge(score = score, level = result.level)

            // Dynamic Tags: [Category] [Source]
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryBadgeBg,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            categoryIcon,
                            contentDescription = null,
                            tint = categoryBadgeFg,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = categoryBadgeFg,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            sourceIcon,
                            contentDescription = null,
                            tint = SigapTheme.colors.textMuted,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = sourceName,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            // Dynamic Time Note
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
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }
        }
    }
}

/** Section 1: Kenapa ini ditandai (Dynamic Temuan based on real ReasonCodes) */
@Composable
private fun WhyFlaggedSection(
    reasons: List<ReasonCode>,
    category: ScamCategory,
    snippet: String,
) {
    val itemsCount = if (reasons.isNotEmpty()) reasons.size else 1

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    Lucide.ShieldAlert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Kenapa ini ditandai",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Text(
                    text = "$itemsCount Temuan",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = SigapTheme.colors.textMuted,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (reasons.isNotEmpty()) {
                    reasons.forEach { code ->
                        val (icon, iconBg, iconTint) = when (code) {
                            ReasonCode.APK_FILE -> Triple(
                                Lucide.FileText,
                                SigapTheme.colors.dangerContainer,
                                SigapTheme.colors.danger,
                            )
                            ReasonCode.CREDENTIAL_REQUEST, ReasonCode.OTP_SHARE_REQUEST -> Triple(
                                Lucide.Lock,
                                SigapTheme.colors.dangerContainer,
                                SigapTheme.colors.danger,
                            )
                            ReasonCode.LOOKALIKE_DOMAIN -> Triple(
                                Lucide.TriangleAlert,
                                SigapTheme.colors.dangerContainer,
                                SigapTheme.colors.danger,
                            )
                            ReasonCode.URGENCY -> Triple(
                                Lucide.Timer,
                                SigapTheme.colors.cautionContainer,
                                SigapTheme.colors.caution,
                            )
                            ReasonCode.SHORTENED_LINK, ReasonCode.IP_LINK, ReasonCode.RISKY_TLD,
                            ReasonCode.USER_INFO_URL, ReasonCode.PUNYCODE_URL -> Triple(
                                Lucide.TriangleAlert,
                                SigapTheme.colors.cautionContainer,
                                SigapTheme.colors.caution,
                            )
                            ReasonCode.FILE_LURE, ReasonCode.MONEY_REWARD, ReasonCode.SOCIAL_ENGINEERING -> Triple(
                                Lucide.CircleAlert,
                                SigapTheme.colors.cautionContainer,
                                SigapTheme.colors.caution,
                            )
                            ReasonCode.LINK_FROM_UNKNOWN, ReasonCode.IMPERSONATION -> Triple(
                                Lucide.PhoneOff,
                                MaterialTheme.colorScheme.surfaceContainer,
                                SigapTheme.colors.textMuted,
                            )
                        }

                        val reasonText = stringResource(code.stringRes())
                        FlaggedItem(
                            icon = icon,
                            iconBg = iconBg,
                            iconTint = iconTint,
                            title = buildAnnotatedString { append(reasonText) },
                        )
                    }
                } else {
                    // Fallback contextual finding when reasons list is empty
                    val (icon, desc) = when (category) {
                        ScamCategory.FAKE_COURIER -> Pair(
                            Lucide.TriangleAlert,
                            "Tautan pelacak paket tidak menggunakan domain resmi ekspedisi terverifikasi.",
                        )
                        ScamCategory.BANK_PHISHING -> Pair(
                            Lucide.Lock,
                            "Pesan meminta konfirmasi atau kredensial akun perbankan melalui jalur tidak resmi.",
                        )
                        ScamCategory.JOB_SCAM -> Pair(
                            Lucide.CircleAlert,
                            "Menawarkan imbalan keuangan instan yang tidak wajar untuk pekerjaan atau tugas ringan.",
                        )
                        ScamCategory.AUTHORITY_IMPERSONATION -> Pair(
                            Lucide.ShieldAlert,
                            "Mengaku dari aparat penegak hukum atau kantor dinas pemerintah dengan format berkas tidak lazim.",
                        )
                        ScamCategory.APK_MALWARE, ScamCategory.FAKE_INVITATION -> Pair(
                            Lucide.FileText,
                            "Pesan mencantumkan file APK berisiko tinggi yang dapat mencuri data dan SMS verifikasi ponsel.",
                        )
                        else -> Pair(
                            Lucide.ShieldAlert,
                            "Pola kalimat dan struktur pesan terindikasi rekayasa sosial manipulatif.",
                        )
                    }
                    FlaggedItem(
                        icon = icon,
                        iconBg = SigapTheme.colors.cautionContainer,
                        iconTint = SigapTheme.colors.caution,
                        title = buildAnnotatedString { append(desc) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FlaggedItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: androidx.compose.ui.text.AnnotatedString,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconBg, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Section 2: Pratinjau pesan (Displays real dynamic message snippet with highlights) */
@Composable
private fun MessagePreviewSection(snippet: String, sourcePackage: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    Lucide.MessageSquare,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Pratinjau pesan",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Lucide.Lock,
                    contentDescription = null,
                    tint = SigapTheme.colors.textMuted,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = "Terenkripsi",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Dynamic message bubble card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val apkRegex = Regex("""([a-zA-Z0-9_\-]+\.apk)""", RegexOption.IGNORE_CASE)
                        val urlRegex = Regex("""(https?://[^\s]+|bit\.ly/[^\s]+)""", RegexOption.IGNORE_CASE)

                        val apkMatch = apkRegex.find(snippet)
                        val urlMatch = if (apkMatch == null) urlRegex.find(snippet) else null

                        if (apkMatch != null) {
                            val apkName = apkMatch.value
                            val before = snippet.substring(0, apkMatch.range.first).trim()
                            val after = snippet.substring(apkMatch.range.last + 1).trim()

                            if (before.isNotEmpty()) {
                                Text(
                                    text = before,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SigapTheme.colors.dangerContainer.copy(alpha = 0.7f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        Lucide.FileText,
                                        contentDescription = null,
                                        tint = SigapTheme.colors.danger,
                                        modifier = Modifier.size(13.dp),
                                    )
                                    Text(
                                        text = apkName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                        color = SigapTheme.colors.danger,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                            if (after.isNotEmpty()) {
                                Text(
                                    text = after,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        } else if (urlMatch != null) {
                            val url = urlMatch.value
                            val before = snippet.substring(0, urlMatch.range.first).trim()
                            val after = snippet.substring(urlMatch.range.last + 1).trim()

                            if (before.isNotEmpty()) {
                                Text(
                                    text = before,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SigapTheme.colors.cautionContainer.copy(alpha = 0.7f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        Lucide.TriangleAlert,
                                        contentDescription = null,
                                        tint = SigapTheme.colors.caution,
                                        modifier = Modifier.size(13.dp),
                                    )
                                    Text(
                                        text = url,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                        color = SigapTheme.colors.onCaution,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                            if (after.isNotEmpty()) {
                                Text(
                                    text = after,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        } else {
                            Text(
                                text = snippet.ifBlank { "Pesan tidak memuat teks pratinjau tambahan." },
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Lucide.ShieldCheck,
                        contentDescription = null,
                        tint = SigapTheme.colors.safe,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "Diperiksa di perangkat (privasi terjaga sepenuhnya)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }
            }
        }
    }
}

/** Section 3: Yang perlu dilakukan (Dynamic recommendations by ScamCategory & RiskLevel) */
@Composable
private fun WhatToDoSection(
    category: ScamCategory,
    level: RiskLevel,
    reasons: List<ReasonCode>,
) {
    val steps = when {
        category == ScamCategory.APK_MALWARE || category == ScamCategory.FAKE_INVITATION || reasons.contains(ReasonCode.APK_FILE) -> listOf(
            "Jangan pernah sentuh, unduh, atau pasang file APK tersebut.",
            "Hapus percakapan dan blokir kontak pengirim agar tidak mengganggu lagi.",
            "Jika terlanjur pasang aplikasi, segera aktifkan panduan darurat di bawah ini.",
        )
        category == ScamCategory.BANK_PHISHING || category == ScamCategory.OTP_THEFT ||
            reasons.contains(ReasonCode.OTP_SHARE_REQUEST) || reasons.contains(ReasonCode.CREDENTIAL_REQUEST) -> listOf(
            "Jangan pernah membuka tautan atau membagikan PIN, kata sandi, dan kode OTP kepada siapa pun.",
            "Pihak bank atau instansi resmi tidak pernah meminta informasi rahasia melalui chat.",
            "Jika terlanjur memasukkan data rekening, segera hubungi call center resmi bank untuk memblokir rekening.",
        )
        category == ScamCategory.FAKE_COURIER -> listOf(
            "Jangan klik tautan atau unduh resi pelacak palsu yang dikirimkan.",
            "Lacak status paket langsung melalui aplikasi atau situs web resmi ekspedisi terkait.",
            "Blokir nomor pengirim dan hapus pesan dari perangkat Anda.",
        )
        category == ScamCategory.JOB_SCAM || category == ScamCategory.INVESTMENT || category == ScamCategory.FAKE_PRIZE -> listOf(
            "Jangan tergiur janji komisi instan atau mentransfer deposit uang untuk tugas pekerjaan.",
            "Perusahaan resmi tidak pernah memungut biaya atau meminta deposit untuk proses rekrutmen.",
            "Tinggalkan grup pesan dan laporkan pengirim ke database komunitas.",
        )
        category == ScamCategory.AUTHORITY_IMPERSONATION -> listOf(
            "Instansi resmi (Polisi/Kejaksaan/DJP) tidak pernah mengirimkan surat tilang atau berkas hukum berupa APK.",
            "Konfirmasi status denda tilang langsung di portal resmi e-Tilang Nasional (etle-pmj.info).",
            "Abaikan pesan, jangan pasang berkas apapun, dan segera blokir kontak pengirim.",
        )
        level == RiskLevel.DANGER -> listOf(
            "Jangan mengeklik tautan atau mengunduh lampiran apapun dari pesan ini.",
            "Hapus pesan dan blokir nomor pengirim dari kontak Anda.",
            "Jika merasa terancam, gunakan tombol Darurat di bawah untuk panduan isolasi perangkat.",
        )
        else -> listOf(
            "Berhati-hatilah dan jangan membagikan data pribadi yang diminta.",
            "Verifikasi kebenaran informasi melalui kontak resmi yang sudah Anda percaya.",
            "Laporkan pesan ini ke komunitas agar pengguna lain terlindungi.",
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                Lucide.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "Yang perlu dilakukan",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            steps.forEachIndexed { index, step ->
                ActionCheckCard(
                    number = "${index + 1}",
                    text = step,
                )
            }
        }
    }
}

@Composable
private fun ActionCheckCard(number: String, text: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Section 4: Perlindungan Aktif Status Card */
@Composable
private fun ProtectionActiveBanner() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SigapTheme.colors.safeContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, SigapTheme.colors.safe.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Lucide.ShieldCheck,
                    contentDescription = null,
                    tint = SigapTheme.colors.safe,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Perlindungan Aktif",
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Pemindai otomatis telah mengisolasi berkas terkait dari galeri dan unduhan berkas ponsel.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }
        }
    }
}
