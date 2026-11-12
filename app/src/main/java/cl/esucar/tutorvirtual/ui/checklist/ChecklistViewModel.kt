package cl.esucar.tutorvirtual.ui.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.esucar.tutorvirtual.data.prefs.SettingsStore
import cl.esucar.tutorvirtual.data.repository.ContentRepository
import cl.esucar.tutorvirtual.data.repository.ProgressRepository
import cl.esucar.tutorvirtual.domain.StageItem
import cl.esucar.tutorvirtual.domain.buildItems
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class ChecklistUi(val items: List<StageItem> = emptyList()) {
    val done: Int get() = items.count { it.status.isDone }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ChecklistViewModel(
    content: ContentRepository,
    progress: ProgressRepository,
    settings: SettingsStore
) : ViewModel() {
    /** Hitos ordenados por fecha límite (HU-08). */
    val ui: StateFlow<ChecklistUi> = settings.sessionUserId
        .flatMapLatest { uid ->
            combine(content.stages(), progress.observeAll(uid)) { stages, prog ->
                ChecklistUi(buildItems(stages, prog).sortedBy { it.stage.deadline })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChecklistUi())
}
