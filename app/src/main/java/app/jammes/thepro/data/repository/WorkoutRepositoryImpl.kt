package app.jammes.thepro.data.repository

import app.jammes.thepro.data.local.AppDatabase
import app.jammes.thepro.data.local.dao.ExerciseDao
import app.jammes.thepro.data.local.dao.SessionDao
import app.jammes.thepro.data.local.dao.WorkoutDao
import app.jammes.thepro.data.local.entity.ExerciseEntity
import app.jammes.thepro.data.local.entity.WorkoutEntity
import app.jammes.thepro.data.local.entity.WorkoutExerciseEntity
import app.jammes.thepro.data.mapper.toDomain
import app.jammes.thepro.data.mapper.toEntity
import app.jammes.thepro.domain.model.ExerciseType
import app.jammes.thepro.domain.model.Workout
import app.jammes.thepro.domain.model.WorkoutExercise
import app.jammes.thepro.domain.repository.WorkoutImportRow
import app.jammes.thepro.domain.repository.WorkoutRepository
import androidx.room.withTransaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val sessionDao: SessionDao
) : WorkoutRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAll(): Flow<List<Workout>> =
        workoutDao.observeAll().flatMapLatest { workouts ->
            if (workouts.isEmpty()) flowOf(emptyList())
            else combine(workouts.map { w -> workoutDao.observeWorkoutExercises(w.id) }) { rows ->
                workouts.mapIndexed { index, w ->
                    w.toDomain(rows[index].map { it.toDomain() })
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeById(id: Long): Flow<Workout?> =
        workoutDao.observeById(id).flatMapLatest { entity ->
            if (entity == null) flowOf(null)
            else workoutDao.observeWorkoutExercises(id)
                .map { rows -> entity.toDomain(rows.map { it.toDomain() }) }
        }

    override suspend fun getById(id: Long): Workout? {
        val entity = workoutDao.getById(id) ?: return null
        return entity.toDomain()
    }

    override suspend fun upsert(workout: Workout): Long {
        val entity = workout.toEntity()
        return if (entity.id == 0L) {
            workoutDao.insert(entity)
        } else {
            workoutDao.update(entity)
            entity.id
        }
    }

    override suspend fun replaceExercises(workoutId: Long, items: List<WorkoutExercise>) {
        database.withTransaction {
            val current = workoutDao.getActiveWorkoutExerciseEntities(workoutId)
            val newIds = items.mapNotNull { it.id.takeIf { id -> id > 0L } }.toSet()

            // Linhas removidas do template: se já tiveram log com performed=true,
            // marcar como inativas (preserva histórico). Caso contrário, deletar.
            current.forEach { row ->
                if (row.id !in newIds) {
                    val performedCount = sessionDao.countPerformedLogs(row.id)
                    if (performedCount > 0) {
                        workoutDao.markWorkoutExerciseInactive(row.id)
                    } else {
                        workoutDao.deleteWorkoutExerciseById(row.id)
                    }
                }
            }

            // Inserir novos / atualizar existentes preservando IDs (logs sobrevivem).
            items.forEachIndexed { idx, item ->
                val entity = item.toEntity(workoutId).copy(orderIndex = idx, activeInWorkout = true)
                if (entity.id == 0L) {
                    workoutDao.insertWorkoutExercise(entity)
                } else {
                    workoutDao.updateWorkoutExercise(entity)
                }
            }
        }
    }

    override suspend fun delete(workout: Workout) = workoutDao.delete(workout.toEntity())

    override suspend fun importMany(rows: List<WorkoutImportRow>): Int {
        if (rows.isEmpty()) return 0
        var importedWorkouts = 0
        database.withTransaction {
            val grouped = rows.groupBy { it.workoutName.trim() }
            grouped.forEach { (workoutName, items) ->
                if (workoutName.isBlank()) return@forEach
                val description = items.firstOrNull { it.workoutDescription.isNotBlank() }
                    ?.workoutDescription.orEmpty()
                val existing = workoutDao.findByName(workoutName)
                val workoutId = existing?.id
                    ?: workoutDao.insert(WorkoutEntity(name = workoutName, description = description))
                if (existing == null) importedWorkouts++

                val sorted = items.sortedBy { it.orderIndex }
                sorted.forEachIndexed { idx, row ->
                    val exerciseName = row.exerciseName.trim()
                    if (exerciseName.isBlank()) return@forEachIndexed
                    val exerciseId = exerciseDao.findByName(exerciseName)?.id
                        ?: exerciseDao.insert(
                            ExerciseEntity(
                                name = exerciseName,
                                description = "",
                                type = ExerciseType.OUTRO.name
                            )
                        ).takeIf { it != -1L }
                        ?: exerciseDao.findByName(exerciseName)!!.id
                    workoutDao.insertWorkoutExercise(
                        WorkoutExerciseEntity(
                            workoutId = workoutId,
                            exerciseId = exerciseId,
                            sets = row.sets,
                            reps = row.reps,
                            weightKg = row.weightKg,
                            durationSeconds = row.durationSeconds,
                            orderIndex = idx
                        )
                    )
                }
            }
        }
        return importedWorkouts
    }

    override suspend fun count(): Int = workoutDao.count()
}
