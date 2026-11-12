package cl.esucar.tutorvirtual.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.esucar.tutorvirtual.di.appViewModel
import cl.esucar.tutorvirtual.domain.UserRole
import cl.esucar.tutorvirtual.ui.components.AppTopBar
import cl.esucar.tutorvirtual.ui.components.ContentWidth

@Composable
fun TeacherProfileScreen(vm: TeacherProfileViewModel = appViewModel()) {
    val user by vm.user.collectAsStateWithLifecycle()

    Scaffold(topBar = { AppTopBar("Mi perfil") }) { pad ->
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
                                    Text(UserRole.PROFESOR.label, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
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
                    InfoCard("Qué puedes hacer", Icons.Filled.Info) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("•  Estudiantes: revisar el avance y aprobar o devolver cada hito con observaciones.", style = MaterialTheme.typography.bodyLarge)
                            Text("•  Etapas: definir la estructura, fechas límite, pautas y pasos de la guía.", style = MaterialTheme.typography.bodyLarge)
                            Text("•  Preguntas: administrar el banco de preguntas frecuentes.", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
