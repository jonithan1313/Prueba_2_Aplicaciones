package cl.esucar.tutorvirtual.data.repository

import cl.esucar.tutorvirtual.data.local.UserDao
import cl.esucar.tutorvirtual.data.local.UserEntity
import cl.esucar.tutorvirtual.data.prefs.SettingsStore
import cl.esucar.tutorvirtual.domain.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface AuthResult {
    data class Success(val userId: Long) : AuthResult
    data class Failure(val message: String) : AuthResult
}

class AuthRepository(
    private val userDao: UserDao,
    private val settings: SettingsStore
) {
    suspend fun register(name: String, email: String, password: String): AuthResult =
        withContext(Dispatchers.Default) {
            val mail = email.trim().lowercase()
            if (userDao.findByEmail(mail) != null) {
                return@withContext AuthResult.Failure("Ya existe una cuenta con ese correo.")
            }
            val salt = PasswordHasher.newSalt()
            val id = userDao.insert(
                UserEntity(
                    name = name.trim(), email = mail,
                    passwordHash = PasswordHasher.hash(password, salt), salt = salt
                )
            )
            settings.setSession(id)
            AuthResult.Success(id)
        }

    suspend fun login(email: String, password: String): AuthResult =
        withContext(Dispatchers.Default) {
            val user = userDao.findByEmail(email.trim().lowercase())
            // Mensaje genérico: no revela si el correo existe.
            if (user == null || !PasswordHasher.verify(password, user.salt, user.passwordHash)) {
                return@withContext AuthResult.Failure("Correo o contraseña incorrectos.")
            }
            settings.setSession(user.id)
            AuthResult.Success(user.id)
        }

    suspend fun logout() = settings.clearSession()

    suspend fun user(id: Long): UserEntity? = if (id > 0) userDao.findById(id) else null
}
