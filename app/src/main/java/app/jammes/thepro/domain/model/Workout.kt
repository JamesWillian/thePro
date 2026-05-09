package app.jammes.thepro.domain.model

data class Workout(
    val id: Long = 0L,
    val name: String,
    val description: String = "",
    val exercises: List<WorkoutExercise> = emptyList()
)

data class WorkoutExercise(
    val id: Long = 0L,
    val workoutId: Long = 0L,
    val exercise: Exercise,
    val sets: Int,
    val reps: Int?,
    val suggestedWeightKg: Double?,
    val durationSeconds: Int?,
    val orderIndex: Int = 0
)
