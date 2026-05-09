package app.jammes.thepro.presentation.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.jammes.thepro.data.csv.CsvParser
import app.jammes.thepro.domain.model.Exercise
import app.jammes.thepro.domain.usecase.DeleteExerciseUseCase
import app.jammes.thepro.domain.usecase.ImportExercisesUseCase
import app.jammes.thepro.domain.usecase.ObserveExercisesUseCase
import app.jammes.thepro.domain.usecase.SaveExerciseUseCase
import app.jammes.thepro.presentation.ui.common.UiMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

data class ExerciseUiState(
    val items: List<Exercise> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    observeExercises: ObserveExercisesUseCase,
    private val saveExercise: SaveExerciseUseCase,
    private val deleteExercise: DeleteExerciseUseCase,
    private val importExercises: ImportExercisesUseCase,
    private val csvParser: CsvParser
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 4)
    val messages = _messages.asSharedFlow()

    val uiState: StateFlow<ExerciseUiState> =
        combine(observeExercises(), query) { items, q ->
            val filtered = if (q.isBlank()) items
            else items.filter {
                it.name.contains(q, ignoreCase = true) ||
                    it.type.displayName.contains(q, ignoreCase = true)
            }
            ExerciseUiState(items = filtered, query = q, isLoading = false)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExerciseUiState()
        )

    fun setQuery(value: String) { query.value = value }

    fun save(exercise: Exercise) {
        viewModelScope.launch {
            runCatching { saveExercise(exercise) }
                .onFailure { _messages.tryEmit(UiMessage.Error(it.message ?: "Erro ao salvar")) }
                .onSuccess { _messages.tryEmit(UiMessage.Info("Exercício salvo")) }
        }
    }

    fun delete(exercise: Exercise) {
        viewModelScope.launch {
            runCatching { deleteExercise(exercise) }
                .onFailure { _messages.tryEmit(UiMessage.Error("Não foi possível excluir (em uso por treino)")) }
        }
    }

    fun importCsv(stream: InputStream) {
        viewModelScope.launch {
            runCatching {
                stream.use { csvParser.parseExercises(it) }
            }.onSuccess { parsed ->
                val inserted = importExercises(parsed)
                _messages.tryEmit(UiMessage.Info("$inserted exercício(s) importado(s)"))
            }.onFailure {
                _messages.tryEmit(UiMessage.Error(it.message ?: "Erro ao importar CSV"))
            }
        }
    }
}
