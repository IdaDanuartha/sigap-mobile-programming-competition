package id.sigap.app.data.local.seeded

import id.sigap.app.domain.learn.HeadlineStat
import id.sigap.app.domain.learn.TrendArticle
import id.sigap.app.domain.model.ScamCategory

/** Verified figures and scam trend articles bundled offline (PRD section S10, Appendix A). */
object SeededTrends {

    val HEADLINE_STATS = listOf(
        HeadlineStat(
            figure = "608.000+",
            labelId = "Laporan ke IASC (OJK) hingga Juni 2026",
            labelEn = "Scam reports to IASC (OJK) through June 2026",
            source = "Selular.ID, detikFinance (Juni 2026)",
        ),
        HeadlineStat(
            figure = "Rp9,3 T",
            labelId = "Total kerugian tercatat; baru sekitar Rp674 M (~7%) berhasil diamankan",
            labelEn = "Recorded losses; only about Rp674 B (~7%) secured",
            source = "Bisnis.com, OJK (Juni 2026)",
        ),
        HeadlineStat(
            figure = "66%",
            labelId = "Warga Indonesia pernah mengalami percobaan penipuan",
            labelEn = "Indonesians who have faced a scam attempt",
            source = "GASA State of Scams in Indonesia 2025 via Katadata",
        ),
    )

    val ARTICLES = listOf(
        TrendArticle(
            id = "apk_wedding_invitation",
            category = ScamCategory.FAKE_INVITATION,
            readMinutes = 2,
            titleId = "Undangan Pernikahan Berwujud File .APK",
            titleEn = "Wedding Invitations Disguised as .APK Files",
            summaryId = "Pelaku mengirim file aplikasi (.apk) bernama Undangan Pernikahan untuk mencuri akses SMS dan OTP.",
            summaryEn = "Scammers send an .apk installer named Wedding Invitation to steal SMS and OTP access.",
            howItWorksId = listOf(
                "Pelaku mengirim pesan dari nomor tak dikenal dengan lampiran bernama 'Undangan Pernikahan.apk'.",
                "Saat korban membuka file, ponsel meminta izin memasang aplikasi dari sumber tak dikenal.",
                "Aplikasi meminta izin baca SMS dan layanan aksesibilitas untuk menyadap OTP mobile banking.",
            ),
            howItWorksEn = listOf(
                "An unknown number sends an attachment named 'Undangan Pernikahan.apk'.",
                "Opening it prompts Android to install an app from an unknown source.",
                "The app requests SMS and accessibility permissions to intercept banking OTPs.",
            ),
            redFlagsId = listOf(
                "Nama file berakhiran .apk atau .xapk (undangan digital asli selalu berupa tautan web, bukan aplikasi).",
                "Pengirim tidak ada di daftar kontak dan mendesak agar segera dibuka.",
                "Muncul permintaan izin baca SMS atau aksesibilitas setelah dibuka.",
            ),
            redFlagsEn = listOf(
                "File ends in .apk or .xapk (genuine digital invitations are web links, never apps).",
                "Sender is not in your contacts and urges you to open it quickly.",
                "Prompts for SMS or accessibility permissions after opening.",
            ),
            actionsId = listOf(
                "Jangan pernah mengunduh atau membuka file .apk dari chat.",
                "Jika telanjur dipasang, nyalakan mode pesawat, hapus aplikasi, dan hubungi bank.",
            ),
            actionsEn = listOf(
                "Never download or install .apk files received in chat.",
                "If installed, turn on airplane mode, uninstall the app, and call your bank.",
            ),
            sourceName = "detikInet, Suara.com & Imbauan BSSN",
            sourceDate = "2025–2026",
        ),
        TrendArticle(
            id = "fake_courier_photo",
            category = ScamCategory.FAKE_COURIER,
            readMinutes = 2,
            titleId = "Modus Kurir: 'Lihat Foto Paket.apk' dan Resi Palsu",
            titleEn = "Fake Courier: 'Lihat Foto Paket.apk' and Phishing Tracking Links",
            summaryId = "Pelaku mengaku kurir ekspedisi dan mengirim file APK atau tautan pelacakan palsu.",
            summaryEn = "Scammers impersonate delivery couriers and send APK files or fake tracking links.",
            howItWorksId = listOf(
                "Pelaku menghubungi lewat WhatsApp mengaku kurir dan mengirim file 'Lihat Foto Paket.apk'.",
                "Versi lain mengirim tautan domain tiruan yang meminta konfirmasi alamat dan PIN/OTP.",
                "Data yang dimasukkan langsung dipakai untuk mengambil alih akun dompet digital atau bank.",
            ),
            howItWorksEn = listOf(
                "A sender on WhatsApp claims to be a courier and attaches 'Lihat Foto Paket.apk'.",
                "Another variant sends a lookalike tracking link asking for address confirmation and PIN/OTP.",
                "Entered credentials are immediately used to drain e-wallet or bank accounts.",
            ),
            redFlagsId = listOf(
                "Foto paket dikirim sebagai file .apk, bukan gambar .jpg atau .png.",
                "Tautan pelacakan memakai domain aneh (.xyz, .top, .click) atau pemendek URL.",
                "Kurir meminta kode OTP atau biaya tambahan melalui tautan.",
            ),
            redFlagsEn = listOf(
                "Package photo has an .apk extension instead of .jpg or .png.",
                "Tracking link uses an unusual TLD (.xyz, .top, .click) or URL shortener.",
                "Courier asks for an OTP or extra fee via a link.",
            ),
            actionsId = listOf(
                "Cek nomor resi hanya di aplikasi resmi ekspedisi atau marketplace.",
                "Tolak permintaan instal aplikasi atau pengisian PIN/OTP.",
            ),
            actionsEn = listOf(
                "Check tracking numbers only inside the official courier or marketplace app.",
                "Refuse any prompt to install an app or enter a PIN/OTP.",
            ),
            sourceName = "detikInet & BSSN",
            sourceDate = "2025–2026",
        ),
        TrendArticle(
            id = "online_shopping_marketplace",
            category = ScamCategory.MARKETPLACE_FRAUD,
            readMinutes = 3,
            titleId = "Penipuan Belanja Online (113.520 Laporan IASC)",
            titleEn = "Online Shopping Fraud (113,520 IASC Reports)",
            summaryId = "Kategori paling banyak dilaporkan ke OJK dengan kerugian sekitar Rp1,78 triliun.",
            summaryEn = "The single most reported scam category to OJK, with Rp1.78 trillion in losses.",
            howItWorksId = listOf(
                "Penjual palsu menawarkan barang murah dan mengajak transaksi di luar aplikasi marketplace.",
                "Setelah korban transfer ke rekening pribadi, pelaku meminta biaya admin tambahan atau menghilang.",
                "Sebagian pelaku mengirim tautan refund palsu yang meminta PIN dan OTP.",
            ),
            howItWorksEn = listOf(
                "Fake sellers offer cheap items and push buyers to pay outside the official marketplace.",
                "After the buyer transfers to a personal account, the scammer demands extra fees or vanishes.",
                "Some send a fake refund link that asks for PIN and OTP.",
            ),
            redFlagsId = listOf(
                "Penjual menolak rekber/escrow resmi dan meminta transfer langsung.",
                "Harga jauh di bawah pasaran dengan alasan cuci gudang mendesak.",
                "Tautan klaim refund di luar aplikasi resmi.",
            ),
            redFlagsEn = listOf(
                "Seller refuses official marketplace escrow and insists on direct transfer.",
                "Price is far below market with urgent clearance claims.",
                "Refund link points outside the official app.",
            ),
            actionsId = listOf(
                "Selalu bertransaksi di dalam aplikasi marketplace resmi.",
                "Periksa nomor rekening di fitur Pemeriksaan Entitas Sigap atau CekRekening.id sebelum transfer.",
            ),
            actionsEn = listOf(
                "Always keep payment inside the official marketplace escrow.",
                "Look up the bank account in Sigap Entity Lookup or CekRekening.id before transferring.",
            ),
            sourceName = "JPNN mengutip OJK (IASC)",
            sourceDate = "Juni 2026",
        ),
        TrendArticle(
            id = "traffic_ticket_authority",
            category = ScamCategory.AUTHORITY_IMPERSONATION,
            readMinutes = 2,
            titleId = "Surat Tilang Elektronik (ETLE) & Mengaku Petugas Resmi",
            titleEn = "Fake Traffic Tickets (ETLE) & Authority Impersonation",
            summaryId = "Pesan mengatasnamakan Polri, Kejaksaan, Pajak, atau OJK berisi file APK atau tautan denda.",
            summaryEn = "Messages claiming to be Police, Tax, or OJK with APK attachments or fine payment links.",
            howItWorksId = listOf(
                "Korban menerima pesan berisi 'Surat Tilang.apk' atau ancaman pemblokiran rekening.",
                "Pelaku menekan korban dengan batas waktu hari ini agar panik dan langsung mengeklik.",
                "Korban diarahkan mentransfer uang jaminan atau memasang aplikasi penyadap.",
            ),
            howItWorksEn = listOf(
                "Victim receives 'Surat Tilang.apk' or a threat of immediate account freeze.",
                "Scammer uses tight deadlines ('today') to cause panic.",
                "Victim is pushed to transfer a fine or install a spyware APK.",
            ),
            redFlagsId = listOf(
                "Instansi resmi dihubungi lewat nomor WhatsApp pribadi.",
                "Surat resmi berformat .apk, bukan dokumen atau situs .go.id.",
                "Meminta transfer ke rekening perorangan atau meminta kode OTP.",
            ),
            redFlagsEn = listOf(
                "Official agency contacts you from a personal WhatsApp number.",
                "Notice is an .apk file instead of an official .go.id page.",
                "Requests transfer to an individual bank account or asks for OTP.",
            ),
            actionsId = listOf(
                "Konfirmasi surat tilang hanya di situs resmi konfirmasi-etle.polri.go.id.",
                "Jangan pernah menanggapi ancaman blokir rekening dari nomor pribadi.",
            ),
            actionsEn = listOf(
                "Verify traffic tickets only on the official polri.go.id portal.",
                "Never act on account-freeze threats from personal phone numbers.",
            ),
            sourceName = "Suara.com & BSSN",
            sourceDate = "2025–2026",
        ),
        TrendArticle(
            id = "part_time_job_commission",
            category = ScamCategory.JOB_SCAM,
            readMinutes = 2,
            titleId = "Lowongan Kerja Paruh Waktu & Komisi Per Tugas",
            titleEn = "Part-Time Job Offers & Per-Task Commission Scams",
            summaryId = "Tawaran kerja mudah (like/subscribe) yang awalnya membayar kecil lalu meminta deposit besar.",
            summaryEn = "Easy task offers that pay small rewards first, then lock larger deposits.",
            howItWorksId = listOf(
                "Pelaku menawarkan komisi harian hanya dengan menyelesaikan tugas sederhana di grup chat.",
                "Satu atau dua tugas pertama benar-benar dibayar agar korban percaya.",
                "Korban kemudian diminta top-up uang yang semakin besar untuk mencairkan komisi.",
            ),
            howItWorksEn = listOf(
                "Scammer offers daily pay for simple tasks in a chat group.",
                "The first one or two tasks pay out small amounts to build trust.",
                "Victim is then told to deposit larger sums to unlock higher commissions.",
            ),
            redFlagsId = listOf(
                "Pekerjaan meminta Anda mentransfer uang terlebih dahulu (deposit/top-up).",
                "Iming-iming gaji harian tinggi tanpa syarat keahlian.",
            ),
            redFlagsEn = listOf(
                "The job requires you to transfer money first (deposit or top-up).",
                "Promises high daily pay with no qualifications required.",
            ),
            actionsId = listOf(
                "Berhenti segera jika diminta menyetor uang untuk bekerja.",
                "Jika sudah telanjur transfer, jangan tambah setor lagi dan segera lapor ke IASC (iasc.ojk.go.id).",
            ),
            actionsEn = listOf(
                "Stop immediately if any job asks you to deposit money.",
                "If you already transferred money, never pay 'unlock fees' and report to IASC (iasc.ojk.go.id).",
            ),
            sourceName = "OJK IASC & Katadata",
            sourceDate = "2025–2026",
        ),
    )
}
