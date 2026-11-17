package id.sigap.app.feature.activity

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.sigap.app.R
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScanHistoryRepository
import id.sigap.app.domain.model.ScanResult
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Level filter chips. A null level means "All". */
enum class ActivityFilter(val level: RiskLevel?, @StringRes val label: Int) {
    ALL(null, R.string.filter_all),
    DANGER(RiskLevel.DANGER, R.string.filter_danger),
    CAUTION(RiskLevel.CAUTION, R.string.filter_caution),
    SAFE(RiskLevel.SAFE, R.string.filter_safe),
}

data class ActivityGroup(@StringRes val title: Int, val items: List<ScanResult>)

data class ActivityUiState(
    val query: String = "",
    val filter: ActivityFilter = ActivityFilter.ALL,
    val groups: List<ActivityGroup> = emptyList(),
)

class ActivityViewModel(private val history: ScanHistoryRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(ActivityFilter.ALL)
    private var lastDeleted: ScanResult? = null

    val uiState: StateFlow<ActivityUiState> =
        combine(history.observeAll(), query, filter) { all, q, f ->
            val filtered = all.filter { r ->
                (f.level == null || r.level == f.level) &&
                    (q.isBlank() || r.redactedSnippet.contains(q, ignoreCase = true))
            }
            ActivityUiState(q, f, group(filtered))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ActivityUiState())

    fun setQuery(value: String) = query.update { value }

    fun setFilter(value: ActivityFilter) = filter.update { value }

    fun delete(result: ScanResult) {
        lastDeleted = result
        viewModelScope.launch { history.delete(result.id) }
    }

    fun undoDelete() {
        val restored = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch { history.save(restored) }
    }

    private fun group(items: List<ScanResult>): List<ActivityGroup> {
        val today = startOfDay(0)
        val yesterday = startOfDay(-1)
        val buckets = items.groupBy {
            when {
                it.timestamp >= today -> R.string.group_today
                it.timestamp >= yesterday -> R.string.group_yesterday
                else -> R.string.group_earlier
            }
        }
        return listOf(R.string.group_today, R.string.group_yesterday, R.string.group_earlier)
            .mapNotNull { title -> buckets[title]?.let { ActivityGroup(title, it) } }
    }

    private fun startOfDay(offsetDays: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, offsetDays)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
