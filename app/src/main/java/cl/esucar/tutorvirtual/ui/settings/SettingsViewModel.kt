package cl.esucar.tutorvirtual.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.esucar.tutorvirtual.data.local.ReminderEntity
import cl.esucar.tutorvirtual.data.local.StageEntity
import cl.esucar.tutorvirtual.data.local.UserEntity
import cl.esucar.tutorvirtual.data.prefs.SettingsStore
import cl.esucar.tutorvirtual.data.repository.AuthRepository
import cl.esucar.tutorvirtual.data.repository.ContentRepository
import cl.esucar.tutorvirtual.data.repository.ProgressRepository
import cl.esucar.tutorvirtual.domain.DeliverableStatus
import cl.esucar.tutorvirtual.domain.Validators
import cl.esucar.tutorvirtual.notifications.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReminderRow(
    val stage: StageEntity,
    val enabled: Boolean,
    val days: String,
    val done: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
    private val settings: SettingsStore,
    private val auth: AuthRepository,
    private val scheduler: ReminderScheduler
) : ViewModel() {

    val user: StateFlow<UserEntity?> = settings.sessionUserId
        .map { auth.user(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val rows: StateFlow<List<ReminderRow>> = settings.sessionUserId
        .flatMapLatest { uid ->
            combine(content.stages(), progress.observeReminders(uid), progress.observeAll(uid)) { stages, reminders, prog ->
                val rByStage = reminders.associateBy { it.stageId }
                val pByStage = prog.associateBy { it.stageId }
                stages.map { st ->
                    ReminderRow(
                        stage = st,
                        enabled = rByStage[st.id]?.enabled ?: false,
                        days = rByStage[st.id]?.daysBefore ?: "3,1",
                        done = DeliverableStatus.from(pByStage[st.id]?.status).isDone
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** La UI delega aquí la validación de los días de anticipación. */
    fun validateDays(text: String): String? = Validators.reminderDays(text)

    fun setEnabled(stageId: Int, enabled: Boolean) = update(stageId) { it.copy(enabled = enabled) }

    fun saveDays(stageId: Int, text: String) {
        if (Validators.reminderDays(text) != null) return
        val normalized = Validators.parseReminderDays(text).joinToString(",")
        update(stageId) { it.copy(daysBefore = normalized) }
    }

    private fun update(stageId: Int, change: (ReminderEntity) -> ReminderEntity) {
        viewModelScope.launch {
            val uid = settings.sessionUserId.first()
            val stage = content.getStage(stageId) ?: return@launch
            val current = progress.getReminder(uid, stageId) ?: ReminderEntity(uid, stageId)
            val updated = change(current)
            progress.saveReminder(updated)
            if (updated.enabled) {
                scheduler.schedule(
                    uid, stageId, stage.title, stage.deadline, Validators.parseReminderDays(updated.daysBefore)
                )
            } else {
                scheduler.cancel(uid, stageId)
            }
        }
    }

    fun logout() {
        viewModelScope.launch { auth.logout() }
    }
}
