package id.sigap.app.feature.check

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.ClipboardPaste
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.QrCode
import com.composables.icons.lucide.Scan
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Smartphone
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.User
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import id.sigap.app.R
import id.sigap.app.SigapApp
import id.sigap.app.core.designsystem.EmptyState
import id.sigap.app.core.designsystem.PrimaryButton
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.Spacing
import id.sigap.app.core.navigation.LaunchRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * S06: manual check hub matching Image 1 (Periksa Tautan / Periksa sesuatu).
 */
@Composable
fun CheckScreen(
    shared: LaunchRequest?,
    onSharedHandled: () -> Unit,
    onBack: () -> Unit,
    onResult: (Long) -> Unit,
    onOpenLookup: () -> Unit,
) {
    val context = LocalContext.current
    val container = (context.applicationContext as SigapApp).container
    val viewModel: CheckViewModel = viewModel(
        factory = viewModelFactory { initializer { CheckViewModel(container.analyzeMessage, container.imageTextExtractor) } },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showInputSheet by remember { mutableStateOf(false) }
    var inputManualText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.results.collect { onResult(it) } }
    LaunchedEffect(shared) {
        when (shared) {
            is LaunchRequest.SharedText -> viewModel.checkText(shared.text)
            is LaunchRequest.SharedImage -> viewModel.checkImage(shared.uri)
            else -> return@LaunchedEffect
        }
        onSharedHandled()
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::checkImage)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top App Bar
            TopBar(
                title = "Periksa Tautan",
                onBack = onBack,
                onOpenLookup = onOpenLookup,
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.ScreenHorizontal),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header section
                HeaderSection()

                // Option 1: Pilih tangkapan layar (Rekomendasi)
                OptionCard(
                    icon = Lucide.Smartphone,
                    title = "Pilih tangkapan layar",
                    description = "Teks dibaca dan dianalisis langsung di perangkatmu tanpa diunggah ke internet.",
                    badge = "Rekomendasi",
                    badgeIcon = Lucide.Sparkles,
                    footer = "Privasi terlindungi via on-device AI",
                    onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                )

                // Option 2: Pindai kode QR
                OptionCard(
                    icon = Lucide.QrCode,
                    title = "Pindai kode QR",
                    description = "Periksa alamat situs di balik QR code sebelum browser membuka tautan.",
                    onClick = {
                        scanQr(context, viewModel::checkText)
                    },
                )

                // Option 3: Cek nomor, rekening, atau tautan
                OptionCard(
                    icon = Lucide.ShieldAlert,
                    title = "Cek nomor, rekening, atau...",
                    description = "Pencarian cepat database penipuan terverifikasi dan laporan komunitas.",
                    onClick = onOpenLookup,
                )

                // Dynamic Live Analysis Progress Card
                AnimatedVisibility(
                    visible = state is CheckUiState.Analyzing,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    val analyzing = state as? CheckUiState.Analyzing
                    if (analyzing != null) {
                        LiveAnalysisCard(
                            analyzingState = analyzing,
                            onCancel = { viewModel.reset() },
                        )
                    }
                }

                // Error / No Text Feedback Card
                AnimatedVisibility(
                    visible = state is CheckUiState.NoText || state is CheckUiState.Error,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SigapTheme.colors.dangerContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, SigapTheme.colors.danger.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(Lucide.TriangleAlert, contentDescription = null, tint = SigapTheme.colors.danger, modifier = Modifier.size(20.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = if (state is CheckUiState.NoText) "Teks Tidak Ditemukan" else "Gagal Membaca Gambar",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = if (state is CheckUiState.NoText) "Pastikan gambar memuat tulisan yang jelas atau gunakan tempel teks di bawah." else "Terjadi kendala saat memproses gambar. Silakan coba lagi.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = SigapTheme.colors.textMuted,
                                )
                            }
                            TextButton(onClick = { viewModel.reset() }) {
                                Text("Tutup", color = SigapTheme.colors.danger, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Interactive Manual Input Box with Quick Presets
                AnimatedVisibility(visible = showInputSheet) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "Ketik / Tempel Teks untuk Dianalisis",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = clipManager.primaryClip
                                            val text = if (clip != null && clip.itemCount > 0) {
                                                clip.getItemAt(0).text?.toString().orEmpty().trim()
                                            } else ""
                                            if (text.isNotBlank()) {
                                                inputManualText = text
                                            } else {
                                                scope.launch { snackbarHostState.showSnackbar("Papan klip kosong.") }
                                            }
                                        },
                                ) {
                                    Text(
                                        "Tempel",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    )
                                }
                            }

                            // Quick Preset Chips for testing
                            Text(
                                "Atau coba contoh jebakan scam:",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = SigapTheme.colors.textMuted,
                            )
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            inputManualText = "Kepada Yth. Bapak/Ibu sekalian, kami mengundang kehadiran Anda di acara pernikahan kami: Undangan_Pernikahan.apk Mohon doa restunya."
                                            viewModel.checkText(inputManualText)
                                        },
                                ) {
                                    Text(
                                        "✉️ Undangan APK",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                        maxLines = 1,
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            inputManualText = "Paket kurir J&T Anda tertahan di gudang DC Cakung karena alamat belum lengkap. Verifikasi segera: https://bit.ly/paket-jnt-resi"
                                            viewModel.checkText(inputManualText)
                                        },
                                ) {
                                    Text(
                                        "📦 Kurir J&T",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                        maxLines = 1,
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            inputManualText = "Pemberitahuan tilang elektronik ETLE Polda Metro Jaya. Kendaraan Anda melanggar aturan. Buka berkas surat_tilang.apk"
                                            viewModel.checkText(inputManualText)
                                        },
                                ) {
                                    Text(
                                        "🚨 Tilang ETLE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                        maxLines = 1,
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = inputManualText,
                                onValueChange = { inputManualText = it },
                                placeholder = { Text("Ketik atau tempel isi pesan mencurigakan, link web, atau nomor...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                ),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Button(
                                    onClick = { showInputSheet = false },
                                    colors = ButtonDefaults.outlinedButtonColors(),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text("Tutup")
                                }
                                Button(
                                    onClick = {
                                        if (inputManualText.isNotBlank()) {
                                            viewModel.checkText(inputManualText)
                                        }
                                    },
                                    enabled = inputManualText.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text("Analisis Sekarang")
                                }
                            }
                        }
                    }
                }

                // Tempel teks langsung dari papan klip
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = clipManager.primaryClip
                            val text = if (clip != null && clip.itemCount > 0) {
                                clip.getItemAt(0).text?.toString().orEmpty().trim()
                            } else ""

                            if (text.isNotBlank()) {
                                inputManualText = text
                                showInputSheet = true
                                viewModel.checkText(text)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Teks berhasil ditempel dari papan klip dan mulai dianalisis...")
                                }
                            } else {
                                showInputSheet = true
                                scope.launch {
                                    snackbarHostState.showSnackbar("Papan klip kosong. Silakan ketik atau pilih contoh di atas.")
                                }
                            }
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Lucide.ClipboardPaste,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Tempel teks langsung dari papan klip",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Footer Supported Format note
                Text(
                    text = "Format yang didukung: Teks SMS, URL web, nomor rekening bank, dan nomor telepon.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                    color = SigapTheme.colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, start = 8.dp, end = 8.dp),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
        )
    }
}

private fun scanQr(context: Context, onValue: (String) -> Unit) {
    GmsBarcodeScanning.getClient(context).startScan()
        .addOnSuccessListener { barcode -> barcode.rawValue?.let(onValue) }
}

@Composable
private fun TopBar(
    title: String,
    onBack: () -> Unit,
    onOpenLookup: () -> Unit = {},
) {
    var showMenu by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }

    if (showGuideDialog) {
        AlertDialog(
            onDismissRequest = { showGuideDialog = false },
            title = { Text("Panduan Pemeriksaan Pesan", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Anda dapat memeriksa keamanan dengan 2 cara mudah:\n\n" +
                        "1. Unggah Tangkapan Layar (Screenshot): Deteksi otomatis teks percakapan dan berkas mencurigakan langsung dari gambar galeri.\n" +
                        "2. Tempel Teks / Tautan Manual: Salin teks SMS, pesan WhatsApp, atau link pendek yang Anda terima untuk mendeteksi indikator bahaya on-device.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showGuideDialog = false }) {
                    Text("Mengerti")
                }
            },
        )
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Lucide.ArrowLeft,
                    contentDescription = stringResource(R.string.action_back),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Lucide.EllipsisVertical,
                        contentDescription = "Menu Opsi",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Cek Nomor / Rekening Resmi") },
                        onClick = {
                            showMenu = false
                            onOpenLookup()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Panduan Deteksi Pesan") },
                        onClick = {
                            showMenu = false
                            showGuideDialog = true
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
private fun HeaderSection() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "PEMERIKSAAN MANDIRI",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = SigapTheme.colors.textMuted,
        )
        Text(
            text = "Periksa sesuatu",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Untuk pesan, dokumen, atau tautan mencurigakan yang belum terdeteksi otomatis oleh Sigap.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                lineHeight = 18.sp,
            ),
            color = SigapTheme.colors.textMuted,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun OptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    badge: String? = null,
    badgeIcon: ImageVector? = null,
    footer: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    Modifier
                        .size(46.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (badge != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                if (badgeIcon != null) {
                                    Icon(
                                        badgeIcon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp),
                                    )
                                }
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    Icon(
                        Lucide.ChevronRight,
                        contentDescription = null,
                        tint = SigapTheme.colors.textMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }

            if (footer != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    Icon(
                        Lucide.Clock,
                        contentDescription = null,
                        tint = SigapTheme.colors.textMuted,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = footer,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }
            }
        }
    }
}

private enum class StepState {
    PENDING,
    RUNNING,
    COMPLETED,
}

@Composable
private fun LiveAnalysisCard(
    analyzingState: CheckUiState.Analyzing,
    onCancel: () -> Unit,
) {
    val stage = analyzingState.stage
    val isImage = analyzingState.isImage

    val step1State = when {
        stage == AnalysisStage.EXTRACTING_TEXT -> StepState.RUNNING
        stage > AnalysisStage.EXTRACTING_TEXT -> StepState.COMPLETED
        else -> StepState.PENDING
    }

    val step2State = when {
        stage < AnalysisStage.CHECKING_PATTERNS -> StepState.PENDING
        stage == AnalysisStage.CHECKING_PATTERNS -> StepState.RUNNING
        else -> StepState.COMPLETED
    }

    val step3State = when {
        stage < AnalysisStage.CALCULATING_SCORE -> StepState.PENDING
        stage == AnalysisStage.CALCULATING_SCORE -> StepState.RUNNING
        else -> StepState.COMPLETED
    }

    val titleStep1 = if (isImage) "Membaca teks dari gambar (OCR)" else "Membaca teks masukan"
    val titleStep2 = "Memeriksa tautan & berkas mencurigakan"
    val titleStep3 = "Menilai skor risiko & keamanan AI"

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header: Scanner icon & Status Title
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Lucide.Scan,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column {
                    Text(
                        "ANALISIS BERLANGSUNG",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        analyzingState.progressText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Step 1: Membaca teks
            AnalysisStepRow(
                title = titleStep1,
                state = step1State,
            )

            // Connector 1
            Box(
                Modifier
                    .padding(start = 11.dp)
                    .width(2.dp)
                    .height(10.dp)
                    .background(if (step1State == StepState.COMPLETED) SigapTheme.colors.safe else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            )

            // Step 2: Memeriksa tautan
            AnalysisStepRow(
                title = titleStep2,
                state = step2State,
            )

            // Connector 2
            Box(
                Modifier
                    .padding(start = 11.dp)
                    .width(2.dp)
                    .height(10.dp)
                    .background(if (step2State == StepState.COMPLETED) SigapTheme.colors.safe else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            )

            // Step 3: Menilai skor risiko
            AnalysisStepRow(
                title = titleStep3,
                state = step3State,
            )

            Spacer(Modifier.height(4.dp))

            // Footer inside card: Enkripsi memori aktif & Batalkan button
            Row(
                Modifier.fillMaxWidth(),
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
                        "Enkripsi memori on-device aktif",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onCancel),
                ) {
                    Text(
                        text = "Batalkan",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalysisStepRow(
    title: String,
    state: StepState,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f, fill = false),
        ) {
            when (state) {
                StepState.COMPLETED -> {
                    Box(
                        Modifier
                            .size(24.dp)
                            .background(SigapTheme.colors.safeContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Lucide.Check,
                            contentDescription = null,
                            tint = SigapTheme.colors.safe,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                StepState.RUNNING -> {
                    Box(
                        Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                StepState.PENDING -> {
                    Box(
                        Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .background(SigapTheme.colors.textMuted, CircleShape)
                        )
                    }
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = if (state == StepState.PENDING) SigapTheme.colors.textMuted else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (state == StepState.RUNNING) FontWeight.Bold else FontWeight.Medium,
            )
        }

        when (state) {
            StepState.COMPLETED -> StatusBadge(
                text = "Selesai",
                bg = SigapTheme.colors.safeContainer,
                textColor = SigapTheme.colors.safe,
            )
            StepState.RUNNING -> StatusBadge(
                text = "Sedang berjalan...",
                bg = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.primary,
            )
            StepState.PENDING -> StatusBadge(
                text = "Menunggu",
                bg = MaterialTheme.colorScheme.surface,
                textColor = SigapTheme.colors.textMuted,
            )
        }
    }
}

@Composable
private fun StatusBadge(text: String, bg: Color, textColor: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
