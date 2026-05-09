package app.jammes.thepro.presentation.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.jammes.thepro.domain.model.DayOfWeekBr
import app.jammes.thepro.domain.model.ExerciseLog
import app.jammes.thepro.domain.model.SessionStatus
import app.jammes.thepro.domain.model.WorkoutSession
import app.jammes.thepro.domain.usecase.EnsureSessionsForDateUseCase
import app.jammes.thepro.domain.usecase.LogExerciseUseCase
import app.jammes.thepro.domain.usecase.ObserveSessionsForDateUseCase
import app.jammes.thepro.domain.usecase.RescheduleSessionUseCase
import app.jammes.thepro.domain.usecase.UpdateSessionStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TodayUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val sessions: List<WorkoutSession> = emptyList(),
    val isLoading: Boolean = true
) {
    val selectedDay: DayOfWeekBr get() = DayOfWeekBr.fromJavaDayOfWeek(selectedDate.dayOfWeek)
}

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val ensureSessionsForDate: EnsureSessionsForDateUseCase,
    observeSessionsForDate: ObserveSessionsForDateUseCase,
    private val updateSessionStatus: UpdateSessionStatusUseCase,
    private val rescheduleSession: RescheduleSessionUseCase,
    private val logExercise: LogExerciseUseCase
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TodayUiState> = selectedDate
        .flatMapLatest { date ->
            viewModelScope.launch { ensureSessionsForDate(date) }
            observeSessionsForDate(date).map { sessions ->
                TodayUiState(selectedDate = date, sessions = sessions, isLoading = false)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodayUiState()
        )

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun selectDay(day: DayOfWeekBr) {
        val today = LocalDate.now()
        val todayDow = DayOfWeekBr.fromJavaDayOfWeek(today.dayOfWeek)
        val diff = day.isoValue - todayDow.isoValue
        selectedDate.value = today.plusDays(diff.toLong())
    }

    fun markCompleted(sessionId: Long) {
        viewModelScope.launch { updateSessionStatus(sessionId, SessionStatus.CONCLUIDO) }
    }

    fun markPending(sessionId: Long) {
        viewModelScope.launch { updateSessionStatus(sessionId, SessionStatus.PENDENTE) }
    }

    fun reschedule(sessionId: Long, newDate: LocalDate) {
        viewModelScope.launch { rescheduleSession(sessionId, newDate) }
    }

    fun logExerciseEntry(log: ExerciseLog) {
        viewModelScope.launch { logExercise(log) }
    }
}
