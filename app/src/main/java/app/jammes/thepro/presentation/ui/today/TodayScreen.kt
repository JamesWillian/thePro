package app.jammes.thepro.presentation.ui.today

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jammes.thepro.domain.model.DayOfWeekBr
import app.jammes.thepro.domain.model.ExerciseLog
import app.jammes.thepro.domain.model.SessionStatus
import app.jammes.thepro.domain.model.WorkoutExercise
import app.jammes.thepro.domain.model.WorkoutSession
import app.jammes.thepro.presentation.ui.theme.SuccessGreen
import app.jammes.thepro.presentation.ui.theme.WarningAmber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PtBr = Locale("pt", "BR")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onAddWorkout: () -> Unit,
    onCreateWorkout: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "thePRO",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            HeaderHero(date = state.selectedDate, isToday = state.selectedDate == LocalDate.now())
            Spacer(Modifier.height(12.dp))
            DaySelectorRow(
                selectedDate = state.selectedDate,
                today = LocalDate.now(),
                onSelect = viewModel::selectDate
            )
            Spacer(Modifier.height(20.dp))

            if (state.sessions.isEmpty() && !state.isLoading) {
                EmptyDayState(onAddWorkout = onAddWorkout, onCreateWorkout = onCreateWorkout)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
private fun HeaderHero(date: LocalDate, isToday: Boolean) {
    val dayName = remember(date) {
        date.format(DateTimeFormatter.ofPattern("EEEE", PtBr))
            .replaceFirstChar { it.titlecase(PtBr) }
    }
    val dateText = remember(date) {
        date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", PtBr))
    }
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(
            text = if (isToday) "Hoje" else dayName,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (isToday) dayName.lowercase(PtBr) + " · " + dateText else dateText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DaySelectorRow(
    selectedDate: LocalDate,
    today: LocalDate,
    onSelect: (LocalDate) -> Unit
) {
    val dates = remember(today) { (-3..3).map { today.plusDays(it.toLong()) } }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        dates.forEach { date ->
            val dow = DayOfWeekBr.fromJavaDayOfWeek(date.dayOfWeek)
            DayPill(
                initial = dow.shortLabel.first().uppercase(),
                dayNumber = date.dayOfMonth,
                isSelected = date == selectedDate,
                isToday = date == today,
                onClick = { onSelect(date) }
            )
        }
    }
}

@Composable
private fun DayPill(
    initial: String,
    dayNumber: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val container by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.surface
            else -> MaterialTheme.colorScheme.background
        },
        label = "day-bg"
    )
    val border by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        label = "day-border"
    )
    val content = if (isSelected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurface
    val width by animateDpAsState(targetValue = if (isSelected) 56.dp else 44.dp, label = "day-w")

    Column(
        modifier = Modifier
            .width(width)
            .clip(RoundedCornerShape(20.dp))
            .background(container)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            initial,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            dayNumber.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = content,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyDayState(onAddWorkout: () -> Unit, onCreateWorkout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.EventAvailable,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Dia de descanso",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Nenhum treino programado. Atribua um existente ou crie um novo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddWorkout,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Atribuir treino para este dia")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onCreateWorkout,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Criar novo treino")
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        session.workout.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (session.workout.description.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            session.workout.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    SessionMeta(session)
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Ações",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (session.status == SessionStatus.CONCLUIDO) {
                            DropdownMenuItem(
                                text = { Text("Reabrir treino") },
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
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            session.workout.exercises.forEachIndexed { idx, we ->
                if (idx > 0) Spacer(Modifier.height(2.dp))
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
private fun SessionMeta(session: WorkoutSession) {
    val performed = session.exerciseLogs.count { it.performed }
    val total = session.workout.exercises.size
    Row(verticalAlignment = Alignment.CenterVertically) {
        StatusChip(status = session.status)
        Spacer(Modifier.width(8.dp))
        Text(
            "$performed/$total exercícios",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusChip(status: SessionStatus) {
    val (label, color) = when (status) {
        SessionStatus.CONCLUIDO -> "Concluído" to SuccessGreen
        SessionStatus.REMARCADO -> "Remarcado" to MaterialTheme.colorScheme.secondary
        SessionStatus.EM_ANDAMENTO -> "Em andamento" to WarningAmber
        SessionStatus.PENDENTE -> "Pendente" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = color.copy(alpha = 0.18f),
        contentColor = color,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            label.uppercase(PtBr),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
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
            (log?.actualWeightKg ?: workoutExercise.suggestedWeightKg)?.let { formatNumber(it) }.orEmpty()
        )
    }

    fun emit(transform: (ExerciseLog) -> ExerciseLog) {
        val base = log ?: ExerciseLog(
            sessionId = sessionId,
            workoutExerciseId = workoutExercise.id,
            exercise = workoutExercise.exercise,
            performed = false,
            actualSets = workoutExercise.sets,
            actualReps = workoutExercise.reps,
            actualWeightKg = workoutExercise.suggestedWeightKg,
            actualDurationSeconds = workoutExercise.durationSeconds
        )
        onChange(transform(base))
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = performed,
            onCheckedChange = { c -> emit { it.copy(performed = c) } },
            colors = CheckboxDefaults.colors(
                checkedColor = SuccessGreen,
                uncheckedColor = MaterialTheme.colorScheme.outline,
                checkmarkColor = MaterialTheme.colorScheme.background
            )
        )
        Spacer(Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                workoutExercise.exercise.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                buildSpec(workoutExercise),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedTextField(
            value = weight,
            onValueChange = { v ->
                weight = v
                val parsed = v.replace(",", ".").toDoubleOrNull()
                emit { it.copy(actualWeightKg = parsed) }
            },
            label = { Text("kg", style = MaterialTheme.typography.labelSmall) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(64.dp)
        )
    }
}

private fun buildSpec(we: WorkoutExercise): String {
    val parts = buildList {
        add("${we.sets}x" + (we.reps?.toString() ?: "?"))
        we.suggestedWeightKg?.let { add("${formatNumber(it)} kg") }
        we.durationSeconds?.let { add(formatDuration(it)) }
    }
    return parts.joinToString(" · ")
}

private fun formatNumber(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else "%.1f".format(Locale.US, v)

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return when {
        m > 0 && s > 0 -> "${m}m${s}s"
        m > 0 -> "${m}min"
        else -> "${s}s"
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun RescheduleDialog(
    current: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Mover treino", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column {
                Text(
                    "Selecione o novo dia da semana:",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                val today = LocalDate.now()
                val currentDow = current.dayOfWeek.value
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DayOfWeekBr.ordered.forEach { d ->
                        AssistChip(
                            onClick = {
                                val diff = d.isoValue - currentDow
                                onConfirm(current.plusDays(diff.toLong()).let {
                                    if (it.isBefore(today.minusDays(1))) it.plusWeeks(1) else it
                                })
                            },
                            label = { Text(d.longLabel) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

