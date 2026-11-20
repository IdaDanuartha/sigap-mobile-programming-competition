package id.sigap.app.feature.learn

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.FileWarning
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.X
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.Spacing

data class QuizOption(
    val text: String,
    val isCorrect: Boolean,
    val explanation: String,
)

data class QuizQuestion(
    val id: Int,
    val categoryBadge: String,
    val senderName: String,
    val senderNumber: String,
    val messageText: String,
    val attachmentName: String? = null,
    val questionText: String,
    val options: List<QuizOption>,
    val safetyTip: String,
)

private val QUIZ_QUESTIONS = listOf(
    QuizQuestion(
        id = 1,
        categoryBadge = "Modus APK Undangan",
        senderName = "Nomor Tidak Dikenal",
        senderNumber = "+62 812-4421-9901",
        messageText = "Selamat pagi kak, kami sekeluarga mengundang kakak di acara pernikahan putra kami. Mohon kehadirannya ya kak. Undangan bisa dibuka di file ini:",
        attachmentName = "Surat_Undangan_Digital.apk",
        questionText = "Pesan ini masuk ke WhatsApp Anda. Apa tindakan yang paling tepat?",
        options = listOf(
            QuizOption(
                text = "Buka dan instal file tersebut karena ingin menghargai kerabat yang mengundang.",
                isCorrect = false,
                explanation = "Salah! Membuka file .apk dari nomor tak dikenal akan memasang spyware pencuri SMS OTP perbankan.",
            ),
            QuizOption(
                text = "Jangan buka! Laporkan pesan dan hapus. Undangan digital resmi berbasis web link, bukan file .apk.",
                isCorrect = true,
                explanation = "Tepat sekali! Ekstensi file .apk adalah paket aplikasi Android, modus umum spyware perbankan berkedok undangan.",
            ),
            QuizOption(
                text = "Kirim ulang file tersebut ke grup keluarga untuk menanyakan siapa yang menikah.",
                isCorrect = false,
                explanation = "Berbahaya! Meneruskan file berbahaya ke keluarga berisiko membuat anggota keluarga lain tak sengaja menginstalnya.",
            ),
        ),
        safetyTip = "Undangan pernikahan online selalu berupa tautan website (URL HTTPS), bukan file installer aplikasi Android (.apk).",
    ),
    QuizQuestion(
        id = 2,
        categoryBadge = "Modus Kurir & Paket",
        senderName = "Kurir J&T Express (Palsu)",
        senderNumber = "+62 878-1920-3341",
        messageText = "Halo kak, paket dengan nomor resi JP982103 tidak dapat dikirim karena alamat kurang lengkap. Silakan lihat foto paket dan konfirmasi ulang:",
        attachmentName = "LIHAT_FOTO_PAKET.apk",
        questionText = "Pemberitahuan paket tertahan ini dikirim via WhatsApp. Bagaimana Anda memverifikasinya?",
        options = listOf(
            QuizOption(
                text = "Foto paket asli berupa format gambar (.jpg / .png) atau cek mandiri di aplikasi/web resmi ekspedisi.",
                isCorrect = true,
                explanation = "Tepat sekali! Kurir ekspedisi resmi tidak pernah meminta pelanggan memasang file APK untuk melihat foto paket.",
            ),
            QuizOption(
                text = "Instal aplikasinya lalu berikan izin SMS agar kurir bisa memandu posisi paket.",
                isCorrect = false,
                explanation = "Fatal! Izin SMS adalah izin paling dicari oleh virus scam perbankan untuk menyedot OTP.",
            ),
            QuizOption(
                text = "Telepon nomor kurir tersebut dan sebutkan data diri KTP untuk verifikasi.",
                isCorrect = false,
                explanation = "Hindari memberikan identitas pribadi kepada nomor kurir tidak resmi dari perpesanan personal.",
            ),
        ),
        safetyTip = "Lacak nomor resi hanya di website atau aplikasi resmi ekspedisi (J&T, JNE, SiCepat), jangan pernah mengunduh file kiriman chat.",
    ),
    QuizQuestion(
        id = 3,
        categoryBadge = "Modus Tilang Elektronik ETLE",
        senderName = "Polda Metro Jaya (Palsu)",
        senderNumber = "+62 821-3312-7809",
        messageText = "Pemberitahuan Pelanggaran ETLE: Kendaraan B 1234 ABC terdeteksi melanggar marka jalan. Unduh surat konfirmasi tilang berikut:",
        attachmentName = "Surat_Tilang_Elektronik.apk",
        questionText = "Bagaimana mekanisme tilang elektronik ETLE yang sebenarnya di Indonesia?",
        options = listOf(
            QuizOption(
                text = "Kepolisian mengirim surat konfirmasi fisik ber-barcode ke alamat pemilik atau cek web resmi etle-korlantas.info.",
                isCorrect = true,
                explanation = "Benar! Korlantas Polri menegaskan TIDAK PERNAH mengirim surat tilang dalam bentuk file APK via WhatsApp.",
            ),
            QuizOption(
                text = "Polisi selalu mengirim file aplikasi tilang langsung ke WhatsApp pemilik kendaraan.",
                isCorrect = false,
                explanation = "Salah besar! Kepolisian Republik Indonesia tidak pernah menggunakan file APK untuk konfirmasi tilang.",
            ),
            QuizOption(
                text = "Langsung bayar denda ke rekening pribadi polisi yang tertera di pesan.",
                isCorrect = false,
                explanation = "Penipuan! Denda tilang resmi hanya disetorkan melalui kode bayar BRIVA atau kas negara resmi.",
            ),
        ),
        safetyTip = "Polri mengonfirmasi bahwa segala bentuk notifikasi tilang berformat .apk di WhatsApp adalah 100% penipuan.",
    ),
    QuizQuestion(
        id = 4,
        categoryBadge = "Modus Kerja Komisi Online",
        senderName = "HRD Talent Recruiter",
        senderNumber = "+62 859-9988-1203",
        messageText = "Peluang kerja paruh waktu online! Cukup like & subscribe video YouTube, bayaran Rp50.000 per tugas. Deposit awal Rp150.000 untuk modal aktivasi sistem.",
        attachmentName = null,
        questionText = "Mengapa penawaran kerja paruh waktu di atas sangat mencurigakan?",
        options = listOf(
            QuizOption(
                text = "Modus 'Task Scam': Meminta deposit uang di awal dengan janji komisi instan yang tidak masuk akal.",
                isCorrect = true,
                explanation = "Tepat! Pekerjaan legal tidak pernah memungut deposit modal dari pekerjanya.",
            ),
            QuizOption(
                text = "Penawaran ini aman dan wajar selama nominal deposit masih di bawah Rp500.000.",
                isCorrect = false,
                explanation = "Keliru! Modus task scam sengaja memancing nominal kecil di awal lalu mengunci dana korban.",
            ),
            QuizOption(
                text = "Hanya mencurigakan jika bayarannya kurang dari Rp100.000 per tugas.",
                isCorrect = false,
                explanation = "Salah! Iming-iming tugas mudah berbayar mahal adalah ciri khas utama penipuan kerja daring.",
            ),
        ),
        safetyTip = "Ingat rumus aman: Tidak ada perusahaan resmi yang meminta uang deposit kepada calon pekerjanya.",
    ),
    QuizQuestion(
        id = 5,
        categoryBadge = "Modus Phishing Perbankan",
        senderName = "Customer Care Bank (Palsu)",
        senderNumber = "+62 888-0192-4411",
        messageText = "Pemberitahuan perubahan tarif transfer antarbank menjadi Rp150.000/bln. Jika tidak setuju, isi formulir pembatalan di bit.ly/batal-tarif-bank.",
        attachmentName = null,
        questionText = "Apa red flag (tanda bahaya) paling jelas dari pesan di atas?",
        options = listOf(
            QuizOption(
                text = "Menggunakan tautan pendek mencurigakan (bit.ly) dan nomor seluler biasa, bukan kanal resmi bank verified centang hijau.",
                isCorrect = true,
                explanation = "Hebat! Bank selalu mengumumkan tarif di website resmi dan menggunakan akun WhatsApp Business terverifikasi centang hijau.",
            ),
            QuizOption(
                text = "Tautan bit.ly aman digunakan oleh instansi perbankan nasional.",
                isCorrect = false,
                explanation = "Salah! Tautan pendek digunakan penipu untuk menyembunyikan domain situs phishing tiruan.",
            ),
            QuizOption(
                text = "Segera klik link dan masukkan PIN serta OTP untuk mencegah pemotongan saldo.",
                isCorrect = false,
                explanation = "Bahaya fatal! Memasukkan PIN/OTP di link tiruan akan menguras seluruh rekening perbankan Anda.",
            ),
        ),
        safetyTip = "Bank tidak pernah meminta OTP, PIN, password, atau CVV dengan alasan pembatalan biaya maupun verifikasi akun.",
    ),
)

@Composable
fun AgilityQuizScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }

    val currentQuestion = QUIZ_QUESTIONS[currentIndex]
    val isAnswered = selectedOptionIndex != null

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // Top App Bar
        QuizTopBar(
            currentIndex = currentIndex,
            totalQuestions = QUIZ_QUESTIONS.size,
            isFinished = isFinished,
            onBack = onBack,
        )

        if (!isFinished) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.ScreenHorizontal, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / QUIZ_QUESTIONS.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                // Scenario Chat Card
                QuizScenarioCard(question = currentQuestion)

                // Question Prompt
                Text(
                    text = currentQuestion.questionText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )

                // Options List
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    currentQuestion.options.forEachIndexed { index, option ->
                        val isSelected = selectedOptionIndex == index
                        QuizOptionItem(
                            option = option,
                            isSelected = isSelected,
                            isAnswered = isAnswered,
                            onSelect = {
                                if (!isAnswered) {
                                    selectedOptionIndex = index
                                    if (option.isCorrect) {
                                        score++
                                    }
                                }
                            },
                        )
                    }
                }

                // Feedback & Explanation Banner
                AnimatedVisibility(
                    visible = isAnswered,
                    enter = fadeIn() + slideInVertically(),
                ) {
                    val chosenOption = selectedOptionIndex?.let { currentQuestion.options[it] }
                    if (chosenOption != null) {
                        QuizExplanationCard(
                            isCorrect = chosenOption.isCorrect,
                            explanation = chosenOption.explanation,
                            safetyTip = currentQuestion.safetyTip,
                        )
                    }
                }

                // Next Button
                if (isAnswered) {
                    Button(
                        onClick = {
                            if (currentIndex < QUIZ_QUESTIONS.size - 1) {
                                currentIndex++
                                selectedOptionIndex = null
                            } else {
                                isFinished = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = if (currentIndex < QUIZ_QUESTIONS.size - 1) "Pertanyaan Berikutnya" else "Lihat Hasil Tes",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                ),
                            )
                            Icon(Lucide.ArrowRight, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        } else {
            // Result Screen
            QuizResultScreen(
                score = score,
                total = QUIZ_QUESTIONS.size,
                onRestart = {
                    currentIndex = 0
                    selectedOptionIndex = null
                    score = 0
                    isFinished = false
                },
                onShare = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "🛡️ Saya baru saja menyelesaikan Tes Ketangkasan Deteksi Penipuan Sigap dengan Skor $score/${QUIZ_QUESTIONS.size}!\n\n" +
                                "Aplikasi Sigap membantu kita mengasah kejelian dari jebakan file APK, surat tilang palsu, dan modus penipuan online. Uji ketangkasanmu juga sekarang!",
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Bagikan Hasil Tes Ketangkasan"))
                },
                onBack = onBack,
            )
        }
    }
}

@Composable
private fun QuizTopBar(
    currentIndex: Int,
    totalQuestions: Int,
    isFinished: Boolean,
    onBack: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Lucide.ArrowLeft,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(
                    text = "Tes Ketangkasan",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = if (isFinished) "Hasil Evaluasi Kuis" else "Pertanyaan ${currentIndex + 1} dari $totalQuestions",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(end = 8.dp),
        ) {
            Row(
                Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Lucide.Sparkles,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "Kuis Kilat",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun QuizScenarioCard(question: QuizQuestion) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SigapTheme.colors.dangerContainer,
                ) {
                    Text(
                        text = question.categoryBadge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = SigapTheme.colors.danger,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }

                Text(
                    text = question.senderNumber,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }

            // Message Bubble
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = question.senderName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = question.messageText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (question.attachmentName != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, SigapTheme.colors.danger.copy(alpha = 0.35f)),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    Lucide.FileWarning,
                                    contentDescription = null,
                                    tint = SigapTheme.colors.danger,
                                    modifier = Modifier.size(20.dp),
                                )
                                Column {
                                    Text(
                                        text = question.attachmentName,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                        ),
                                        color = SigapTheme.colors.danger,
                                    )
                                    Text(
                                        text = "Berkas Aplikasi Android (Pencuri SMS/OTP)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = SigapTheme.colors.textMuted,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizOptionItem(
    option: QuizOption,
    isSelected: Boolean,
    isAnswered: Boolean,
    onSelect: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            !isAnswered && isSelected -> MaterialTheme.colorScheme.primary
            isAnswered && option.isCorrect -> SigapTheme.colors.safe
            isAnswered && isSelected && !option.isCorrect -> SigapTheme.colors.danger
            else -> MaterialTheme.colorScheme.outline
        },
        label = "borderColor",
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            !isAnswered && isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            isAnswered && option.isCorrect -> SigapTheme.colors.safeContainer
            isAnswered && isSelected && !option.isCorrect -> SigapTheme.colors.dangerContainer
            else -> MaterialTheme.colorScheme.surface
        },
        label = "containerColor",
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !isAnswered, onClick = onSelect),
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(26.dp)
                    .border(
                        1.5.dp,
                        when {
                            isAnswered && option.isCorrect -> SigapTheme.colors.safe
                            isAnswered && isSelected && !option.isCorrect -> SigapTheme.colors.danger
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline
                        },
                        CircleShape,
                    )
                    .background(
                        when {
                            isAnswered && option.isCorrect -> SigapTheme.colors.safe
                            isAnswered && isSelected && !option.isCorrect -> SigapTheme.colors.danger
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> Color.Transparent
                        },
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isAnswered) {
                    if (option.isCorrect) {
                        Icon(Lucide.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else if (isSelected) {
                        Icon(Lucide.X, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Text(
                text = option.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = if (isSelected || (isAnswered && option.isCorrect)) FontWeight.Bold else FontWeight.Normal,
                    lineHeight = 18.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuizExplanationCard(
    isCorrect: Boolean,
    explanation: String,
    safetyTip: String,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isCorrect) SigapTheme.colors.safeContainer else SigapTheme.colors.attentionCardBg,
        border = BorderStroke(
            1.dp,
            if (isCorrect) SigapTheme.colors.safe.copy(alpha = 0.35f) else SigapTheme.colors.caution.copy(alpha = 0.4f),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = if (isCorrect) Lucide.ShieldCheck else Lucide.TriangleAlert,
                    contentDescription = null,
                    tint = if (isCorrect) SigapTheme.colors.safe else SigapTheme.colors.onCaution,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = if (isCorrect) "Tepat Sekali!" else "Perhatikan Tanda Bahaya",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    color = if (isCorrect) SigapTheme.colors.safe else SigapTheme.colors.onCaution,
                )
            }

            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            ) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                        text = "Tips Sigap: $safetyTip",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizResultScreen(
    score: Int,
    total: Int,
    onRestart: () -> Unit,
    onShare: () -> Unit,
    onBack: () -> Unit,
) {
    val percentage = (score.toFloat() / total.toFloat()) * 100

    val (badgeText, titleText, descText) = when {
        score == 5 -> Triple(
            "Mata Elang Anti-Penipuan 🦅",
            "Luar Biasa! Kemampuan Sempurna",
            "Anda memiliki ketelitian tingkat tinggi dalam mengenali modus phishing, manipulasi social engineering, dan file APK berbahaya. Pertahankan dan lindungi keluarga Anda!",
        )
        score >= 3 -> Triple(
            "Waspada & Tanggap",
            "Kemampuan Baik & Terlatih",
            "Sebagian besar tanda bahaya penipuan berhasil Anda kenali dengan tepat. Tingkatkan kepekaan pada jenis pesan instan berkedok kurir atau instansi resmi.",
        )
        else -> Triple(
            "Perlu Pelatihan Lebih",
            "Hati-hati terhadap Jebakan APK",
            "Modus penipuan saat ini sangat lihai meniru kerabat dan pihak resmi. Disarankan untuk membaca panduan modus terkini di tab Belajar aplikasi Sigap.",
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.ScreenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        // Big Score Circle
        Box(
            Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$score / $total",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "${percentage.toInt()}% Benar",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primary,
        ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                ),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        Text(
            text = titleText,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = descText,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp, lineHeight = 20.sp),
            textAlign = TextAlign.Center,
            color = SigapTheme.colors.textMuted,
        )

        Spacer(Modifier.height(8.dp))

        // Buttons
        Button(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Lucide.Share2, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Bagikan Hasil ke Keluarga", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Surface(
            onClick = onRestart,
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Lucide.RefreshCw,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Ulangi Tes",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Surface(
            onClick = onBack,
            shape = RoundedCornerShape(14.dp),
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Kembali ke Pusat Edukasi",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

