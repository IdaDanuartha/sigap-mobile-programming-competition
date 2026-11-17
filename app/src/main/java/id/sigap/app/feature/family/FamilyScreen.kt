package id.sigap.app.feature.family

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Ban
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.HandHeart
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquare
import com.composables.icons.lucide.Phone
import com.composables.icons.lucide.QrCode
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Smartphone
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.User
import com.composables.icons.lucide.UserPlus
import com.composables.icons.lucide.Users
import com.composables.icons.lucide.X
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import id.sigap.app.R
import id.sigap.app.SigapApp
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.Spacing
import id.sigap.app.domain.family.FamilyAlertLevel
import id.sigap.app.domain.family.FamilyLink
import id.sigap.app.domain.family.FamilyRole
import id.sigap.app.domain.family.GuardianAlert
import id.sigap.app.domain.model.RiskLevel
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Sub-screens inside the Family flow */
sealed interface FamilySubScreen {
    data object Overview : FamilySubScreen
    data object InviteGuardian : FamilySubScreen
    data object BecomeGuardian : FamilySubScreen
    data class GuardianMonitoring(val linkId: Long = 0L, val targetName: String = "", val phoneNumber: String = "") : FamilySubScreen
    data class GuardianAlertDetail(val alertId: Long = 0L, val phoneNumber: String = "") : FamilySubScreen
}

/**
 * Main Family Screen coordinating the 5 family screens from mockups.
 */
@Composable
fun FamilyScreen(
    onOpenNotifications: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as SigapApp).container.family
    val links by repository.observeLinks().collectAsStateWithLifecycle(initialValue = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<FamilySubScreen>(FamilySubScreen.Overview) }

    BackHandler(enabled = currentScreen !is FamilySubScreen.Overview) {
        currentScreen = when (currentScreen) {
            is FamilySubScreen.GuardianAlertDetail -> FamilySubScreen.Overview
            else -> FamilySubScreen.Overview
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "familyNavigation",
        ) { screen ->
            when (screen) {
                is FamilySubScreen.Overview -> {
                    FamilyOverviewContent(
                        links = links,
                        onAddGuardian = { currentScreen = FamilySubScreen.InviteGuardian },
                        onBecomeGuardian = { currentScreen = FamilySubScreen.BecomeGuardian },
                        onOpenMonitoring = { linkId, name, phone -> currentScreen = FamilySubScreen.GuardianMonitoring(linkId, name, phone) },
                        onUnlink = { linkId, name ->
                            scope.launch {
                                repository.unlink(linkId)
                                snackbarHostState.showSnackbar("$name telah dihapus dari daftar")
                            }
                        },
                        onTogglePause = { linkId, paused ->
                            scope.launch {
                                repository.togglePause(linkId, paused)
                                snackbarHostState.showSnackbar(if (paused) "Perlindungan dijeda" else "Perlindungan diaktifkan kembali")
                            }
                        },
                        onOpenNotifications = onOpenNotifications,
                        onOpenProfile = onOpenProfile,
                        onShowSnackbar = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                    )
                }
                is FamilySubScreen.InviteGuardian -> {
                    InviteGuardianContent(
                        onBack = { currentScreen = FamilySubScreen.Overview },
                    )
                }
                is FamilySubScreen.BecomeGuardian -> {
                    BecomeGuardianContent(
                        onBack = { currentScreen = FamilySubScreen.Overview },
                        onConnected = { name ->
                            scope.launch { snackbarHostState.showSnackbar("Berhasil terhubung dengan $name") }
                            currentScreen = FamilySubScreen.Overview
                        },
                    )
                }
                is FamilySubScreen.GuardianMonitoring -> {
                    GuardianMonitoringContent(
                        linkId = screen.linkId,
                        targetName = screen.targetName,
                        phoneNumber = screen.phoneNumber,
                        onBack = { currentScreen = FamilySubScreen.Overview },
                        onOpenAlertDetail = { alertId -> currentScreen = FamilySubScreen.GuardianAlertDetail(alertId, screen.phoneNumber) },
                        onAddMore = { currentScreen = FamilySubScreen.InviteGuardian },
                    )
                }
                is FamilySubScreen.GuardianAlertDetail -> {
                    GuardianAlertDetailContent(
                        alertId = screen.alertId,
                        phoneNumber = screen.phoneNumber,
                        onBack = { currentScreen = FamilySubScreen.Overview },
                        onHandled = {
                            scope.launch {
                                repository.markAlertHandled(screen.alertId)
                                snackbarHostState.showSnackbar("Status telah diperbarui menjadi Sudah Ditangani")
                            }
                            currentScreen = FamilySubScreen.Overview
                        },
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
        )
    }
}

// ==============================================================================================
// 1. IMAGE 1: FAMILY OVERVIEW CONTENT (Tab Keluarga)
// ==============================================================================================
@Composable
private fun FamilyOverviewContent(
    links: List<FamilyLink>,
    onAddGuardian: () -> Unit,
    onBecomeGuardian: () -> Unit,
    onOpenMonitoring: (Long, String, String) -> Unit,
    onUnlink: (Long, String) -> Unit,
    onTogglePause: (Long, Boolean) -> Unit,
    onOpenNotifications: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onShowSnackbar: (String) -> Unit = {},
) {
    val guardians = remember(links) { links.filter { it.role == FamilyRole.GUARDIAN_FOR_ME } }
    val protectedList = remember(links) { links.filter { it.role == FamilyRole.PROTECTED_BY_ME } }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Top Bar
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
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
                Text(
                    text = "Keluarga",
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                IconButton(onClick = onOpenNotifications) {
                    Icon(Lucide.Bell, contentDescription = "Pusat Pemberitahuan", tint = MaterialTheme.colorScheme.onSurface)
                }
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenProfile)
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

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Description & Badge
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Lucide.ShieldCheck,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Jaringan Keluarga Sigap",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Text(
                    text = "Lindungi dan saling jaga dari pesan mencurigakan secara otomatis tanpa melanggar privasi.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }

            // 2 Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Button 1: Tambah Penjaga (Primary Filled)
                Button(
                    onClick = onAddGuardian,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Lucide.UserPlus, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Tambah Penjaga", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Button 2: Jadi Penjaga (Secondary Surface)
                Surface(
                    onClick = onBecomeGuardian,
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Row(
                        Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Lucide.HandHeart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Jadi Penjaga",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            // Section: Penjagaku
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Penjagaku",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Orang yang menerima pemberitahuan saat kamu mendapat pesan berbahaya",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }

                if (guardians.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Lucide.UserPlus, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Text("Belum Ada Penjaga Terhubung", style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                            Text(
                                "Tambahkan wali keluarga terpercaya yang akan menerima peringatan otomatis bila ponsel Anda mendeteksi ancaman berbahaya.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = SigapTheme.colors.textMuted,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    guardians.forEach { guardian ->
                        GuardianItemCard(
                            guardian = guardian,
                            onUnlink = { onUnlink(guardian.id, guardian.displayName) },
                            onTogglePause = { onTogglePause(guardian.id, !guardian.paused) },
                            onShowSnackbar = onShowSnackbar,
                        )
                    }
                }
            }

            // Section: Orang yang kujaga
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Orang yang kujaga",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Anggota keluarga yang kamu pantau keselamatannya",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }

                if (protectedList.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Lucide.HandHeart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Text("Belum Ada Anggota yang Dijaga", style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                            Text(
                                "Gunakan fitur Wali Amanat untuk melindungi orang tua atau kerabat dari modus penipuan berformat APK.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = SigapTheme.colors.textMuted,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    protectedList.forEach { member ->
                        ProtectedMemberCard(
                            member = member,
                            onClick = { onOpenMonitoring(member.id, member.displayName, member.phoneNumber) },
                            onUnlink = { onUnlink(member.id, member.displayName) },
                            onShowSnackbar = onShowSnackbar,
                        )
                    }
                }
            }

            // Educational Card: "Bagaimana Sigap Menjaga Privasi Keluarga?"
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Lucide.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Bagaimana Sigap Menjaga Privasi Keluarga?",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Keluarga hanya melihat tingkat risiko dan jenis penipuan (misal: APK atau tautan palsu). Isi pesan, kontak, dan foto pribadi tidak pernah dibagikan.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                            ),
                            color = SigapTheme.colors.textMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuardianItemCard(
    guardian: FamilyLink,
    onUnlink: () -> Unit,
    onTogglePause: () -> Unit,
    onShowSnackbar: (String) -> Unit,
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    val initials = remember(guardian.displayName) {
        guardian.displayName.split(" ")
            .mapNotNull { part -> part.filter { it.isLetterOrDigit() }.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifEmpty { "P" }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Hapus Penjaga?", fontWeight = FontWeight.Bold) },
            text = {
                Text("${guardian.displayName} tidak akan lagi menerima pemberitahuan otomatis jika terdeteksi pesan berbahaya di ponsel Anda.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRemoveDialog = false
                        onUnlink()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Batal")
                }
            },
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
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .background(
                                if (guardian.paused) MaterialTheme.colorScheme.surfaceContainer else SigapTheme.colors.safeContainer,
                                RoundedCornerShape(12.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initials,
                            fontWeight = FontWeight.Bold,
                            color = if (guardian.paused) SigapTheme.colors.textMuted else SigapTheme.colors.safe,
                            fontSize = 14.sp,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = guardian.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (guardian.alertLevel == FamilyAlertLevel.DANGER_ONLY) "Hanya Ancaman Berbahaya" else "Semua Peringatan",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = SigapTheme.colors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(Modifier.width(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (guardian.paused) MaterialTheme.colorScheme.surfaceContainer else SigapTheme.colors.safeContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(
                                if (guardian.paused) Lucide.Clock else Lucide.Check,
                                contentDescription = null,
                                tint = if (guardian.paused) SigapTheme.colors.textMuted else SigapTheme.colors.safe,
                                modifier = Modifier.size(11.dp),
                            )
                            Text(
                                text = if (guardian.paused) "Dijeda" else "Aktif",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                color = if (guardian.paused) SigapTheme.colors.textMuted else SigapTheme.colors.safe,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Lucide.EllipsisVertical, contentDescription = "Menu Penjaga", tint = SigapTheme.colors.textMuted, modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Telepon ${guardian.displayName}") },
                                onClick = {
                                    showMenu = false
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${guardian.phoneNumber}"))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        onShowSnackbar("Tidak dapat membuka panggilan telepon")
                                    }
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Kirim Pesan WhatsApp") },
                                onClick = {
                                    showMenu = false
                                    try {
                                        val cleanPhone = guardian.phoneNumber.replace("+", "").replace("-", "").replace(" ", "")
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=Halo,%20ini%20tes%20koneksi%20keamanan%20keluarga%20dari%20Sigap."))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        onShowSnackbar("Tidak dapat membuka WhatsApp")
                                    }
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(if (guardian.paused) "Aktifkan Kembali" else "Jeda Pemberitahuan") },
                                onClick = {
                                    showMenu = false
                                    onTogglePause()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Hapus Penjaga", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    showRemoveDialog = true
                                },
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Lucide.Info,
                    contentDescription = null,
                    tint = SigapTheme.colors.textMuted,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "${guardian.displayName} menerima peringatan saat ${if (guardian.alertLevel == FamilyAlertLevel.DANGER_ONLY) "terdeteksi ancaman berbahaya" else "semua peringatan"}.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun ProtectedMemberCard(
    member: FamilyLink,
    onClick: () -> Unit,
    onUnlink: () -> Unit,
    onShowSnackbar: (String) -> Unit,
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    val initials = remember(member.displayName) {
        member.displayName.split(" ")
            .mapNotNull { part -> part.filter { it.isLetterOrDigit() }.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifEmpty { "K" }
    }

    val hasDanger = member.alertsThisWeek > 0

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Hentikan Perlindungan?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Anda tidak akan lagi menerima pemberitahuan darurat dari ponsel ${member.displayName}.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRemoveDialog = false
                        onUnlink()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Batal")
                }
            },
        )
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (hasDanger) SigapTheme.colors.danger.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .background(
                                if (hasDanger) MaterialTheme.colorScheme.primary else SigapTheme.colors.safeContainer,
                                RoundedCornerShape(12.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initials,
                            fontWeight = FontWeight.Bold,
                            color = if (hasDanger) Color.White else SigapTheme.colors.safe,
                            fontSize = 14.sp,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = member.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (hasDanger) "Ponsel Terdeteksi Bahaya" else "Ponsel Keluarga Terlindungi",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = if (hasDanger) SigapTheme.colors.danger else SigapTheme.colors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(Modifier.width(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (hasDanger) SigapTheme.colors.dangerContainer else SigapTheme.colors.safeContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            if (hasDanger) {
                                Icon(
                                    Lucide.TriangleAlert,
                                    contentDescription = null,
                                    tint = SigapTheme.colors.danger,
                                    modifier = Modifier.size(11.dp),
                                )
                                Text(
                                    text = "Perlu Perhatian",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = SigapTheme.colors.danger,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            } else {
                                Icon(
                                    Lucide.Check,
                                    contentDescription = null,
                                    tint = SigapTheme.colors.safe,
                                    modifier = Modifier.size(11.dp),
                                )
                                Text(
                                    text = "Aman",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = SigapTheme.colors.safe,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Lucide.EllipsisVertical, contentDescription = "Menu Opsi", tint = SigapTheme.colors.textMuted, modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Buka Detail Pantauan") },
                                onClick = {
                                    showMenu = false
                                    onClick()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Telepon ${member.displayName}") },
                                onClick = {
                                    showMenu = false
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phoneNumber}"))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        onShowSnackbar("Tidak dapat membuka panggilan telepon")
                                    }
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Hapus dari Pantauan", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    showRemoveDialog = true
                                },
                            )
                        }
                    }
                }
            }

            if (hasDanger) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SigapTheme.colors.dangerContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
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
                                text = "${member.alertsThisWeek} ancaman Berbahaya terdeteksi",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = SigapTheme.colors.danger,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                Lucide.Clock,
                                contentDescription = null,
                                tint = SigapTheme.colors.textMuted,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = member.lastAlertTime?.let { DateUtils.getRelativeTimeSpanString(it).toString() } ?: "Baru saja",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = SigapTheme.colors.textMuted,
                            )
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phoneNumber}"))
                            context.startActivity(dialIntent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Lucide.Phone, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Telepon",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                            )
                        }
                    }

                    Surface(
                        onClick = onClick,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "Buka Detail",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Lucide.ChevronRight,
                                contentDescription = null,
                                tint = SigapTheme.colors.textMuted,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Lucide.ShieldCheck,
                            contentDescription = null,
                            tint = SigapTheme.colors.safe,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Tidak ada peringatan minggu ini. Seluruh pesan aman dan sistem memantau secara aktif.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

// ==============================================================================================
// 2. IMAGE 2: INVITE GUARDIAN CONTENT (Tambah Penjaga / Undang Penjaga)
// ==============================================================================================
@Composable
private fun InviteGuardianContent(onBack: () -> Unit) {
    val context = LocalContext.current
    var shareSnippet by remember { mutableStateOf(false) }
    var inviteCode by remember { mutableStateOf("482 913") }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // App Bar
        TopBarSimple(title = "Tambah Penjaga", onBack = onBack)

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Badge & Title
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Lucide.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Text("PERLINDUNGAN KELUARGA", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
                Text("Undang Penjaga", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                Text("Berikan kode atau tautkan langsung agar orang tepercaya dapat membantumu saat menerima ancaman siber.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp), color = SigapTheme.colors.textMuted)
            }

            // Komitmen Privasi Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Lucide.ShieldCheck, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                            Text("Komitmen Privasi", style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                            Text("Transparan", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = SigapTheme.colors.textMuted, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                    Text("Penjaga hanya menerima indikator keamanan sistem tanpa mengekspos obrolan pribadi Anda.", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = SigapTheme.colors.textMuted)

                    // Dibagikan Box
                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF1F8F5), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Lucide.Check, contentDescription = null, tint = Color(0xFF14594A), modifier = Modifier.size(16.dp))
                                Text("Dibagikan", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF14594A))
                            }
                            PrivacyItem(isPositive = true, text = "Tingkat risiko (Aman, Waspada, Berbahaya)")
                            PrivacyItem(isPositive = true, text = "Kategori ancaman (misal: APK, Kurir Palsu)")
                            PrivacyItem(isPositive = true, text = "Alasan singkat deteksi kecerdasan buatan")
                            PrivacyItem(isPositive = true, text = "Nama platform (WhatsApp, SMS, Telegram)")
                            PrivacyItem(isPositive = true, text = "Waktu persis insiden terdeteksi")
                        }
                    }

                    // Tidak Dibagikan Box
                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFDF3F2), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Lucide.Ban, contentDescription = null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(16.dp))
                                Text("Tidak Dibagikan", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFBA1A1A))
                            }
                            PrivacyItem(isPositive = false, text = "Isi pesan percakapan utuh & histori chat")
                            PrivacyItem(isPositive = false, text = "Nama kontak & nomor telepon pengirim")
                            PrivacyItem(isPositive = false, text = "Galeri foto, dokumen media & berkas rahasia")
                        }
                    }

                    // Toggle: Bagikan cuplikan tersamar
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Bagikan cuplikan tersamar", style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
                                Text("Tampilkan teks pesan dengan nomor telepon, nama orang, dan saldo yang telah disamarkan otomatis.", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = SigapTheme.colors.textMuted)
                            }
                            Switch(
                                checked = shareSnippet,
                                onCheckedChange = { shareSnippet = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary),
                            )
                        }
                    }
                }
            }

            // Kode Undangan Penjaga Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Lucide.Lock, contentDescription = null, tint = SigapTheme.colors.textMuted, modifier = Modifier.size(16.dp))
                        Text("Kode Undangan Penjaga", style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold), color = SigapTheme.colors.textMuted)
                    }

                    // Big 6-digit Code Box
                    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = inviteCode,
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                        )
                    }

                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFF3D6)) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Lucide.Clock, contentDescription = null, tint = Color(0xFF996500), modifier = Modifier.size(12.dp))
                            Text("Berlaku 09:41 menit", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color(0xFF996500), fontWeight = FontWeight.Bold)
                        }
                    }

                    // QR Code Graphic
                    Box(
                        Modifier
                            .size(140.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Lucide.QrCode, contentDescription = null, tint = Color(0xFF14594A), modifier = Modifier.size(110.dp))
                    }

                    Text(
                        text = "Minta anggota keluarga membuka Sigap > Jadi Penjaga dan memindai QR ini atau memasukkan kode di atas.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                        color = SigapTheme.colors.textMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )

                    Row(
                        modifier = Modifier.clickable {
                            val nextNum = 100_000 + Random.nextInt(900_000)
                            inviteCode = "${nextNum.toString().take(3)} ${nextNum.toString().takeLast(3)}"
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Lucide.RefreshCw, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Text("Buat kode baru", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Share Button
            Button(
                onClick = {
                    val shareText = "Halo, gunakan kode undangan Sigap saya [$inviteCode] untuk terhubung sebagai Penjaga Keluarga dan saling melindungi dari kejahatan siber."
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Bagikan Kode Penjaga"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Lucide.Share2, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Bagikan Kode via WhatsApp / Pesan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ==============================================================================================
// 3. IMAGE 3: BECOME GUARDIAN CONTENT (Jadi Penjaga / Hubungkan dengan Keluarga)
// ==============================================================================================
@Composable
private fun BecomeGuardianContent(
    onBack: () -> Unit,
    onConnected: (String) -> Unit,
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as SigapApp).container.family
    val scope = rememberCoroutineScope()

    var pinDigits by remember { mutableStateOf(List(6) { "" }) }
    var nickname by remember { mutableStateOf("") }
    var alertLevelOnlyDanger by remember { mutableStateOf(true) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopBarSimple(title = "Jadi Penjaga", onBack = onBack)

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Badge & Title
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Lucide.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Text("FITUR WALI AMANAT", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
                Text("Hubungkan dengan\nKeluarga", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                Text("Masukkan 6 digit kode atau pindai kode QR dari layar ponsel orang yang ingin kamu lindungi.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp), color = SigapTheme.colors.textMuted)
            }

            // 6-digit Code Box Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Masukkan 6 digit kode undangan", style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(Modifier.size(6.dp).background(SigapTheme.colors.safe, CircleShape))
                            Text("Aktif", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = SigapTheme.colors.safe, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 6 Pin boxes
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        pinDigits.forEachIndexed { idx, digit ->
                            Box(
                                Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(digit, style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    // ATAU divider
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline))
                        Text("ATAU", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = SigapTheme.colors.textMuted, modifier = Modifier.padding(horizontal = 12.dp))
                        Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline))
                    }

                    // Pindai Kode QR Button
                    Surface(
                        onClick = {
                            GmsBarcodeScanning.getClient(context).startScan().addOnSuccessListener { barcode ->
                                barcode.rawValue?.take(6)?.let {
                                    if (it.length == 6) pinDigits = it.map { c -> c.toString() }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                    ) {
                        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Lucide.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Pindai Kode QR", style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
                        }
                    }
                }
            }

            // Pengaturan Pemantauan Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Lucide.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                        Text("Pengaturan Pemantauan", style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
                    }

                    // Nama panggilan input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Nama panggilan orang yang dijaga", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium))
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            leadingIcon = { Icon(Lucide.User, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            ),
                        )
                    }

                    // Ambang Batas Peringatan
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Ambang Batas Peringatan", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = alertLevelOnlyDanger,
                                onClick = { alertLevelOnlyDanger = true },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(Modifier.size(6.dp).background(Color(0xFFBA1A1A), CircleShape))
                                        Text("Hanya berbahaya", fontSize = 12.sp)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer),
                            )
                            FilterChip(
                                selected = !alertLevelOnlyDanger,
                                onClick = { alertLevelOnlyDanger = false },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(Modifier.size(6.dp).background(Color(0xFF945200), CircleShape))
                                        Text("Berbahaya & waspada", fontSize = 12.sp)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer),
                            )
                        }
                    }

                    // Note card
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Lucide.Bell, contentDescription = null, tint = SigapTheme.colors.textMuted, modifier = Modifier.size(16.dp))
                            val label = nickname.trim().ifBlank { "anggota keluarga" }
                            Text(
                                "Kamu hanya akan menerima notifikasi darurat saat ponsel $label mendeteksi ancaman sesuai ambang batas ini.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                                color = SigapTheme.colors.textMuted,
                            )
                        }
                    }
                }
            }

            // Komitmen Privasi Bersama
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Lucide.ShieldCheck, contentDescription = null, tint = SigapTheme.colors.safe, modifier = Modifier.size(18.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Komitmen Privasi Bersama", style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
                        Text(
                            "Sebagai penjaga, kamu tidak dapat membaca obrolan pribadi mereka. Sigap hanya meneruskan indikator risiko untuk perlindungan bersama.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                            color = SigapTheme.colors.textMuted,
                        )
                    }
                }
            }

            // Hubungkan Sekarang Button
            Button(
                onClick = {
                    val codeStr = pinDigits.joinToString("")
                    scope.launch {
                        val level = if (alertLevelOnlyDanger) FamilyAlertLevel.DANGER_ONLY else FamilyAlertLevel.DANGER_AND_CAUTION
                        repository.redeemInvite(codeStr, nickname, level)
                        onConnected(nickname.trim())
                    }
                },
                enabled = pinDigits.all { it.isNotBlank() } && nickname.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Lucide.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Hubungkan Sekarang", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Text(
                text = "Persetujuan mutual terlindungi enkripsi perangkat ke perangkat",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = SigapTheme.colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ==============================================================================================
// 4. IMAGE 4: GUARDIAN MONITORING CONTENT (Pantauan Keluarga / Detail Peringatan Penjaga)
// ==============================================================================================
@Composable
private fun GuardianMonitoringContent(
    linkId: Long,
    targetName: String,
    phoneNumber: String = "+6281388214490",
    onBack: () -> Unit,
    onOpenAlertDetail: (Long) -> Unit,
    onAddMore: () -> Unit,
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as SigapApp).container.family
    val alerts by repository.observeAlerts(linkId).collectAsStateWithLifecycle(initialValue = emptyList())
    val unhandledAlerts = remember(alerts) { alerts.filter { !it.handled } }
    val criticalCount = remember(unhandledAlerts) { unhandledAlerts.count { it.riskLevel == RiskLevel.DANGER } }
    val safeCount = if (criticalCount == 0) 1 else 0
    val initials = remember(targetName) {
        targetName.split(" ")
            .mapNotNull { part -> part.filter { it.isLetterOrDigit() }.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifEmpty { "K" }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopBarSimple(title = "Detail Peringatan Penjaga", onBack = onBack)

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header: Badges & Title
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFD5EBE2)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(Modifier.size(5.dp).background(Color(0xFF14594A), CircleShape))
                            Text("Sistem Penjaga Aktif", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color(0xFF14594A), fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Lucide.RefreshCw, contentDescription = null, tint = SigapTheme.colors.textMuted, modifier = Modifier.size(12.dp))
                        Text("Sinkronisasi Langsung", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = SigapTheme.colors.textMuted)
                    }
                }
                Text("Pantauan Keluarga", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                Text("Status keamanan real-time untuk anggota keluarga yang kamu dampingi.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp), color = SigapTheme.colors.textMuted)
            }

            // Stat Cards: Ancaman Kritis vs Kondisi Aman
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFE8E7),
                    border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                    modifier = Modifier.weight(1f),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(36.dp).background(Color(0xFFFFCDD2), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Icon(Lucide.ShieldAlert, contentDescription = null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(criticalCount.toString(), style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = Color(0xFFBA1A1A))
                            Text("Ancaman Kritis", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color(0xFFBA1A1A))
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE8F5E9),
                    border = BorderStroke(1.dp, Color(0xFFC8E6C9)),
                    modifier = Modifier.weight(1f),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(36.dp).background(Color(0xFFC8E6C9), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Icon(Lucide.ShieldCheck, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(safeCount.toString(), style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = Color(0xFF2E7D32))
                            Text("Kondisi Aman", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color(0xFF2E7D32))
                        }
                    }
                }
            }

            // Real alerts or condition safe
            if (unhandledAlerts.isNotEmpty()) {
                unhandledAlerts.forEach { alert ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column {
                            val isDanger = alert.riskLevel == RiskLevel.DANGER
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(if (isDanger) SigapTheme.colors.danger else SigapTheme.colors.caution)
                            )

                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isDanger) SigapTheme.colors.dangerContainer else SigapTheme.colors.cautionContainer
                                        ) {
                                            Text(
                                                if (isDanger) "Berbahaya" else "Waspada",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = if (isDanger) SigapTheme.colors.danger else SigapTheme.colors.onCaution,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                                            Text(alert.category.name.replace('_', ' '), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    Text(DateUtils.getRelativeTimeSpanString(alert.timestamp).toString(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = SigapTheme.colors.textMuted)
                                }

                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                            Text(initials, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        }
                                        Column {
                                            Text(alert.protectedName, style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                Icon(
                                                    Lucide.Smartphone,
                                                    contentDescription = null,
                                                    tint = SigapTheme.colors.textMuted,
                                                    modifier = Modifier.size(13.dp),
                                                )
                                                Text(
                                                    text = "Ponsel Keluarga Terhubung",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                    color = SigapTheme.colors.textMuted,
                                                )
                                            }
                                        }
                                    }
                                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFECC8)) {
                                        Text("Belum ditangani", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFF945200), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                    }
                                }

                                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFDF3F2), modifier = Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Lucide.TriangleAlert, contentDescription = null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(16.dp))
                                            Text("Aktivitas Berisiko Tinggi Terdeteksi", style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = Color(0xFFBA1A1A))
                                        }
                                        Text(alert.maskedSnippet ?: "Pesan berbahaya dari ${alert.sourcePackage} terdeteksi.", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp), color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }

                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                                            context.startActivity(dialIntent)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                        ) {
                                            Icon(Lucide.Phone, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = "Telepon",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                            )
                                        }
                                    }

                                    Surface(
                                        onClick = { onOpenAlertDetail(alert.id) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                        ) {
                                            Text(
                                                text = "Lihat Detail",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                Lucide.ChevronRight,
                                                contentDescription = null,
                                                tint = SigapTheme.colors.textMuted,
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFD5EBE2)) {
                                Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Lucide.Check, contentDescription = null, tint = Color(0xFF14594A), modifier = Modifier.size(12.dp))
                                    Text("Aman", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFF14594A), fontWeight = FontWeight.Bold)
                                }
                            }
                            Text("Diperbarui baru saja", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = SigapTheme.colors.textMuted)
                        }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.size(40.dp).background(Color(0xFFD5EBE2), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Text(initials, fontWeight = FontWeight.Bold, color = Color(0xFF14594A), fontSize = 14.sp)
                                }
                                Column {
                                    Text(targetName, style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
                                    Text(
                                        text = "Status: Terlindungi & Aktif",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = SigapTheme.colors.textMuted,
                                    )
                                }
                            }
                        }

                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Tidak ada peringatan aktif. Seluruh pesan dan notifikasi yang dipindai otomatis berjalan aman.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(12.dp),
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Lucide.Check, contentDescription = null, tint = SigapTheme.colors.safe, modifier = Modifier.size(12.dp))
                                Text("Patch Terkini", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = SigapTheme.colors.textMuted)
                            }
                            Text("•", color = SigapTheme.colors.textMuted)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Lucide.Shield, contentDescription = null, tint = SigapTheme.colors.safe, modifier = Modifier.size(12.dp))
                                Text("Proteksi Notifikasi: Aktif", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = SigapTheme.colors.textMuted)
                            }
                        }
                    }
                }
            }

            // Tips Card
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Lucide.Users, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Tips Pencegahan Bersama", style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            Text("Biasakan telepon langsung saat ada kerabat kirim file tak dikenal.", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    IconButton(onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Tips Keamanan Keluarga Sigap:\n\nBiasakan telepon langsung saat ada kerabat kirim file tak dikenal atau meminta transfer mendesak. Jangan pernah membuka file berformat .apk!",
                            )
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Bagikan Tips Keamanan"))
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(Lucide.Share2, contentDescription = "Bagikan Tips", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Ingin Menambahkan Anggota Keluarga Lain Card
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.surface, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Lucide.UserPlus, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    }
                    Text("Ingin menambahkan anggota keluarga lain?", style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                    Text("Lindungi ponsel kakek, nenek, atau anak dari jebakan file penipuan digital.", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = SigapTheme.colors.textMuted, textAlign = TextAlign.Center)
                    Button(
                        onClick = onAddMore,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Lucide.UserPlus, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Tambah anggota keluarga", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Lucide.Lock,
                    contentDescription = null,
                    tint = SigapTheme.colors.textMuted,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Enkripsi ujung-ke-ujung • Privasi data keluarga terjamin",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = SigapTheme.colors.textMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ==============================================================================================
// 5. IMAGE 5: GUARDIAN ALERT DETAIL CONTENT (Detail Peringatan Penjaga)
// ==============================================================================================
@Composable
private fun GuardianAlertDetailContent(
    alertId: Long,
    phoneNumber: String = "+6281388214490",
    onBack: () -> Unit,
    onHandled: () -> Unit,
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as SigapApp).container.family
    val alert by repository.observeAlert(alertId).collectAsStateWithLifecycle(initialValue = null)

    val targetName = alert?.protectedName ?: "Anggota Keluarga"
    val categoryName = alert?.category?.name?.replace('_', ' ') ?: "—"
    val timeStr = alert?.let { DateUtils.getRelativeTimeSpanString(it.timestamp).toString() } ?: "—"
    val snippetText = alert?.maskedSnippet ?: "Tidak ada cuplikan pesan yang dibagikan."

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopBarSimple(title = "Detail Peringatan Penjaga", onBack = onBack)

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Hero Alert Banner
            val isCaution = alert?.riskLevel == RiskLevel.CAUTION
            val bannerBg = if (isCaution) SigapTheme.colors.cautionContainer else SigapTheme.colors.dangerContainer
            val bannerBorder = if (isCaution) SigapTheme.colors.caution.copy(alpha = 0.5f) else SigapTheme.colors.danger.copy(alpha = 0.5f)
            val badgeFg = if (isCaution) SigapTheme.colors.onCaution else SigapTheme.colors.danger

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = bannerBg,
                border = BorderStroke(1.dp, bannerBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(6.dp), color = bannerBg) {
                            Text(
                                if (isCaution) "WASPADA" else "BERBAHAYA",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = badgeFg,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)) {
                            Text(categoryName, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Text(
                        "Ancaman $categoryName pada Ponsel $targetName",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Lucide.Clock, contentDescription = null, tint = badgeFg, modifier = Modifier.size(13.dp))
                        Text("$timeStr • Melalui ${alert?.sourcePackage ?: "WhatsApp"}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = badgeFg)
                    }
                }
            }

            // Section 1: Mengapa Ini Sangat Berbahaya
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Lucide.ShieldAlert, contentDescription = null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(18.dp))
                        Text("Mengapa Ini Sangat Berbahaya", style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
                    }

                    // Reason 1
                    DangerReasonItem(
                        icon = Lucide.FileText,
                        title = "Pengirim Tidak Dikenal",
                        desc = "File atau pesan mencurigakan dikirim dari pengirim yang tidak terverifikasi di kontak $targetName.",
                    )
                    // Reason 2
                    DangerReasonItem(
                        icon = Lucide.Lock,
                        title = "Pencurian Kode Rahasia",
                        desc = "Modus ini berpotensi mengeksploitasi izin aksesibilitas atau membaca SMS OTP perbankan otomatis.",
                    )
                    // Reason 3
                    DangerReasonItem(
                        icon = Lucide.TriangleAlert,
                        title = "Status Tindakan",
                        desc = if (alert?.handled == true) "Peringatan ini telah ditandai selesai ditangani." else "Wali keluarga disarankan segera menghubungi $targetName untuk memastikan berkas tidak dibuka.",
                    )
                }
            }

            // Section 2: Pratinjau Pesan yang Diterima
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Lucide.MessageSquare, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                            Text("Pratinjau Pesan yang Diterima", style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(Modifier.size(6.dp).background(Color(0xFFBA1A1A), CircleShape))
                            Text("Terdeteksi", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color(0xFFBA1A1A), fontWeight = FontWeight.Bold)
                        }
                    }

                    // Redacted chat bubble
                    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Lucide.User, contentDescription = null, tint = SigapTheme.colors.textMuted, modifier = Modifier.size(14.dp))
                                    Text("+62 812-****-XXXX", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold))
                                }
                                Text("Terbaru", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = SigapTheme.colors.textMuted)
                            }
                            Text(
                                "“$snippetText”",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                            )

                            // Attachment Box
                            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFFE8E7), modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(Modifier.size(32.dp).background(Color(0xFFBA1A1A), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                            Icon(Lucide.FileText, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                        Column {
                                            Text("File_Mencurigakan.apk", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold))
                                            Text("Paket Aplikasi Android • Risiko Tinggi", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = SigapTheme.colors.textMuted)
                                        }
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFBA1A1A)) {
                                        Text("BAHAYA", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Lucide.Lock, contentDescription = null, tint = SigapTheme.colors.textMuted, modifier = Modifier.size(12.dp))
                                Text("Nomor pengirim dan identitas spesifik telah disamarkan demi privasi.", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = SigapTheme.colors.textMuted)
                            }
                        }
                    }
                }
            }

            // Section 3: Panduan Yang Bisa Kamu Katakan
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Lucide.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Text("Panduan: Yang Bisa Kamu Katakan", style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Gunakan nada tenang agar $targetName tidak panik atau buru-buru menyembunyikan masalah.", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = SigapTheme.colors.textMuted)

                    // Script quote 1
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("“", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("“Tadi ada pesan masuk berisi file mencurigakan. Tolong jangan disentuh atau dibuka ya, itu potensi penipuan.”", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp))
                        }
                    }

                    // Script quote 2
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("“", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("“Kalau sempat kepencet unduh, langsung kasih tahu aku sekarang biar kita matikan koneksi internetnya bareng-bareng.”", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp))
                        }
                    }
                }
            }

            // Status note
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Lucide.ShieldCheck, contentDescription = null, tint = SigapTheme.colors.safe, modifier = Modifier.size(16.dp))
                    Text("Perlindungan aktif. Kamu terhubung sebagai Penjaga untuk ponsel $targetName.", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = SigapTheme.colors.textMuted)
                }
            }

            // Bottom Buttons
            Button(
                onClick = {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                    context.startActivity(dialIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Lucide.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Telepon $targetName Sekarang", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onHandled)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Lucide.Check, contentDescription = null, tint = SigapTheme.colors.textMuted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (alert?.handled == true) "Sudah Ditandai Ditangani" else "Tandai Sudah Ditangani", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold), color = SigapTheme.colors.textMuted)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TopBarSimple(title: String, onBack: () -> Unit) {
    var showPrivacyInfo by remember { mutableStateOf(false) }

    if (showPrivacyInfo) {
        AlertDialog(
            onDismissRequest = { showPrivacyInfo = false },
            title = { Text("Jaminan Privasi Keluarga", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Jaringan Keluarga Sigap dirancang berbasis prinsip zero-knowledge:\n\n" +
                        "• Pesan obrolan, kontak, dan foto pribadi TIDAK PERNAH dikirim atau dibagikan ke penjaga.\n" +
                        "• Penjaga HANYA menerima sinyal notifikasi tingkat risiko dan jenis ancaman (misal: File APK berbahaya atau tautan phishing).\n" +
                        "• Seluruh proses pemindaian dilakukan secara lokal di perangkat Anda (on-device AI).",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyInfo = false }) {
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
                Icon(Lucide.ArrowLeft, contentDescription = stringResource(R.string.action_back), tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(onClick = { showPrivacyInfo = true }) {
                Icon(Lucide.Info, contentDescription = "Info Privasi", tint = MaterialTheme.colorScheme.onSurface)
            }
            Box(Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Lucide.User, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun PrivacyItem(isPositive: Boolean, text: String) {
    val iconColor = if (isPositive) SigapTheme.colors.safe else SigapTheme.colors.danger
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            imageVector = if (isPositive) Lucide.Check else Lucide.X,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = iconColor,
        )
    }
}

@Composable
private fun DangerReasonItem(
    icon: ImageVector,
    title: String,
    desc: String,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(32.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
                Text(desc, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp), color = SigapTheme.colors.textMuted)
            }
        }
    }
}
