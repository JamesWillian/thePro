package app.jammes.thepro.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.jammes.thepro.presentation.ui.exercise.ExercisesScreen
import app.jammes.thepro.presentation.ui.history.HistoryScreen
import app.jammes.thepro.presentation.ui.theme.AccentSoft
import app.jammes.thepro.presentation.ui.today.TodayScreen
import app.jammes.thepro.presentation.ui.week.WeekScreen
import app.jammes.thepro.presentation.ui.workout.WorkoutEditScreen
import app.jammes.thepro.presentation.ui.workout.WorkoutsScreen

private data class BottomItem(
    val route: String,
    val label: String,
    val iconActive: ImageVector,
    val iconInactive: ImageVector
)

private val bottomItems = listOf(
    BottomItem(Routes.TODAY, "Hoje", Icons.Filled.Today, Icons.Outlined.Today),
    BottomItem(Routes.WEEK, "Semana", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    BottomItem(Routes.WORKOUTS_LIST, "Treinos", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    BottomItem(Routes.EXERCISES, "Exercícios", Icons.Filled.Bolt, Icons.Outlined.Bolt),
    BottomItem(Routes.HISTORY, "Progresso", Icons.Filled.QueryStats, Icons.Outlined.QueryStats)
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
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
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
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.iconActive else item.iconInactive,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = AccentSoft,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
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
            composable(Routes.WEEK) { WeekScreen() }
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
            composable(Routes.EXERCISES) { ExercisesScreen() }
            composable(Routes.HISTORY) { HistoryScreen() }
        }
    }
}
