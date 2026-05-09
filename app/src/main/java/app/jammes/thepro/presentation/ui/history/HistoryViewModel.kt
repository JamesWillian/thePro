package app.jammes.thepro.presentation.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.jammes.thepro.domain.model.SessionStatus
import app.jammes.thepro.domain.model.WorkoutSession
import app.jammes.thepro.domain.usecase.ObserveSessionsRangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class WeekSummary(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val sessions: List<WorkoutSession>,
    val total: Int,
    val completed: Int,
    val rescheduled: Int,
    val pending: Int
) {
    val completionRate: Float get() = if (total == 0) 0f else completed.toFloat() / total.toFloat()
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val observeSessionsRange: ObserveSessionsRangeUseCase
) : ViewModel() {

    private val anchor = MutableStateFlow(LocalDate.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    val summary: StateFlow<WeekSummary> = anchor
        .flatMapLatest { date ->
            val start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val end = start.plusDays(6)
            observeSessionsRange(start, end).map { sessions ->
                WeekSummary(
                    weekStart = start,
                    weekEnd = end,
                    sessions = sessions.sortedBy { it.date },
                    total = sessions.size,
                    completed = sessions.count { it.status == SessionStatus.CONCLUIDO },
                    rescheduled = sessions.count { it.status == SessionStatus.REMARCADO },
                    pending = sessions.count { it.status == SessionStatus.PENDENTE }
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            WeekSummary(
                weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                weekEnd = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusDays(6),
                sessions = emptyList(), total = 0, completed = 0, rescheduled = 0, pending = 0
            )
        )

    fun previousWeek() { anchor.value = anchor.value.minusWeeks(1) }
    fun nextWeek() { anchor.value = anchor.value.plusWeeks(1) }
    fun thisWeek() { anchor.value = LocalDate.now() }
}
