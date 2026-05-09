package app.jammes.thepro.domain.repository

import app.jammes.thepro.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun observeAll(): Flow<List<Exercise>>
    suspend fun getById(id: Long): Exercise?
    suspend fun upsert(exercise: Exercise): Long
    suspend fun delete(exercise: Exercise)
    suspend fun importMany(exercises: List<Exercise>): Int
    suspend fun count(): Int
}
