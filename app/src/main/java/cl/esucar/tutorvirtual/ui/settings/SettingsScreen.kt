package cl.esucar.tutorvirtual.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.esucar.tutorvirtual.di.appViewModel
import cl.esucar.tutorvirtual.domain.DateUtils
import cl.esucar.tutorvirtual.ui.components.AppTextField
import cl.esucar.tutorvirtual.ui.components.AppTopBar
import cl.esucar.tutorvirtual.ui.components.ContentWidth
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: SettingsViewModel = appViewModel()) {
    val user by vm.user.collectAsStateWithLifecycle()
    val rows by vm.rows.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var pendingStage by rememberSaveable { mutableStateOf<Int?>(null) }
    var editing by rememberSaveable { mutableStateOf<Int?>(null) }

    // Recurso nativo: permiso de notificaciones pedido solo al activar el primer recordatorio.
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val stage = pendingStage
        pendingStage = null
        if (granted && stage != null) {
            vm.setEnabled(stage, true)
        } else {
            scope.launch {
                val result = snackbar.showSnackbar(
                    "Sin permiso no podremos avisarte de tus entregas.",
                    actionLabel = "Abrir ajustes"
                )
                if (result == SnackbarResult.ActionPerformed) {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
        }
    }

    fun onToggle(stageId: Int, enable: Boolean) {
        if (!enable) { vm.setEnabled(stageId, false); return }
        val needsPermission = Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            pendingStage = stageId
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            vm.setEnabled(stageId, true)
        }
    }

    Scaffold(
        topBar = { AppTopBar("Ajustes") },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        ContentWidth(Modifier.padding(pad)) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Column {
                                    Text(user?.name.orEmpty(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(user?.email.orEmpty(), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            OutlinedButton(onClick = vm::logout, modifier = Modifier.heightIn(min = 48.dp)) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                                Spacer(Modifier.size(8.dp))
                                Text("Cerrar sesión")
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Recordatorios de entrega", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Se programan en el dispositivo y funcionan sin internet.", style = MaterialTheme.typography.bodyMedium)
                }
                items(rows, key = { it.stage.id }) { row ->
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(row.stage.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Vence: ${DateUtils.formatLong(row.stage.deadline)}", style = MaterialTheme.typography.bodyMedium)
                                }
                                Switch(
                                    checked = row.enabled,
                                    onCheckedChange = { onToggle(row.stage.id, it) },
                                    modifier = Modifier.semantics { contentDescription = "Recordatorio para ${row.stage.title}" }
                                )
                            }
                            if (row.done) {
                                Text("Entregado: no se enviarán avisos.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            AnimatedVisibility(row.enabled, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Alarm, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.size(8.dp))
                                    Text("Avisos: ${row.days.replace(",", ", ")} días antes", Modifier.weight(1f))
                                    TextButton(onClick = { editing = row.stage.id }) { Text("Cambiar") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val editRow = rows.firstOrNull { it.stage.id == editing }
    if (editRow != null) {
        var text by rememberSaveable(editRow.stage.id) { mutableStateOf(editRow.days) }
        var error by rememberSaveable(editRow.stage.id) { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Días de anticipación") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(editRow.stage.title)
                    AppTextField(
                        value = text,
                        onValueChange = { text = it; error = vm.validateDays(it) },
                        label = "Días antes (ej. 3,1)",
                        leadingIcon = Icons.Filled.Alarm,
                        error = error,
                        showValid = text.isNotBlank() && error == null,
                        hint = "Hasta 3 valores entre 0 y 30",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val e = vm.validateDays(text)
                    if (e != null) error = e else { vm.saveDays(editRow.stage.id, text); editing = null }
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancelar") } }
        )
    }
}
