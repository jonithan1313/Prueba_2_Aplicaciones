package cl.esucar.tutorvirtual.ui.checklist

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.esucar.tutorvirtual.di.appViewModel
import cl.esucar.tutorvirtual.domain.DateUtils
import cl.esucar.tutorvirtual.domain.StageItem
import cl.esucar.tutorvirtual.ui.components.AppTopBar
import cl.esucar.tutorvirtual.ui.components.ContentWidth
import cl.esucar.tutorvirtual.ui.components.DueBadge
import cl.esucar.tutorvirtual.ui.components.EmptyState
import cl.esucar.tutorvirtual.ui.components.StatusChip

@Composable
fun ChecklistScreen(onOpen: (Int) -> Unit, vm: ChecklistViewModel = appViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    Scaffold(topBar = { AppTopBar("Checklist de hitos") }) { pad ->
        ContentWidth(Modifier.padding(pad)) {
            if (ui.items.isEmpty()) {
                EmptyState(Icons.Filled.Checklist, "Cargando hitos…")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(340.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            "${ui.done} de ${ui.items.size} hitos completados",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(ui.items, key = { it.stage.id }) { item ->
                        ChecklistRow(item, onClick = { onOpen(item.stage.id) }, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistRow(item: StageItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val urgent = item.isDueSoon || item.isOverdue
    val container by animateColorAsState(
        if (urgent) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surface,
        tween(400), label = "rowColor"
    )
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) {
            contentDescription = "${item.stage.title}, ${item.status.label}, fecha límite ${DateUtils.formatLong(item.stage.deadline)}"
        },
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(
                if (item.status.isDone) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null, modifier = Modifier.size(30.dp),
                tint = if (item.status.isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(item.stage.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(DateUtils.formatLong(item.stage.deadline), style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(item.status)
                    DueBadge(item.daysLeft, item.status.isDone)
                }
            }
        }
    }
}
