package cl.esucar.tutorvirtual.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.esucar.tutorvirtual.ui.teacher.TeacherFaqScreen
import cl.esucar.tutorvirtual.ui.teacher.TeacherProfileScreen
import cl.esucar.tutorvirtual.ui.teacher.TeacherReviewScreen
import cl.esucar.tutorvirtual.ui.teacher.TeacherStageEditScreen
import cl.esucar.tutorvirtual.ui.teacher.TeacherStagesScreen
import cl.esucar.tutorvirtual.ui.teacher.TeacherStepsScreen
import cl.esucar.tutorvirtual.ui.teacher.TeacherStudentDetailScreen
import cl.esucar.tutorvirtual.ui.teacher.TeacherStudentsScreen

object TeacherRoutes {
    const val STUDENTS = "t_students"
    const val STAGES = "t_stages"
    const val FAQ = "t_faq"
    const val PROFILE = "t_profile"
    const val STUDENT = "t_student/{studentId}"
    const val REVIEW = "t_review/{studentId}/{stageId}"
    const val STAGE_EDIT = "t_stage_edit/{stageId}"
    const val STEPS = "t_steps/{stageId}"

    fun student(id: Long) = "t_student/$id"
    fun review(studentId: Long, stageId: Int) = "t_review/$studentId/$stageId"

    /** stageId = 0 abre el formulario de etapa nueva. */
    fun stageEdit(stageId: Int) = "t_stage_edit/$stageId"
    fun steps(stageId: Int) = "t_steps/$stageId"
}

private data class TeacherDest(val route: String, val label: String, val icon: ImageVector)

private val teacherDestinations = listOf(
    TeacherDest(TeacherRoutes.STUDENTS, "Estudiantes", Icons.Filled.Groups),
    TeacherDest(TeacherRoutes.STAGES, "Etapas", Icons.Filled.Dashboard),
    TeacherDest(TeacherRoutes.FAQ, "Preguntas", Icons.AutoMirrored.Filled.HelpOutline),
    TeacherDest(TeacherRoutes.PROFILE, "Perfil", Icons.Filled.Person)
)

private fun NavHostController.goTop(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** App principal del perfil profesor guía: supervisión de estudiantes y definición de la estructura. */
@Composable
fun TeacherScaffold(widthSizeClass: WindowWidthSizeClass) {
    val nav = rememberNavController()
    val backEntry by nav.currentBackStackEntryAsState()
    val route = backEntry?.destination?.route
    val showNav = route in teacherDestinations.map { it.route }
    val useRail = widthSizeClass != WindowWidthSizeClass.Compact // tablets / pantallas anchas

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = showNav && !useRail,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                NavigationBar {
                    teacherDestinations.forEach { d ->
                        NavigationBarItem(
                            selected = route == d.route,
                            onClick = { nav.goTop(d.route) },
                            icon = { Icon(d.icon, contentDescription = null) },
                            label = { Text(d.label) }
                        )
                    }
                }
            }
        }
    ) { padding: PaddingValues ->
        Row(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            if (showNav && useRail) {
                NavigationRail {
                    teacherDestinations.forEach { d ->
                        NavigationRailItem(
                            selected = route == d.route,
                            onClick = { nav.goTop(d.route) },
                            icon = { Icon(d.icon, contentDescription = null) },
                            label = { Text(d.label) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
            NavHost(
                navController = nav,
                startDestination = TeacherRoutes.STUDENTS,
                modifier = Modifier.weight(1f),
                enterTransition = { fadeIn(tween(250)) + slideInHorizontally(tween(250)) { it / 10 } },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(250)) },
                popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(250)) { it / 10 } }
            ) {
                val studentArg = navArgument("studentId") { type = NavType.LongType }
                val stageArg = navArgument("stageId") { type = NavType.IntType }

                composable(TeacherRoutes.STUDENTS) {
                    TeacherStudentsScreen(onOpen = { nav.navigate(TeacherRoutes.student(it)) })
                }
                composable(TeacherRoutes.STAGES) {
                    TeacherStagesScreen(onEdit = { nav.navigate(TeacherRoutes.stageEdit(it)) })
                }
                composable(TeacherRoutes.FAQ) { TeacherFaqScreen() }
                composable(TeacherRoutes.PROFILE) { TeacherProfileScreen() }

                composable(TeacherRoutes.STUDENT, arguments = listOf(studentArg)) { entry ->
                    val studentId = entry.arguments?.getLong("studentId") ?: 0L
                    TeacherStudentDetailScreen(
                        onBack = { nav.popBackStack() },
                        onReview = { stageId -> nav.navigate(TeacherRoutes.review(studentId, stageId)) }
                    )
                }
                composable(TeacherRoutes.REVIEW, arguments = listOf(studentArg, stageArg)) {
                    TeacherReviewScreen(onBack = { nav.popBackStack() })
                }
                composable(TeacherRoutes.STAGE_EDIT, arguments = listOf(stageArg)) { entry ->
                    val stageId = entry.arguments?.getInt("stageId") ?: 0
                    TeacherStageEditScreen(
                        onBack = { nav.popBackStack() },
                        onSteps = { nav.navigate(TeacherRoutes.steps(stageId)) }
                    )
                }
                composable(TeacherRoutes.STEPS, arguments = listOf(stageArg)) {
                    TeacherStepsScreen(onBack = { nav.popBackStack() })
                }
            }
        }
    }
}
