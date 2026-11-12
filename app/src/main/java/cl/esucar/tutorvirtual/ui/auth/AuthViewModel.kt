package cl.esucar.tutorvirtual.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.esucar.tutorvirtual.data.repository.AuthRepository
import cl.esucar.tutorvirtual.data.repository.AuthResult
import cl.esucar.tutorvirtual.domain.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val submitted: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirm: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val submitted: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)

/** Estado y validación de login/registro. La UI solo pinta; toda la lógica vive aquí y en [Validators]. */
class AuthViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login.asStateFlow()

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register.asStateFlow()

    // ---------- Login ----------
    fun onLoginEmail(v: String) = _login.update { it.copy(email = v, emailError = Validators.email(v), error = null) }
    fun onLoginPassword(v: String) =
        _login.update { it.copy(password = v, passwordError = Validators.loginPassword(v), error = null) }

    fun submitLogin() {
        val s = _login.value
        val eErr = Validators.email(s.email)
        val pErr = Validators.loginPassword(s.password)
        if (eErr != null || pErr != null) {
            _login.value = s.copy(submitted = true, emailError = eErr, passwordError = pErr)
            return
        }
        viewModelScope.launch {
            _login.update { it.copy(loading = true, submitted = true, error = null) }
            when (val r = auth.login(s.email, s.password)) {
                is AuthResult.Success -> Unit // la sesión cambia y la navegación reacciona sola
                is AuthResult.Failure -> _login.update { it.copy(loading = false, error = r.message) }
            }
        }
    }

    // ---------- Registro ----------
    fun onRegName(v: String) = _register.update { it.copy(name = v, nameError = Validators.name(v), error = null) }
    fun onRegEmail(v: String) = _register.update { it.copy(email = v, emailError = Validators.email(v), error = null) }
    fun onRegPassword(v: String) = _register.update {
        it.copy(
            password = v,
            passwordError = Validators.password(v),
            confirmError = if (it.confirm.isEmpty()) it.confirmError else Validators.confirmPassword(v, it.confirm),
            error = null
        )
    }

    fun onRegConfirm(v: String) = _register.update {
        it.copy(confirm = v, confirmError = Validators.confirmPassword(it.password, v), error = null)
    }

    fun submitRegister() {
        val s = _register.value
        val checked = s.copy(
            submitted = true,
            nameError = Validators.name(s.name),
            emailError = Validators.email(s.email),
            passwordError = Validators.password(s.password),
            confirmError = Validators.confirmPassword(s.password, s.confirm)
        )
        if (listOf(checked.nameError, checked.emailError, checked.passwordError, checked.confirmError).any { it != null }) {
            _register.value = checked
            return
        }
        viewModelScope.launch {
            _register.update { checked.copy(loading = true, error = null) }
            when (val r = auth.register(s.name, s.email, s.password)) {
                is AuthResult.Success -> Unit
                is AuthResult.Failure -> _register.update { it.copy(loading = false, error = r.message) }
            }
        }
    }
}
