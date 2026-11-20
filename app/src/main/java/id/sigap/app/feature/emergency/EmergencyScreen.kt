package id.sigap.app.feature.emergency

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Phone
import com.composables.icons.lucide.Settings as SettingsIcon
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.User
import id.sigap.app.R
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.Spacing
import id.sigap.app.domain.emergency.EmergencyStep
import id.sigap.app.domain.emergency.Situation
import id.sigap.app.domain.emergency.StepAction
import id.sigap.app.domain.emergency.situationFor
import id.sigap.app.domain.model.ScamCategory

/**
 * S07B: Lakukan Sekarang (Panduan Tindakan Darurat).
 * Fully real and interactive emergency checklist matching Image 2.
 */
@Composable
fun EmergencyScreen(categoryName: String?, onBack: () -> Unit, onOpenDraft: () -> Unit) {
    val context = LocalContext.current

    // Initialize with steps 1 and 2 completed as illustrated in Image 2
    var completedSteps by rememberSaveable {
        mutableStateOf(setOf(EmergencyStep.AIRPLANE_MODE, EmergencyStep.UNINSTALL_APP))
    }

    val totalSteps = 6
    val currentDoneCount = completedSteps.size.coerceAtMost(totalSteps)
    val progressPercent = (currentDoneCount * 100) / totalSteps
    val animatedProgress by animateFloatAsState(targetValue = currentDoneCount.toFloat() / totalSteps, label = "progress")

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        // App Bar: Back button, Title "S07B Lakukan Sekarang", User Avatar
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
                    text = "Lakukan Sekarang",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
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

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Progress Bar Section
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "$currentDoneCount dari $totalSteps langkah selesai",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                )
            }

            // Hero Card: Panduan Tindakan Darurat
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Lucide.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Panduan Tindakan Darurat",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Fokus selesaikan langkah kritis paling atas terlebih dahulu untuk memutus akses penipu ke ponselmu.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        )
                    }
                }
            }

            // Step 1: Aktifkan mode pesawat
            StepItemCard(
                stepNumber = 1,
                title = "Aktifkan mode pesawat",
                description = "Sambungan data dan internet terputus otomatis untuk mencegah pencurian data berlanjut.",
                isDone = EmergencyStep.AIRPLANE_MODE in completedSteps,
                badge = if (EmergencyStep.AIRPLANE_MODE in completedSteps) "Selesai" else null,
                badgeColor = Color(0xFFD5EBE2),
                badgeTextColor = Color(0xFF14594A),
                onToggle = {
                    completedSteps = if (EmergencyStep.AIRPLANE_MODE in completedSteps) {
                        completedSteps - EmergencyStep.AIRPLANE_MODE
                    } else {
                        completedSteps + EmergencyStep.AIRPLANE_MODE
                    }
                },
            )

            // Step 2: Hapus aplikasi tak dikenal
            StepItemCard(
                stepNumber = 2,
                title = "Hapus aplikasi tak dikenal",
                description = "Berkas APK 'Undangan Pernikahan' telah di-uninstall dari perangkat.",
                isDone = EmergencyStep.UNINSTALL_APP in completedSteps,
                badge = if (EmergencyStep.UNINSTALL_APP in completedSteps) "Selesai" else null,
                badgeColor = Color(0xFFD5EBE2),
                badgeTextColor = Color(0xFF14594A),
                buttonLabel = "Buka daftar aplikasi",
                buttonIcon = Lucide.LayoutGrid,
                onButtonClick = {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS))
                },
                onToggle = {
                    completedSteps = if (EmergencyStep.UNINSTALL_APP in completedSteps) {
                        completedSteps - EmergencyStep.UNINSTALL_APP
                    } else {
                        completedSteps + EmergencyStep.UNINSTALL_APP
                    }
                },
            )

            // Step 3: Periksa layanan aksesibilitas (Active Critical Step)
            val isStep3Done = EmergencyStep.REVIEW_ACCESSIBILITY in completedSteps
            StepItemCard(
                stepNumber = 3,
                title = "Periksa layanan aksesibilitas",
                description = "Pastikan malware tidak memiliki izin mengontrol layar dan membaca SMS OTP secara otomatis.",
                isDone = isStep3Done,
                badge = if (isStep3Done) "Selesai" else "Kritis",
                badgeColor = if (isStep3Done) Color(0xFFD5EBE2) else Color(0xFFFFECC8),
                badgeTextColor = if (isStep3Done) Color(0xFF14594A) else Color(0xFF945200),
                primaryActionLabel = "Buka pengaturan",
                primaryActionIcon = Lucide.SettingsIcon,
                onPrimaryActionClick = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                secondaryActionLabel = if (!isStep3Done) "Sudah saya matikan" else null,
                onSecondaryActionClick = {
                    completedSteps = completedSteps + EmergencyStep.REVIEW_ACCESSIBILITY
                },
                onToggle = {
                    completedSteps = if (EmergencyStep.REVIEW_ACCESSIBILITY in completedSteps) {
                        completedSteps - EmergencyStep.REVIEW_ACCESSIBILITY
                    } else {
                        completedSteps + EmergencyStep.REVIEW_ACCESSIBILITY
                    }
                },
            )

            // Step 4: Ganti kata sandi dan PIN dari perangkat lain
            val isStep4Done = EmergencyStep.CHANGE_PASSWORDS_CLEAN_DEVICE in completedSteps
            StepItemCard(
                stepNumber = 4,
                title = "Ganti kata sandi dan PIN dari perangkat lain",
                description = "Gunakan HP atau laptop anggota keluarga lain yang aman untuk mengubah password m-banking & email.",
                isDone = isStep4Done,
                badge = if (isStep4Done) "Selesai" else null,
                badgeColor = Color(0xFFD5EBE2),
                badgeTextColor = Color(0xFF14594A),
                onToggle = {
                    completedSteps = if (isStep4Done) {
                        completedSteps - EmergencyStep.CHANGE_PASSWORDS_CLEAN_DEVICE
                    } else {
                        completedSteps + EmergencyStep.CHANGE_PASSWORDS_CLEAN_DEVICE
                    }
                },
            )

            // Step 5: Hubungi bank kamu
            val isStep5Done = EmergencyStep.CONTACT_BANK in completedSteps
            StepItemCard(
                stepNumber = 5,
                title = "Hubungi bank kamu",
                description = "Pakai nomor resmi di belakang kartu ATM atau aplikasi resmi, minta pemblokiran sementara rekening.",
                isDone = isStep5Done,
                badge = if (isStep5Done) "Selesai" else "Penting",
                badgeColor = if (isStep5Done) Color(0xFFD5EBE2) else Color(0xFFFFE0DF),
                badgeTextColor = if (isStep5Done) Color(0xFF14594A) else Color(0xFFB3261E),
                onToggle = {
                    completedSteps = if (isStep5Done) {
                        completedSteps - EmergencyStep.CONTACT_BANK
                    } else {
                        completedSteps + EmergencyStep.CONTACT_BANK
                    }
                },
            )

            // Step 6: Laporkan ke IASC
            val isStep6Done = EmergencyStep.REPORT_IASC in completedSteps
            StepItemCard(
                stepNumber = 6,
                title = "Laporkan ke IASC",
                description = "Kirim laporan resmi ke Indonesia Anti-Scam Centre untuk membekukan rekening penipu.",
                isDone = isStep6Done,
                badge = if (isStep6Done) "Selesai" else null,
                badgeColor = Color(0xFFD5EBE2),
                badgeTextColor = Color(0xFF14594A),
                buttonLabel = "Buat draf laporan",
                buttonIcon = Lucide.FileText,
                onButtonClick = onOpenDraft,
                onToggle = {
                    completedSteps = if (isStep6Done) {
                        completedSteps - EmergencyStep.REPORT_IASC
                    } else {
                        completedSteps + EmergencyStep.REPORT_IASC
                    }
                },
            )

            Spacer(Modifier.height(16.dp))
        }

        // Persistent Bottom Action Bar matching Image 2
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.ScreenHorizontal, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // "Hubungi bank sekarang" primary danger CTA
            Button(
                onClick = {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1500000"))
                    context.startActivity(dialIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SigapTheme.colors.danger,
                    contentColor = Color.White,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Lucide.Phone,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Hubungi bank sekarang",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color.White,
                    )
                }
            }

            // "Tandai semua langkah darurat selesai" secondary action (48dp touch target)
            Surface(
                onClick = {
                    completedSteps = setOf(
                        EmergencyStep.AIRPLANE_MODE,
                        EmergencyStep.UNINSTALL_APP,
                        EmergencyStep.REVIEW_ACCESSIBILITY,
                        EmergencyStep.CHANGE_PASSWORDS_CLEAN_DEVICE,
                        EmergencyStep.CONTACT_BANK,
                        EmergencyStep.REPORT_IASC,
                    )
                },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Lucide.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Tandai semua langkah darurat selesai",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun StepItemCard(
    stepNumber: Int,
    title: String,
    description: String,
    isDone: Boolean,
    badge: String? = null,
    badgeColor: Color = Color(0xFFD5EBE2),
    badgeTextColor: Color = Color(0xFF14594A),
    buttonLabel: String? = null,
    buttonIcon: ImageVector? = null,
    onButtonClick: (() -> Unit)? = null,
    primaryActionLabel: String? = null,
    primaryActionIcon: ImageVector? = null,
    onPrimaryActionClick: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryActionClick: (() -> Unit)? = null,
    onToggle: () -> Unit,
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(16.dp),
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
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Circle Indicator: Green Check if done, otherwise subtle ring/dot
                Box(
                    Modifier
                        .size(26.dp)
                        .background(
                            if (isDone) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isDone) {
                        Icon(
                            Lucide.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    } else {
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                        )
                    }
                }

                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        if (badge != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeColor,
                            ) {
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = badgeTextColor,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        }
                    }

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                        ),
                        color = SigapTheme.colors.textMuted,
                    )
                }
            }

            // Single secondary button (e.g., "Buka daftar aplikasi" or "Buat draf laporan")
            if (buttonLabel != null && onButtonClick != null) {
                Surface(
                    onClick = onButtonClick,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.padding(start = 38.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (buttonIcon != null) {
                            Icon(
                                buttonIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Text(
                            text = buttonLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            // Dual action buttons for active critical step: Primary green + Secondary outlined
            if (primaryActionLabel != null && onPrimaryActionClick != null) {
                Column(
                    Modifier.padding(start = 38.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onPrimaryActionClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(40.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (primaryActionIcon != null) {
                                Icon(
                                    primaryActionIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Text(
                                text = primaryActionLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }

                    if (secondaryActionLabel != null && onSecondaryActionClick != null) {
                        Surface(
                            onClick = onSecondaryActionClick,
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Text(
                                text = secondaryActionLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
