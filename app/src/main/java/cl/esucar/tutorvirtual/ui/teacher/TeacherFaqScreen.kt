package cl.esucar.tutorvirtual.ui.teacher

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.esucar.tutorvirtual.data.local.FaqEntity
import cl.esucar.tutorvirtual.di.appViewModel
import cl.esucar.tutorvirtual.ui.components.AppTextField
import cl.esucar.tutorvirtual.ui.components.AppTopBar
import cl.esucar.tutorvirtual.ui.components.ContentWidth
import cl.esucar.tutorvirtual.ui.components.EmptyState
import cl.esucar.tutorvirtual.ui.components.fieldError

/** El profesor administra el banco de preguntas frecuentes que ven los estudiantes. */
@Composable
fun TeacherFaqScreen(vm: TeacherFaqViewModel = appViewModel()) {
    val faqs by vm.faqs.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()

    var dialogOpen by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Int?>(null) }
    var category by rememberSaveable { mutableStateOf("") }
    var question by rememberSaveable { mutableStateOf("") }
    var answer by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var deletingId by rememberSaveable { mutableStateOf<Int?>(null) }

    fun openDialog(faq: FaqEntity?) {
        editingId = faq?.id
        category = faq?.category.orEmpty()
        question = faq?.question.orEmpty()
        answer = faq?.answer.orEmpty()
        submitted = false
        dialogOpen = true
    }

    Scaffold(
        topBar = { AppTopBar("Preguntas frecuentes") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { openDialog(null) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva pregunta") }
            )
        }
    ) { pad ->
        ContentWidth(Modifier.padding(pad)) {
            if (faqs.isEmpty()) {
                EmptyState(Icons.AutoMirrored.Filled.HelpOutline, "Aún no hay preguntas frecuentes. Agrega la primera.")
            } else {
                val grouped = faqs.groupBy { it.category }
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    grouped.forEach { (cat, list) ->
                        item(key = "cat_$cat") {
                            Text(cat, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        items(list, key = { it.id }) { faq ->
                            Card(
                                Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 4.dp)) {
                                    Text(faq.question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        faq.answer, style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 3, overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Row {
                                        IconButton(onClick = { openDialog(faq) }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Editar pregunta: ${faq.question}")
                                        }
                                        IconButton(onClick = { deletingId = faq.id }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar pregunta: ${faq.question}", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (dialogOpen) {
        val categoryError = vm.validateCategory(category)
        val questionError = vm.validateQuestion(question)
        val answerError = vm.validateAnswer(answer)
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            title = { Text(if (editingId == null) "Nueva pregunta" else "Editar pregunta") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTextField(
                        value = category, onValueChange = { category = it }, label = "Categoría", leadingIcon = Icons.Filled.Category,
                        error = fieldError(categoryError, category, submitted),
                        showValid = category.isNotBlank() && categoryError == null,
                        hint = "Ej.: Citación, Metodología, Formato"
                    )
                    if (categories.isNotEmpty()) {
                        // Atajos para reutilizar categorías existentes y no duplicarlas con otra ortografía.
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            categories.forEach { c ->
                                AssistChip(onClick = { category = c }, label = { Text(c) })
                            }
                        }
                    }
                    AppTextField(
                        value = question, onValueChange = { question = it }, label = "Pregunta", leadingIcon = Icons.AutoMirrored.Filled.HelpOutline,
                        error = fieldError(questionError, question, submitted),
                        showValid = question.isNotBlank() && questionError == null,
                        singleLine = false, minLines = 2
                    )
                    AppTextField(
                        value = answer, onValueChange = { answer = it }, label = "Respuesta", leadingIcon = Icons.AutoMirrored.Filled.Notes,
                        error = fieldError(answerError, answer, submitted),
                        showValid = answer.isNotBlank() && answerError == null,
                        hint = "${answer.length}/800",
                        singleLine = false, minLines = 4
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    submitted = true
                    if (categoryError == null && questionError == null && answerError == null) {
                        vm.save(editingId, category, question, answer)
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
            title = "¿Eliminar esta pregunta?",
            text = "Los estudiantes dejarán de verla en el buscador de preguntas frecuentes.",
            confirmLabel = "Eliminar",
            onConfirm = { vm.delete(toDelete); deletingId = null },
            onDismiss = { deletingId = null }
        )
    }
}
