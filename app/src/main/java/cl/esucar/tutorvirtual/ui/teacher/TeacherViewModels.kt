package cl.esucar.tutorvirtual.ui.teacher

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.esucar.tutorvirtual.data.local.FaqEntity
import cl.esucar.tutorvirtual.data.local.ProgressEntity
import cl.esucar.tutorvirtual.data.local.StageEntity
import cl.esucar.tutorvirtual.data.local.StepEntity
import cl.esucar.tutorvirtual.data.local.UserEntity
import cl.esucar.tutorvirtual.data.repository.AuthRepository
import cl.esucar.tutorvirtual.data.repository.StageDraft
import cl.esucar.tutorvirtual.data.repository.TeacherRepository
import cl.esucar.tutorvirtual.domain.DateUtils
import cl.esucar.tutorvirtual.domain.DeliverableStatus
import cl.esucar.tutorvirtual.domain.StageItem
import cl.esucar.tutorvirtual.domain.Validators
import cl.esucar.tutorvirtual.domain.buildItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ================================================================== Estudiantes (supervisión)

data class StudentRow(
    val user: UserEntity,
    val approved: Int,
    val sent: Int,        // enviados, a la espera de revisión
    val inDevelopment: Int,
    val total: Int
) {
    val fraction: Float get() = if (total == 0) 0f else approved.toFloat() / total
}

data class StudentsUi(
    val loaded: Boolean = false,
    val rows: List<StudentRow> = emptyList(),
    val pending: Int = 0,
    val stageCount: Int = 0
)

class TeacherStudentsViewModel(repo: TeacherRepository) : ViewModel() {
    val ui: StateFlow<StudentsUi> = combine(repo.students(), repo.stages(), repo.allProgress()) { students, stages, progress ->
        val stageIds = stages.map { it.id }.toSet()
        val byUser = progress.filter { it.stageId in stageIds }.groupBy { it.userId }
        val rows = students.map { u ->
            val statuses = byUser[u.id].orEmpty().map { DeliverableStatus.from(it.status) }
            StudentRow(
                user = u,
                approved = statuses.count { it == DeliverableStatus.APROBADO },
                sent = statuses.count { it == DeliverableStatus.ENVIADO },
                inDevelopment = statuses.count { it == DeliverableStatus.EN_DESARROLLO },
                total = stages.size
            )
        }.sortedWith(compareByDescending<StudentRow> { it.sent }.thenBy { it.user.name.lowercase() })
        StudentsUi(loaded = true, rows = rows, pending = rows.sumOf { it.sent }, stageCount = stages.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StudentsUi())
}

data class StudentDetailUi(
    val loaded: Boolean = false,
    val student: UserEntity? = null,
    val items: List<StageItem> = emptyList()
) {
    val approved: Int get() = items.count { it.status == DeliverableStatus.APROBADO }
}

class TeacherStudentDetailViewModel(
    saved: SavedStateHandle,
    repo: TeacherRepository
) : ViewModel() {
    private val studentId: Long = checkNotNull(saved.get<Long>("studentId"))

    val ui: StateFlow<StudentDetailUi> = combine(
        repo.student(studentId), repo.stages(), repo.progressOf(studentId)
    ) { student, stages, progress ->
        StudentDetailUi(loaded = true, student = student, items = buildItems(stages, progress))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StudentDetailUi())
}

// ================================================================== Revisión de un hito

data class ReviewUi(
    val loaded: Boolean = false,
    val studentName: String = "",
    val stage: StageEntity? = null,
    val progress: ProgressEntity? = null,
    val feedback: String = "",
    val feedbackError: String? = null,
    /** true una vez que el profesor intentó devolver el hito: desde entonces las observaciones son obligatorias. */
    val strict: Boolean = false,
    val saving: Boolean = false,
    /** Decisión tomada en esta pantalla (para mostrar la confirmación). */
    val result: DeliverableStatus? = null
) {
    val status: DeliverableStatus get() = DeliverableStatus.from(progress?.status)
    val canReview: Boolean get() = status == DeliverableStatus.ENVIADO || status == DeliverableStatus.APROBADO
    val rubric: List<String> get() = stage?.rubric?.split("\n")?.filter { it.isNotBlank() }.orEmpty()
}

class TeacherReviewViewModel(
    saved: SavedStateHandle,
    private val repo: TeacherRepository
) : ViewModel() {
    private val studentId: Long = checkNotNull(saved.get<Long>("studentId"))
    private val stageId: Int = checkNotNull(saved.get<Int>("stageId"))

    private val _ui = MutableStateFlow(ReviewUi())
    val ui: StateFlow<ReviewUi> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            _ui.value = ReviewUi(
                loaded = true,
                studentName = repo.getStudent(studentId)?.name.orEmpty(),
                stage = repo.getStage(stageId),
                progress = repo.getProgress(studentId, stageId)
            )
        }
    }

    fun onFeedback(v: String) = _ui.update { it.copy(feedback = v, feedbackError = Validators.feedback(v, it.strict)) }

    fun approve() = decide(DeliverableStatus.APROBADO)
    fun returnForFixes() = decide(DeliverableStatus.EN_DESARROLLO)

    private fun decide(target: DeliverableStatus) {
        val s = _ui.value
        if (!s.canReview || s.saving || s.result != null) return
        val returning = target != DeliverableStatus.APROBADO
        val error = Validators.feedback(s.feedback, returning)
        if (error != null) {
            _ui.value = s.copy(feedbackError = error, strict = returning)
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(saving = true, feedbackError = null) }
            val ok = repo.review(studentId, stageId, target, s.feedback.trim())
            val latest = repo.getProgress(studentId, stageId)
            _ui.update { it.copy(saving = false, progress = latest, result = if (ok) target else null) }
        }
    }
}

// ================================================================== Estructura: etapas

class TeacherStagesViewModel(private val repo: TeacherRepository) : ViewModel() {
    val stages: StateFlow<List<StageEntity>> = repo.stages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun move(id: Int, delta: Int) {
        viewModelScope.launch { repo.moveStage(id, delta) }
    }
}

data class StageEditUi(
    val loaded: Boolean = false,
    val isNew: Boolean = true,
    val title: String = "",
    val description: String = "",
    val format: String = "",
    val deadline: String = "",
    val rubric: String = "",
    val titleError: String? = null,
    val descriptionError: String? = null,
    val formatError: String? = null,
    val deadlineError: String? = null,
    val rubricError: String? = null,
    val stepCount: Int = 0,
    val submitted: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false
) {
    val hasErrors: Boolean
        get() = listOf(titleError, descriptionError, formatError, deadlineError, rubricError).any { it != null }
}

/** stageId = 0 significa "etapa nueva". */
class TeacherStageEditViewModel(
    saved: SavedStateHandle,
    private val repo: TeacherRepository
) : ViewModel() {
    val stageId: Int = checkNotNull(saved.get<Int>("stageId"))

    private val _ui = MutableStateFlow(StageEditUi())
    val ui: StateFlow<StageEditUi> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            if (stageId == 0) {
                _ui.value = StageEditUi(loaded = true, isNew = true).validated()
            } else {
                val st = repo.getStage(stageId)
                if (st == null) {
                    _ui.value = StageEditUi(loaded = true, isNew = false, deleted = true)
                } else {
                    _ui.value = StageEditUi(
                        loaded = true, isNew = false,
                        title = st.title, description = st.description, format = st.format,
                        deadline = DateUtils.parseIso(st.deadline)?.let { DateUtils.formatDisplay(it) } ?: st.deadline,
                        rubric = st.rubric
                    ).validated()
                }
            }
        }
        if (stageId != 0) {
            viewModelScope.launch {
                repo.steps(stageId).collect { list -> _ui.update { it.copy(stepCount = list.size) } }
            }
        }
    }

    private fun StageEditUi.validated() = copy(
        titleError = Validators.stageTitle(title),
        descriptionError = Validators.stageDescription(description),
        formatError = Validators.stageFormat(format),
        deadlineError = Validators.deadline(deadline),
        rubricError = Validators.rubric(rubric)
    )

    fun onTitle(v: String) = _ui.update { it.copy(title = v).validated() }
    fun onDescription(v: String) = _ui.update { it.copy(description = v).validated() }
    fun onFormat(v: String) = _ui.update { it.copy(format = v).validated() }
    fun onDeadline(v: String) = _ui.update { it.copy(deadline = v).validated() }
    fun onRubric(v: String) = _ui.update { it.copy(rubric = v).validated() }

    fun save() {
        val s = _ui.value.copy(submitted = true).validated()
        if (!s.loaded || s.hasErrors) {
            _ui.value = s
            return
        }
        val iso = DateUtils.parseDisplay(s.deadline)?.toString() ?: return
        viewModelScope.launch {
            _ui.value = s.copy(saving = true)
            repo.saveStage(
                StageDraft(
                    id = if (stageId == 0) null else stageId,
                    title = s.title.trim(),
                    description = s.description.trim(),
                    format = s.format.trim(),
                    deadlineIso = iso,
                    rubric = Validators.parseRubric(s.rubric).joinToString("\n")
                )
            )
            _ui.update { it.copy(saving = false, saved = true) }
        }
    }

    fun delete() {
        if (stageId == 0) return
        viewModelScope.launch {
            repo.deleteStage(stageId)
            _ui.update { it.copy(deleted = true) }
        }
    }
}

// ================================================================== Estructura: pasos de la guía

class TeacherStepsViewModel(
    saved: SavedStateHandle,
    private val repo: TeacherRepository
) : ViewModel() {
    val stageId: Int = checkNotNull(saved.get<Int>("stageId"))

    val stage: StateFlow<StageEntity?> = repo.stage(stageId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val steps: StateFlow<List<StepEntity>> = repo.steps(stageId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun validateTitle(v: String): String? = Validators.stepTitle(v)
    fun validateContent(v: String): String? = Validators.stepContent(v)

    fun save(id: Long?, title: String, content: String) {
        if (Validators.stepTitle(title) != null || Validators.stepContent(content) != null) return
        viewModelScope.launch { repo.saveStep(id, stageId, title.trim(), content.trim()) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repo.deleteStep(stageId, id) }
    }

    fun move(id: Long, delta: Int) {
        viewModelScope.launch { repo.moveStep(stageId, id, delta) }
    }
}

// ================================================================== Preguntas frecuentes

class TeacherFaqViewModel(private val repo: TeacherRepository) : ViewModel() {
    val faqs: StateFlow<List<FaqEntity>> = repo.faqs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categories: StateFlow<List<String>> = repo.faqs().map { list -> list.map { it.category }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun validateCategory(v: String): String? = Validators.faqCategory(v)
    fun validateQuestion(v: String): String? = Validators.faqQuestion(v)
    fun validateAnswer(v: String): String? = Validators.faqAnswer(v)

    fun save(id: Int?, category: String, question: String, answer: String) {
        if (Validators.faqCategory(category) != null || Validators.faqQuestion(question) != null ||
            Validators.faqAnswer(answer) != null
        ) return
        viewModelScope.launch { repo.saveFaq(id, category.trim(), question.trim(), answer.trim()) }
    }

    fun delete(id: Int) {
        viewModelScope.launch { repo.deleteFaq(id) }
    }
}

// ================================================================== Perfil

class TeacherProfileViewModel(private val auth: AuthRepository) : ViewModel() {
    val user: StateFlow<UserEntity?> = auth.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun logout() {
        viewModelScope.launch { auth.logout() }
    }
}
