package com.sana.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sana.app.data.fake.FakeRepositories
import com.sana.app.model.Milestone
import com.sana.app.model.PlanItem
import com.sana.app.model.UserProfile
import com.sana.app.model.WeeklyStat
import com.sana.app.repository.SanaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: UserProfile? = null,
    val planItems: List<PlanItem> = emptyList(),
    val weeklyStats: List<WeeklyStat> = emptyList(),
    val milestones: List<Milestone> = emptyList(),
    val error: String? = null,
)

class HomeViewModel(
    private val sanaRepository: SanaRepository = FakeRepositories.sanaRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                sanaRepository.observeUserProfile(),
                sanaRepository.observeCurrentPlan(),
                sanaRepository.observeWeeklyStats(),
                sanaRepository.observeMilestones(),
            ) { user, planItems, weeklyStats, milestones ->
                HomeUiState(
                    isLoading = false,
                    user = user,
                    planItems = planItems,
                    weeklyStats = weeklyStats,
                    milestones = milestones,
                )
            }.collect { state ->
                mutableUiState.value = state
            }
        }
    }
}
