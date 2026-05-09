package app.jammes.thepro.domain.usecase

import app.jammes.thepro.domain.model.DayOfWeekBr
import app.jammes.thepro.domain.model.WeeklyScheduleEntry
import app.jammes.thepro.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
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
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(day: DayOfWeekBr, workoutId: Long): Long =
        repository.assign(day, workoutId)
}

class UnassignScheduleEntryUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(entryId: Long) = repository.unassign(entryId)
}
