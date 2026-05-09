package app.jammes.thepro.domain.usecase

import app.jammes.thepro.domain.model.Workout
import app.jammes.thepro.domain.model.WorkoutExercise
import app.jammes.thepro.domain.repository.WorkoutImportRow
import app.jammes.thepro.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWorkoutsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<Workout>> = repository.observeAll()
}

class ObserveWorkoutByIdUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(id: Long): Flow<Workout?> = repository.observeById(id)
}

class SaveWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(workout: Workout, exercises: List<WorkoutExercise>): Long {
        require(workout.name.isNotBlank()) { "Nome do treino é obrigatório" }
        val id = repository.upsert(workout)
        repository.replaceExercises(id, exercises.mapIndexed { idx, e -> e.copy(orderIndex = idx) })
        return id
    }
}

class DeleteWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(workout: Workout) = repository.delete(workout)
}

class ImportWorkoutsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(rows: List<WorkoutImportRow>): Int =
        repository.importMany(rows)
}
