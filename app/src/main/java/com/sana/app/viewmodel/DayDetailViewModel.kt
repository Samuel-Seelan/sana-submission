@file:OptIn(ExperimentalCoroutinesApi::class)

package com.sana.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sana.app.di.AppModule
import com.sana.app.model.DayStats
import com.sana.app.model.ExerciseCatalog
import com.sana.app.model.Recording
import com.sana.app.repository.SanaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/*
 * DayDetailViewModel.kt — one calendar day's recordings and totals.
 * What: streams the recordings saved on a given day (joined to the catalog for names) plus the
 *       derived day stats. Call load(epochDay) once from the route.
 * Who: Max.
 * When: Goal 7 — Firebase integration.
 */
data class DayDetailUiState(
    val isLoading: Boolean = true,
    val recordings: List<Recording> = emptyList(),
    val dayStats: DayStats? = null,
)

class DayDetailViewModel(
    sanaRepository: SanaRepository = AppModule.sanaRepository,
) : ViewModel() {

    private val epochDay = MutableStateFlow<Long?>(null)

    private val recordingsFlow = epochDay.flatMapLatest { day ->
        if (day == null) flowOf(emptyList()) else sanaRepository.observeRecordingsForDay(day)
    }
    private val statsFlow = epochDay.flatMapLatest { day ->
        if (day == null) flowOf(null) else sanaRepository.observeDayStats(day)
    }

    val uiState: StateFlow<DayDetailUiState> = combine(
        epochDay,
        recordingsFlow,
        statsFlow,
    ) { day, recordings, stats ->
        DayDetailUiState(
            isLoading = day == null,
            recordings = recordings.mapIndexedNotNull { index, metadata ->
                ExerciseCatalog.find(metadata.exerciseId)?.let { exercise ->
                    Recording(
                        id = index.toLong(),
                        exercise = exercise,
                        sets = metadata.sets,
                        reps = metadata.reps,
                        durationMs = metadata.durationMs,
                        hasClip = metadata.videoStoragePath != null,
                    )
                }
            },
            dayStats = stats,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DayDetailUiState(),
    )

    fun load(day: Long) {
        if (epochDay.value != day) epochDay.value = day
    }
}
