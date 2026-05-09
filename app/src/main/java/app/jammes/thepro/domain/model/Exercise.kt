package app.jammes.thepro.domain.model

data class Exercise(
    val id: Long = 0L,
    val name: String,
    val description: String,
    val type: ExerciseType
)
