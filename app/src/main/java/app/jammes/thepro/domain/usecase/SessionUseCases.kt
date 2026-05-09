package app.jammes.thepro.domain.usecase

import app.jammes.thepro.domain.model.ExerciseLog
import app.jammes.thepro.domain.model.SessionStatus
import app.jammes.thepro.domain.model.WorkoutSession
import app.jammes.thepro.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class ObserveSessionsForDateUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    operator fun invoke(date: LocalDate): Flow<List<WorkoutSession>> =
        repository.observeForDate(date)
}

class ObserveSessionsRangeUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    operator fun invoke(from: LocalDate, to: LocalDate): Flow<List<WorkoutSession>> =
        repository.observeRange(from, to)
}

class EnsureSessionsForDateUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(date: LocalDate): List<WorkoutSession> =
        repository.ensureSessionsForDate(date)
}

class UpdateSessionStatusUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Long, status: SessionStatus) =
        repository.updateStatus(sessionId, status)
}

class RescheduleSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Long, newDate: LocalDate) =
        repository.reschedule(sessionId, newDate)
}

class LogExerciseUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(log: ExerciseLog): Long =
        repository.upsertExerciseLog(log)
}
