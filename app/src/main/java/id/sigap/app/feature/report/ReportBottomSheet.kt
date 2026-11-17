package id.sigap.app.feature.report

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Landmark
import com.composables.icons.lucide.Link
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Phone
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.Users
import com.composables.icons.lucide.X
import id.sigap.app.R
import id.sigap.app.SigapApp
import id.sigap.app.core.designsystem.PrimaryButton
import id.sigap.app.core.designsystem.SecondaryButton
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.Spacing
import id.sigap.app.core.util.stringRes
import id.sigap.app.domain.community.CommunityHit
import id.sigap.app.domain.model.ScamCategory
import id.sigap.app.domain.normalization.EntityType
import kotlinx.coroutines.launch

/**
 * S12: bottom sheet for reporting to the community matching Image 3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBottomSheet(
    initialType: EntityType = EntityType.PHONE,
    initialValue: String = "+62 821-4820-9921",
    initialCategory: ScamCategory = ScamCategory.FAKE_INVITATION,
    onDismiss: () -> Unit,
    onReported: (CommunityHit) -> Unit = {},
) {
    val container = (LocalContext.current.applicationContext as SigapApp).container
    var type by remember { mutableStateOf(initialType) }
    var rawValue by remember {
        mutableStateOf(if (initialValue.isBlank()) "+62 821-4820-9921" else initialValue)
    }
    var category by remember { mutableStateOf(initialCategory) }
    var note by remember { mutableStateOf("") }
    var submittedHit by remember { mutableStateOf<CommunityHit?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.ScreenHorizontal)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Row: "👥 Jaringan Warga Sigap" badge and "X" close button
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Lucide.Users,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Jaringan Warga Sigap",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Lucide.X,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Title and Subtitle
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Laporkan ke komunitas",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Bantu sesama pengguna Sigap terhindar dari kontak dan rekening penipu ini.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    ),
                    color = SigapTheme.colors.textMuted,
                )
            }

            val hit = submittedHit
            if (hit != null) {
                // Success feedback card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Lucide.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                "Laporan Berhasil Dikirim",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text(
                            "Terima kasih atas kontribusi Anda. Entitas ini kini telah tercatat dengan total ${hit.reporterCount} laporan di database komunitas.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                SecondaryButton(
                    text = "Selesai",
                    onClick = onDismiss,
                )
            } else {
                // Choice: Tipe entitas yang dilaporkan
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tipe entitas yang dilaporkan",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Phone chip
                        EntityTypePill(
                            selected = type == EntityType.PHONE,
                            icon = Lucide.Phone,
                            label = "Nomor\nTelepon",
                            onClick = { type = EntityType.PHONE; if (rawValue.isBlank()) rawValue = "+62 821-4820-9921" },
                            modifier = Modifier.weight(1f),
                        )
                        // Bank chip
                        EntityTypePill(
                            selected = type == EntityType.BANK_ACCOUNT,
                            icon = Lucide.Landmark,
                            label = "Rekening\nBank",
                            onClick = { type = EntityType.BANK_ACCOUNT; rawValue = "BCA 8830192841" },
                            modifier = Modifier.weight(1f),
                        )
                        // Link chip
                        EntityTypePill(
                            selected = type == EntityType.LINK,
                            icon = Lucide.Link,
                            label = "Domain /\nLink",
                            onClick = { type = EntityType.LINK; rawValue = "https://bit.ly/paket-jnt-resi" },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Section: Nilai yang dilaporkan
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Nilai yang dilaporkan",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                Lucide.Check,
                                contentDescription = null,
                                tint = SigapTheme.colors.safe,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = "Diambil dari analisis",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = SigapTheme.colors.safe,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    // Value Card matching Image 3 mockup
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                Modifier
                                    .size(42.dp)
                                    .background(Color(0xFFFFE0DF), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    when (type) {
                                        EntityType.PHONE -> Lucide.Phone
                                        EntityType.BANK_ACCOUNT -> Lucide.Landmark
                                        EntityType.LINK -> Lucide.Link
                                    },
                                    contentDescription = null,
                                    tint = Color(0xFFB3261E),
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = rawValue,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "Terdeteksi pengirim APK malware",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                    color = Color(0xFFBA1A1A),
                                )
                            }

                            Icon(
                                Lucide.Lock,
                                contentDescription = null,
                                tint = SigapTheme.colors.textMuted,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                // Section: Kategori
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Kategori Modus",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = SigapTheme.colors.textMuted,
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val categories = listOf(
                            ScamCategory.FAKE_INVITATION to "Penipuan APK",
                            ScamCategory.FAKE_COURIER to "Kurir Palsu",
                            ScamCategory.BANK_PHISHING to "Phishing Bank",
                            ScamCategory.JOB_SCAM to "Kerja Palsu",
                            ScamCategory.AUTHORITY_IMPERSONATION to "Surat Tilang",
                        )
                        items(categories) { (cat, label) ->
                            FilterChip(
                                selected = cat == category,
                                onClick = { category = cat },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                ),
                            )
                        }
                    }
                }

                // Note input
                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 150) note = it },
                    placeholder = { Text("Tambahkan kronologi singkat (opsional)...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                    maxLines = 2,
                )

                // Privacy consent
                Text(
                    text = "Laporan akan diverifikasi otomatis sebelum ditambahkan ke database komunitas.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = SigapTheme.colors.textMuted,
                )

                // Submit Button
                Button(
                    onClick = {
                        val normalized = container.entityNormalizer.normalize(type, rawValue.trim())
                        if (normalized != null) {
                            scope.launch {
                                isSubmitting = true
                                val updated = container.community.submitReport(normalized, category, note)
                                submittedHit = updated
                                onReported(updated)
                                isSubmitting = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isSubmitting,
                ) {
                    Text(
                        if (isSubmitting) "Mengirim laporan..." else "Kirim Laporan ke Komunitas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun EntityTypePill(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Color(0xFFD5EBE2) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
        modifier = modifier.height(56.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                if (selected) Lucide.Check else icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                ),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
