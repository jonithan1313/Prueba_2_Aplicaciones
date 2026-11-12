package cl.esucar.tutorvirtual.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.esucar.tutorvirtual.di.appViewModel
import cl.esucar.tutorvirtual.domain.DeliverableStatus
import cl.esucar.tutorvirtual.ui.components.AppTextField
import cl.esucar.tutorvirtual.ui.components.AppTopBar
import cl.esucar.tutorvirtual.ui.components.ContentWidth
import cl.esucar.tutorvirtual.ui.components.EmptyState
import cl.esucar.tutorvirtual.ui.components.StatusChip
import cl.esucar.tutorvirtual.ui.components.fieldError
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.io.File

/** El profesor revisa la entrega de un estudiante contra la pauta y la aprueba o la devuelve con observaciones. */
@Composable
fun TeacherReviewScreen(onBack: () -> Unit, vm: TeacherReviewViewModel = appViewModel()) {
    val s by vm.ui.collectAsStateWithLifecycle()
    val stage = s.stage
    val progress = s.progress

    // Confirmación visual y regreso automático tras decidir.
    LaunchedEffect(s.result) {
        if (s.result != null) {
            delay(1100)
            onBack()
        }
    }

    Scaffold(topBar = { AppTopBar("Revisar entrega", onBack) }) { pad ->
        ContentWidth(Modifier.padding(pad)) {
            if (stage == null) {
                EmptyState(Icons.Filled.RateReview, if (s.loaded) "La etapa ya no existe." else "Cargando…")
            } else {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(stage.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Estudiante: ${s.studentName}", style = MaterialTheme.typography.bodyLarge)

                    SuccessBanner(
                        visible = s.result != null,
                        text = if (s.result == DeliverableStatus.APROBADO) "Hito aprobado" else "Hito devuelto al estudiante con tus observaciones"
                    )

                    InfoCard("Entrega del estudiante", Icons.Filled.Info) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatusChip(s.status)
                            if (progress != null && progress.sentDate.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text("Fecha de entrega: ${progress.sentDate}")
                                }
                            }
                            if (progress != null && progress.notes.isNotBlank()) {
                                Text(progress.notes, style = MaterialTheme.typography.bodyLarge)
                            } else {
                                Text("El estudiante no dejó notas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (progress != null && progress.evidencePath.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text("Evidencia adjunta", fontWeight = FontWeight.SemiBold)
                                }
                                AsyncImage(
                                    model = File(progress.evidencePath),
                                    contentDescription = "Foto de evidencia entregada por el estudiante",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))
                                )
                            } else {
                                Text("Sin foto de evidencia.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    InfoCard("Pauta de evaluación", Icons.Filled.Checklist) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            s.rubric.forEach { Text("•  $it", style = MaterialTheme.typography.bodyLarge) }
                        }
                    }

                    if (s.canReview) {
                        Text("Tu evaluación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        AppTextField(
                            value = s.feedback, onValueChange = vm::onFeedback,
                            label = "Observaciones para el estudiante", leadingIcon = Icons.AutoMirrored.Filled.Notes,
                            error = fieldError(s.feedbackError, s.feedback, s.strict),
                            showValid = s.feedback.isNotBlank() && s.feedbackError == null,
                            hint = "${s.feedback.length}/500 · obligatorias si devuelves el hito",
                            singleLine = false, minLines = 3
                        )
                        Button(
                            onClick = vm::approve,
                            enabled = !s.saving && s.result == null,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                        ) {
                            if (s.saving) {
                                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                                Spacer(Modifier.size(8.dp))
                                Text("Aprobar hito", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        OutlinedButton(
                            onClick = vm::returnForFixes,
                            enabled = !s.saving && s.result == null,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                        ) {
                            Icon(Icons.Filled.Replay, contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text("Devolver con observaciones", style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        InfoCard("Aún no se puede evaluar", Icons.Filled.Info) {
                            Text(
                                "El estudiante todavía no marca este hito como Enviado. Podrás aprobarlo o devolverlo cuando lo entregue.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
