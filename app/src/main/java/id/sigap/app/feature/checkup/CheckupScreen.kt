package id.sigap.app.feature.checkup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquare
import com.composables.icons.lucide.Send
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Smartphone
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.User
import id.sigap.app.R
import id.sigap.app.SigapApp
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.Spacing
import id.sigap.app.core.util.PermissionChecks
import id.sigap.app.domain.checkup.CheckIds
import id.sigap.app.domain.checkup.CheckKind
import id.sigap.app.domain.checkup.CheckupItem
import id.sigap.app.domain.checkup.CheckupLabel

/**
 * S09: Security Checkup Screen matching Image 6.
 */
@Composable
fun CheckupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = (context.applicationContext as SigapApp).container
    val viewModel: CheckupViewModel = viewModel(
        factory = viewModelFactory { initializer { CheckupViewModel(container.checkupSource, container.settings) } },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    androidx.compose.runtime.DisposableEffect(Unit) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        val lifecycle = (context as? androidx.lifecycle.LifecycleOwner)?.lifecycle
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        // App Bar
        TopBar(title = "Cek Keamanan", onBack = onBack)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Gauge Score Card
            GaugeScoreCard(
                score = state.score.coerceIn(0, 100),
                label = if (state.label == CheckupLabel.GOOD) "BAGUS" else "PERIKSA",
                needsAttention = state.score < 90,
            )

            // Section 1: Dicek Otomatis oleh Sigap
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Dicek Otomatis oleh Sigap",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = "4 parameter",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        // 1. Akses notifikasi Sigap
                        val notifItem = state.autoChecks.find { it.id == CheckIds.LISTENER }
                        val isNotifPassed = notifItem?.passed ?: true
                        AutoCheckRow(
                            icon = Lucide.Check,
                            iconBg = if (isNotifPassed) Color(0xFFD5EBE2) else Color(0xFFFFECC8),
                            iconTint = if (isNotifPassed) Color(0xFF14594A) else Color(0xFF945200),
                            title = "Akses notifikasi Sigap",
                            subtitle = if (isNotifPassed) "Aktif memantau pesan masuk secara pasif" else "Izin membaca notifikasi diperlukan",
                            badgeText = if (isNotifPassed) "Aman" else "Perlu Aksi",
                            isSafe = isNotifPassed,
                            onAction = { context.startActivity(PermissionChecks.listenerSettingsIntent()) },
                        )

                        // 2. Kunci layar ponsel
                        val lockItem = state.autoChecks.find { it.id == CheckIds.SCREEN_LOCK }
                        val isLockPassed = lockItem?.passed ?: true
                        AutoCheckRow(
                            icon = Lucide.Lock,
                            iconBg = if (isLockPassed) Color(0xFFD5EBE2) else Color(0xFFFFECC8),
                            iconTint = if (isLockPassed) Color(0xFF14594A) else Color(0xFF945200),
                            title = "Kunci layar ponsel",
                            subtitle = if (isLockPassed) "Biometrik dan PIN aktif" else "Ponsel belum menggunakan PIN / pola",
                            badgeText = if (isLockPassed) "Aman" else "Atur",
                            isSafe = isLockPassed,
                            onAction = { context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS)) },
                        )

                        // 3. Patch keamanan Android
                        val patchItem = state.autoChecks.find { it.id == CheckIds.PATCH }
                        val isPatchPassed = patchItem?.passed ?: false
                        AutoCheckRow(
                            icon = Lucide.TriangleAlert,
                            iconBg = if (isPatchPassed) Color(0xFFD5EBE2) else Color(0xFFFFECC8),
                            iconTint = if (isPatchPassed) Color(0xFF14594A) else Color(0xFF945200),
                            title = "Patch keamanan Android",
                            subtitle = if (isPatchPassed) "Sistem Android mutakhir" else "Terakhir diperbarui beberapa bulan lalu",
                            buttonLabel = if (!isPatchPassed) "Perbarui" else null,
                            badgeText = if (isPatchPassed) "Aman" else null,
                            isSafe = isPatchPassed,
                            onAction = { context.startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)) },
                        )

                        // 4. Layanan aksesibilitas
                        val a11yItem = state.autoChecks.find { it.id == CheckIds.ACCESSIBILITY }
                        val isA11yPassed = a11yItem?.passed ?: false
                        AutoCheckRow(
                            icon = Lucide.ShieldAlert,
                            iconBg = if (isA11yPassed) Color(0xFFD5EBE2) else Color(0xFFFFE0DF),
                            iconTint = if (isA11yPassed) Color(0xFF14594A) else Color(0xFFB3261E),
                            title = "Layanan aksesibilitas",
                            hasRedDot = !isA11yPassed,
                            subtitle = if (isA11yPassed) "Tidak ada aplikasi mencurigakan" else "1 aplikasi tak dikenal memiliki izin kontrol layar",
                            buttonLabel = if (!isA11yPassed) "Tinjau" else null,
                            buttonIsDanger = true,
                            badgeText = if (isA11yPassed) "Aman" else null,
                            isSafe = isA11yPassed,
                            onAction = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                        )
                    }
                }
            }

            // Section 2: Konfirmasi Mandiri
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Konfirmasi Mandiri",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Periksa pengaturan penting di bawah ini secara berkala:",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Blokir instal aplikasi dari sumber tak dikenal",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Pastikan aplikasi berikut tidak diberi izin instal APK mandiri",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = SigapTheme.colors.textMuted,
                            )
                        }

                        // App 1: WhatsApp
                        val waItem = state.selfChecks.find { it.packageName?.contains("whatsapp", ignoreCase = true) == true }
                        val isWaChecked = waItem?.passed ?: true
                        SelfCheckAppRow(
                            icon = Lucide.MessageSquare,
                            iconBg = MaterialTheme.colorScheme.primaryContainer,
                            iconTint = MaterialTheme.colorScheme.primary,
                            name = "WhatsApp",
                            statusText = if (isWaChecked) "Sumber ini: Nonaktif" else "Sumber ini: Memiliki izin",
                            isConfirmed = isWaChecked,
                            onToggle = {
                                if (waItem != null) viewModel.toggleConfirmed(waItem.id, !isWaChecked)
                            },
                        )

                        // App 2: Google Chrome
                        val chromeItem = state.selfChecks.find { it.packageName?.contains("chrome", ignoreCase = true) == true }
                        val isChromeChecked = chromeItem?.passed ?: true
                        SelfCheckAppRow(
                            icon = Lucide.Globe,
                            iconBg = MaterialTheme.colorScheme.primaryContainer,
                            iconTint = MaterialTheme.colorScheme.primary,
                            name = "Google Chrome",
                            statusText = if (isChromeChecked) "Sumber ini: Nonaktif" else "Sumber ini: Memiliki izin",
                            isConfirmed = isChromeChecked,
                            onToggle = {
                                if (chromeItem != null) viewModel.toggleConfirmed(chromeItem.id, !isChromeChecked)
                            },
                        )

                        // App 3: Telegram
                        val teleItem = state.selfChecks.find { it.packageName?.contains("telegram", ignoreCase = true) == true }
                        val isTeleChecked = teleItem?.passed ?: true
                        SelfCheckAppRow(
                            icon = Lucide.Send,
                            iconBg = MaterialTheme.colorScheme.primaryContainer,
                            iconTint = MaterialTheme.colorScheme.primary,
                            name = "Telegram",
                            statusText = if (isTeleChecked) "Sumber ini: Nonaktif" else "Sumber ini: Memiliki izin",
                            isConfirmed = isTeleChecked,
                            onToggle = {
                                if (teleItem != null) viewModel.toggleConfirmed(teleItem.id, !isTeleChecked)
                            },
                        )

                        // Google Play Protect toggle switch row
                        val protectItem = state.selfChecks.find { it.id == CheckIds.PLAY_PROTECT }
                        val isProtectOn = protectItem?.passed ?: true
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Box(
                                        Modifier
                                            .size(34.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Lucide.ShieldCheck,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Google Play Protect",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = "Aktif memindai harian",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = SigapTheme.colors.textMuted,
                                        )
                                    }
                                }

                                Switch(
                                    checked = isProtectOn,
                                    onCheckedChange = {
                                        viewModel.toggleConfirmed(CheckIds.PLAY_PROTECT, it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                                        uncheckedThumbColor = SigapTheme.colors.textMuted,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                                        uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            // Footnote Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        Lucide.Shield,
                        contentDescription = null,
                        tint = SigapTheme.colors.textMuted,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Pemeriksaan ini dilakukan sepenuhnya di perangkat Anda dan tidak mengirim data ponsel ke internet.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Pemeriksaan Keamanan Sistem", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Skor keamanan perangkat dihitung berdasarkan parameter esensial perlindungan dari malware dan spyware:\n\n" +
                        "1. Izin Pasang Aplikasi Tidak Dikenal: Memastikan instalasi berkas .apk dari browser/chat dinonaktifkan.\n" +
                        "2. Status Google Play Protect: Memastikan perlindungan pemindaian aplikasi resmi Google tetap aktif.\n" +
                        "3. Izin Pendengar Notifikasi: Memastikan Sigap dapat memfilter pesan penipuan secara langsung.\n" +
                        "4. Izin Aksesibilitas Berisiko: Mendeteksi jika ada aplikasi pihak ketiga yang menyalahgunakan hak kontrol layar.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Saya Mengerti")
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
            IconButton(onClick = { showInfoDialog = true }) {
                Icon(
                    Lucide.Info,
                    contentDescription = "Informasi Checkup",
                    tint = MaterialTheme.colorScheme.onSurface,
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
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun GaugeScoreCard(
    score: Int,
    label: String,
    needsAttention: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
    ) {
        Column(
            Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Circular Arc Gauge
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp)) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)

                Canvas(modifier = Modifier.size(120.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    // Background track arc
                    drawArc(
                        color = trackColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                    // Active progress arc
                    drawArc(
                        color = primaryColor,
                        startAngle = 135f,
                        sweepAngle = 270f * (score.toFloat() / 100f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (needsAttention) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFECC8),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF945200),
                            fontSize = 12.sp,
                        )
                        Text(
                            text = "Perlu Perhatian",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = Color(0xFF945200),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Text(
                text = "2 langkah perbaikan disarankan untuk perlindungan maksimal dari malware APK.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                color = SigapTheme.colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun AutoCheckRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    hasRedDot: Boolean = false,
    badgeText: String? = null,
    buttonLabel: String? = null,
    buttonIsDanger: Boolean = false,
    isSafe: Boolean = true,
    onAction: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onAction)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(38.dp)
                    .background(iconBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (hasRedDot) {
                        Box(Modifier.size(6.dp).background(Color(0xFFBA1A1A), CircleShape))
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }
        }

        if (badgeText != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSafe) Color(0xFFD5EBE2) else Color(0xFFFFECC8),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (isSafe) {
                        Icon(
                            Lucide.Check,
                            contentDescription = null,
                            tint = Color(0xFF14594A),
                            modifier = Modifier.size(12.dp),
                        )
                    }
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = if (isSafe) Color(0xFF14594A) else Color(0xFF945200),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        } else if (buttonLabel != null) {
            Surface(
                onClick = onAction,
                shape = RoundedCornerShape(8.dp),
                color = if (buttonIsDanger) Color(0xFFFFE0DF) else MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Text(
                    text = buttonLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = if (buttonIsDanger) Color(0xFFBA1A1A) else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun SelfCheckAppRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    name: String,
    statusText: String,
    isConfirmed: Boolean,
    onToggle: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier
                        .size(34.dp)
                        .background(iconBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }
            }

            Surface(
                onClick = onToggle,
                shape = RoundedCornerShape(8.dp),
                color = if (isConfirmed) SigapTheme.colors.safeContainer else SigapTheme.colors.cautionContainer,
                border = BorderStroke(
                    1.dp,
                    if (isConfirmed) SigapTheme.colors.safe.copy(alpha = 0.3f) else SigapTheme.colors.caution.copy(alpha = 0.4f),
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (isConfirmed) {
                        Icon(
                            Lucide.Check,
                            contentDescription = null,
                            tint = SigapTheme.colors.safe,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                    Text(
                        text = if (isConfirmed) "Sudah" else "Belum",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = if (isConfirmed) SigapTheme.colors.safe else SigapTheme.colors.onCaution,
                    )
                }
            }
        }
    }
}
