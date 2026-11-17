package id.sigap.app.feature.simulation

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.Fingerprint
import com.composables.icons.lucide.Lightbulb
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Radio
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Smartphone
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.Users
import id.sigap.app.SigapApp
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.Spacing
import id.sigap.app.core.util.PermissionChecks
import id.sigap.app.domain.model.MessageInput
import id.sigap.app.domain.model.SenderKind
import kotlinx.coroutines.launch

private enum class SimulationFilter(val label: String) {
    ALL("Semua (3)"),
    TRIGGER("Pemicu Nyata"),
    HEADS_UP("Heads-Up"),
    LOCK_SCREEN("Layar Kunci"),
}

/**
 * Screen demonstrating and triggering live Android system notifications:
 * 1. Pemicu Notifikasi Nyata (kirim langsung ke bilah status Android)
 * 2. Mode Heads-Up (banner melayang dengan prioritas tinggi saat layar aktif)
 * 3. Layar Kunci (sensor privasi saat layar terkunci)
 * 4. Edukasi "Mengapa Tampil Berbeda?"
 */
@Composable
fun NotificationSimulationScreen(
    onBack: () -> Unit,
    onOpenAlert: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = (context.applicationContext as SigapApp).container
    val scope = rememberCoroutineScope()
    var selectedFilter by remember { mutableStateOf(SimulationFilter.ALL) }

    var hasPostNotificationPermission by remember {
        mutableStateOf(PermissionChecks.canPostNotifications(context))
    }
    var hasListenerPermission by remember {
        mutableStateOf(PermissionChecks.isNotificationListenerEnabled(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        hasPostNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Izin notifikasi diberikan! Anda sekarang dapat menerima peringatan nyata.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Izin notifikasi ditolak. Peringatan tidak dapat muncul di bilah status.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        hasPostNotificationPermission = PermissionChecks.canPostNotifications(context)
        hasListenerPermission = PermissionChecks.isNotificationListenerEnabled(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        // Top App Bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Lucide.ArrowLeft,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header: Android System View badge & title
            HeaderSection()

            // Filter Tabs: [Semua] [Pemicu Nyata] [Heads-Up] [Layar Kunci]
            FilterSection(
                selected = selectedFilter,
                onSelect = { selectedFilter = it },
            )

            // 0. Live Android System Push Trigger
            AnimatedVisibility(
                visible = selectedFilter == SimulationFilter.ALL || selectedFilter == SimulationFilter.TRIGGER,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                RealNotificationTriggerSection(
                    hasPostNotificationPermission = hasPostNotificationPermission,
                    hasListenerPermission = hasListenerPermission,
                    onRequestPostPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onOpenListenerSettings = {
                        runCatching { context.startActivity(PermissionChecks.listenerSettingsIntent()) }
                    },
                    onTriggerDanger = {
                        scope.launch {
                            if (!hasPostNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                return@launch
                            }
                            val result = container.analyzeMessage(
                                MessageInput(
                                    sourcePackage = "com.whatsapp",
                                    senderLabel = "+62 821-4820-9921",
                                    text = "Kepada Yth. Bapak/Ibu sekalian, kami mengundang kehadiran Anda di acara pernikahan kami. Silakan buka surat undangan: Undangan_Pernikahan.apk Mohon doa restunya.",
                                    timestamp = System.currentTimeMillis(),
                                    senderKind = SenderKind.UNKNOWN,
                                ),
                                notify = true,
                            )
                            Toast.makeText(
                                context,
                                "🚨 Notifikasi Bahaya Nyata terkirim ke status bar ponsel Anda! Tarik status bar untuk melihat.",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    },
                    onTriggerCaution = {
                        scope.launch {
                            if (!hasPostNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                return@launch
                            }
                            val result = container.analyzeMessage(
                                MessageInput(
                                    sourcePackage = "com.google.android.apps.messaging",
                                    senderLabel = "INFO-KURIR",
                                    text = "Tautan pendek mencurigakan mengaku dari kurir paket J&T untuk verifikasi alamat https://bit.ly/paket-jnt-resi",
                                    timestamp = System.currentTimeMillis(),
                                    senderKind = SenderKind.UNKNOWN,
                                ),
                                notify = true,
                            )
                            Toast.makeText(
                                context,
                                "⚠️ Notifikasi Waspada Nyata terkirim ke status bar ponsel Anda!",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                    onTriggerFamily = {
                        scope.launch {
                            if (!hasPostNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                return@launch
                            }
                            container.alertNotifier.notifyFamily(
                                memberName = "Pak Budi (Ayah)",
                                threatSummary = "Undangan_Pernikahan.apk terdeteksi berbahaya",
                                alertId = 1L,
                            )
                            Toast.makeText(
                                context,
                                "👨‍👩‍👧 Notifikasi Peringatan Keluarga terkirim ke status bar!",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                )
            }

            // 1. Heads-Up Card
            AnimatedVisibility(
                visible = selectedFilter == SimulationFilter.ALL || selectedFilter == SimulationFilter.HEADS_UP,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                HeadsUpSimulationCard(
                    onOpenAlert = { onOpenAlert(1L) },
                    onTrustSender = {
                        scope.launch {
                            container.trustedSenders.trust("+62 821-4820-9921")
                            Toast.makeText(context, "Pengirim +62 821-4820-9921 telah dipercayai.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDismissAlert = {
                        Toast.makeText(context, "Peringatan diabaikan.", Toast.LENGTH_SHORT).show()
                    },
                )
            }

            // 2. Lock Screen Card
            AnimatedVisibility(
                visible = selectedFilter == SimulationFilter.ALL || selectedFilter == SimulationFilter.LOCK_SCREEN,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                LockScreenSimulationCard(
                    onClickCard = { onOpenAlert(1L) },
                )
            }

            // 3. Why it looks different explainer
            WhyDifferentExplainer()

            Spacer(Modifier.height(Spacing.Section))
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Lucide.Smartphone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = "Android System View",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Text(
            text = "Simulasi & Notifikasi Real",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Kirim notifikasi nyata ke bilah status ponsel Anda atau pratinjau tampilan sistem Android.",
            style = MaterialTheme.typography.bodyMedium,
            color = SigapTheme.colors.textMuted,
        )
    }
}

@Composable
private fun FilterSection(
    selected: SimulationFilter,
    onSelect: (SimulationFilter) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SimulationFilter.entries.forEach { filter ->
                val isSelected = filter == selected
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelect(filter) },
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else SigapTheme.colors.textMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

/** Card 0: Live Android System Push Trigger */
@Composable
private fun RealNotificationTriggerSection(
    hasPostNotificationPermission: Boolean,
    hasListenerPermission: Boolean,
    onRequestPostPermission: () -> Unit,
    onOpenListenerSettings: () -> Unit,
    onTriggerDanger: () -> Unit,
    onTriggerCaution: () -> Unit,
    onTriggerFamily: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Lucide.Radio,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Column {
                        Text(
                            text = "Kirim Notifikasi Nyata ke Ponsel",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Muncul di bilah status (status bar) Android Anda",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = SigapTheme.colors.textMuted,
                        )
                    }
                }
            }

            // Permission Warning if POST_NOTIFICATIONS is not granted
            if (!hasPostNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SigapTheme.colors.dangerContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, SigapTheme.colors.danger.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            Lucide.TriangleAlert,
                            contentDescription = null,
                            tint = SigapTheme.colors.danger,
                            modifier = Modifier.size(20.dp),
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Izin Notifikasi Belum Diberikan",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Izinkan notifikasi agar bilah status ponsel dapat menampilkan tanda bahaya.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = SigapTheme.colors.textMuted,
                            )
                        }
                        Button(
                            onClick = onRequestPostPermission,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SigapTheme.colors.danger),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text("Izinkan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Push Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Button 1: Danger (WhatsApp APK)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SigapTheme.colors.dangerContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, SigapTheme.colors.danger.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onTriggerDanger),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .background(SigapTheme.colors.danger, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Lucide.ShieldAlert,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "🚨 Uji Notifikasi Bahaya Nyata (Heads-Up)",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "WhatsApp • Berkas Undangan_Pernikahan.apk berbahaya",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = SigapTheme.colors.textMuted,
                            )
                        }
                        Icon(
                            Lucide.ChevronRight,
                            contentDescription = null,
                            tint = SigapTheme.colors.danger,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                // Button 2: Caution (SMS Paket)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SigapTheme.colors.cautionContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, SigapTheme.colors.onCaution.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onTriggerCaution),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .background(SigapTheme.colors.cautionContainer, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Lucide.TriangleAlert,
                                contentDescription = null,
                                tint = SigapTheme.colors.onCaution,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "⚠️ Uji Notifikasi Waspada Nyata",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "SMS INFO-KURIR • Tautan pelacak paket palsu",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = SigapTheme.colors.textMuted,
                            )
                        }
                        Icon(
                            Lucide.ChevronRight,
                            contentDescription = null,
                            tint = SigapTheme.colors.onCaution,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                // Button 3: Family Alert
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onTriggerFamily),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Lucide.Users,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "👨‍👩‍👧 Uji Notifikasi Perlindungan Keluarga Nyata",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Peringatan ponsel Pak Budi (Ayah) terdeteksi bahaya",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = SigapTheme.colors.textMuted,
                            )
                        }
                        Icon(
                            Lucide.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            // Real-time Background Interceptor Status & Shortcut
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .background(
                                if (hasListenerPermission) SigapTheme.colors.safe else SigapTheme.colors.onCaution,
                                CircleShape,
                            )
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = if (hasListenerPermission) "Pemindai Latar Belakang Nyata Aktif" else "Akses Pembaca Notifikasi Belum Aktif",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (hasListenerPermission)
                                "Sigap otomatis memindai pesan masuk WhatsApp, Telegram, dan SMS secara real-time."
                            else
                                "Aktifkan agar Sigap dapat memindai pesan masuk WhatsApp & SMS asli secara otomatis.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = SigapTheme.colors.textMuted,
                        )
                    }
                    if (!hasListenerPermission) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenListenerSettings),
                        ) {
                            Text(
                                text = "Buka",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Card 1: Mode Heads-Up (Banner Melayang) */
@Composable
private fun HeadsUpSimulationCard(
    onOpenAlert: () -> Unit = {},
    onTrustSender: () -> Unit = {},
    onDismissAlert: () -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(SigapTheme.colors.danger, CircleShape),
                )
                Text(
                    text = "MODE HEADS-UP (BANNER MELAYANG)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "Prioritas Tinggi",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = SigapTheme.colors.textMuted,
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Floating banner notification inside mock phone viewport
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Header: Sigap logo + app name + time + expand arrow
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    Modifier
                                        .size(24.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Lucide.ShieldCheck,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                                Text(
                                    text = "Sigap",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "• 5m lalu",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SigapTheme.colors.textMuted,
                                )
                            }
                            Icon(
                                Lucide.ChevronDown,
                                contentDescription = null,
                                tint = SigapTheme.colors.textMuted,
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        // Notification body with warning icon and Android badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        Lucide.TriangleAlert,
                                        contentDescription = null,
                                        tint = SigapTheme.colors.danger,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = "Pesan kemungkinan penipuan",
                                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                Text(
                                    text = "Pengirim tak dikenal mengirim file APK bernama ",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = SigapTheme.colors.textMuted,
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SigapTheme.colors.dangerContainer.copy(alpha = 0.7f),
                                ) {
                                    Text(
                                        text = "Undangan Pernikahan.apk",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = SigapTheme.colors.danger,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }

                            // Android avatar icon
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SigapTheme.colors.dangerContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(40.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Lucide.Smartphone,
                                        contentDescription = null,
                                        tint = SigapTheme.colors.danger,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }

                        // Action Buttons: [Detail] [Percayai pengirim] [Abaikan]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(onClick = onOpenAlert),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        Lucide.ShieldCheck,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(12.dp),
                                    )
                                    Text(
                                        text = "Detail",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(onClick = onTrustSender),
                            ) {
                                Text(
                                    text = "Percayai pengirim",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(onClick = onDismissAlert),
                            ) {
                                Text(
                                    text = "Abaikan",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = SigapTheme.colors.textMuted,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }
                }

                // Footer note inside phone frame
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Aplikasi berjalan di latar belakang",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                    Icon(
                        Lucide.Eye,
                        contentDescription = null,
                        tint = SigapTheme.colors.textMuted,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

/** Card 2: Layar Kunci (Sensor Privasi) */
@Composable
private fun LockScreenSimulationCard(
    onClickCard: () -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Lucide.Lock,
                    contentDescription = null,
                    tint = SigapTheme.colors.textMuted,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "LAYAR KUNCI (SENSOR PRIVASI)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "Isi Disamarkan",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = SigapTheme.colors.textMuted,
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Lock icon
                Icon(
                    Lucide.Lock,
                    contentDescription = null,
                    tint = SigapTheme.colors.textMuted,
                    modifier = Modifier.size(18.dp),
                )

                // Lockscreen Clock (09:41)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "09:41",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp, lineHeight = 46.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Senin, 24 Maret",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }

                // Redacted notification card on lockscreen
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onClickCard),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Lucide.ShieldCheck,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }

                        Column(Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = "Sigap",
                                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "• Baru saja",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = SigapTheme.colors.textMuted,
                                )
                            }
                            Text(
                                text = "Sigap: ada pesan yang perlu kamu periksa...",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = SigapTheme.colors.textMuted,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                        }

                        Icon(
                            Lucide.ChevronRight,
                            contentDescription = null,
                            tint = SigapTheme.colors.textMuted,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                // Unlock hint
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Lucide.Fingerprint,
                        contentDescription = null,
                        tint = SigapTheme.colors.textMuted,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "Buka kunci untuk memeriksa detail keamanan",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }
            }
        }
    }
}

/** Card 3: "Mengapa Tampil Berbeda?" */
@Composable
private fun WhyDifferentExplainer() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                Lucide.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Mengapa Tampil Berbeda?",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = "Sigap menjaga kerahasiaan percakapan Anda saat ponsel terkunci. Nama kontak dan isi file APK hanya dapat dibaca setelah verifikasi biometrik aktif untuk mencegah pihak lain membaca informasi sensitif.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 20.sp),
            color = SigapTheme.colors.textMuted,
        )
    }
}
