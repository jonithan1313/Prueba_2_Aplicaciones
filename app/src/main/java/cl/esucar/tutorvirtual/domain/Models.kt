package cl.esucar.tutorvirtual.domain

import cl.esucar.tutorvirtual.data.local.ProgressEntity
import cl.esucar.tutorvirtual.data.local.StageEntity
import java.time.LocalDate

/** Estados posibles de un entregable (caso ESUCAR, sección 3.2). */
enum class DeliverableStatus(val label: String) {
    POR_INICIAR("Por iniciar"),
    EN_DESARROLLO("En desarrollo"),
    ENVIADO("Enviado"),
    APROBADO("Aprobado");

    val isDone: Boolean get() = this == ENVIADO || this == APROBADO

    /** "Aprobado" solo lo asigna el profesor guía; el estudiante no puede elegirlo. */
    val studentSelectable: Boolean get() = this != APROBADO

    companion object {
        fun from(name: String?): DeliverableStatus =
            entries.firstOrNull { it.name == name } ?: POR_INICIAR
    }
}

/** Etapa + estado del usuario + días restantes, lista para pintar en la UI. */
data class StageItem(
    val stage: StageEntity,
    val status: DeliverableStatus,
    val daysLeft: Long?
) {
    val isOverdue: Boolean get() = !status.isDone && daysLeft != null && daysLeft < 0
    val isDueSoon: Boolean get() = !status.isDone && daysLeft != null && daysLeft in 0..3
}

fun buildItems(
    stages: List<StageEntity>,
    progress: List<ProgressEntity>,
    today: LocalDate = LocalDate.now()
): List<StageItem> {
    val byStage = progress.associateBy { it.stageId }
    return stages.map {
        StageItem(it, DeliverableStatus.from(byStage[it.id]?.status), DateUtils.daysUntil(it.deadline, today))
    }
}
