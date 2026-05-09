package app.jammes.thepro.presentation.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jammes.thepro.domain.model.DayOfWeekBr
import app.jammes.thepro.domain.model.ExerciseLog
import app.jammes.thepro.domain.model.SessionStatus
import app.jammes.thepro.domain.model.WorkoutExercise
import app.jammes.thepro.domain.model.WorkoutSession
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onAddWorkout: () -> Unit,
    onCreateWorkout: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR")) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Treino do Dia") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            DaySelector(
                selectedDay = state.selectedDay,
                onSelect = viewModel::selectDay
            )
            Text(
                text = state.selectedDate.format(dateFormatter)
                    .replaceFirstChar { it.titlecase(Locale("pt", "BR")) },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))

            if (state.sessions.isEmpty() && !state.isLoading) {
                EmptyDayState(
                    onAddWorkout = onAddWorkout,
                    onCreateWorkout = onCreateWorkout
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.sessions, key = { it.id }) { session ->
                        SessionCard(
                            session = session,
                            onMarkCompleted = { viewModel.markCompleted(session.id) },
                            onMarkPending = { viewModel.markPending(session.id) },
                            onReschedule = { newDate -> viewModel.reschedule(session.id, newDate) },
                            onLog = { viewModel.logExerciseEntry(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DaySelector(
    selectedDay: DayOfWeekBr,
    onSelect: (DayOfWeekBr) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DayOfWeekBr.ordered.forEach { day ->
                DayChip(
                    label = day.shortLabel,
                    selected = day == selectedDay,
                    onClick = { onSelect(day) }
                )
            }
        }
    }
}

@Composable
private fun DayChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(bg, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = fg, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyDayState(
    onAddWorkout: () -> Unit,
    onCreateWorkout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Nenhum treino para este dia",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Você pode atribuir um treino existente ou criar um novo.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = onAddWorkout) {
            Text("Atribuir treino para este dia")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onCreateWorkout) {
            Text("Adicionar novo treino")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionCard(
    session: WorkoutSession,
    onMarkCompleted: () -> Unit,
    onMarkPending: () -> Unit,
    onReschedule: (LocalDate) -> Unit,
    onLog: (ExerciseLog) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    var rescheduleOpen by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(session.workout.name, style = MaterialTheme.typography.titleMedium)
                    if (session.workout.description.isNotBlank()) {
                        Text(
                            session.workout.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusChip(status = session.status)
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Ações")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (session.status == SessionStatus.CONCLUIDO) {
                            DropdownMenuItem(
                                text = { Text("Reabrir") },
                                onClick = { menuOpen = false; onMarkPending() }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Marcar como concluído") },
                                leadingIcon = { Icon(Icons.Filled.Check, null) },
                                onClick = { menuOpen = false; onMarkCompleted() }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Mover para outro dia") },
                            leadingIcon = { Icon(Icons.Filled.SwapHoriz, null) },
                            onClick = { menuOpen = false; rescheduleOpen = true }
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            session.workout.exercises.forEach { we ->
                val log = session.exerciseLogs.firstOrNull { it.workoutExerciseId == we.id }
                ExerciseLogRow(
                    workoutExercise = we,
                    log = log,
                    sessionId = session.id,
                    onChange = onLog
                )
            }
        }
    }

    if (rescheduleOpen) {
        RescheduleDialog(
            current = session.date,
            onDismiss = { rescheduleOpen = false },
            onConfirm = { newDate ->
                rescheduleOpen = false
                onReschedule(newDate)
            }
        )
    }
}

@Composable
private fun StatusChip(status: SessionStatus) {
    val (label, container, content) = when (status) {
        SessionStatus.CONCLUIDO -> Triple(
            "Concluído",
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.onTertiary
        )
        SessionStatus.REMARCADO -> Triple(
            "Remarcado",
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.onSecondary
        )
        SessionStatus.EM_ANDAMENTO -> Triple(
            "Em andamento",
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary
        )
        SessionStatus.PENDENTE -> Triple(
            "Pendente",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Surface(
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ExerciseLogRow(
    workoutExercise: WorkoutExercise,
    log: ExerciseLog?,
    sessionId: Long,
    onChange: (ExerciseLog) -> Unit
) {
    val performed = log?.performed ?: false
    var weight by remember(log?.id, workoutExercise.id) {
        mutableStateOf(
            (log?.actualWeightKg ?: workoutExercise.suggestedWeightKg)?.toString().orEmpty()
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = performed,
            onCheckedChange = { checked ->
                onChange(
                    (log ?: ExerciseLog(
                        sessionId = sessionId,
                        workoutExerciseId = workoutExercise.id,
                        exercise = workoutExercise.exercise,
                        performed = false,
                        actualSets = workoutExercise.sets,
                        actualReps = workoutExercise.reps,
                        actualWeightKg = workoutExercise.suggestedWeightKg,
                        actualDurationSeconds = workoutExercise.durationSeconds
                    )).copy(performed = checked)
                )
            }
        )
        Spacer(Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(workoutExercise.exercise.name, fontWeight = FontWeight.Medium)
            Text(
                buildSpec(workoutExercise),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedTextField(
            value = weight,
            onValueChange = { value ->
                weight = value
                val parsed = value.replace(",", ".").toDoubleOrNull()
                onChange(
                    (log ?: ExerciseLog(
                        sessionId = sessionId,
                        workoutExerciseId = workoutExercise.id,
                        exercise = workoutExercise.exercise,
                        performed = performed,
                        actualSets = workoutExercise.sets,
                        actualReps = workoutExercise.reps,
                        actualWeightKg = workoutExercise.suggestedWeightKg,
                        actualDurationSeconds = workoutExercise.durationSeconds
                    )).copy(actualWeightKg = parsed)
                )
            },
            label = { Text("kg") },
            singleLine = true,
            modifier = Modifier.width(100.dp)
        )
    }
}

private fun buildSpec(we: WorkoutExercise): String {
    val parts = buildList {
        add("${we.sets}x" + (we.reps?.toString() ?: "?"))
        we.suggestedWeightKg?.let { add("${formatNumber(it)} kg") }
        we.durationSeconds?.let { add(formatDuration(it)) }
    }
    return parts.joinToString(" • ")
}

private fun formatNumber(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else "%.1f".format(Locale.US, v)

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0 && s > 0) "${m}m${s}s"
    else if (m > 0) "${m}min"
    else "${s}s"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RescheduleDialog(
    current: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mover treino para...") },
        text = {
            Column {
                Text("Selecione o novo dia da semana:")
                Spacer(Modifier.height(12.dp))
                val today = LocalDate.now()
                val currentDow = current.dayOfWeek.value
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DayOfWeekBr.ordered.forEach { d ->
                        AssistChip(
                            onClick = {
                                val diff = d.isoValue - currentDow
                                onConfirm(current.plusDays(diff.toLong()).let {
                                    if (it.isBefore(today.minusDays(1))) it.plusWeeks(1) else it
                                })
                            },
                            label = { Text(d.longLabel) },
                            colors = AssistChipDefaults.assistChipColors()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
