package cl.esucar.tutorvirtual.data.repository

import android.content.Context
import androidx.room.withTransaction
import cl.esucar.tutorvirtual.data.local.AppDatabase
import cl.esucar.tutorvirtual.data.local.FaqEntity
import cl.esucar.tutorvirtual.data.local.StageEntity
import cl.esucar.tutorvirtual.data.local.StepEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Contenido académico (etapas, guía, FAQ). Se siembra desde assets/content.json y se lee offline desde Room. */
class ContentRepository(
    private val context: Context,
    private val db: AppDatabase
) {
    private val dao = db.contentDao()

    fun stages(): Flow<List<StageEntity>> = dao.observeStages()
    fun stage(id: Int): Flow<StageEntity?> = dao.observeStage(id)
    fun steps(stageId: Int): Flow<List<StepEntity>> = dao.observeSteps(stageId)
    fun faqs(): Flow<List<FaqEntity>> = dao.observeFaqs()
    suspend fun getStage(id: Int): StageEntity? = dao.getStage(id)

    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        if (dao.countStages() > 0) return@withContext
        val text = context.assets.open("content.json").bufferedReader().use { it.readText() }
        val root = JSONObject(text)

        val stages = mutableListOf<StageEntity>()
        val steps = mutableListOf<StepEntity>()
        root.getJSONArray("stages").objects().forEach { o ->
            val id = o.getInt("id")
            stages += StageEntity(
                id = id,
                position = o.getInt("position"),
                title = o.getString("title"),
                description = o.getString("description"),
                format = o.getString("format"),
                deadline = o.getString("deadline"),
                rubric = o.getJSONArray("rubric").strings().joinToString("\n")
            )
            o.getJSONArray("steps").objects().forEachIndexed { i, s ->
                steps += StepEntity(
                    stageId = id, position = i + 1,
                    title = s.getString("title"), content = s.getString("content")
                )
            }
        }
        val faqs = root.getJSONArray("faqs").objects().mapIndexed { i, f ->
            FaqEntity(i + 1, f.getString("category"), f.getString("question"), f.getString("answer"))
        }

        db.withTransaction {
            dao.insertStages(stages)
            dao.insertSteps(steps)
            dao.insertFaqs(faqs)
        }
    }

    private fun JSONArray.objects(): List<JSONObject> = List(length()) { getJSONObject(it) }
    private fun JSONArray.strings(): List<String> = List(length()) { getString(it) }
}
