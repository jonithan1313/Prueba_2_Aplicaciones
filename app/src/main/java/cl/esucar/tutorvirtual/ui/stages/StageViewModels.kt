package cl.esucar.tutorvirtual.ui.stages

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.esucar.tutorvirtual.data.local.ProgressEntity
import cl.esucar.tutorvirtual.data.local.StageEntity
import cl.esucar.tutorvirtual.data.local.StepEntity
import cl.esucar.tutorvirtual.data.prefs.SettingsStore
import cl.esucar.tutorvirtual.data.repository.ContentRepository
import cl.esucar.tutorvirtual.data.repository.ProgressRepository
import cl.esucar.tutorvirtual.domain.DateUtils
import cl.esucar.tutorvirtual.domain.DeliverableStatus
import cl.esucar.tutorvirtual.domain.StageItem
import cl.esucar.tutorvirtual.domain.Validators
import cl.esucar.tutorvirtual.domain.buildItems
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

// ------------------------------------------------------------------ Listado de etapas (HU-01, HU-06)
data class StagesUi(val items: List<StageItem> = emptyList()) {
    val done: Int get() = items.count { it.status.isDone }
    val fraction: Float get() = if (items.isEmpty()) 0f else done.toFloat() / items.size
}

@OptIn(ExperimentalCoroutinesApi::class)
class StagesViewModel(
    content: ContentRepository,
    progress: ProgressRepository,
    settings: SettingsStore
) : ViewModel() {
    val ui: StateFlow<StagesUi> = settings.sessionUserId
        .flatMapLatest { uid ->
            combine(content.stages(), progress.observeAll(uid)) { stages, prog -> StagesUi(buildItems(stages, prog)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StagesUi())
}

// ------------------------------------------------------------------ Detalle de etapa (HU-02, HU-10)
data class StageDetailUi(
    val stage: StageEntity? = null,
    val stepCount: Int = 0,
    val progress: ProgressEntity? = null
) {
    val status: DeliverableStatus get() = DeliverableStatus.from(progress?.status)
    val daysLeft: Long? get() = stage?.let { DateUtils.daysUntil(it.deadline) }
    val rubric: List<String> get() = stage?.rubric?.split("\n")?.filter { it.isNotBlank() }.orEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class)
class StageDetailViewModel(
    saved: SavedStateHandle,
    content: ContentRepository,
    progress: ProgressRepository,
    settings: SettingsStore
) : ViewModel() {
    private val stageId: Int = checkNotNull(saved.get<Int>("stageId"))

    val ui: StateFlow<StageDetailUi> = settings.sessionUserId
        .flatMapLatest { uid ->
            combine(content.stage(stageId), content.steps(stageId), progress.observe(uid, stageId)) { s, steps, p ->
                StageDetailUi(s, steps.size, p)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StageDetailUi())
}

// ------------------------------------------------------------------ Guía paso a paso (HU-03)
class GuideViewModel(
    private val saved: SavedStateHandle,
    content: ContentRepository
) : ViewModel() {
    private val stageId: Int = checkNotNull(saved.get<Int>("stageId"))

    val stage: StateFlow<StageEntity?> = content.stage(stageId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val steps: StateFlow<List<StepEntity>> = content.steps(stageId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** El paso actual sobrevive a rotaciones y a la muerte del proceso. */
    val index: StateFlow<Int> = saved.getStateFlow("index", 0)

    fun next() {
        if (index.value < steps.value.size - 1) saved["index"] = index.value + 1
    }

    fun prev() {
        if (index.value > 0) saved["index"] = index.value - 1
    }
}

// ------------------------------------------------------------------ Formulario de actualización (HU-07)
data class EditUiState(
    val loaded: Boolean = false,
    val stageTitle: String = "",
    val status: DeliverableStatus = DeliverableStatus.POR_INICIAR,
    val sentDate: String = "",
    val notes: String = "",
    val evidencePath: String = "",
    val notesError: String? = null,
    val dateError: String? = null,
    val submitted: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false
)

class EditMilestoneViewModel(
    saved: SavedStateHandle,
    private val content: ContentRepository,
    private val progress: ProgressRepository,
    private val settings: SettingsStore
) : ViewModel() {
    val stageId: Int = checkNotNull(saved.get<Int>("stageId"))
    private var userId: Long = 0L
    private var originalEvidence: String = ""
    private var persisted = false
    // Fila de avance existente: se conserva para no perder las observaciones del profesor al guardar.
    private var existing: ProgressEntity? = null

    private val _ui = MutableStateFlow(EditUiState())
    val ui: StateFlow<EditUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            userId = settings.sessionUserId.first()
            val stage = content.getStage(stageId)
            val p = progress.get(userId, stageId)
            existing = p
            originalEvidence = p?.evidencePath.orEmpty()
            _ui.value = EditUiState(
                loaded = true,
                stageTitle = stage?.title.orEmpty(),
                status = DeliverableStatus.from(p?.status),
                sentDate = p?.sentDate.orEmpty(),
                notes = p?.notes.orEmpty(),
                evidencePath = originalEvidence
            ).validated()
        }
    }

    private fun EditUiState.validated() = copy(
        notesError = Validators.notes(notes, status),
        dateError = Validators.sentDate(sentDate, status)
    )

    fun onStatus(s: DeliverableStatus) = _ui.update {
        val prefill = if (s.isDone && it.sentDate.isBlank()) DateUtils.formatDisplay(LocalDate.now()) else it.sentDate
        it.copy(status = s, sentDate = prefill).validated()
    }

    fun onNotes(v: String) = _ui.update { it.copy(notes = v).validated() }
    fun onSentDate(v: String) = _ui.update { it.copy(sentDate = v).validated() }
    fun onEvidence(path: String) = _ui.update { it.copy(evidencePath = path) }

    fun evidenceFileName(): String = "ev_${userId}_${stageId}_${System.currentTimeMillis()}.jpg"

    fun save() {
        // Un hito aprobado por el profesor ya no puede modificarse ni reabrirse desde la cuenta del estudiante.
        if (DeliverableStatus.from(existing?.status) == DeliverableStatus.APROBADO) return
        val s = _ui.value.copy(submitted = true).validated()
        if (!s.loaded || s.notesError != null || s.dateError != null) {
            _ui.value = s
            return
        }
        viewModelScope.launch {
            _ui.value = s.copy(saving = true)
            val base = existing ?: ProgressEntity(userId = userId, stageId = stageId)
            progress.save(
                base.copy(
                    status = s.status.name,
                    notes = s.notes.trim(), sentDate = s.sentDate.trim(),
                    evidencePath = s.evidencePath, updatedAt = System.currentTimeMillis()
                )
            )
            if (originalEvidence.isNotEmpty() && originalEvidence != s.evidencePath) File(originalEvidence).delete()
            persisted = true
            _ui.value = _ui.value.copy(saving = false, saved = true)
        }
    }

    override fun onCleared() {
        // Si el usuario se va sin guardar, elimina la foto nueva para no dejar archivos huérfanos.
        val current = _ui.value.evidencePath
        if (!persisted && current.isNotEmpty() && current != originalEvidence) File(current).delete()
    }
}
