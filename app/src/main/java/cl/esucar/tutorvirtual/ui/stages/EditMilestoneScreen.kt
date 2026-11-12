package cl.esucar.tutorvirtual.ui.stages

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.esucar.tutorvirtual.di.appViewModel
import cl.esucar.tutorvirtual.domain.DeliverableStatus
import cl.esucar.tutorvirtual.ui.components.AppTextField
import cl.esucar.tutorvirtual.ui.components.AppTopBar
import cl.esucar.tutorvirtual.ui.components.ContentWidth
import cl.esucar.tutorvirtual.ui.components.StatusChip
import cl.esucar.tutorvirtual.ui.components.fieldError
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.io.File

/** Formulario validado: estado (selector), fecha de entrega, notas y foto de evidencia (cámara). */
@Composable
fun EditMilestoneScreen(onBack: () -> Unit, vm: EditMilestoneViewModel = appViewModel()) {
    val s by vm.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }
    var cameraError by rememberSaveable { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val path = pendingPath
        if (ok && path != null) vm.onEvidence(path) else path?.let { File(it).delete() }
        pendingPath = null
    }

    fun openCamera() {
        cameraError = null
        val dir = File(context.filesDir, "evidence").apply { mkdirs() }
        val file = File(dir, vm.evidenceFileName())
        pendingPath = file.absolutePath
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        try {
            cameraLauncher.launch(uri)
        } catch (e: ActivityNotFoundException) {
            pendingPath = null
            cameraError = "No se encontró una aplicación de cámara en este dispositivo."
        }
    }

    // Confirmación visual y regreso automático tras guardar.
    LaunchedEffect(s.saved) {
        if (s.saved) {
            delay(1100)
            onBack()
        }
    }

    Scaffold(topBar = { AppTopBar("Actualizar entregable", onBack) }) { pad ->
        ContentWidth(Modifier.padding(pad)) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(s.stageTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                AnimatedVisibility(s.saved, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer, shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null)
                            Text("Estado actualizado correctamente", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Text("Estado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Column(Modifier.selectableGroup()) {
                    DeliverableStatus.entries.filter { it.studentSelectable }.forEach { option ->
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 52.dp)
                                .selectable(selected = option == s.status, onClick = { vm.onStatus(option) }, role = Role.RadioButton)
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = option == s.status, onClick = null)
                            Spacer(Modifier.size(12.dp))
                            StatusChip(option)
                        }
                    }
                }

                AppTextField(
                    value = s.sentDate, onValueChange = vm::onSentDate,
                    label = "Fecha de entrega (dd/MM/aaaa)", leadingIcon = Icons.Filled.CalendarMonth,
                    error = fieldError(s.dateError, s.sentDate, s.submitted),
                    showValid = s.sentDate.isNotBlank() && s.dateError == null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                AppTextField(
                    value = s.notes, onValueChange = vm::onNotes,
                    label = "Notas del avance", leadingIcon = Icons.AutoMirrored.Filled.Notes,
                    error = fieldError(s.notesError, s.notes, s.submitted),
                    showValid = s.notes.isNotBlank() && s.notesError == null,
                    hint = "${s.notes.length}/500",
                    singleLine = false, minLines = 3
                )

                Text("Evidencia (foto)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                AnimatedVisibility(s.evidencePath.isNotEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    AsyncImage(
                        model = File(s.evidencePath),
                        contentDescription = "Vista previa de la foto de evidencia",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { openCamera() }, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text(if (s.evidencePath.isEmpty()) "Tomar foto" else "Reemplazar")
                    }
                    if (s.evidencePath.isNotEmpty()) {
                        TextButton(onClick = { vm.onEvidence("") }, modifier = Modifier.heightIn(min = 52.dp)) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                            Spacer(Modifier.size(6.dp))
                            Text("Quitar")
                        }
                    }
                }
                cameraError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

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
                        Text("Guardar cambios", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
