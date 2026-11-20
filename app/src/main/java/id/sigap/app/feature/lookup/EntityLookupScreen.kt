package id.sigap.app.feature.lookup

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.ExternalLink
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Flag
import com.composables.icons.lucide.Lightbulb
import com.composables.icons.lucide.Link
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquare
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Smartphone
import com.composables.icons.lucide.User
import com.composables.icons.lucide.X
import id.sigap.app.R
import id.sigap.app.SigapApp
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.Spacing
import id.sigap.app.core.util.stringRes
import id.sigap.app.domain.community.CommunityHit
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScamCategory
import id.sigap.app.domain.normalization.EntityType
import id.sigap.app.domain.normalization.NormalizedEntity
import id.sigap.app.feature.report.ReportBottomSheet
import kotlinx.coroutines.launch

private const val CEK_REKENING_URL = "https://cekrekening.id"

/** S06b: Periksa Tautan, Nomor, & Rekening dengan verifikasi integritas & database terpadu */
@Composable
fun EntityLookupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = (context.applicationContext as SigapApp).container
    val keyboardController = LocalSoftwareKeyboardController.current
    val isDemoMode = remember { container.settingsState.value.demoMode }

    var selectedType by remember { mutableStateOf(EntityType.LINK) }
    var input by remember { mutableStateOf(if (isDemoMode) "https://bit.ly/paket-jnt-resi" else "") }
    var searchedEntity by remember { mutableStateOf<NormalizedEntity?>(null) }
    var hit by remember { mutableStateOf<CommunityHit?>(null) }
    var hasSearched by remember { mutableStateOf(false) }
    var invalid by remember { mutableStateOf(false) }
    var showReportSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Run initial search only when Demo Mode is enabled
    LaunchedEffect(isDemoMode) {
        if (isDemoMode && input.isNotBlank()) {
            val normalized = container.entityNormalizer.normalize(selectedType, input)
            if (normalized != null) {
                searchedEntity = normalized
                hit = container.community.lookup(normalized)
                hasSearched = true
            }
        }
    }

    val doSearch: () -> Unit = {
        keyboardController?.hide()
        val normalized = container.entityNormalizer.normalize(selectedType, input.trim())
        if (normalized == null) {
            invalid = true
        } else {
            invalid = false
            searchedEntity = normalized
            scope.launch {
                hit = container.community.lookup(normalized)
                hasSearched = true
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        // App Bar: Back button, Title "Periksa Tautan / Nomor / Rekening", More icon & User avatar
        TopBar(
            title = when (selectedType) {
                EntityType.LINK -> "Periksa Tautan"
                EntityType.PHONE -> "Periksa Nomor"
                EntityType.BANK_ACCOUNT -> "Periksa Rekening"
            },
            onBack = onBack,
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header: Verifikasi Integritas + Database Terpadu badge
            HeaderDescription()

            // Segmented Tab Control: [Nomor] [Rekening] [Tautan]
            SegmentedTypeSelector(
                selected = selectedType,
                onSelect = {
                    selectedType = it
                    hasSearched = false
                    invalid = false
                    input = if (isDemoMode) {
                        when (it) {
                            EntityType.LINK -> "https://bit.ly/paket-jnt-resi"
                            EntityType.PHONE -> "+62 821-4820-9921"
                            EntityType.BANK_ACCOUNT -> "1234567890"
                        }
                    } else {
                        ""
                    }
                },
            )

            // Search Box with Link icon, clear button, and "Cek ->" button
            SearchBox(
                value = input,
                onValueChange = { input = it; invalid = false },
                onClear = { input = "" },
                onSearch = doSearch,
                invalid = invalid,
            )

            // Result Content
            if (hasSearched && searchedEntity != null) {
                val currentHit = hit
                if (currentHit != null) {
                    FoundReportResultCard(
                        hit = currentHit,
                        entity = searchedEntity!!,
                        onReportThis = { showReportSheet = true },
                        onOpenExternal = {
                            val targetUrl = when (searchedEntity!!.type) {
                                EntityType.BANK_ACCOUNT -> "https://cekrekening.id"
                                EntityType.PHONE -> "https://aduannomor.id"
                                EntityType.LINK -> "https://transparencyreport.google.com/safe-browsing/search"
                            }
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)))
                        },
                    )
                } else {
                    NotFoundReportResultCard(
                        entity = searchedEntity!!,
                        onReportThis = { showReportSheet = true },
                    )
                }
            }

            // Bottom Note: Catatan Keamanan Digital
            DigitalSecurityNote()

            Spacer(Modifier.height(Spacing.Section))
        }
    }

    if (showReportSheet) {
        ReportBottomSheet(
            initialType = selectedType,
            initialValue = input,
            onDismiss = { showReportSheet = false },
            onReported = { updated -> hit = updated; hasSearched = true },
        )
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Basis Data Satgas PASTI & IASC", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Data entitas legalitas, rekening terindikasi penipuan, dan nomor telepon berisiko dikompilasi secara berkala dari:\n\n" +
                        "1. Satgas PASTI (Satuan Tugas Pemberantasan Aktivitas Keuangan Ilegal) Otoritas Jasa Keuangan (OJK).\n" +
                        "2. Indonesia Anti-Scam Center (IASC) di bawah koordinasi OJK dan Komdigi RI.\n" +
                        "3. Laporan masyarakat terverifikasi dan basis data penipu terdaftar.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Tutup")
                }
            },
        )
    }

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
                text = title,
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
                        contentDescription = "Menu Entitas",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Hubungi Kontak OJK 157") },
                        onClick = {
                            showMenu = false
                            try {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:157"))
                                context.startActivity(dialIntent)
                            } catch (_: Exception) {}
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Kunjungi Portal Resmi Satgas PASTI") },
                        onClick = {
                            showMenu = false
                            try {
                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ojk.go.id"))
                                context.startActivity(webIntent)
                            } catch (_: Exception) {}
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Tentang Basis Data") },
                        onClick = {
                            showMenu = false
                            showAboutDialog = true
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

@Composable
private fun HeaderDescription() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "VERIFIKASI INTEGRITAS",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                fontWeight = FontWeight.Bold,
                color = SigapTheme.colors.textMuted,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Lucide.ShieldCheck,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "Database Terpadu",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Text(
            text = "Periksa reputasi kontak, rekening bank, atau domain mencurigakan secara terverifikasi.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
            color = SigapTheme.colors.textMuted,
        )
    }
}

@Composable
private fun SegmentedTypeSelector(
    selected: EntityType,
    onSelect: (EntityType) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf(
                EntityType.PHONE to "Nomor",
                EntityType.BANK_ACCOUNT to "Rekening",
                EntityType.LINK to "Tautan",
            ).forEach { (type, label) ->
                val isSelected = selected == type
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelect(type) },
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else SigapTheme.colors.textMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBox(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    invalid: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, if (invalid) SigapTheme.colors.danger else MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Lucide.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("https://...", style = MaterialTheme.typography.bodyMedium) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                )

                if (value.isNotEmpty()) {
                    IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Lucide.X,
                            contentDescription = "Hapus",
                            tint = SigapTheme.colors.textMuted,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                Button(
                    onClick = onSearch,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier.height(42.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("Cek", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Icon(Lucide.ArrowRight, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
        if (invalid) {
            Text(
                "Format input tidak valid. Periksa kembali tautan atau nomor Anda.",
                style = MaterialTheme.typography.labelSmall,
                color = SigapTheme.colors.danger,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

/** Card when reports are found in community database (Matches reference image with dynamic data) */
@Composable
private fun FoundReportResultCard(
    hit: CommunityHit,
    entity: NormalizedEntity,
    onReportThis: () -> Unit,
    onOpenExternal: () -> Unit,
) {
    val categoryName = stringResource(hit.topCategory.stringRes())

    val firstSeenText = DateUtils.getRelativeTimeSpanString(
        hit.firstSeen,
        System.currentTimeMillis(),
        DateUtils.DAY_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()

    val lastSeenText = DateUtils.getRelativeTimeSpanString(
        hit.lastSeen,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()

    val modusDescription = when (entity.type) {
        EntityType.PHONE -> when (hit.topCategory) {
            ScamCategory.FAKE_INVITATION, ScamCategory.APK_MALWARE ->
                "Nomor telepon aktif mengirimkan pesan manipulasi dengan berkas APK malware mencurigakan."
            ScamCategory.FAKE_COURIER ->
                "Nomor dilaporkan menghubungi korban dengan kedok konfirmasi paket atau resi pengiriman fiktif."
            ScamCategory.AUTHORITY_IMPERSONATION ->
                "Nomor mengatasnamakan instansi kepolisian, kejaksaan, atau kantor pajak untuk memeras korban."
            ScamCategory.BANK_PHISHING, ScamCategory.OTP_THEFT ->
                "Nomor terindikasi modus rekayasa sosial untuk mencuri kredensial perbankan dan kode OTP."
            ScamCategory.JOB_SCAM, ScamCategory.INVESTMENT ->
                "Nomor digunakan untuk menyebarkan tawaran pekerjaan freelance atau investasi dengan janji komisi instan."
            else ->
                "Nomor telepon ini memiliki rekam jejak aktivitas berbahaya dan telah dilaporkan berulang kali oleh masyarakat."
        }
        EntityType.BANK_ACCOUNT ->
            "Nomor rekening dilaporkan terkait transaksi transfer penipuan finansial dan telah diajukan pemblokiran oleh korban."
        EntityType.LINK -> when (hit.topCategory) {
            ScamCategory.BANK_PHISHING ->
                "Tautan palsu yang meniru portal resmi perbankan untuk menjebak korban agar memasukkan nomor kartu dan PIN."
            ScamCategory.FAKE_COURIER ->
                "Tautan mencurigakan yang mengarahkan korban mengunduh berkas pelacak resi palsu berekstensi malware."
            else ->
                "Tautan berbahaya yang dirancang untuk menyusupkan malware atau mencuri data pribadi pengunjung secara diam-diam."
        }
    }

    val (authorityTitle, authorityDesc) = when (entity.type) {
        EntityType.BANK_ACCOUNT -> Pair(
            "Peringatan Database OJK & CekRekening.id",
            "Rekening terindikasi tindak pidana penipuan finansial dan tercatat dalam penanganan Satgas PASTI Otoritas Jasa Keuangan."
        )
        EntityType.PHONE -> Pair(
            "Peringatan AduanNomor.id & Kominfo",
            "Nomor telepon terdaftar dalam basis data spam massal dan rekayasa sosial manipulatif yang diawasi regulator telekomunikasi."
        )
        EntityType.LINK -> Pair(
            "Peringatan Otoritas Siber Komdigi & Safe Browsing",
            "Tautan telah dilaporkan berbahaya dan masuk dalam daftar hitam penyebaran phishing serta malware resmi pemerintah."
        )
    }

    val (evidenceIcon, evidenceTitle) = when (entity.type) {
        EntityType.PHONE -> Pair(Lucide.MessageSquare, "Sampel Tangkapan Layar Pesan / Chat Pelaku")
        EntityType.BANK_ACCOUNT -> Pair(Lucide.FileText, "Bukti Riwayat Transfer & Pengaduan Korban")
        EntityType.LINK -> Pair(Lucide.Smartphone, "Sampel Halaman Web Phishing / Unduhan Berkas")
    }

    val externalLinkText = when (entity.type) {
        EntityType.BANK_ACCOUNT -> "Buka Rekap di CekRekening.id"
        EntityType.PHONE -> "Cek Status di AduanNomor.id"
        EntityType.LINK -> "Periksa di Google Transparency Report"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Badges: [Berbahaya] [Category] [Aktif]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SigapTheme.colors.dangerContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                Lucide.ShieldAlert,
                                contentDescription = null,
                                tint = SigapTheme.colors.danger,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = "Berbahaya",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = SigapTheme.colors.danger,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }

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
                        text = "Aktif",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            // Big Bold Heading: "Dilaporkan oleh X korban" + Dynamic Modus Description
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Dilaporkan oleh ${hit.reporterCount} korban",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = modusDescription,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }

            // Detection Timeline Box: [Pertama Terdeteksi] [Laporan Terakhir]
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "Pertama Terdeteksi",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = SigapTheme.colors.textMuted,
                        )
                        Text(
                            text = firstSeenText,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Laporan Terakhir",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = SigapTheme.colors.textMuted,
                        )
                        Text(
                            text = lastSeenText,
                            style = MaterialTheme.typography.titleSmall,
                            color = SigapTheme.colors.danger,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // Peringatan Otoritas Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SigapTheme.colors.dangerContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, SigapTheme.colors.danger.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        Lucide.ShieldAlert,
                        contentDescription = null,
                        tint = SigapTheme.colors.danger,
                        modifier = Modifier.size(18.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = authorityTitle,
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                            color = SigapTheme.colors.danger,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = authorityDesc,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            // Bukti Lampiran Pelapor with thumbnail
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Bukti Lampiran Pelapor",
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                ) {
                    Box {
                        Image(
                            painter = painterResource(R.drawable.sample_phishing_proof),
                            contentDescription = "Bukti Phishing",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    evidenceIcon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = evidenceTitle,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }

            // Button: [Laporkan Penipuan Ini]
            Button(
                onClick = onReportThis,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Lucide.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Laporkan Penipuan Ini", fontWeight = FontWeight.Bold)
                }
            }

            // Dynamic External Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenExternal)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = externalLinkText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Lucide.ExternalLink,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

/** Card when NO reports are found in community database */
@Composable
private fun NotFoundReportResultCard(
    entity: NormalizedEntity,
    onReportThis: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SigapTheme.colors.safeContainer,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Lucide.ShieldCheck,
                        contentDescription = null,
                        tint = SigapTheme.colors.safe,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = "Belum Ada Laporan Negatif",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.safe,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Text(
                text = entity.maskedDisplay,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Entitas ini belum pernah dilaporkan oleh komunitas Sigap maupun database otoritas siber.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = SigapTheme.colors.textMuted,
            )

            Button(
                onClick = onReportThis,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Lucide.Flag, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("Tetap Laporkan Entitas Ini", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }
    }
}

/** Bottom Note: Catatan Keamanan Digital */
@Composable
private fun DigitalSecurityNote() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                Modifier
                    .size(28.dp)
                    .background(SigapTheme.colors.cautionContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Lucide.Lightbulb,
                    contentDescription = null,
                    tint = SigapTheme.colors.caution,
                    modifier = Modifier.size(16.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Catatan Keamanan Digital",
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Jika pencarian menghasilkan \"Belum ada laporan\", hal ini tidak menjamin entitas tersebut 100% aman. Waspadai selalu permintaan transfer darurat atau izin aplikasi asing.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }
        }
    }
}
