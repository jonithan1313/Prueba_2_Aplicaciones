package cl.esucar.tutorvirtual.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cl.esucar.tutorvirtual.MainActivity
import cl.esucar.tutorvirtual.R
import cl.esucar.tutorvirtual.TutorApp
import cl.esucar.tutorvirtual.domain.DeliverableStatus

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val userId = inputData.getLong(KEY_USER, 0L)
        val stageId = inputData.getInt(KEY_STAGE, 0)
        val title = inputData.getString(KEY_TITLE).orEmpty()
        val days = inputData.getInt(KEY_DAYS, 0)

        val container = (applicationContext as TutorApp).container
        // No avisar si el hito ya fue entregado o el recordatorio fue desactivado.
        val status = DeliverableStatus.from(container.progressRepository.get(userId, stageId)?.status)
        if (status.isDone) return Result.success()
        if (container.progressRepository.getReminder(userId, stageId)?.enabled != true) return Result.success()

        val granted = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted || !NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            return Result.success()
        }

        val message = when (days) {
            0 -> "Hoy vence la entrega: $title"
            1 -> "Mañana vence la entrega: $title"
            else -> "Faltan $days días para la entrega: $title"
        }
        val intent = Intent(applicationContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pending = PendingIntent.getActivity(
            applicationContext, stageId, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Recordatorio de entrega")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(applicationContext)
            .notify((userId * 1000 + stageId * 10 + days).toInt(), notification)
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "deadline_reminders"
        const val KEY_USER = "user"
        const val KEY_STAGE = "stage"
        const val KEY_TITLE = "title"
        const val KEY_DAYS = "days"
    }
}
