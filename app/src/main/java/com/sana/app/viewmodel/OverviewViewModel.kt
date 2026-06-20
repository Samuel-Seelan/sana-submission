package com.sana.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sana.app.di.AppModule
import com.sana.app.model.DayCell
import com.sana.app.model.DayStatus
import com.sana.app.model.WeekRow
import com.sana.app.model.WorkoutSession
import com.sana.app.repository.SanaRepository
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/*
 * OverviewViewModel.kt — the full-plan calendar, derived from real sessions.
 * What: turns the user's saved sessions into a "this week" Mon–Sun strip and one row per recovery
 *       week. A day is Done if it has a session, Missed if it's a past day without one, Upcoming if
 *       it's in the future.
 * Who: Max.
 * When: Goal 7 — Firebase integration.
 */
data class OverviewUiState(
    val isLoading: Boolean = true,
    val thisWeek: List<DayCell> = emptyList(),
    val weeks: List<WeekRow> = emptyList(),
)

class OverviewViewModel(
    sanaRepository: SanaRepository = AppModule.sanaRepository,
) : ViewModel() {
    val uiState: StateFlow<OverviewUiState> = sanaRepository.observeSessions()
        .map { sessions -> buildOverview(sessions) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OverviewUiState(),
        )

    private fun buildOverview(sessions: List<WorkoutSession>): OverviewUiState {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        val sessionDays: Set<Long> = sessions
            .map { Instant.ofEpochMilli(it.startedAtMillis).atZone(zone).toLocalDate().toEpochDay() }
            .toSet()

        // Show every week back to the user's first session (min 4 weeks so the screen looks lived-in).
        val weeksBack = if (sessions.isEmpty()) {
            3
        } else {
            val earliestMonday = sessions
                .minOf { it.startedAtMillis }
                .let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            ChronoUnit.WEEKS.between(earliestMonday, thisMonday).toInt().coerceIn(3, 11)
        }

        val weeks = (0..weeksBack).map { back ->
            val weekStart = thisMonday.minusWeeks(back.toLong())
            val days = buildWeek(weekStart, today, sessionDays)
            WeekRow(
                weekStartEpochDay = weekStart.toEpochDay(),
                label = "Week of ${weekStart.month.name.lowercase()
                    .replaceFirstChar { it.uppercase() }} ${weekStart.dayOfMonth}",
                days = days,
                sessionCount = days.count { it.status == DayStatus.DONE },
            )
        }

        return OverviewUiState(
            isLoading = false,
            thisWeek = buildWeek(thisMonday, today, sessionDays),
            weeks = weeks,
        )
    }

    private fun buildWeek(weekStart: LocalDate, today: LocalDate, sessionDays: Set<Long>): List<DayCell> =
        (0..6).map { offset ->
            val date = weekStart.plusDays(offset.toLong())
            val status = when {
                date.toEpochDay() in sessionDays -> DayStatus.DONE
                date.isAfter(today) -> DayStatus.UPCOMING
                else -> DayStatus.MISSED
            }
            DayCell(
                epochDay = date.toEpochDay(),
                dayLetter = dayLetter(date),
                dayNumber = date.dayOfMonth,
                status = status,
                isToday = date == today,
            )
        }

    private fun dayLetter(date: LocalDate): String = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "M"
        DayOfWeek.TUESDAY -> "T"
        DayOfWeek.WEDNESDAY -> "W"
        DayOfWeek.THURSDAY -> "T"
        DayOfWeek.FRIDAY -> "F"
        DayOfWeek.SATURDAY -> "S"
        DayOfWeek.SUNDAY -> "S"
    }
}
