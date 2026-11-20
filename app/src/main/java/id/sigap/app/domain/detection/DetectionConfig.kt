package id.sigap.app.domain.detection

import id.sigap.app.domain.model.ReasonCode
import id.sigap.app.domain.model.ScamCategory

/** Matches whole words or phrases, case-insensitively, without matching inside longer words. */
class KeywordGroup(words: List<String>) {
    private val regex = Regex(
        words.joinToString("|", prefix = "(?<![\\p{L}\\d])(?:", postfix = ")(?![\\p{L}\\d])") { Regex.escape(it) },
        RegexOption.IGNORE_CASE,
    )

    fun matches(text: String): Boolean = regex.containsMatchIn(text)
}

/** Tunable detection constants: weights, keywords, brand and domain lists. */
object DetectionConfig {
    const val MAX_SCORE = 100
    const val SNIPPET_MAX_CHARS = 260
    const val MAX_REASONS = 5
    const val SAFE_OTP_SCORE = 5
    const val LOOKALIKE_MAX_DISTANCE = 1
    const val LOOKALIKE_MIN_BRAND_LENGTH = 4
    const val LOOKALIKE_CONTAINS_MIN_BRAND_LENGTH = 5

    val WEIGHTS: Map<ReasonCode, Int> = mapOf(
        ReasonCode.APK_FILE to 70,
        ReasonCode.FILE_LURE to 20,
        ReasonCode.SHORTENED_LINK to 20,
        ReasonCode.IP_LINK to 35,
        ReasonCode.RISKY_TLD to 20,
        ReasonCode.LOOKALIKE_DOMAIN to 40,
        ReasonCode.USER_INFO_URL to 30,
        ReasonCode.PUNYCODE_URL to 30,
        ReasonCode.LINK_FROM_UNKNOWN to 10,
        ReasonCode.URGENCY to 15,
        ReasonCode.CREDENTIAL_REQUEST to 25,
        ReasonCode.OTP_SHARE_REQUEST to 35,
        ReasonCode.IMPERSONATION to 15,
        ReasonCode.MONEY_REWARD to 15,
        ReasonCode.SOCIAL_ENGINEERING to 20,
    )

    val APK_FILE = Regex("\\.(?:apk|xapk)(?![\\p{L}\\d])", RegexOption.IGNORE_CASE)

    val FILE_LURE = KeywordGroup(
        listOf(
            "undangan pernikahan", "undangan digital", "undangan", "lihat foto paket", "foto paket",
            "surat tilang", "e-tilang", "tilang", "pps", "resi", "wedding invitation", "package photo",
            "traffic ticket",
        ),
    )
    val URGENCY = KeywordGroup(
        listOf(
            "segera", "hari ini", "batas waktu", "akan diblokir", "diblokir", "last warning",
            "immediately", "urgent", "within 24 hours", "final notice",
        ),
    )
    val OTP_TERMS = KeywordGroup(listOf("otp", "kode verifikasi", "verification code", "kode rahasia"))
    val CREDENTIAL = KeywordGroup(
        listOf(
            "otp", "pin", "cvv", "password", "kata sandi", "kode verifikasi", "verification code",
            "konfirmasi data", "verifikasi data",
        ),
    )
    val NO_SHARE_WARNING = KeywordGroup(
        listOf(
            "jangan berikan", "jangan bagikan", "jangan beritahu", "rahasia", "do not share",
            "don't share", "never share",
        ),
    )
    val OTP_SHARE_REQUEST = Regex(
        "(?<!jangan )(?<!not )(?<!don't )(?<!never )(?:kirim(?:kan)?|berikan|bagikan|sebutkan|send|share|reply with)\\s+(?:me\\s+|us\\s+)?(?:the\\s+|kode\\s+)?(?:otp|code|kode|pin)",
        RegexOption.IGNORE_CASE,
    )
    val IMPERSONATION = KeywordGroup(
        listOf(
            "ppatk", "ojk", "polisi", "polri", "kurir", "petugas", "bank", "bpjs", "pajak", "kejaksaan",
            "customer service", "official",
        ),
    )
    val MONEY_REWARD = KeywordGroup(
        listOf(
            "hadiah", "selamat anda terpilih", "selamat anda menang", "transfer", "rekening", "diputihkan",
            "profit harian", "tanpa agunan", "komisi per tugas", "you have won", "prize", "pinjaman cair",
        ),
    )
    val SOCIAL_ENGINEERING = KeywordGroup(
        listOf(
            "tolong transfer dulu", "pinjam uang dulu", "gaji harian", "kerja sampingan", "kerja online",
            "part time", "part-time", "work from home", "easy income",
        ),
    )

    // Category hints, checked in order. First match wins.
    val CATEGORY_HINTS: List<Pair<KeywordGroup, ScamCategory>> = listOf(
        KeywordGroup(listOf("undangan", "wedding invitation")) to ScamCategory.FAKE_INVITATION,
        KeywordGroup(listOf("paket", "resi", "kurir", "parcel", "package")) to ScamCategory.FAKE_COURIER,
        KeywordGroup(listOf("tilang", "ppatk", "polisi", "polri", "kejaksaan", "pajak", "bpjs")) to ScamCategory.AUTHORITY_IMPERSONATION,
        KeywordGroup(listOf("hadiah", "selamat anda terpilih", "selamat anda menang", "you have won", "prize")) to ScamCategory.FAKE_PRIZE,
        KeywordGroup(listOf("investasi", "profit harian", "investment", "trading")) to ScamCategory.INVESTMENT,
        KeywordGroup(listOf("pinjaman", "tanpa agunan", "diputihkan", "loan")) to ScamCategory.ONLINE_LOAN,
        KeywordGroup(listOf("lowongan", "gaji harian", "kerja sampingan", "komisi per tugas", "part time", "part-time", "work from home")) to ScamCategory.JOB_SCAM,
        KeywordGroup(listOf("otp", "kode verifikasi", "verification code")) to ScamCategory.OTP_THEFT,
        KeywordGroup(listOf("bank", "rekening", "kartu kredit", "mobile banking")) to ScamCategory.BANK_PHISHING,
    )

    val URL_SHORTENERS = setOf(
        "bit.ly", "tinyurl.com", "t.co", "is.gd", "cutt.ly", "s.id", "shorturl.at", "rebrand.ly",
        "tiny.cc", "ow.ly", "goo.gl", "bit.do", "rb.gy", "t.ly",
    )
    val RISKY_TLDS = setOf("xyz", "top", "icu", "click", "link", "cc", "tk", "ml", "ga", "cf", "gq", "buzz", "rest", "cyou", "sbs")
    val FILE_EXTENSIONS = setOf("apk", "xapk", "pdf", "jpg", "jpeg", "png", "doc", "docx", "xls", "xlsx", "zip", "mp4", "txt")
    val SECOND_LEVEL_LABELS = setOf("co", "go", "or", "ac", "web", "my", "sch", "net", "com", "org")

    /** Brand name to its official registrable domains, for lookalike detection. */
    val BRANDS: Map<String, Set<String>> = mapOf(
        "bca" to setOf("bca.co.id", "klikbca.com"),
        "bni" to setOf("bni.co.id"),
        "bri" to setOf("bri.co.id"),
        "mandiri" to setOf("bankmandiri.co.id", "livin.id"),
        "btn" to setOf("btn.co.id"),
        "cimb" to setOf("cimbniaga.co.id"),
        "ovo" to setOf("ovo.id"),
        "gopay" to setOf("gopay.co.id", "gojek.com"),
        "dana" to setOf("dana.id"),
        "shopeepay" to setOf("shopee.co.id"),
        "linkaja" to setOf("linkaja.id"),
        "jne" to setOf("jne.co.id"),
        "sicepat" to setOf("sicepat.com"),
        "anteraja" to setOf("anteraja.id"),
        "tokopedia" to setOf("tokopedia.com"),
        "shopee" to setOf("shopee.co.id"),
        "pln" to setOf("pln.co.id"),
        "bpjs" to setOf("bpjs-kesehatan.go.id", "bpjsketenagakerjaan.go.id"),
        "pajak" to setOf("pajak.go.id"),
        "polri" to setOf("polri.go.id"),
        "ojk" to setOf("ojk.go.id"),
    )
}


