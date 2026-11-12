package cl.esucar.tutorvirtual.ui.auth

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.esucar.tutorvirtual.di.appViewModel
import cl.esucar.tutorvirtual.ui.components.AppTextField
import cl.esucar.tutorvirtual.ui.components.ContentWidth
import cl.esucar.tutorvirtual.ui.components.PasswordField
import cl.esucar.tutorvirtual.ui.components.fieldError

@Composable
fun LoginScreen(onGoRegister: () -> Unit, vm: AuthViewModel = appViewModel()) {
    val s by vm.login.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current

    ContentWidth(Modifier.safeDrawingPadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.primary) {
                Icon(
                    Icons.Filled.School, contentDescription = null,
                    modifier = Modifier.padding(18.dp).size(44.dp), tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text("Tutor Virtual ESUCAR", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
            Text("Tu guía para el Trabajo de Título", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.size(8.dp))

            AnimatedVisibility(s.error != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Surface(color = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer, shape = MaterialTheme.shapes.medium) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Error, contentDescription = null)
                        Text(s.error.orEmpty())
                    }
                }
            }

            AppTextField(
                value = s.email, onValueChange = vm::onLoginEmail, label = "Correo", leadingIcon = Icons.Filled.Email,
                error = fieldError(s.emailError, s.email, s.submitted),
                showValid = s.email.isNotEmpty() && s.emailError == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) })
            )
            PasswordField(
                value = s.password, onValueChange = vm::onLoginPassword, label = "Contraseña",
                error = fieldError(s.passwordError, s.password, s.submitted),
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus(); vm.submitLogin() })
            )

            Button(
                onClick = { focus.clearFocus(); vm.submitLogin() },
                enabled = !s.loading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) {
                if (s.loading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                else Text("Ingresar", style = MaterialTheme.typography.titleMedium)
            }
            TextButton(onClick = onGoRegister, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}
