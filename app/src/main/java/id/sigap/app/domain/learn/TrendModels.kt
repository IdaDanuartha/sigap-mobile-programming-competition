package id.sigap.app.domain.learn

import id.sigap.app.domain.model.ScamCategory

/** Verified headline stat for the Learn feed header (Appendix A). */
data class HeadlineStat(
    val figure: String,
    val labelId: String,
    val labelEn: String,
    val source: String,
)

/** A scam trend article (S10a, S10b). Supports both Indonesian and English. */
data class TrendArticle(
    val id: String,
    val category: ScamCategory,
    val readMinutes: Int,
    val titleId: String,
    val titleEn: String,
    val summaryId: String,
    val summaryEn: String,
    val howItWorksId: List<String>,
    val howItWorksEn: List<String>,
    val redFlagsId: List<String>,
    val redFlagsEn: List<String>,
    val actionsId: List<String>,
    val actionsEn: List<String>,
    val sourceName: String,
    val sourceDate: String,
)
