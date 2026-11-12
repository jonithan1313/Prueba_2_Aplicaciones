package cl.esucar.tutorvirtual.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    /** [cl.esucar.tutorvirtual.domain.UserRole].name */
    val role: String = "ESTUDIANTE"
)

/** Etapa / hito de la tesis. [rubric] guarda los criterios separados por salto de línea. */
@Entity(tableName = "stages")
data class StageEntity(
    @PrimaryKey val id: Int,
    val position: Int,
    val title: String,
    val description: String,
    val format: String,
    val deadline: String, // ISO yyyy-MM-dd
    val rubric: String
)

@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity = StageEntity::class,
            parentColumns = ["id"],
            childColumns = ["stageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("stageId")]
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stageId: Int,
    val position: Int,
    val title: String,
    val content: String
)

@Entity(tableName = "faqs")
data class FaqEntity(
    @PrimaryKey val id: Int,
    val category: String,
    val question: String,
    val answer: String
)

/** Avance del usuario en cada etapa (estado, notas, fecha de entrega y foto de evidencia). */
@Entity(tableName = "progress", primaryKeys = ["userId", "stageId"])
data class ProgressEntity(
    val userId: Long,
    val stageId: Int,
    val status: String = "POR_INICIAR",
    val notes: String = "",
    val sentDate: String = "",
    val evidencePath: String = "",
    val updatedAt: Long = 0,
    /** Observaciones del profesor guía al revisar el hito. */
    val feedback: String = "",
    /** Momento de la última revisión del profesor (0 = sin revisar). */
    val reviewedAt: Long = 0
)

/** Preferencia de recordatorio por hito. [daysBefore] ej. "3,1". */
@Entity(tableName = "reminders", primaryKeys = ["userId", "stageId"])
data class ReminderEntity(
    val userId: Long,
    val stageId: Int,
    val enabled: Boolean = false,
    val daysBefore: String = "3,1"
)
