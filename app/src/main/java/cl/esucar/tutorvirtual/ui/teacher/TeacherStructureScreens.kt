package cl.esucar.tutorvirtual.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.esucar.tutorvirtual.di.appViewModel
import cl.esucar.tutorvirtual.domain.DateUtils
import cl.esucar.tutorvirtual.ui.components.AppTextField
import cl.esucar.tutorvirtual.ui.components.AppTopBar
import cl.esucar.tutorvirtual.ui.components.ContentWidth
import cl.esucar.tutorvirtual.ui.components.EmptyState
import cl.esucar.tutorvirtual.ui.components.fieldError
import kotlinx.coroutines.delay

// ------------------------------------------------------------------ Lista de etapas

@Composable
fun TeacherStagesScreen(onEdit: (stageId: Int) -> Unit, vm: TeacherStagesViewModel = appViewModel()) {
    val stages by vm.stages.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppTopBar("Estructura de etapas") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEdit(0) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva etapa") }
            )
        }
    ) { pad ->
        ContentWidth(Modifier.padding(pad)) {
            if (stages.isEmpty()) {
                EmptyState(Icons.AutoMirrored.Filled.MenuBook, "Aún no hay etapas. Crea la primera con el botón «Nueva etapa».")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Lo que defines aquí es lo que ven los estudiantes en su guía, pautas y fechas límite.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    itemsIndexed(stages, key = { _, st -> st.id }) { index, st ->
                        Card(
                            onClick = { onEdit(st.id) },
                            modifier = Modifier.fillMaxWidth().animateItem().semantics(mergeDescendants = true) {
                                contentDescription = "Etapa ${st.position}: ${st.title}. Toca para editar"
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                                    Text(
                                        "${st.position}", Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                    Text(st.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text("Fecha límite: ${DateUtils.formatLong(st.deadline)}", style = MaterialTheme.typography.bodyMedium)
                                }
                                ReorderButtons(
                                    canUp = index > 0, canDown = index < stages.lastIndex,
                                    onUp = { vm.move(st.id, -1) }, onDown = { vm.move(st.id, 1) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Crear / editar una etapa

@Composable
fun TeacherStageEditScreen(
    onBack: () -> Unit,
    onSteps: (stageId: Int) -> Unit,
    vm: TeacherStageEditViewModel = appViewModel()
) {
    val s by vm.ui.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(s.saved) {
        if (s.saved) {
            delay(1100)
            onBack()
        }
    }
    LaunchedEffect(s.deleted) { if (s.deleted) onBack() }

    Scaffold(topBar = { AppTopBar(if (s.isNew) "Nueva etapa" else "Editar etapa", onBack) }) { pad ->
        ContentWidth(Modifier.padding(pad)) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SuccessBanner(s.saved, "Etapa guardada correctamente")

                AppTextField(
                    value = s.title, onValueChange = vm::onTitle, label = "Título de la etapa", leadingIcon = Icons.Filled.Title,
                    error = fieldError(s.titleError, s.title, s.submitted),
                    showValid = s.title.isNotBlank() && s.titleError == null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                AppTextField(
                    value = s.description, onValueChange = vm::onDescription, label = "Descripción", leadingIcon = Icons.Filled.Description,
                    error = fieldError(s.descriptionError, s.description, s.submitted),
                    showValid = s.description.isNotBlank() && s.descriptionError == null,
                    hint = "${s.description.length}/400",
                    singleLine = false, minLines = 3
                )
                AppTextField(
                    value = s.format, onValueChange = vm::onFormat, label = "Formato requerido", leadingIcon = Icons.AutoMirrored.Filled.Notes,
                    error = fieldError(s.formatError, s.format, s.submitted),
                    showValid = s.format.isNotBlank() && s.formatError == null,
                    hint = "Ej.: PDF de 4 a 6 páginas, APA 7",
                    singleLine = false, minLines = 2
                )
                AppTextField(
                    value = s.deadline, onValueChange = vm::onDeadline, label = "Fecha límite (dd/MM/aaaa)", leadingIcon = Icons.Filled.CalendarMonth,
                    error = fieldError(s.deadlineError, s.deadline, s.submitted),
                    showValid = s.deadline.isNotBlank() && s.deadlineError == null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                AppTextField(
                    value = s.rubric, onValueChange = vm::onRubric, label = "Pauta de evaluación", leadingIcon = Icons.Filled.Checklist,
                    error = fieldError(s.rubricError, s.rubric, s.submitted),
                    showValid = s.rubric.isNotBlank() && s.rubricError == null,
                    hint = "Un criterio por línea (máx. 10)",
                    singleLine = false, minLines = 4
                )

                Button(
                    onClick = vm::save,
                    enabled = s.loaded && !s.saving && !s.saved,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) {
                    if (s.saving) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Guardar etapa", style = MaterialTheme.typography.titleMedium)
                    }
                }

                if (s.isNew) {
                    Text(
                        "Guarda la etapa para poder agregar los pasos de su guía.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    OutlinedButton(onClick = { onSteps(vm.stageId) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Pasos de la guía (${s.stepCount})", style = MaterialTheme.typography.titleMedium)
                    }
                    TextButton(
                        onClick = { confirmDelete = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.size(8.dp))
                        Text("Eliminar etapa", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "¿Eliminar esta etapa?",
            text = "Se borrarán también sus pasos y el avance que los estudiantes hayan registrado en ella. Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            onConfirm = { confirmDelete = false; vm.delete() },
            onDismiss = { confirmDelete = false }
        )
    }
}

// ------------------------------------------------------------------ Pasos de la guía

@Composable
fun TeacherStepsScreen(onBack: () -> Unit, vm: TeacherStepsViewModel = appViewModel()) {
    val stage by vm.stage.collectAsStateWithLifecycle()
    val steps by vm.steps.collectAsStateWithLifecycle()

    var dialogOpen by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var text by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var deletingId by rememberSaveable { mutableStateOf<Long?>(null) }

    fun openDialog(id: Long?, t: String, c: String) {
        editingId = id; title = t; text = c; submitted = false; dialogOpen = true
    }

    Scaffold(
        topBar = { AppTopBar(stage?.title?.let { "Pasos: $it" } ?: "Pasos de la guía", onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { openDialog(null, "", "") },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Agregar paso") }
            )
        }
    ) { pad ->
        ContentWidth(Modifier.padding(pad)) {
            if (steps.isEmpty()) {
                EmptyState(Icons.AutoMirrored.Filled.MenuBook, "Esta etapa aún no tiene pasos. Agrega el primero para que el estudiante tenga su guía.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(steps, key = { _, st -> st.id }) { index, step ->
                        Card(
                            Modifier.fillMaxWidth().animateItem(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        "Paso ${index + 1}: ${step.title}",
                                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(step.content, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                    Row {
                                        IconButton(onClick = { openDialog(step.id, step.title, step.content) }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Editar paso ${index + 1}")
                                        }
                                        IconButton(onClick = { deletingId = step.id }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar paso ${index + 1}", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                                ReorderButtons(
                                    canUp = index > 0, canDown = index < steps.lastIndex,
                                    onUp = { vm.move(step.id, -1) }, onDown = { vm.move(step.id, 1) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (dialogOpen) {
        val titleError = vm.validateTitle(title)
        val textError = vm.validateContent(text)
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            title = { Text(if (editingId == null) "Nuevo paso" else "Editar paso") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTextField(
                        value = title, onValueChange = { title = it }, label = "Título del paso", leadingIcon = Icons.Filled.Title,
                        error = fieldError(titleError, title, submitted),
                        showValid = title.isNotBlank() && titleError == null
                    )
                    AppTextField(
                        value = text, onValueChange = { text = it }, label = "Contenido", leadingIcon = Icons.AutoMirrored.Filled.Notes,
                        error = fieldError(textError, text, submitted),
                        showValid = text.isNotBlank() && textError == null,
                        hint = "${text.length}/600",
                        singleLine = false, minLines = 4
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    submitted = true
                    if (titleError == null && textError == null) {
                        vm.save(editingId, title, text)
                        dialogOpen = false
                    }
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { dialogOpen = false }) { Text("Cancelar") } }
        )
    }

    val toDelete = deletingId
    if (toDelete != null) {
        ConfirmDialog(
            title = "¿Eliminar este paso?",
            text = "Los estudiantes dejarán de verlo en la guía de esta etapa.",
            confirmLabel = "Eliminar",
            onConfirm = { vm.delete(toDelete); deletingId = null },
            onDismiss = { deletingId = null }
        )
    }
}
