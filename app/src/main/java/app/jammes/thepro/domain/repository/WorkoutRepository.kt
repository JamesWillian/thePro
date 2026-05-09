package app.jammes.thepro.domain.repository

import app.jammes.thepro.domain.model.Workout
import app.jammes.thepro.domain.model.WorkoutExercise
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeAll(): Flow<List<Workout>>
    fun observeById(id: Long): Flow<Workout?>
    suspend fun getById(id: Long): Workout?
    suspend fun upsert(workout: Workout): Long
    suspend fun replaceExercises(workoutId: Long, items: List<WorkoutExercise>)
    suspend fun delete(workout: Workout)
    suspend fun importMany(rows: List<WorkoutImportRow>): Int
    suspend fun count(): Int
}

data class WorkoutImportRow(
    val workoutName: String,
    val workoutDescription: String,
    val exerciseName: String,
    val sets: Int,
    val reps: Int?,
    val weightKg: Double?,
    val durationSeconds: Int?,
    val orderIndex: Int
)
