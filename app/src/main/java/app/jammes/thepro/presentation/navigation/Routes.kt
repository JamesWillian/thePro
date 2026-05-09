package app.jammes.thepro.presentation.navigation

object Routes {
    const val TODAY = "today"
    const val WEEK = "week"
    const val WORKOUTS_LIST = "workouts"
    const val WORKOUT_EDIT = "workouts/edit?workoutId={workoutId}"
    const val EXERCISES = "exercises"
    const val HISTORY = "history"

    fun workoutEdit(workoutId: Long? = null): String =
        "workouts/edit?workoutId=${workoutId ?: -1L}"
}
