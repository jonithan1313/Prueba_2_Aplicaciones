package cl.esucar.tutorvirtual.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?
}

@Dao
interface ContentDao {
    @Query("SELECT * FROM stages ORDER BY position")
    fun observeStages(): Flow<List<StageEntity>>

    @Query("SELECT * FROM stages WHERE id = :id")
    fun observeStage(id: Int): Flow<StageEntity?>

    @Query("SELECT * FROM stages WHERE id = :id")
    suspend fun getStage(id: Int): StageEntity?

    @Query("SELECT * FROM steps WHERE stageId = :stageId ORDER BY position")
    fun observeSteps(stageId: Int): Flow<List<StepEntity>>

    @Query("SELECT * FROM faqs ORDER BY category, id")
    fun observeFaqs(): Flow<List<FaqEntity>>

    @Query("SELECT COUNT(*) FROM stages")
    suspend fun countStages(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStages(list: List<StageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(list: List<StepEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaqs(list: List<FaqEntity>)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress WHERE userId = :userId")
    fun observeAll(userId: Long): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE userId = :userId AND stageId = :stageId")
    fun observe(userId: Long, stageId: Int): Flow<ProgressEntity?>

    @Query("SELECT * FROM progress WHERE userId = :userId AND stageId = :stageId")
    suspend fun get(userId: Long, stageId: Int): ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: ProgressEntity)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE userId = :userId")
    fun observeAll(userId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND stageId = :stageId")
    suspend fun get(userId: Long, stageId: Int): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: ReminderEntity)
}
