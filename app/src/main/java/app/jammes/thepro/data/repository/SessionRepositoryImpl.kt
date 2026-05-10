package app.jammes.thepro.data.repository

import androidx.room.withTransaction
import app.jammes.thepro.data.local.AppDatabase
import app.jammes.thepro.data.local.dao.ScheduleDao
import app.jammes.thepro.data.local.dao.SessionDao
import app.jammes.thepro.data.local.dao.WorkoutDao
import app.jammes.thepro.data.local.entity.WorkoutSessionEntity
import app.jammes.thepro.data.mapper.toDomain
import app.jammes.thepro.data.mapper.toEntity
import app.jammes.thepro.domain.model.DayOfWeekBr
import app.jammes.thepro.domain.model.ExerciseLog
import app.jammes.thepro.domain.model.SessionStatus
import app.jammes.thepro.domain.model.Workout
import app.jammes.thepro.domain.model.WorkoutSession
import app.jammes.thepro.domain.repository.SessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val sessionDao: SessionDao,
    private val workoutDao: WorkoutDao,
    private val scheduleDao: ScheduleDao
) : SessionRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeForDate(date: LocalDate): Flow<List<WorkoutSession>> =
        sessionDao.observeForDate(date.toEpochDay()).flatMapLatest { sessions ->
            hydrate(sessions)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<WorkoutSession>> =
        sessionDao.observeRange(from.toEpochDay(), to.toEpochDay()).flatMapLatest { sessions ->
            hydrate(sessions)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun hydrate(sessions: List<WorkoutSessionEntity>): Flow<List<WorkoutSession>> {
        if (sessions.isEmpty()) return flowOf(emptyList())
        return combine(sessions.map { s ->
            combine(
                workoutDao.observeById(s.workoutId),
                workoutDao.observeWorkoutExercises(s.workoutId),
                sessionDao.observeLogs(s.id)
            ) { workoutEntity, exerciseRows, logs ->
                val workout = workoutEntity?.toDomain(exerciseRows.map { it.toDomain() })
                if (workout == null) null
                else {
                    val byId = workout.exercises.associateBy { it.id }
                    val mappedLogs = logs.mapNotNull { log ->
                        val we = byId[log.workoutExerciseId] ?: return@mapNotNull null
                        log.toDomain(we.exercise)
                    }
                    s.toDomain(workout, mappedLogs)
                }
            }
        }) { it.filterNotNull().toList() }
    }

    override suspend fun ensureSessionsForDate(date: LocalDate): List<WorkoutSession> {
        return database.withTransaction {
            val existing = sessionDao.listForDate(date.toEpochDay())
            if (existing.isEmpty()) {
                materializeFromSchedule(date)
            } else {
                reconcileInternal(date)
            }
            sessionDao.listForDate(date.toEpochDay()).toDomainList()
        }
    }

    override suspend fun reconcileForDate(date: LocalDate) {
        database.withTransaction { reconcileInternal(date) }
    }

    /**
     * Sincroniza sessões pendentes (sem logs) com o schedule atual.
     * - Remove sessões PENDENTE+vazias cujo workoutId não está mais no schedule do dia.
     * - Cria sessões pendentes para entradas do schedule que ainda não foram materializadas.
     * Só age em datas >= hoje; passado é histórico imutável.
     */
    private suspend fun reconcileInternal(date: LocalDate) {
        if (date.isBefore(LocalDate.now())) return
        val dow = DayOfWeekBr.fromJavaDayOfWeek(date.dayOfWeek)
        val schedule = scheduleDao.listForDay(dow.isoValue)
        val scheduleWorkoutIds = schedule.map { it.workoutId }.toSet()
        val existing = sessionDao.listForDate(date.toEpochDay())

        existing.forEach { s ->
            if (s.status == SessionStatus.PENDENTE.name && s.workoutId !in scheduleWorkoutIds) {
                if (sessionDao.listLogs(s.id).isEmpty()) {
                    sessionDao.deleteById(s.id)
                }
            }
        }

        val existingWorkoutIds = sessionDao.listForDate(date.toEpochDay()).map { it.workoutId }.toSet()
        schedule.forEach { entry ->
            if (entry.workoutId !in existingWorkoutIds) {
                sessionDao.insert(
                    WorkoutSessionEntity(
                        dateEpochDay = date.toEpochDay(),
                        workoutId = entry.workoutId,
                        status = SessionStatus.PENDENTE.name,
                        originalDateEpochDay = null,
                        notes = ""
                    )
                )
            }
        }
    }

    private suspend fun materializeFromSchedule(date: LocalDate) {
        val dow = DayOfWeekBr.fromJavaDayOfWeek(date.dayOfWeek)
        scheduleDao.listForDay(dow.isoValue).forEach { entry ->
            sessionDao.insert(
                WorkoutSessionEntity(
                    dateEpochDay = date.toEpochDay(),
                    workoutId = entry.workoutId,
                    status = SessionStatus.PENDENTE.name,
                    originalDateEpochDay = null,
                    notes = ""
                )
            )
        }
    }

    private suspend fun List<WorkoutSessionEntity>.toDomainList(): List<WorkoutSession> {
        return mapNotNull { s ->
            val workoutEntity = workoutDao.getById(s.workoutId) ?: return@mapNotNull null
            val workout = Workout(
                id = workoutEntity.id,
                name = workoutEntity.name,
                description = workoutEntity.description
            )
            s.toDomain(workout, emptyList())
        }
    }

    override suspend fun updateStatus(sessionId: Long, status: SessionStatus) {
        sessionDao.updateStatus(sessionId, status.name)
    }

    override suspend fun reschedule(sessionId: Long, newDate: LocalDate) {
        val current = sessionDao.getById(sessionId) ?: return
        sessionDao.reschedule(
            id = sessionId,
            newEpochDay = newDate.toEpochDay(),
            originalEpochDay = current.dateEpochDay,
            status = SessionStatus.REMARCADO.name
        )
    }

    override suspend fun upsertExerciseLog(log: ExerciseLog): Long =
        sessionDao.upsertLog(log.toEntity())

    override suspend fun getSession(sessionId: Long): WorkoutSession? {
        val session = sessionDao.getById(sessionId) ?: return null
        val workoutEntity = workoutDao.getById(session.workoutId) ?: return null
        return session.toDomain(workoutEntity.toDomain(emptyList()), emptyList())
    }
}
