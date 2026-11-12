package cl.esucar.tutorvirtual.data.repository

import cl.esucar.tutorvirtual.data.local.ProgressDao
import cl.esucar.tutorvirtual.data.local.ProgressEntity
import cl.esucar.tutorvirtual.data.local.ReminderDao
import cl.esucar.tutorvirtual.data.local.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ProgressRepository(
    private val progressDao: ProgressDao,
    private val reminderDao: ReminderDao
) {
    fun observeAll(userId: Long): Flow<List<ProgressEntity>> = progressDao.observeAll(userId)
    fun observe(userId: Long, stageId: Int): Flow<ProgressEntity?> = progressDao.observe(userId, stageId)
    suspend fun get(userId: Long, stageId: Int): ProgressEntity? = progressDao.get(userId, stageId)
    suspend fun save(progress: ProgressEntity) = progressDao.upsert(progress)

    fun observeReminders(userId: Long): Flow<List<ReminderEntity>> = reminderDao.observeAll(userId)
    suspend fun getReminder(userId: Long, stageId: Int): ReminderEntity? = reminderDao.get(userId, stageId)
    suspend fun saveReminder(reminder: ReminderEntity) = reminderDao.upsert(reminder)
}
