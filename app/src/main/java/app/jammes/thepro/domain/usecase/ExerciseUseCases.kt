package app.jammes.thepro.domain.usecase

import app.jammes.thepro.domain.model.Exercise
import app.jammes.thepro.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveExercisesUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    operator fun invoke(): Flow<List<Exercise>> = repository.observeAll()
}

class SaveExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke(exercise: Exercise): Long {
        require(exercise.name.isNotBlank()) { "Nome do exercício é obrigatório" }
        return repository.upsert(exercise)
    }
}

class DeleteExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke(exercise: Exercise) = repository.delete(exercise)
}

class ImportExercisesUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke(exercises: List<Exercise>): Int =
        repository.importMany(exercises)
}
