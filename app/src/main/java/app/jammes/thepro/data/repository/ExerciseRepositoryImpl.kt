package app.jammes.thepro.data.repository

import app.jammes.thepro.data.local.dao.ExerciseDao
import app.jammes.thepro.data.mapper.toDomain
import app.jammes.thepro.data.mapper.toEntity
import app.jammes.thepro.domain.model.Exercise
import app.jammes.thepro.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val dao: ExerciseDao
) : ExerciseRepository {

    override fun observeAll(): Flow<List<Exercise>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Exercise? = dao.getById(id)?.toDomain()

    override suspend fun upsert(exercise: Exercise): Long {
        val entity = exercise.toEntity()
        return if (entity.id == 0L) {
            val inserted = dao.insert(entity)
            if (inserted == -1L) {
                dao.findByName(entity.name)?.id ?: 0L
            } else {
                inserted
            }
        } else {
            dao.update(entity)
            entity.id
        }
    }

    override suspend fun delete(exercise: Exercise) = dao.delete(exercise.toEntity())

    override suspend fun importMany(exercises: List<Exercise>): Int {
        if (exercises.isEmpty()) return 0
        val entities = exercises.map { it.copy(id = 0L).toEntity() }
        val ids = dao.insertAllIgnore(entities)
        return ids.count { it != -1L }
    }

    override suspend fun count(): Int = dao.count()
}
