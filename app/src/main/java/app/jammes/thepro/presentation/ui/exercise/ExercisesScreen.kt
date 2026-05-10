package app.jammes.thepro.presentation.ui.exercise

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jammes.thepro.domain.model.Exercise
import app.jammes.thepro.domain.model.ExerciseType
import app.jammes.thepro.presentation.ui.common.CsvImport
import app.jammes.thepro.presentation.ui.common.UiMessage
import app.jammes.thepro.presentation.ui.theme.TypeCardio
import app.jammes.thepro.presentation.ui.theme.TypeCore
import app.jammes.thepro.presentation.ui.theme.TypeFlexibilidade
import app.jammes.thepro.presentation.ui.theme.TypeFuncional
import app.jammes.thepro.presentation.ui.theme.TypeMusculacao
import app.jammes.thepro.presentation.ui.theme.TypeOutro

private fun ExerciseType.color(): Color = when (this) {
    ExerciseType.MUSCULACAO -> TypeMusculacao
    ExerciseType.CARDIO -> TypeCardio
    ExerciseType.CORE -> TypeCore
    ExerciseType.FLEXIBILIDADE -> TypeFlexibilidade
    ExerciseType.FUNCIONAL -> TypeFuncional
    ExerciseType.OUTRO -> TypeOutro
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(viewModel: ExerciseViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { msg ->
            when (msg) {
                is UiMessage.Info -> snackbar.showSnackbar(msg.text)
                is UiMessage.Error -> snackbar.showSnackbar(msg.text)
            }
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { CsvImport.openStream(context, it) }?.let(viewModel::importCsv)
    }

    var editing by remember { mutableStateOf<Exercise?>(null) }
    var showForm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Biblioteca", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Exercícios", style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = { csvLauncher.launch(arrayOf("text/*", "text/csv", "text/comma-separated-values", "*/*")) }) {
                        Icon(Icons.Filled.FileUpload, contentDescription = "Importar CSV",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; showForm = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Novo") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Buscar exercício ou tipo…") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpar")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (state.items.isEmpty() && !state.isLoading) {
                EmptyExerciseState(
                    onCreate = { editing = null; showForm = true },
                    onImport = { csvLauncher.launch(arrayOf("text/*", "text/csv", "text/comma-separated-values", "*/*")) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.items, key = { it.id }) { ex ->
                        ExerciseCard(
                            exercise = ex,
                            onEdit = { editing = ex; showForm = true },
                            onDelete = { viewModel.delete(ex) }
                        )
                    }
                }
            }
        }
    }

    if (showForm) {
        ExerciseFormDialog(
            initial = editing,
            onDismiss = { showForm = false },
            onConfirm = { ex ->
                viewModel.save(ex)
                showForm = false
            }
        )
    }
}

@Composable
private fun ExerciseCard(exercise: Exercise, onEdit: () -> Unit, onDelete: () -> Unit) {
    val typeColor = exercise.type.color()
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(typeColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = typeColor)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                TypeBadge(label = exercise.type.displayName, color = typeColor)
                if (exercise.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        exercise.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, "Editar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, "Excluir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TypeBadge(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.18f),
        contentColor = color,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun EmptyExerciseState(onCreate: () -> Unit, onImport: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
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
                Icons.Filled.Bolt,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(20.dp))
        Text("Nenhum exercício cadastrado", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(6.dp))
        Text(
            "Cadastre manualmente ou importe um CSV com seu catálogo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onCreate,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(6.dp))
            Text("Criar exercício")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onImport,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.FileUpload, null)
            Spacer(Modifier.width(6.dp))
            Text("Importar CSV")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseFormDialog(
    initial: Exercise?,
    onDismiss: () -> Unit,
    onConfirm: (Exercise) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var description by remember { mutableStateOf(initial?.description.orEmpty()) }
    var type by remember { mutableStateOf(initial?.type ?: ExerciseType.MUSCULACAO) }
    var typeMenuOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                if (initial == null) "Novo exercício" else "Editar exercício",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                ExposedDropdownMenuBox(
                    expanded = typeMenuOpen,
                    onExpandedChange = { typeMenuOpen = !typeMenuOpen }
                ) {
                    OutlinedTextField(
                        value = type.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeMenuOpen) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeMenuOpen,
                        onDismissRequest = { typeMenuOpen = false }
                    ) {
                        ExerciseType.entries.forEach { t ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(t.color())
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(t.displayName)
                                    }
                                },
                                onClick = { type = t; typeMenuOpen = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        Exercise(
                            id = initial?.id ?: 0L,
                            name = name.trim(),
                            description = description.trim(),
                            type = type
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
