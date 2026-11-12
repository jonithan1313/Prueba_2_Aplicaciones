package cl.esucar.tutorvirtual.ui.faq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.esucar.tutorvirtual.data.local.FaqEntity
import cl.esucar.tutorvirtual.data.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.Normalizer

data class FaqUi(
    val categories: List<String> = emptyList(),
    val selected: String? = null,
    val grouped: Map<String, List<FaqEntity>> = emptyMap(),
    val total: Int = 0
)

private val accents = Regex("\\p{Mn}+")

/** Minúsculas y sin tildes, para búsquedas tolerantes (HU-05). */
fun String.normalized(): String =
    accents.replace(Normalizer.normalize(lowercase(), Normalizer.Form.NFD), "")

class FaqViewModel(content: ContentRepository) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val _category = MutableStateFlow<String?>(null)

    val ui: StateFlow<FaqUi> = combine(content.faqs(), _query, _category) { faqs, q, cat ->
        val tokens = q.normalized().split(" ").filter { it.isNotBlank() }
        val filtered = faqs.filter { f ->
            (cat == null || f.category == cat) && tokens.all { t ->
                f.question.normalized().contains(t) || f.answer.normalized().contains(t)
            }
        }
        FaqUi(
            categories = faqs.map { it.category }.distinct(),
            selected = cat,
            grouped = filtered.groupBy { it.category },
            total = filtered.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FaqUi())

    fun onQuery(v: String) { _query.value = v }
    fun onCategory(c: String?) { _category.value = if (_category.value == c) null else c }
}
