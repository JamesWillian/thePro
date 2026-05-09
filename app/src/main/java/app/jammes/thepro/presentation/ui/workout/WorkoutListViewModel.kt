package app.jammes.thepro.presentation.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.jammes.thepro.data.csv.CsvParser
import app.jammes.thepro.domain.model.Workout
import app.jammes.thepro.domain.usecase.DeleteWorkoutUseCase
import app.jammes.thepro.domain.usecase.ImportWorkoutsUseCase
import app.jammes.thepro.domain.usecase.ObserveWorkoutsUseCase
import app.jammes.thepro.presentation.ui.common.UiMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

data class WorkoutListUiState(
    val items: List<Workout> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class WorkoutListViewModel @Inject constructor(
    observeWorkouts: ObserveWorkoutsUseCase,
    private val deleteWorkout: DeleteWorkoutUseCase,
    private val importWorkouts: ImportWorkoutsUseCase,
    private val csvParser: CsvParser
) : ViewModel() {

    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 4)
    val messages = _messages.asSharedFlow()

    val uiState: StateFlow<WorkoutListUiState> =
        observeWorkouts()
            .map { WorkoutListUiState(items = it, isLoading = false) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkoutListUiState())

    fun delete(workout: Workout) {
        viewModelScope.launch {
            runCatching { deleteWorkout(workout) }
                .onFailure { _messages.tryEmit(UiMessage.Error("Não foi possível excluir o treino")) }
        }
    }

    fun importCsv(stream: InputStream) {
        viewModelScope.launch {
            runCatching { stream.use { csvParser.parseWorkouts(it) } }
                .onSuccess { rows ->
                    val count = importWorkouts(rows)
                    _messages.tryEmit(UiMessage.Info("$count treino(s) importado(s)"))
                }
                .onFailure { _messages.tryEmit(UiMessage.Error(it.message ?: "Erro ao importar")) }
        }
    }
}
