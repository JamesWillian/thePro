package app.jammes.thepro.domain.repository

import app.jammes.thepro.domain.model.ExerciseLog
import app.jammes.thepro.domain.model.SessionStatus
import app.jammes.thepro.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SessionRepository {
    fun observeForDate(date: LocalDate): Flow<List<WorkoutSession>>
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<WorkoutSession>>
    suspend fun ensureSessionsForDate(date: LocalDate): List<WorkoutSession>
    suspend fun reconcileForDate(date: LocalDate)
    suspend fun updateStatus(sessionId: Long, status: SessionStatus)
    suspend fun reschedule(sessionId: Long, newDate: LocalDate)
    suspend fun upsertExerciseLog(log: ExerciseLog): Long
    suspend fun getSession(sessionId: Long): WorkoutSession?
}
