package id.sigap.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScanHistoryRepository
import id.sigap.app.domain.model.ScanResult
import id.sigap.app.domain.model.TodayStats
import java.util.Calendar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Single day entry for the 7-day threats bar strip. */
data class DayThreatBar(val dayInitial: String, val count: Int, val isToday: Boolean)

/** Immutable state for the Home screen, derived from live Room scan history. */
data class HomeUiState(
    val stats: TodayStats = TodayStats(),
    val latestAlerts: List<ScanResult> = emptyList(),
    val weeklyBlocked: Int = 0,
    val weeklyDangerCount: Int = 0,
    val weeklyCautionCount: Int = 0,
    val weeklyBars: List<DayThreatBar> = emptyList(),
)

class HomeViewModel(history: ScanHistoryRepository) : ViewModel() {
    val uiState: StateFlow<HomeUiState> =
        combine(
            history.observeTodayStats(),
            history.observeLatestAlerts(LATEST_ALERT_COUNT),
            history.observeAll(),
        ) { todayStats, latest, allResults ->
            val now = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfWeekWindow = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -6) }.timeInMillis

            val weeklyThreats = allResults.filter {
                it.timestamp >= startOfWeekWindow && it.level != RiskLevel.SAFE
            }

            val bars = (-6..0).map { offset ->
                val dayCal = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
                val dayStart = dayCal.timeInMillis
                val dayEnd = (dayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
                val count = weeklyThreats.count { it.timestamp in dayStart until dayEnd }
                DayThreatBar(
                    dayInitial = indonesianDayInitial(dayCal.get(Calendar.DAY_OF_WEEK)),
                    count = count,
                    isToday = offset == 0,
                )
            }

            HomeUiState(
                stats = todayStats,
                latestAlerts = latest,
                weeklyBlocked = weeklyThreats.size,
                weeklyDangerCount = weeklyThreats.count { it.level == RiskLevel.DANGER },
                weeklyCautionCount = weeklyThreats.count { it.level == RiskLevel.CAUTION },
                weeklyBars = bars,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState())

    private fun indonesianDayInitial(dayOfWeek: Int): String = when (dayOfWeek) {
        Calendar.SUNDAY -> "M"
        Calendar.MONDAY -> "S"
        Calendar.TUESDAY -> "S"
        Calendar.WEDNESDAY -> "R"
        Calendar.THURSDAY -> "K"
        Calendar.FRIDAY -> "J"
        Calendar.SATURDAY -> "S"
        else -> "-"
    }

    private companion object {
        const val LATEST_ALERT_COUNT = 4
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
