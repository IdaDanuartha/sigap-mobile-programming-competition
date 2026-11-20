package id.sigap.app.domain.model

/** Risk level of a scanned message. */
enum class RiskLevel { SAFE, CAUTION, DANGER }

/** Scam categories (PRD section 5.5). */
enum class ScamCategory {
    APK_MALWARE, FAKE_COURIER, FAKE_INVITATION, AUTHORITY_IMPERSONATION, BANK_PHISHING, FAKE_PRIZE,
    INVESTMENT, ONLINE_LOAN, ROMANCE, JOB_SCAM, MARKETPLACE_FRAUD, OTP_THEFT, OTHER,
}

/** Machine-readable reason a message was flagged. Mapped to localized text in the UI layer. */
enum class ReasonCode {
    APK_FILE, FILE_LURE, SHORTENED_LINK, IP_LINK, RISKY_TLD, LOOKALIKE_DOMAIN, USER_INFO_URL,
    PUNYCODE_URL, LINK_FROM_UNKNOWN, URGENCY, CREDENTIAL_REQUEST, OTP_SHARE_REQUEST,
    IMPERSONATION, MONEY_REWARD, SOCIAL_ENGINEERING,
}

/** How the sender was classified before scanning. */
enum class SenderKind { UNKNOWN, SHORTCODE }

/** Score thresholds per sensitivity setting (PRD section 5.4). */
enum class Sensitivity(val cautionAt: Int, val dangerAt: Int) {
    LOW(45, 80),
    BALANCED(35, 70),
    HIGH(25, 60),
}

/** A message ready for analysis. [text] is held in memory only and never persisted. */
data class MessageInput(
    val sourcePackage: String,
    val senderLabel: String,
    val text: String,
    val timestamp: Long,
    val senderKind: SenderKind,
)

/** A weighted rule hit. */
data class Signal(val reason: ReasonCode, val weight: Int)

/** Result of the rule engine. [reasons] are ordered by weight, strongest first. */
data class Verdict(
    val score: Int,
    val level: RiskLevel,
    val category: ScamCategory,
    val reasons: List<ReasonCode>,
    val hardDanger: Boolean,
)

/** A stored scan result. Contains only a redacted snippet, never the full message. */
data class ScanResult(
    val id: Long,
    val timestamp: Long,
    val sourcePackage: String,
    val senderKind: SenderKind,
    val level: RiskLevel,
    val score: Int,
    val category: ScamCategory,
    val reasons: List<ReasonCode>,
    val redactedSnippet: String,
    val checkedOnDevice: Boolean,
    /** SHA-256 of the sender label, or null for manual checks. */
    val senderKey: String? = null,
)

/** Today's counters for the Home screen. */
data class TodayStats(val scanned: Int = 0, val warnings: Int = 0, val dangerous: Int = 0)


/** Theme preference. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** All persisted user preferences (PRD section 7.1). */
data class UserSettings(
    val userName: String = "Danuartha",
    val sensitivity: Sensitivity = Sensitivity.BALANCED,
    val cloudConsent: Boolean = true,
    val retentionDays: Int = DEFAULT_RETENTION_DAYS,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hideDangerous: Boolean = false,
    val demoMode: Boolean = false,
    val disabledPackages: Set<String> = emptySet(),
    val confirmedChecks: Set<String> = emptySet(),
) {
    companion object {
        const val DEFAULT_RETENTION_DAYS = 30
        val RETENTION_OPTIONS = listOf(7, 30, 90)
    }
}

/** Result of cloud AI deep analysis (OpenAI GPT with Gemini fallback). */
data class AiAnalysisResult(
    val provider: String,
    val level: RiskLevel,
    val category: ScamCategory,
    val confidence: Float,
    val explanation: String,
    val advice: String,
    val isFallback: Boolean = false,
)

/** A sender the user trusts. Only the hashed key is stored. */
data class TrustedSender(val senderKey: String, val addedAt: Long)
