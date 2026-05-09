package app.jammes.thepro.presentation.ui.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.jammes.thepro.domain.model.DayOfWeekBr
import app.jammes.thepro.domain.model.WeeklyScheduleEntry
import app.jammes.thepro.domain.model.Workout
import app.jammes.thepro.domain.usecase.AssignWorkoutToDayUseCase
import app.jammes.thepro.domain.usecase.ObserveWeekUseCase
import app.jammes.thepro.domain.usecase.ObserveWorkoutsUseCase
import app.jammes.thepro.domain.usecase.UnassignScheduleEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeekUiState(
    val schedule: Map<DayOfWeekBr, List<WeeklyScheduleEntry>> = DayOfWeekBr.entries.associateWith { emptyList() },
    val workouts: List<Workout> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class WeekViewModel @Inject constructor(
    observeWeek: ObserveWeekUseCase,
    observeWorkouts: ObserveWorkoutsUseCase,
    private val assignUseCase: AssignWorkoutToDayUseCase,
    private val unassignUseCase: UnassignScheduleEntryUseCase
) : ViewModel() {

    val uiState: StateFlow<WeekUiState> =
        combine(observeWeek(), observeWorkouts()) { schedule, workouts ->
            WeekUiState(schedule = schedule, workouts = workouts, isLoading = false)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeekUiState())

    fun assign(day: DayOfWeekBr, workoutId: Long) {
        viewModelScope.launch { assignUseCase(day, workoutId) }
    }

    fun unassign(entryId: Long) {
        viewModelScope.launch { unassignUseCase(entryId) }
    }
}
