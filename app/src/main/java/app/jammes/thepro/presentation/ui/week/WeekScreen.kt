package app.jammes.thepro.presentation.ui.week

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jammes.thepro.domain.model.DayOfWeekBr
import app.jammes.thepro.domain.model.Workout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekScreen(viewModel: WeekViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var assigningTo by remember { mutableStateOf<DayOfWeekBr?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Programação Semanal") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(DayOfWeekBr.ordered, key = { it.isoValue }) { day ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                day.longLabel,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { assigningTo = day }) {
                                Icon(Icons.Filled.Add, contentDescription = "Adicionar treino")
                            }
                        }
                        val entries = state.schedule[day].orEmpty()
                        if (entries.isEmpty()) {
                            Text(
                                "Sem treino atribuído",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            entries.forEach { entry ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(entry.workout.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${entry.workout.exercises.size} exercício(s)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { viewModel.unassign(entry.id) }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Remover")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val target = assigningTo
    if (target != null) {
        AssignWorkoutDialog(
            day = target,
            workouts = state.workouts,
            onDismiss = { assigningTo = null },
            onPick = { workoutId ->
                viewModel.assign(target, workoutId)
                assigningTo = null
            }
        )
    }
}

@Composable
private fun AssignWorkoutDialog(
    day: DayOfWeekBr,
    workouts: List<Workout>,
    onDismiss: () -> Unit,
    onPick: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atribuir treino — ${day.longLabel}") },
        text = {
            if (workouts.isEmpty()) {
                Text("Cadastre um treino na aba Treinos antes de atribuir.")
            } else {
                Column {
                    workouts.forEach { w ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            TextButton(
                                onClick = { onPick(w.id) },
                                modifier = Modifier.fillMaxWidth().padding(8.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(w.name, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "${w.exercises.size} exercício(s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}
