package cl.esucar.tutorvirtual.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE role = :role ORDER BY name")
    fun observeByRole(role: String): Flow<List<UserEntity>>
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

    // ---- Edición por el profesor guía.
    // OJO: para editar una etapa se usa @Update y NO insert(REPLACE): REPLACE borra la fila y,
    // por el ON DELETE CASCADE de "steps", eliminaría todos sus pasos.
    @Query("SELECT * FROM stages ORDER BY position")
    suspend fun allStages(): List<StageEntity>

    @Update
    suspend fun updateStage(stage: StageEntity)

    @Query("UPDATE stages SET position = :position WHERE id = :id")
    suspend fun setStagePosition(id: Int, position: Int)

    @Query("DELETE FROM stages WHERE id = :id")
    suspend fun deleteStage(id: Int)

    @Query("SELECT COALESCE(MAX(id), 0) FROM stages")
    suspend fun maxStageId(): Int

    @Query("SELECT COALESCE(MAX(position), 0) FROM stages")
    suspend fun maxStagePosition(): Int

    @Query("SELECT * FROM steps WHERE stageId = :stageId ORDER BY position")
    suspend fun getSteps(stageId: Int): List<StepEntity>

    @Insert
    suspend fun insertStep(step: StepEntity): Long

    @Update
    suspend fun updateStep(step: StepEntity)

    @Query("UPDATE steps SET position = :position WHERE id = :id")
    suspend fun setStepPosition(id: Long, position: Int)

    @Query("DELETE FROM steps WHERE id = :id")
    suspend fun deleteStep(id: Long)

    @Update
    suspend fun updateFaq(faq: FaqEntity)

    @Query("DELETE FROM faqs WHERE id = :id")
    suspend fun deleteFaq(id: Int)

    @Query("SELECT COALESCE(MAX(id), 0) FROM faqs")
    suspend fun maxFaqId(): Int
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

    // ---- Vista del profesor guía (todos los estudiantes).
    @Query("SELECT * FROM progress")
    fun observeEvery(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE stageId = :stageId")
    suspend fun forStage(stageId: Int): List<ProgressEntity>

    @Query("DELETE FROM progress WHERE stageId = :stageId")
    suspend fun deleteForStage(stageId: Int)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE userId = :userId")
    fun observeAll(userId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND stageId = :stageId")
    suspend fun get(userId: Long, stageId: Int): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE stageId = :stageId")
    suspend fun forStage(stageId: Int): List<ReminderEntity>

    @Query("DELETE FROM reminders WHERE stageId = :stageId")
    suspend fun deleteForStage(stageId: Int)
}
