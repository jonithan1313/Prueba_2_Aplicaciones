package cl.esucar.tutorvirtual.di

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.esucar.tutorvirtual.data.local.AppDatabase
import cl.esucar.tutorvirtual.data.prefs.SettingsStore
import cl.esucar.tutorvirtual.data.repository.AuthRepository
import cl.esucar.tutorvirtual.data.repository.ContentRepository
import cl.esucar.tutorvirtual.data.repository.ProgressRepository
import cl.esucar.tutorvirtual.data.repository.TeacherRepository
import cl.esucar.tutorvirtual.notifications.ReminderScheduler
import cl.esucar.tutorvirtual.ui.auth.AuthViewModel
import cl.esucar.tutorvirtual.ui.checklist.ChecklistViewModel
import cl.esucar.tutorvirtual.ui.faq.FaqViewModel
import cl.esucar.tutorvirtual.ui.settings.SettingsViewModel
import cl.esucar.tutorvirtual.ui.stages.EditMilestoneViewModel
import cl.esucar.tutorvirtual.ui.stages.GuideViewModel
import cl.esucar.tutorvirtual.ui.stages.StageDetailViewModel
import cl.esucar.tutorvirtual.ui.stages.StagesViewModel
import cl.esucar.tutorvirtual.ui.teacher.TeacherFaqViewModel
import cl.esucar.tutorvirtual.ui.teacher.TeacherProfileViewModel
import cl.esucar.tutorvirtual.ui.teacher.TeacherReviewViewModel
import cl.esucar.tutorvirtual.ui.teacher.TeacherStageEditViewModel
import cl.esucar.tutorvirtual.ui.teacher.TeacherStagesViewModel
import cl.esucar.tutorvirtual.ui.teacher.TeacherStepsViewModel
import cl.esucar.tutorvirtual.ui.teacher.TeacherStudentDetailViewModel
import cl.esucar.tutorvirtual.ui.teacher.TeacherStudentsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Contenedor de dependencias manual (suficiente para un MVP, sin frameworks de DI). */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val db = AppDatabase.get(appContext)
    val settings = SettingsStore(appContext)
    val authRepository = AuthRepository(db.userDao(), settings)
    val contentRepository = ContentRepository(appContext, db)
    val progressRepository = ProgressRepository(db.progressDao(), db.reminderDao())
    val reminderScheduler = ReminderScheduler(appContext)
    val teacherRepository = TeacherRepository(db, reminderScheduler)

    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { AuthViewModel(authRepository) }
        initializer { StagesViewModel(contentRepository, progressRepository, settings) }
        initializer { StageDetailViewModel(createSavedStateHandle(), contentRepository, progressRepository, settings) }
        initializer { GuideViewModel(createSavedStateHandle(), contentRepository) }
        initializer { EditMilestoneViewModel(createSavedStateHandle(), contentRepository, progressRepository, settings) }
        initializer { FaqViewModel(contentRepository) }
        initializer { ChecklistViewModel(contentRepository, progressRepository, settings) }
        initializer {
            SettingsViewModel(contentRepository, progressRepository, settings, authRepository, reminderScheduler)
        }
        // Perfil profesor guía
        initializer { TeacherStudentsViewModel(teacherRepository) }
        initializer { TeacherStudentDetailViewModel(createSavedStateHandle(), teacherRepository) }
        initializer { TeacherReviewViewModel(createSavedStateHandle(), teacherRepository) }
        initializer { TeacherStagesViewModel(teacherRepository) }
        initializer { TeacherStageEditViewModel(createSavedStateHandle(), teacherRepository) }
        initializer { TeacherStepsViewModel(createSavedStateHandle(), teacherRepository) }
        initializer { TeacherFaqViewModel(teacherRepository) }
        initializer { TeacherProfileViewModel(authRepository) }
    }
}

val LocalContainer = staticCompositionLocalOf<AppContainer> { error("AppContainer no provisto") }

@Composable
inline fun <reified VM : ViewModel> appViewModel(): VM =
    viewModel(factory = LocalContainer.current.viewModelFactory)
