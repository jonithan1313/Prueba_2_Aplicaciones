package cl.esucar.tutorvirtual.ui.teacher

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.esucar.tutorvirtual.di.appViewModel
import cl.esucar.tutorvirtual.domain.DateUtils
import cl.esucar.tutorvirtual.domain.DeliverableStatus
import cl.esucar.tutorvirtual.domain.StageItem
import cl.esucar.tutorvirtual.ui.components.AppTopBar
import cl.esucar.tutorvirtual.ui.components.ContentWidth
import cl.esucar.tutorvirtual.ui.components.DueBadge
import cl.esucar.tutorvirtual.ui.components.EmptyState
import cl.esucar.tutorvirtual.ui.components.StatusChip

// ------------------------------------------------------------------ Lista de estudiantes

@Composable
fun TeacherStudentsScreen(onOpen: (Long) -> Unit, vm: TeacherStudentsViewModel = appViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    Scaffold(topBar = { AppTopBar("Panel del profesor") }) { pad ->
        ContentWidth(Modifier.padding(pad)) {
            if (ui.rows.isEmpty()) {
                EmptyState(
                    Icons.Filled.Groups,
                    if (ui.loaded) "Aún no hay estudiantes registrados en este dispositivo." else "Cargando…"
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(340.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) { PendingSummary(ui) }
                    items(ui.rows, key = { it.user.id }) { row ->
                        StudentCard(row, onClick = { onOpen(row.user.id) }, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingSummary(ui: StudentsUi) {
    val hasPending = ui.pending > 0
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (hasPending) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                Icons.Filled.PendingActions, contentDescription = null, modifier = Modifier.size(36.dp),
                tint = if (hasPending) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
            )
            Column {
                Text(
                    when (ui.pending) {
                        0 -> "No tienes entregas por revisar"
                        1 -> "1 entrega por revisar"
                        else -> "${ui.pending} entregas por revisar"
                    },
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    color = if (hasPending) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "${ui.rows.size} estudiantes · ${ui.stageCount} etapas",
                    color = if (hasPending) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun StudentCard(row: StudentRow, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(row.fraction, tween(600), label = "studentProgress")
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) {
            contentDescription = "${row.user.name}. ${row.approved} de ${row.total} etapas aprobadas. ${row.sent} por revisar"
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(row.user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(row.user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
            Text("Aprobadas: ${row.approved} de ${row.total}", style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(
                progress = { animated },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (row.sent > 0) ReviewBadge("${row.sent} por revisar")
                if (row.inDevelopment > 0) {
                    Text("${row.inDevelopment} en desarrollo", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

/** Marca "por revisar": ícono + texto (no depende solo del color). */
@Composable
fun ReviewBadge(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Filled.PendingActions, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ------------------------------------------------------------------ Avance de un estudiante

@Composable
fun TeacherStudentDetailScreen(
    onBack: () -> Unit,
    onReview: (stageId: Int) -> Unit,
    vm: TeacherStudentDetailViewModel = appViewModel()
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    Scaffold(topBar = { AppTopBar(ui.student?.name ?: "Estudiante", onBack) }) { pad ->
        ContentWidth(Modifier.padding(pad)) {
            if (ui.items.isEmpty()) {
                EmptyState(Icons.Filled.Groups, if (ui.loaded) "No hay etapas definidas." else "Cargando…")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Avance: ${ui.approved} de ${ui.items.size} etapas aprobadas",
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(ui.student?.email.orEmpty(), color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                    items(ui.items, key = { it.stage.id }) { item ->
                        StageReviewCard(item, onClick = { onReview(item.stage.id) }, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@Composable
private fun StageReviewCard(item: StageItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val stage = item.stage
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) {
            contentDescription = "Etapa ${stage.position}: ${stage.title}. Estado: ${item.status.label}"
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${stage.position}. ${stage.title}", Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Fecha límite: ${DateUtils.formatLong(stage.deadline)}", style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                StatusChip(item.status)
                if (item.status == DeliverableStatus.ENVIADO) ReviewBadge("Por revisar")
                DueBadge(item.daysLeft, item.status.isDone)
            }
        }
    }
}
