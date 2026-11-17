package id.sigap.app.feature.home

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.CheckCheck
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Radio
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.X
import id.sigap.app.SigapApp
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.domain.family.FamilyRole
import id.sigap.app.domain.model.RiskLevel

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val badge: String,
    val badgeColor: Color,
    val badgeBg: Color,
    val isUnread: Boolean = true,
    val actionLabel: String? = null,
    val actionType: NotificationActionType? = null,
)

enum class NotificationActionType {
    ALERT_DETAIL,
    QUIZ,
    SIMULATION,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterSheet(
    onDismiss: () -> Unit,
    onOpenAlertDetail: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenSimulation: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val container = (context.applicationContext as SigapApp).container
    val recentScans by container.scanHistory.observeLatestAlerts(5).collectAsStateWithLifecycle(initialValue = emptyList())
    val familyLinks by container.family.observeLinks().collectAsStateWithLifecycle(initialValue = emptyList())
    val sigapColors = SigapTheme.colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    var readIds by remember { mutableStateOf(setOf<String>()) }

    val rawNotifications = remember(recentScans, familyLinks, sigapColors, primaryColor, primaryContainer) {
        val list = mutableListOf<NotificationItem>()

        val endangeredFamily = familyLinks.firstOrNull { it.role == FamilyRole.PROTECTED_BY_ME && it.alertsThisWeek > 0 }
        if (endangeredFamily != null) {
            list.add(
                NotificationItem(
                    id = "family_${endangeredFamily.id}",
                    title = "Peringatan Keluarga: ${endangeredFamily.displayName}",
                    message = "Terdeteksi ${endangeredFamily.alertsThisWeek} aktivitas berisiko tinggi. Segera hubungi anggota keluarga untuk verifikasi.",
                    timeAgo = endangeredFamily.lastAlertTime?.let { DateUtils.getRelativeTimeSpanString(it).toString() } ?: "12 menit lalu",
                    icon = Lucide.ShieldAlert,
                    iconTint = sigapColors.danger,
                    iconBg = sigapColors.dangerContainer,
                    badge = "Kritis",
                    badgeColor = sigapColors.danger,
                    badgeBg = sigapColors.dangerContainer,
                    isUnread = true,
                    actionLabel = "Lihat Detail Peringatan",
                    actionType = NotificationActionType.ALERT_DETAIL,
                )
            )
        }

        recentScans.filter { it.level != RiskLevel.SAFE }.forEach { scan ->
            val isDanger = scan.level == RiskLevel.DANGER
            list.add(
                NotificationItem(
                    id = "scan_${scan.id}",
                    title = "Peringatan: ${scan.category.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }}",
                    message = scan.redactedSnippet,
                    timeAgo = DateUtils.getRelativeTimeSpanString(scan.timestamp).toString(),
                    icon = if (isDanger) Lucide.ShieldAlert else Lucide.TriangleAlert,
                    iconTint = if (isDanger) sigapColors.danger else sigapColors.onCaution,
                    iconBg = if (isDanger) sigapColors.dangerContainer else sigapColors.cautionContainer,
                    badge = if (isDanger) "Kritis" else "Waspada",
                    badgeColor = if (isDanger) sigapColors.danger else sigapColors.onCaution,
                    badgeBg = if (isDanger) sigapColors.dangerContainer else sigapColors.cautionContainer,
                    isUnread = true,
                    actionLabel = "Lihat Detail Peringatan",
                    actionType = NotificationActionType.ALERT_DETAIL,
                )
            )
        }

        list.add(
            NotificationItem(
                id = "sys_protect",
                title = "Proteksi Real-time Aktif",
                message = "Pemindaian notifikasi WhatsApp, Telegram, dan SMS berjalan normal tanpa gangguan.",
                timeAgo = "Aktif",
                icon = Lucide.ShieldCheck,
                iconTint = sigapColors.safe,
                iconBg = sigapColors.safeContainer,
                badge = "Aktif",
                badgeColor = sigapColors.safe,
                badgeBg = sigapColors.safeContainer,
                isUnread = false,
            )
        )

        list.add(
            NotificationItem(
                id = "sys_quiz",
                title = "Tantangan: Tes Ketangkasan",
                message = "Uji kejelian Anda mengenali 5 jebakan modus APK dan surat tilang palsu yang sedang marak.",
                timeAgo = "Tersedia",
                icon = Lucide.Sparkles,
                iconTint = primaryColor,
                iconBg = primaryContainer,
                badge = "Edukasi",
                badgeColor = primaryColor,
                badgeBg = primaryContainer,
                isUnread = true,
                actionLabel = "Mulai Tes Ketangkasan",
                actionType = NotificationActionType.QUIZ,
            )
        )

        list
    }

    val notifications = rawNotifications.map { item ->
        if (item.id in readIds) item.copy(isUnread = false) else item
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Pemberitahuan",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        val unreadCount = notifications.count { it.isUnread }
                        if (unreadCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                            ) {
                                Text(
                                    text = "$unreadCount",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    Text(
                        text = "Aktivitas keamanan & peringatan sistem terkini",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = SigapTheme.colors.textMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.width(12.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            readIds = readIds + rawNotifications.map { it.id }.toSet()
                        },
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Lucide.CheckCheck, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Tandai dibaca",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Notifications List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(notifications, key = { it.id }) { item ->
                    NotificationCard(
                        item = item,
                        onClickAction = {
                            onDismiss()
                            when (item.actionType) {
                                NotificationActionType.ALERT_DETAIL -> onOpenAlertDetail()
                                NotificationActionType.QUIZ -> onOpenQuiz()
                                NotificationActionType.SIMULATION -> onOpenSimulation()
                                null -> Unit
                            }
                        },
                    )
                }

                item {
                    Spacer(Modifier.height(10.dp))

                    // Developer / Tester Simulation Shortcut
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onDismiss()
                                onOpenSimulation()
                            },
                    ) {
                        Row(
                            Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                Modifier
                                    .size(36.dp)
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

                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "Uji Notifikasi Interseptor & Real Push",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "Simulasikan dan kirim notifikasi nyata ke bilah status Android",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = SigapTheme.colors.textMuted,
                                )
                            }

                            Icon(
                                Lucide.ChevronRight,
                                contentDescription = null,
                                tint = SigapTheme.colors.textMuted,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onClickAction: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (item.isUnread) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            1.dp,
            if (item.isUnread) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .background(item.iconBg, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(item.icon, contentDescription = null, tint = item.iconTint, modifier = Modifier.size(16.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = item.badgeBg,
                    ) {
                        Text(
                            text = item.badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                            ),
                            color = item.badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                Text(
                    text = item.timeAgo,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = item.message,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (item.actionLabel != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onClickAction),
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = item.actionLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Icon(Lucide.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

