package cl.esucar.tutorvirtual.data.repository

import androidx.room.withTransaction
import cl.esucar.tutorvirtual.data.local.AppDatabase
import cl.esucar.tutorvirtual.data.local.FaqEntity
import cl.esucar.tutorvirtual.data.local.ProgressEntity
import cl.esucar.tutorvirtual.data.local.StageEntity
import cl.esucar.tutorvirtual.data.local.StepEntity
import cl.esucar.tutorvirtual.data.local.UserEntity
import cl.esucar.tutorvirtual.domain.DeliverableStatus
import cl.esucar.tutorvirtual.domain.UserRole
import cl.esucar.tutorvirtual.domain.Validators
import cl.esucar.tutorvirtual.notifications.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

/** Datos que el profesor ingresa para crear (id = null) o editar una etapa. [deadlineIso] ya validada. */
data class StageDraft(
    val id: Int?,
    val title: String,
    val description: String,
    val format: String,
    val deadlineIso: String,
    val rubric: String
)

/**
 * Operaciones del perfil profesor guía: definir la estructura (etapas, pasos, pauta, FAQ),
 * ver el avance de los estudiantes y aprobar / devolver hitos con observaciones.
 */
class TeacherRepository(
    private val db: AppDatabase,
    private val scheduler: ReminderScheduler
) {
    private val content = db.contentDao()
    private val progress = db.progressDao()
    private val reminders = db.reminderDao()
    private val users = db.userDao()

    // ------------------------------------------------------------------ Lectura
    fun students(): Flow<List<UserEntity>> = users.observeByRole(UserRole.ESTUDIANTE.name)
    fun student(id: Long): Flow<UserEntity?> = users.observeById(id)
    fun allProgress(): Flow<List<ProgressEntity>> = progress.observeEvery()
    fun progressOf(userId: Long): Flow<List<ProgressEntity>> = progress.observeAll(userId)
    fun stages(): Flow<List<StageEntity>> = content.observeStages()
    fun stage(id: Int): Flow<StageEntity?> = content.observeStage(id)
    fun steps(stageId: Int): Flow<List<StepEntity>> = content.observeSteps(stageId)
    fun faqs(): Flow<List<FaqEntity>> = content.observeFaqs()

    suspend fun getStage(id: Int): StageEntity? = content.getStage(id)
    suspend fun getStudent(id: Long): UserEntity? = users.findById(id)
    suspend fun getProgress(userId: Long, stageId: Int): ProgressEntity? = progress.get(userId, stageId)

    // ------------------------------------------------------------------ Revisión de hitos
    /**
     * Aprueba ([DeliverableStatus.APROBADO]) o devuelve ([DeliverableStatus.EN_DESARROLLO]) un entregable.
     * Solo se puede revisar un hito que el estudiante ya envió (o reabrir uno aprobado).
     * @return false si no había nada que revisar.
     */
    suspend fun review(userId: Long, stageId: Int, decision: DeliverableStatus, feedback: String): Boolean {
        val current = progress.get(userId, stageId) ?: return false
        val status = DeliverableStatus.from(current.status)
        if (status != DeliverableStatus.ENVIADO && status != DeliverableStatus.APROBADO) return false
        val now = System.currentTimeMillis()
        progress.upsert(current.copy(status = decision.name, feedback = feedback, reviewedAt = now, updatedAt = now))
        return true
    }

    // ------------------------------------------------------------------ Etapas
    suspend fun saveStage(draft: StageDraft): Int {
        val id = db.withTransaction {
            if (draft.id == null) {
                val newId = content.maxStageId() + 1
                content.insertStages(
                    listOf(
                        StageEntity(
                            id = newId, position = content.maxStagePosition() + 1,
                            title = draft.title, description = draft.description, format = draft.format,
                            deadline = draft.deadlineIso, rubric = draft.rubric
                        )
                    )
                )
                newId
            } else {
                val old = content.getStage(draft.id) ?: return@withTransaction draft.id
                content.updateStage(
                    old.copy(
                        title = draft.title, description = draft.description, format = draft.format,
                        deadline = draft.deadlineIso, rubric = draft.rubric
                    )
                )
                draft.id
            }
        }
        // Si cambió la fecha límite, los recordatorios ya programados por los estudiantes quedarían desfasados.
        if (draft.id != null) rescheduleReminders(id)
        return id
    }

    private suspend fun rescheduleReminders(stageId: Int) {
        val stage = content.getStage(stageId) ?: return
        reminders.forStage(stageId).filter { it.enabled }.forEach { r ->
            scheduler.schedule(r.userId, stageId, stage.title, stage.deadline, Validators.parseReminderDays(r.daysBefore))
        }
    }

    suspend fun deleteStage(id: Int) {
        // Se cancelan los recordatorios y se borran las fotos de evidencia de esa etapa antes de eliminarla.
        reminders.forStage(id).forEach { scheduler.cancel(it.userId, id) }
        val evidence = progress.forStage(id).map { it.evidencePath }.filter { it.isNotBlank() }
        db.withTransaction {
            reminders.deleteForStage(id)
            progress.deleteForStage(id)
            content.deleteStage(id) // los pasos se eliminan en cascada
            content.allStages().forEachIndexed { i, s ->
                if (s.position != i + 1) content.setStagePosition(s.id, i + 1)
            }
        }
        withContext(Dispatchers.IO) { evidence.forEach { File(it).delete() } }
    }

    /** Sube (delta = -1) o baja (delta = +1) una etapa en el orden de la guía. */
    suspend fun moveStage(id: Int, delta: Int) {
        db.withTransaction {
            val list = content.allStages()
            val i = list.indexOfFirst { it.id == id }
            val j = i + delta
            if (i >= 0 && j in list.indices) {
                content.setStagePosition(list[i].id, list[j].position)
                content.setStagePosition(list[j].id, list[i].position)
            }
        }
    }

    // ------------------------------------------------------------------ Pasos de la guía
    suspend fun saveStep(id: Long?, stageId: Int, title: String, text: String) {
        db.withTransaction {
            val steps = content.getSteps(stageId)
            if (id == null) {
                val next = (steps.maxOfOrNull { it.position } ?: 0) + 1
                content.insertStep(StepEntity(stageId = stageId, position = next, title = title, content = text))
            } else {
                steps.firstOrNull { it.id == id }?.let { content.updateStep(it.copy(title = title, content = text)) }
            }
        }
    }

    suspend fun deleteStep(stageId: Int, id: Long) {
        db.withTransaction {
            content.deleteStep(id)
            content.getSteps(stageId).forEachIndexed { i, s ->
                if (s.position != i + 1) content.setStepPosition(s.id, i + 1)
            }
        }
    }

    suspend fun moveStep(stageId: Int, id: Long, delta: Int) {
        db.withTransaction {
            val list = content.getSteps(stageId)
            val i = list.indexOfFirst { it.id == id }
            val j = i + delta
            if (i >= 0 && j in list.indices) {
                content.setStepPosition(list[i].id, list[j].position)
                content.setStepPosition(list[j].id, list[i].position)
            }
        }
    }

    // ------------------------------------------------------------------ Preguntas frecuentes
    suspend fun saveFaq(id: Int?, category: String, question: String, answer: String) {
        db.withTransaction {
            if (id == null) {
                content.insertFaqs(listOf(FaqEntity(content.maxFaqId() + 1, category, question, answer)))
            } else {
                content.updateFaq(FaqEntity(id, category, question, answer))
            }
        }
    }

    suspend fun deleteFaq(id: Int) = content.deleteFaq(id)
}
