package app.jammes.thepro.presentation.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jammes.thepro.domain.model.SessionStatus
import app.jammes.thepro.domain.model.WorkoutSession
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val weekFmt = remember { DateTimeFormatter.ofPattern("dd 'de' MMM", Locale("pt", "BR")) }
    val sessionFmt = remember { DateTimeFormatter.ofPattern("EEE dd/MM", Locale("pt", "BR")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico Semanal") },
                actions = {
                    IconButton(onClick = viewModel::thisWeek) {
                        Icon(Icons.Filled.Today, contentDescription = "Semana atual")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = viewModel::previousWeek) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Semana anterior")
                }
                Text(
                    "${summary.weekStart.format(weekFmt)} – ${summary.weekEnd.format(weekFmt)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = viewModel::nextWeek) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Próxima semana")
                }
            }

            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Boletim",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { summary.completionRate },
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryItem("Total", summary.total)
                        SummaryItem("Concluídos", summary.completed, MaterialTheme.colorScheme.tertiary)
                        SummaryItem("Pendentes", summary.pending)
                        SummaryItem("Remarcados", summary.rescheduled, MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Sessões", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))

            if (summary.sessions.isEmpty()) {
                Text(
                    "Sem sessões registradas nesta semana.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(summary.sessions, key = { it.id }) { session ->
                        SessionHistoryItem(session, sessionFmt)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: Int,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SessionHistoryItem(session: WorkoutSession, fmt: DateTimeFormatter) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(session.workout.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        session.date.format(fmt).replaceFirstChar { it.titlecase(Locale("pt", "BR")) },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusPill(session.status)
            }
            val performed = session.exerciseLogs.count { it.performed }
            val total = session.workout.exercises.size
            if (total > 0) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("$performed / $total exercícios realizados",
                    style = MaterialTheme.typography.bodyMedium)
                session.exerciseLogs.filter { it.performed }.forEach { log ->
                    Text(
                        "• ${log.exercise.name}" +
                            (log.actualWeightKg?.let { " — ${formatNumber(it)} kg" }.orEmpty()) +
                            (log.actualReps?.let { " x $it reps" }.orEmpty()),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: SessionStatus) {
    val (label, color) = when (status) {
        SessionStatus.CONCLUIDO -> "Concluído" to MaterialTheme.colorScheme.tertiary
        SessionStatus.REMARCADO -> "Remarcado" to MaterialTheme.colorScheme.secondary
        SessionStatus.EM_ANDAMENTO -> "Em andamento" to MaterialTheme.colorScheme.primary
        SessionStatus.PENDENTE -> "Pendente" to MaterialTheme.colorScheme.surfaceVariant
    }
    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

private fun formatNumber(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else "%.1f".format(Locale.US, v)
