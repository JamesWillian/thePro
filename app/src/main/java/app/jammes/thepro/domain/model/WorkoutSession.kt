package app.jammes.thepro.domain.model

import java.time.LocalDate

enum class SessionStatus {
    PENDENTE,
    EM_ANDAMENTO,
    CONCLUIDO,
    REMARCADO
}

data class WorkoutSession(
    val id: Long = 0L,
    val date: LocalDate,
    val workout: Workout,
    val status: SessionStatus,
    val originalDate: LocalDate? = null,
    val notes: String = "",
    val exerciseLogs: List<ExerciseLog> = emptyList()
)

data class ExerciseLog(
    val id: Long = 0L,
    val sessionId: Long = 0L,
    val workoutExerciseId: Long,
    val exercise: Exercise,
    val performed: Boolean,
    val actualSets: Int?,
    val actualReps: Int?,
    val actualWeightKg: Double?,
    val actualDurationSeconds: Int?,
    val notes: String = ""
)
