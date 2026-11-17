package id.sigap.app.feature.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.User
import id.sigap.app.SigapApp
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.Spacing
import id.sigap.app.core.util.MonitoredApps
import id.sigap.app.core.util.PermissionChecks
import id.sigap.app.domain.model.UserSettings
import kotlinx.coroutines.launch

private data class PermissionStateSnapshot(
    val listener: Boolean,
    val post: Boolean,
    val contacts: Boolean,
)

private fun snapshot(context: Context): PermissionStateSnapshot = PermissionStateSnapshot(
    listener = PermissionChecks.isNotificationListenerEnabled(context),
    post = PermissionChecks.canPostNotifications(context),
    contacts = PermissionChecks.hasContactsAccess(context),
)

/** A user-facing messaging app (possibly several Android packages) that can be monitored. */
private data class AppOption(val label: String, val packages: Set<String>)

private val AppOptions = listOf(
    AppOption("WhatsApp", setOf("com.whatsapp", "com.whatsapp.w4b")),
    AppOption("Telegram", setOf("org.telegram.messenger")),
    AppOption("Pesan Google", setOf("com.google.android.apps.messaging")),
    AppOption("SMS", MonitoredApps.SMS_PACKAGES - "com.google.android.apps.messaging"),
)

private val CardShape = RoundedCornerShape(16.dp)

/** One permission row (steps 1-3). */
private data class PermissionStep(
    val number: Int,
    val title: String,
    val required: Boolean,
    val done: Boolean,
    val doneSubtitle: String,
    val pendingSubtitle: String,
    val explanation: String,
    val privacyNote: String?,
    val actionLabel: String,
    val onAction: () -> Unit,
)

/** S02: setup permissions, 6 steps with the current step expanded. */
@Composable
fun PermissionsScreen(
    allowManageGranted: Boolean = false,
    onFinished: () -> Unit,
) {
    val context = LocalContext.current
    val settingsRepo = remember { (context.applicationContext as SigapApp).container.settings }
    val settings by settingsRepo.settings.collectAsStateWithLifecycle(initialValue = UserSettings())
    val scope = rememberCoroutineScope()

    var state by remember { mutableStateOf(snapshot(context)) }
    var contactsSkipped by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) state = snapshot(context)
        }
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    val postLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        state = snapshot(context)
    }
    val contactsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        state = snapshot(context)
    }

    val permissionSteps = listOf(
        PermissionStep(
            number = 1,
            title = "Akses notifikasi",
            required = true,
            done = state.listener,
            doneSubtitle = "Telah diizinkan • Membaca ancaman masuk",
            pendingSubtitle = "Agar Sigap bisa memeriksa pesan masuk",
            explanation = "Sigap perlu membaca notifikasi pesan masuk untuk mendeteksi penipuan.",
            privacyNote = "Isi lengkap pesan tidak disimpan dan riwayat chat tidak dibaca.",
            actionLabel = "Buka pengaturan",
            onAction = { context.startActivity(PermissionChecks.listenerSettingsIntent()) },
        ),
        PermissionStep(
            number = 2,
            title = "Izin menampilkan notifikasi",
            required = true,
            done = state.post,
            doneSubtitle = "Telah diizinkan • Peringatan real-time",
            pendingSubtitle = "Agar Sigap bisa memperingatkan Anda",
            explanation = "Peringatan muncul langsung saat ada pesan yang mencurigakan.",
            privacyNote = null,
            actionLabel = "Izinkan",
            onAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    postLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
        ),
        PermissionStep(
            number = 3,
            title = "Akses kontak",
            required = false,
            done = state.contacts || contactsSkipped,
            doneSubtitle = if (state.contacts) "Telah diizinkan • Pengirim tersimpan dilewati" else "Dilewati",
            pendingSubtitle = "Fokus saat ini",
            explanation = "Agar pengirim yang kamu kenal tidak ikut diperiksa.",
            privacyNote = "Data buku telepon tetap berada di perangkat dan tidak pernah diunggah.",
            actionLabel = "Izinkan",
            onAction = { contactsLauncher.launch(Manifest.permission.READ_CONTACTS) },
        ),
    )

    val currentStep = permissionSteps.firstOrNull { !it.done }
    val handled = permissionSteps.count { it.done }
    val stepNumber = (handled + 1).coerceAtMost(6)
    val progress = stepNumber / 6f
    val allRequiredGranted = state.listener && state.post

    val enabledApps = AppOptions.count { opt -> opt.packages.any { it !in settings.disabledPackages } }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Header(stepNumber = stepNumber, progress = progress)

            permissionSteps.forEach { step ->
                if (step === currentStep) {
                    ExpandedStepCard(
                        step = step,
                        onSkip = if (step.required) null else ({ contactsSkipped = true }),
                    )
                } else {
                    val onManage: (() -> Unit)? = if (allowManageGranted && step.done) {
                        {
                            when (step.number) {
                                1 -> context.startActivity(PermissionChecks.listenerSettingsIntent())
                                2 -> context.startActivity(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null),
                                    ),
                                )
                                3 -> if (state.contacts) {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", context.packageName, null),
                                        ),
                                    )
                                } else {
                                    contactsSkipped = false
                                }
                            }
                        }
                    } else {
                        null
                    }
                    CollapsedStepCard(step = step, onManage = onManage)
                }
            }

            // Step 4: monitored apps
            SimpleCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NumberBadge(4)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Pilih aplikasi yang dipantau",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "$enabledApps aktif, ${AppOptions.size} aplikasi tersedia",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                            color = SigapTheme.colors.textMuted,
                        )
                    }
                }
                Row(
                    Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppOptions.forEach { opt ->
                        val enabled = opt.packages.any { it !in settings.disabledPackages }
                        AppChip(label = opt.label, selected = enabled) {
                            scope.launch { opt.packages.forEach { settingsRepo.setPackageEnabled(it, !enabled) } }
                        }
                    }
                }
            }

            // Step 5: cloud analysis
            SimpleCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NumberBadge(5)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        TitleWithBadge("Analisis cloud", required = false)
                        Text(
                            "Hanya teks yang sudah disamarkan yang dikirim",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                            color = SigapTheme.colors.textMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = settings.cloudConsent,
                        onCheckedChange = { scope.launch { settingsRepo.setCloudConsent(it) } },
                    )
                }
            }

            // Step 6: battery
            SimpleCard(onClick = { context.startActivity(PermissionChecks.batterySettingsIntent()) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NumberBadge(6)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        TitleWithBadge("Pengaturan baterai", required = false)
                        Text(
                            "Mencegah sistem mematikan proteksi",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                            color = SigapTheme.colors.textMuted,
                        )
                    }
                    Icon(
                        Lucide.ChevronRight,
                        contentDescription = null,
                        tint = SigapTheme.colors.textMuted,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
        }

        // Bottom CTA
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.ScreenHorizontal)
                .padding(top = 12.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onFinished,
                enabled = allRequiredGranted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(Lucide.ArrowRight, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Selesai", style = MaterialTheme.typography.labelLarge)
            }
            if (!allRequiredGranted) {
                Text(
                    "Langkah wajib perlu diselesaikan sebelum melanjutkan",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun Header(stepNumber: Int, progress: Float) {
    Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
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
                        "Langkah $stepNumber dari 6",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                "${(progress * 100).toInt()}% Selesai",
                style = MaterialTheme.typography.labelSmall,
                color = SigapTheme.colors.textMuted,
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Aktifkan perlindungan",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp, lineHeight = 30.sp),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "Beberapa langkah singkat untuk menjaga privasi pesan Anda",
                style = MaterialTheme.typography.bodyMedium,
                color = SigapTheme.colors.textMuted,
            )
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun SimpleCard(onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val base = Modifier.fillMaxWidth()
    Surface(
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = if (onClick != null) base.clickable(onClick = onClick) else base,
    ) {
        Column(Modifier.padding(14.dp)) { content() }
    }
}

@Composable
private fun NumberBadge(number: Int) {
    Box(
        Modifier
            .size(32.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            number.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = SigapTheme.colors.textMuted,
        )
    }
}

@Composable
private fun CheckBadge() {
    Box(
        Modifier
            .size(32.dp)
            .background(SigapTheme.colors.safeContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Lucide.Check, contentDescription = null, tint = SigapTheme.colors.safe, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun RequirementTag(required: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Text(
            if (required) "Wajib" else "Opsional",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 14.sp),
            color = SigapTheme.colors.textMuted,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun TitleWithBadge(title: String, required: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 20.sp),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f, fill = false),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        RequirementTag(required)
    }
}

@Composable
private fun CollapsedStepCard(
    step: PermissionStep,
    onManage: (() -> Unit)? = null,
) {
    SimpleCard(onClick = onManage) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (step.done) CheckBadge() else NumberBadge(step.number)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                TitleWithBadge(step.title, step.required)
                Text(
                    if (step.done) step.doneSubtitle else step.pendingSubtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = if (step.done) SigapTheme.colors.safe else SigapTheme.colors.textMuted,
                )
            }
            if (onManage != null) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Ubah",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Lucide.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            } else if (step.done) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    Lucide.Check,
                    contentDescription = null,
                    tint = SigapTheme.colors.textMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun ExpandedStepCard(step: PermissionStep, onSkip: (() -> Unit)?) {
    Surface(
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        step.number.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    TitleWithBadge(step.title, step.required)
                    Text(
                        step.pendingSubtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        Lucide.User,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(18.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            step.explanation,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        step.privacyNote?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                                color = SigapTheme.colors.textMuted,
                            )
                        }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = step.onAction,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(Lucide.ShieldCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(step.actionLabel, style = MaterialTheme.typography.labelLarge)
                }
                if (onSkip != null) {
                    TextButton(onClick = onSkip, modifier = Modifier.height(48.dp)) {
                        Text(
                            "Lewati",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
    val fg = if (selected) MaterialTheme.colorScheme.primary else SigapTheme.colors.textMuted
    Row(
        Modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, if (selected) Color.Transparent else MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (selected) {
            Icon(Lucide.Check, contentDescription = null, tint = fg, modifier = Modifier.size(12.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = fg, maxLines = 1)
    }
}

