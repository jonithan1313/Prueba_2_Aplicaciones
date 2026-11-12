package cl.esucar.tutorvirtual.ui.faq

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.esucar.tutorvirtual.data.local.FaqEntity
import cl.esucar.tutorvirtual.di.appViewModel
import cl.esucar.tutorvirtual.ui.components.AppTextField
import cl.esucar.tutorvirtual.ui.components.AppTopBar
import cl.esucar.tutorvirtual.ui.components.ContentWidth
import cl.esucar.tutorvirtual.ui.components.EmptyState

@Composable
fun FaqScreen(vm: FaqViewModel = appViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    var expandedId by rememberSaveable { mutableStateOf<Int?>(null) }
    val focus = LocalFocusManager.current

    Scaffold(topBar = { AppTopBar("Preguntas frecuentes") }) { pad ->
        ContentWidth(Modifier.padding(pad)) {
            Column {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AppTextField(
                        value = query, onValueChange = vm::onQuery,
                        label = "Buscar por palabra clave", leadingIcon = Icons.Filled.Search,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                        trailingContent = if (query.isNotEmpty()) ({
                            IconButton(onClick = { vm.onQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Borrar búsqueda")
                            }
                        }) else null
                    )
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ui.categories.forEach { cat ->
                            FilterChip(
                                selected = ui.selected == cat,
                                onClick = { vm.onCategory(cat) },
                                label = { Text(cat) },
                                modifier = Modifier.heightIn(min = 40.dp)
                            )
                        }
                    }
                }

                if (ui.total == 0) {
                    EmptyState(
                        Icons.Filled.Search,
                        if (query.isBlank() && ui.categories.isEmpty()) "Cargando preguntas…"
                        else "No encontramos preguntas para \"$query\". Prueba con otra palabra."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ui.grouped.forEach { (category, faqs) ->
                            item(key = "h_$category") {
                                Text(
                                    category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            items(faqs, key = { it.id }) { faq ->
                                FaqItem(
                                    faq = faq, expanded = expandedId == faq.id,
                                    onToggle = { expandedId = if (expandedId == faq.id) null else faq.id }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqItem(faq: FaqEntity, expanded: Boolean, onToggle: () -> Unit) {
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, tween(250), label = "chevron")
    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth().semantics { stateDescription = if (expanded) "Respuesta visible" else "Respuesta oculta" },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(faq.question, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Contraer" else "Expandir",
                    modifier = Modifier.rotate(rotation)
                )
            }
            AnimatedVisibility(expanded, enter = fadeIn(tween(250)) + expandVertically(tween(250)), exit = fadeOut(tween(150)) + shrinkVertically(tween(250))) {
                Text(faq.answer, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}
