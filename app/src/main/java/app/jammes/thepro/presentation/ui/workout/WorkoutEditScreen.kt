package app.jammes.thepro.presentation.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jammes.thepro.domain.model.Exercise
import app.jammes.thepro.domain.model.WorkoutExercise
import app.jammes.thepro.presentation.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEditScreen(
    workoutId: Long?,
    onBack: () -> Unit,
    viewModel: WorkoutEditViewModel = hiltViewModel()
) {
    LaunchedEffect(workoutId) { viewModel.load(workoutId) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val available by viewModel.availableExercises.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { snackbar.showSnackbar(it) }
    }

    var pickerMode by remember { mutableStateOf<PickerMode?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (workoutId == null) "Novo treino" else "Editar treino",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::setName,
                label = { Text("Nome do treino") },
                placeholder = { Text("Ex: Treino A · Peito · Pernas") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::setDescription,
                label = { Text("Descrição (opcional)") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Exercícios",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "${state.items.size} no treino",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = { pickerMode = PickerMode.Add },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Adicionar")
                }
            }
            Spacer(Modifier.height(12.dp))

            if (state.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Toque em \"Adicionar\" para escolher exercícios.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(state.items, key = { _, it -> it.exercise.id.toString() + "-" + it.orderIndex }) { index, item ->
                        WorkoutExerciseEditor(
                            index = index,
                            total = state.items.size,
                            item = item,
                            onUpdate = { tx -> viewModel.updateItem(index, tx) },
                            onRemove = { viewModel.removeItem(index) },
                            onMoveUp = { viewModel.moveItem(index, index - 1) },
                            onMoveDown = { viewModel.moveItem(index, index + 1) },
                            onSubstitute = { pickerMode = PickerMode.Substitute(index) }
                        )
                    }
                }
            }
        }
    }

    val mode = pickerMode
    if (mode != null) {
        ExercisePickerSheet(
            mode = mode,
            available = available,
            selectedIds = state.items.map { it.exercise.id }.toSet(),
            onDismiss = { pickerMode = null },
            onAdd = viewModel::addExercise,
            onRemove = viewModel::removeExerciseById,
            onSubstitute = { idx, exercise ->
                viewModel.substituteAt(idx, exercise)
                pickerMode = null
            }
        )
    }
}

sealed interface PickerMode {
    data object Add : PickerMode
    data class Substitute(val itemIndex: Int) : PickerMode
}

@Composable
private fun WorkoutExerciseEditor(
    index: Int,
    total: Int,
    item: WorkoutExercise,
    onUpdate: ((WorkoutExercise) -> WorkoutExercise) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onSubstitute: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.exercise.name,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        item.exercise.type.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onSubstitute) {
                    Icon(Icons.Filled.SwapHoriz, "Substituir exercício",
                        tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onMoveUp, enabled = index > 0) {
                    Icon(Icons.Filled.ArrowUpward, "Subir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onMoveDown, enabled = index < total - 1) {
                    Icon(Icons.Filled.ArrowDownward, "Descer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, "Remover",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            var setsText by remember(item.exercise.id) { mutableStateOf(item.sets.toString()) }
            var repsText by remember(item.exercise.id) { mutableStateOf(item.reps?.toString().orEmpty()) }
            var weightText by remember(item.exercise.id) {
                mutableStateOf(item.suggestedWeightKg?.let { formatNumber(it) }.orEmpty())
            }
            var durationText by remember(item.exercise.id) {
                mutableStateOf(item.durationSeconds?.toString().orEmpty())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(
                    label = "Séries",
                    value = setsText,
                    onValue = { v ->
                        if (v.isEmpty() || v.all { ch -> ch.isDigit() }) {
                            setsText = v
                            v.toIntOrNull()?.let { n -> onUpdate { it.copy(sets = n) } }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    label = "Reps",
                    value = repsText,
                    onValue = { v ->
                        if (v.isEmpty() || v.all { ch -> ch.isDigit() }) {
                            repsText = v
                            onUpdate { it.copy(reps = v.toIntOrNull()) }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(
                    label = "Carga (kg)",
                    value = weightText,
                    onValue = { v ->
                        weightText = v
                        onUpdate { it.copy(suggestedWeightKg = v.replace(",", ".").toDoubleOrNull()) }
                    },
                    modifier = Modifier.weight(1f),
                    decimal = true
                )
                NumberField(
                    label = "Duração (s)",
                    value = durationText,
                    onValue = { v ->
                        if (v.isEmpty() || v.all { ch -> ch.isDigit() }) {
                            durationText = v
                            onUpdate { it.copy(durationSeconds = v.toIntOrNull()) }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumberField(
    label: String,
    value: String,
    onValue: (String) -> Unit,
    modifier: Modifier = Modifier,
    decimal: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePickerSheet(
    mode: PickerMode,
    available: List<Exercise>,
    selectedIds: Set<Long>,
    onDismiss: () -> Unit,
    onAdd: (Exercise) -> Unit,
    onRemove: (Long) -> Unit,
    onSubstitute: (Int, Exercise) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    val filtered = remember(available, query) {
        if (query.isBlank()) available
        else available.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.type.displayName.contains(query, ignoreCase = true)
        }
    }
    val isSubstitute = mode is PickerMode.Substitute

    fun closeWith(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) action()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 8.dp)
        ) {
            Text(
                if (isSubstitute) "Substituir exercício" else "Exercícios",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                if (isSubstitute) "Toque em um exercício para selecionar"
                else "${selectedIds.size} selecionado(s)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar exercício…") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpar")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nenhum exercício encontrado.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                ) {
                    itemsIndexed(filtered, key = { _, e -> e.id }) { idx, exercise ->
                        when (mode) {
                            is PickerMode.Add -> ExercisePickerRow(
                                exercise = exercise,
                                isSelected = exercise.id in selectedIds,
                                onAdd = { onAdd(exercise) },
                                onRemove = { onRemove(exercise.id) }
                            )
                            is PickerMode.Substitute -> ExerciseSubstituteRow(
                                exercise = exercise,
                                onClick = { closeWith { onSubstitute(mode.itemIndex, exercise) } }
                            )
                        }
                        if (idx < filtered.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { closeWith(onDismiss) }) {
                    Text(if (isSubstitute) "Cancelar" else "Fechar")
                }
            }
            // safe area inferior (system nav bar)
            Spacer(
                modifier = Modifier.windowInsetsBottomHeight(
                    WindowInsets.navigationBars
                )
            )
        }
    }
}

@Composable
private fun ExerciseSubstituteRow(exercise: Exercise, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(exercise.name, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface)
            Text(
                exercise.type.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Filled.SwapHoriz,
            contentDescription = "Selecionar",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ExercisePickerRow(
    exercise: Exercise,
    isSelected: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text(exercise.name, fontWeight = FontWeight.Medium)
            Text(
                exercise.type.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        val bg = if (isSelected) SuccessGreen
        else MaterialTheme.colorScheme.background
        val fg = if (isSelected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .background(bg)
                .clickable { if (isSelected) onRemove() else onAdd() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.Check else Icons.Filled.Add,
                contentDescription = if (isSelected) "Remover" else "Adicionar",
                tint = fg
            )
        }
    }
}

private fun formatNumber(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()

