package cl.esucar.tutorvirtual.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.esucar.tutorvirtual.di.LocalContainer
import cl.esucar.tutorvirtual.ui.auth.LoginScreen
import cl.esucar.tutorvirtual.ui.auth.RegisterScreen
import cl.esucar.tutorvirtual.ui.checklist.ChecklistScreen
import cl.esucar.tutorvirtual.ui.faq.FaqScreen
import cl.esucar.tutorvirtual.ui.settings.SettingsScreen
import cl.esucar.tutorvirtual.ui.stages.EditMilestoneScreen
import cl.esucar.tutorvirtual.ui.stages.GuideScreen
import cl.esucar.tutorvirtual.ui.stages.StageDetailScreen
import cl.esucar.tutorvirtual.ui.stages.StagesScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val STAGES = "stages"
    const val FAQ = "faq"
    const val CHECKLIST = "checklist"
    const val SETTINGS = "settings"
    const val DETAIL = "stage/{stageId}"
    const val GUIDE = "guide/{stageId}"
    const val EDIT = "edit/{stageId}"

    fun detail(id: Int) = "stage/$id"
    fun guide(id: Int) = "guide/$id"
    fun edit(id: Int) = "edit/$id"
}

private data class TopDest(val route: String, val label: String, val icon: ImageVector)

private val topDestinations = listOf(
    TopDest(Routes.STAGES, "Etapas", Icons.AutoMirrored.Filled.MenuBook),
    TopDest(Routes.FAQ, "Preguntas", Icons.AutoMirrored.Filled.HelpOutline),
    TopDest(Routes.CHECKLIST, "Checklist", Icons.Filled.Checklist),
    TopDest(Routes.SETTINGS, "Ajustes", Icons.Filled.Settings)
)

/** Raíz: según la sesión muestra el flujo de autenticación o la app principal. */
@Composable
fun AppRoot(widthSizeClass: WindowWidthSizeClass) {
    val container = LocalContainer.current
    // -1 = cargando, 0 = sin sesión, >0 = usuario autenticado
    val session by container.settings.sessionUserId.collectAsStateWithLifecycle(initialValue = -1L)
    val state = when {
        session < 0L -> 0
        session == 0L -> 1
        else -> 2
    }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Crossfade(targetState = state, animationSpec = tween(350), label = "root") { s ->
            when (s) {
                0 -> Unit
                1 -> AuthNavHost()
                else -> MainScaffold(widthSizeClass)
            }
        }
    }
}

@Composable
private fun AuthNavHost() {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = Routes.LOGIN,
        enterTransition = { fadeIn(tween(250)) + slideInHorizontally(tween(250)) { it / 10 } },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(250)) },
        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(250)) { it / 10 } }
    ) {
        composable(Routes.LOGIN) { LoginScreen(onGoRegister = { nav.navigate(Routes.REGISTER) }) }
        composable(Routes.REGISTER) { RegisterScreen(onBack = { nav.popBackStack() }) }
    }
}

private fun NavHostController.navigateTop(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun MainScaffold(widthSizeClass: WindowWidthSizeClass) {
    val nav = rememberNavController()
    val backEntry by nav.currentBackStackEntryAsState()
    val route = backEntry?.destination?.route
    val showNav = route in topDestinations.map { it.route }
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
                    topDestinations.forEach { d ->
                        NavigationBarItem(
                            selected = route == d.route,
                            onClick = { nav.navigateTop(d.route) },
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
                    topDestinations.forEach { d ->
                        NavigationRailItem(
                            selected = route == d.route,
                            onClick = { nav.navigateTop(d.route) },
                            icon = { Icon(d.icon, contentDescription = null) },
                            label = { Text(d.label) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
            NavHost(
                navController = nav,
                startDestination = Routes.STAGES,
                modifier = Modifier.weight(1f),
                enterTransition = { fadeIn(tween(250)) + slideInHorizontally(tween(250)) { it / 10 } },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(250)) },
                popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(250)) { it / 10 } }
            ) {
                val stageArg = listOf(navArgument("stageId") { type = NavType.IntType })
                composable(Routes.STAGES) { StagesScreen(onOpen = { nav.navigate(Routes.detail(it)) }) }
                composable(Routes.FAQ) { FaqScreen() }
                composable(Routes.CHECKLIST) { ChecklistScreen(onOpen = { nav.navigate(Routes.detail(it)) }) }
                composable(Routes.SETTINGS) { SettingsScreen() }
                composable(Routes.DETAIL, arguments = stageArg) { entry ->
                    val id = entry.arguments?.getInt("stageId") ?: 0
                    StageDetailScreen(
                        onBack = { nav.popBackStack() },
                        onGuide = { nav.navigate(Routes.guide(id)) },
                        onEdit = { nav.navigate(Routes.edit(id)) }
                    )
                }
                composable(Routes.GUIDE, arguments = stageArg) { GuideScreen(onBack = { nav.popBackStack() }) }
                composable(Routes.EDIT, arguments = stageArg) { EditMilestoneScreen(onBack = { nav.popBackStack() }) }
            }
        }
    }
}
