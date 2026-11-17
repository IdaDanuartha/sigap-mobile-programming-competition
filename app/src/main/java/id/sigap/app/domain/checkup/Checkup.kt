package id.sigap.app.domain.checkup

/** Whether a check is read from the device or confirmed by the user. */
enum class CheckKind { AUTO, SELF }

/** One security check. [flaggedCount] carries extra detail, such as the number of risky services. */
data class CheckupItem(
    val id: String,
    val kind: CheckKind,
    val weight: Int,
    val passed: Boolean,
    val flaggedCount: Int = 0,
    val packageName: String? = null,
)

/** Known check ids and weights. */
object CheckIds {
    const val LISTENER = "listener"
    const val SCREEN_LOCK = "screen_lock"
    const val PATCH = "patch"
    const val ACCESSIBILITY = "accessibility"
    const val PLAY_PROTECT = "play_protect"
    const val UNKNOWN_SOURCES_PREFIX = "unknown_sources:"

    const val WEIGHT_LISTENER = 30
    const val WEIGHT_SCREEN_LOCK = 20
    const val WEIGHT_PATCH = 15
    const val WEIGHT_ACCESSIBILITY = 15
    const val WEIGHT_PLAY_PROTECT = 10
    const val WEIGHT_UNKNOWN_SOURCES = 5
}

/** Quality label of a checkup score. */
enum class CheckupLabel { GOOD, NEEDS_WORK }

/** Weighted scoring for the Security checkup (S09). */
object CheckupScorer {
    const val GOOD_THRESHOLD = 80
    private const val MAX = 100

    fun score(items: List<CheckupItem>): Int {
        val total = items.sumOf { it.weight }
        if (total == 0) return MAX
        return items.filter { it.passed }.sumOf { it.weight } * MAX / total
    }

    fun label(score: Int): CheckupLabel = if (score >= GOOD_THRESHOLD) CheckupLabel.GOOD else CheckupLabel.NEEDS_WORK
}
