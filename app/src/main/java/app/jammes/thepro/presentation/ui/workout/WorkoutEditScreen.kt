package app.jammes.thepro.presentation.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jammes.thepro.domain.model.Exercise
import app.jammes.thepro.domain.model.WorkoutExercise

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
    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    var pickerOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (workoutId == null) "Novo Treino" else "Editar Treino") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::save,
                icon = { Icon(Icons.Filled.Save, contentDescription = null) },
                text = { Text("Salvar") }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::setName,
                label = { Text("Nome (ex: Treino A, Costas, Peito...)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::setDescription,
                label = { Text("Descrição (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Exercícios",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(onClick = { pickerOpen = true }) {
                    Text("Adicionar exercício")
                }
            }
            Spacer(Modifier.height(8.dp))

            if (state.items.isEmpty()) {
                Text(
                    "Nenhum exercício adicionado.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(state.items, key = { _, it -> it.exercise.id.toString() + "-" + it.orderIndex }) { index, item ->
                        WorkoutExerciseEditor(
                            index = index,
                            total = state.items.size,
                            item = item,
                            onUpdate = { tx -> viewModel.updateItem(index, tx) },
                            onRemove = { viewModel.removeItem(index) },
                            onMoveUp = { viewModel.moveItem(index, index - 1) },
                            onMoveDown = { viewModel.moveItem(index, index + 1) }
                        )
                    }
                }
            }
        }
    }

    if (pickerOpen) {
        ExercisePickerDialog(
            available = available,
            selectedIds = state.items.map { it.exercise.id }.toSet(),
            onDismiss = { pickerOpen = false },
            onAdd = viewModel::addExercise,
            onRemove = viewModel::removeExerciseById
        )
    }
}

@Composable
private fun WorkoutExerciseEditor(
    index: Int,
    total: Int,
    item: WorkoutExercise,
    onUpdate: ((WorkoutExercise) -> WorkoutExercise) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.exercise.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        item.exercise.type.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onMoveUp, enabled = index > 0) {
                    Icon(Icons.Filled.ArrowUpward, "Subir")
                }
                IconButton(onClick = onMoveDown, enabled = index < total - 1) {
                    Icon(Icons.Filled.ArrowDownward, "Descer")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, "Remover")
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(
                    label = "Séries",
                    value = item.sets.toString(),
                    onValue = { v -> onUpdate { it.copy(sets = v.toIntOrNull() ?: it.sets) } },
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    label = "Reps",
                    value = item.reps?.toString().orEmpty(),
                    onValue = { v -> onUpdate { it.copy(reps = v.toIntOrNull()) } },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(
                    label = "Carga (kg)",
                    value = item.suggestedWeightKg?.let { formatNumber(it) }.orEmpty(),
                    onValue = { v ->
                        onUpdate { it.copy(suggestedWeightKg = v.replace(",", ".").toDoubleOrNull()) }
                    },
                    modifier = Modifier.weight(1f),
                    decimal = true
                )
                NumberField(
                    label = "Duração (s)",
                    value = item.durationSeconds?.toString().orEmpty(),
                    onValue = { v -> onUpdate { it.copy(durationSeconds = v.toIntOrNull()) } },
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
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        modifier = modifier
    )
}

@Composable
private fun ExercisePickerDialog(
    available: List<Exercise>,
    selectedIds: Set<Long>,
    onDismiss: () -> Unit,
    onAdd: (Exercise) -> Unit,
    onRemove: (Long) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(available, query) {
        if (query.isBlank()) available
        else available.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.type.displayName.contains(query, ignoreCase = true)
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar exercício") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                if (filtered.isEmpty()) {
                    Text(
                        "Nenhum exercício encontrado. Cadastre na aba Exercícios.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                        itemsIndexed(filtered, key = { _, e -> e.id }) { _, exercise ->
                            ExercisePickerRow(
                                exercise = exercise,
                                isSelected = exercise.id in selectedIds,
                                onAdd = { onAdd(exercise) },
                                onRemove = { onRemove(exercise.id) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
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
        val bg = if (isSelected) MaterialTheme.colorScheme.tertiary
        else MaterialTheme.colorScheme.surfaceVariant
        val fg = if (isSelected) MaterialTheme.colorScheme.onTertiary
        else MaterialTheme.colorScheme.onSurfaceVariant
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

