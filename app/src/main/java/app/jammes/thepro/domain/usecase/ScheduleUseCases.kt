package app.jammes.thepro.domain.usecase

import app.jammes.thepro.domain.model.DayOfWeekBr
import app.jammes.thepro.domain.model.WeeklyScheduleEntry
import app.jammes.thepro.domain.repository.ScheduleRepository
import app.jammes.thepro.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class ObserveWeekUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    operator fun invoke(): Flow<Map<DayOfWeekBr, List<WeeklyScheduleEntry>>> =
        repository.observeWeek()
}

class ObserveDayScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    operator fun invoke(day: DayOfWeekBr): Flow<List<WeeklyScheduleEntry>> =
        repository.observeForDay(day)
}

class AssignWorkoutToDayUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(day: DayOfWeekBr, workoutId: Long): Long {
        val id = scheduleRepository.assign(day, workoutId)
        sessionRepository.reconcileForDate(LocalDate.now())
        return id
    }
}

class UnassignScheduleEntryUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(entryId: Long) {
        scheduleRepository.unassign(entryId)
        sessionRepository.reconcileForDate(LocalDate.now())
    }
}
