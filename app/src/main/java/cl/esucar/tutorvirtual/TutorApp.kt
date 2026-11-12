package cl.esucar.tutorvirtual

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import cl.esucar.tutorvirtual.di.AppContainer
import cl.esucar.tutorvirtual.notifications.ReminderWorker
import kotlinx.coroutines.launch

class TutorApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannel()
        // Carga el contenido inicial (offline) la primera vez que se abre la app.
        container.appScope.launch { container.contentRepository.seedIfNeeded() }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            ReminderWorker.CHANNEL_ID,
            "Recordatorios de entregas",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Avisos antes de la fecha límite de cada hito" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
