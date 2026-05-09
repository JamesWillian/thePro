package app.jammes.thepro.presentation.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.jammes.thepro.domain.model.Exercise
import app.jammes.thepro.domain.model.Workout
import app.jammes.thepro.domain.model.WorkoutExercise
import app.jammes.thepro.domain.usecase.ObserveExercisesUseCase
import app.jammes.thepro.domain.usecase.ObserveWorkoutByIdUseCase
import app.jammes.thepro.domain.usecase.SaveWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutEditUiState(
    val id: Long? = null,
    val name: String = "",
    val description: String = "",
    val items: List<WorkoutExercise> = emptyList(),
    val saved: Boolean = false
)

@HiltViewModel
class WorkoutEditViewModel @Inject constructor(
    observeExercises: ObserveExercisesUseCase,
    private val observeWorkout: ObserveWorkoutByIdUseCase,
    private val saveWorkout: SaveWorkoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutEditUiState())
    val state: StateFlow<WorkoutEditUiState> = _state

    val availableExercises: StateFlow<List<Exercise>> =
        observeExercises().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events = _events.asSharedFlow()

    fun load(id: Long?) {
        if (id == null || id <= 0) return
        viewModelScope.launch {
            observeWorkout(id).collect { w ->
                if (w != null) {
                    _state.update {
                        it.copy(
                            id = w.id,
                            name = w.name,
                            description = w.description,
                            items = w.exercises
                        )
                    }
                }
            }
        }
    }

    fun setName(value: String) = _state.update { it.copy(name = value) }
    fun setDescription(value: String) = _state.update { it.copy(description = value) }

    fun addExercise(exercise: Exercise) {
        _state.update { current ->
            current.copy(
                items = current.items + WorkoutExercise(
                    workoutId = current.id ?: 0L,
                    exercise = exercise,
                    sets = 3,
                    reps = 10,
                    suggestedWeightKg = null,
                    durationSeconds = null,
                    orderIndex = current.items.size
                )
            )
        }
    }

    fun updateItem(index: Int, transform: (WorkoutExercise) -> WorkoutExercise) {
        _state.update { current ->
            current.copy(items = current.items.toMutableList().also {
                if (index in it.indices) it[index] = transform(it[index])
            })
        }
    }

    fun removeItem(index: Int) {
        _state.update { current ->
            current.copy(items = current.items.filterIndexed { i, _ -> i != index })
        }
    }

    fun removeExerciseById(exerciseId: Long) {
        _state.update { current ->
            val firstMatch = current.items.indexOfFirst { it.exercise.id == exerciseId }
            if (firstMatch < 0) current
            else current.copy(items = current.items.filterIndexed { i, _ -> i != firstMatch })
        }
    }

    fun moveItem(from: Int, to: Int) {
        _state.update { current ->
            if (from !in current.items.indices || to !in current.items.indices) return@update current
            val list = current.items.toMutableList()
            val item = list.removeAt(from)
            list.add(to, item)
            current.copy(items = list)
        }
    }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _events.tryEmit("Informe o nome do treino")
            return
        }
        if (s.items.isEmpty()) {
            _events.tryEmit("Adicione pelo menos um exercício")
            return
        }
        viewModelScope.launch {
            runCatching {
                val id = saveWorkout(
                    Workout(id = s.id ?: 0L, name = s.name, description = s.description),
                    s.items
                )
                _state.update { it.copy(id = id, saved = true) }
                _events.tryEmit("Treino salvo")
            }.onFailure { _events.tryEmit(it.message ?: "Erro ao salvar") }
        }
    }
}
