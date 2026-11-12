package cl.esucar.tutorvirtual.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/** Preferencias livianas (sesión actual) con DataStore. 0 = sin sesión. */
class SettingsStore(private val context: Context) {
    private val keyUser = longPreferencesKey("session_user_id")

    val sessionUserId: Flow<Long> = context.dataStore.data.map { it[keyUser] ?: 0L }

    suspend fun setSession(userId: Long) {
        context.dataStore.edit { it[keyUser] = userId }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.remove(keyUser) }
    }
}
