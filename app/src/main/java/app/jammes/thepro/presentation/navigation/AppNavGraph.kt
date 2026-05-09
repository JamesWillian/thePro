package app.jammes.thepro.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.jammes.thepro.presentation.ui.exercise.ExercisesScreen
import app.jammes.thepro.presentation.ui.history.HistoryScreen
import app.jammes.thepro.presentation.ui.today.TodayScreen
import app.jammes.thepro.presentation.ui.week.WeekScreen
import app.jammes.thepro.presentation.ui.workout.WorkoutEditScreen
import app.jammes.thepro.presentation.ui.workout.WorkoutsScreen

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

private val bottomItems = listOf(
    BottomItem(Routes.TODAY, "Hoje", Icons.Filled.Home),
    BottomItem(Routes.WEEK, "Semana", Icons.Filled.CalendarMonth),
    BottomItem(Routes.WORKOUTS_LIST, "Treinos", Icons.Filled.FitnessCenter),
    BottomItem(Routes.EXERCISES, "Exercícios", Icons.Filled.ListAlt),
    BottomItem(Routes.HISTORY, "Histórico", Icons.Filled.History)
)

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = bottomItems.any { it.route == currentRoute } || currentRoute == null

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TODAY,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.TODAY) {
                TodayScreen(
                    onAddWorkout = { navController.navigate(Routes.WEEK) },
                    onCreateWorkout = { navController.navigate(Routes.workoutEdit()) }
                )
            }
            composable(Routes.WEEK) {
                WeekScreen()
            }
            composable(Routes.WORKOUTS_LIST) {
                WorkoutsScreen(
                    onCreate = { navController.navigate(Routes.workoutEdit()) },
                    onEdit = { id -> navController.navigate(Routes.workoutEdit(id)) }
                )
            }
            composable(
                route = Routes.WORKOUT_EDIT,
                arguments = listOf(
                    navArgument("workoutId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { entry ->
                val id = entry.arguments?.getLong("workoutId") ?: -1L
                WorkoutEditScreen(
                    workoutId = id.takeIf { it > 0 },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.EXERCISES) {
                ExercisesScreen()
            }
            composable(Routes.HISTORY) {
                HistoryScreen()
            }
        }
    }
}
