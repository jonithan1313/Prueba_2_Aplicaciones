package cl.esucar.tutorvirtual.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import cl.esucar.tutorvirtual.domain.DateUtils
import java.time.Duration
import java.time.LocalDateTime

/**
 * Programa recordatorios locales con WorkManager: funcionan sin internet,
 * sobreviven reinicios del equipo y no requieren permisos de alarma exacta.
 */
class ReminderScheduler(context: Context) {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    private fun tag(userId: Long, stageId: Int) = "reminder_${userId}_$stageId"

    fun cancel(userId: Long, stageId: Int) {
        workManager.cancelAllWorkByTag(tag(userId, stageId))
    }

    fun schedule(
        userId: Long,
        stageId: Int,
        title: String,
        deadlineIso: String,
        days: List<Int>,
        now: LocalDateTime = LocalDateTime.now()
    ) {
        cancel(userId, stageId)
        val deadline = DateUtils.parseIso(deadlineIso) ?: return
        days.forEach { d ->
            val trigger = deadline.minusDays(d.toLong()).atTime(9, 0)
            val wait = Duration.between(now, trigger)
            if (wait.isNegative || wait.isZero) return@forEach
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(wait)
                .setInputData(
                    workDataOf(
                        ReminderWorker.KEY_USER to userId,
                        ReminderWorker.KEY_STAGE to stageId,
                        ReminderWorker.KEY_TITLE to title,
                        ReminderWorker.KEY_DAYS to d
                    )
                )
                .addTag(tag(userId, stageId))
                .build()
            workManager.enqueueUniqueWork("${tag(userId, stageId)}_d$d", ExistingWorkPolicy.REPLACE, request)
        }
    }
}
