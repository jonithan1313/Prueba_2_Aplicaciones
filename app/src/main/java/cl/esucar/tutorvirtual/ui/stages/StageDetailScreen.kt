package cl.esucar.tutorvirtual.ui.stages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.esucar.tutorvirtual.di.appViewModel
import cl.esucar.tutorvirtual.domain.DateUtils
import cl.esucar.tutorvirtual.domain.DeliverableStatus
import cl.esucar.tutorvirtual.ui.components.AppTopBar
import cl.esucar.tutorvirtual.ui.components.ContentWidth
import cl.esucar.tutorvirtual.ui.components.DueBadge
import cl.esucar.tutorvirtual.ui.components.EmptyState
import cl.esucar.tutorvirtual.ui.components.StatusChip
import coil.compose.AsyncImage
import java.io.File

@Composable
fun StageDetailScreen(
    onBack: () -> Unit,
    onGuide: () -> Unit,
    onEdit: () -> Unit,
    vm: StageDetailViewModel = appViewModel()
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val stage = ui.stage

    Scaffold(topBar = { AppTopBar(stage?.title ?: "Etapa", onBack) }) { pad ->
        ContentWidth(Modifier.padding(pad)) {
            if (stage == null) {
                EmptyState(Icons.AutoMirrored.Filled.MenuBook, "Cargando etapa…")
            } else {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        StatusChip(ui.status)
                        DueBadge(ui.daysLeft, ui.status.isDone)
                    }

                    SectionCard("Descripción", Icons.Filled.Info) {
                        Text(stage.description, style = MaterialTheme.typography.bodyLarge)
                    }
                    SectionCard("Formato requerido", Icons.Filled.Description) {
                        Text(stage.format, style = MaterialTheme.typography.bodyLarge)
                    }
                    SectionCard("Fecha límite", Icons.Filled.CalendarMonth) {
                        Text(DateUtils.formatLong(stage.deadline), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                    SectionCard("Pauta de evaluación", Icons.Filled.Checklist) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ui.rubric.forEach { Text("•  $it", style = MaterialTheme.typography.bodyLarge) }
                        }
                    }

                    val teacherFeedback = ui.progress?.feedback.orEmpty()
                    AnimatedVisibility(
                        visible = teacherFeedback.isNotBlank(),
                        enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()
                    ) {
                        SectionCard("Observaciones de tu profesor guía", Icons.Filled.RateReview) {
                            Text(teacherFeedback, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    val progress = ui.progress
                    AnimatedVisibility(
                        visible = progress != null && (progress.notes.isNotBlank() || progress.evidencePath.isNotBlank()),
                        enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()
                    ) {
                        if (progress != null) {
                            SectionCard("Mi registro", Icons.Filled.PhotoCamera) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    if (progress.sentDate.isNotBlank()) Text("Fecha de entrega: ${progress.sentDate}")
                                    if (progress.notes.isNotBlank()) Text(progress.notes, style = MaterialTheme.typography.bodyLarge)
                                    if (progress.evidencePath.isNotBlank()) {
                                        AsyncImage(
                                            model = File(progress.evidencePath),
                                            contentDescription = "Foto de evidencia de la entrega",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = onGuide,
                        enabled = ui.stepCount > 0,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) {
                        Text("Iniciar guía paso a paso", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.size(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                    if (ui.status == DeliverableStatus.APROBADO) {
                        Text(
                            "Tu profesor guía aprobó este hito, por lo que ya no se puede modificar.",
                            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text("Actualizar estado y evidencia", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            content()
        }
    }
}
