package app.jammes.thepro.data.mapper

import app.jammes.thepro.data.local.dao.WorkoutExerciseRow
import app.jammes.thepro.data.local.entity.ExerciseEntity
import app.jammes.thepro.data.local.entity.ExerciseLogEntity
import app.jammes.thepro.data.local.entity.ScheduleEntryEntity
import app.jammes.thepro.data.local.entity.WorkoutEntity
import app.jammes.thepro.data.local.entity.WorkoutExerciseEntity
import app.jammes.thepro.data.local.entity.WorkoutSessionEntity
import app.jammes.thepro.domain.model.DayOfWeekBr
import app.jammes.thepro.domain.model.Exercise
import app.jammes.thepro.domain.model.ExerciseLog
import app.jammes.thepro.domain.model.ExerciseType
import app.jammes.thepro.domain.model.SessionStatus
import app.jammes.thepro.domain.model.WeeklyScheduleEntry
import app.jammes.thepro.domain.model.Workout
import app.jammes.thepro.domain.model.WorkoutExercise
import app.jammes.thepro.domain.model.WorkoutSession
import java.time.LocalDate

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    description = description,
    type = ExerciseType.fromString(type)
)

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    name = name.trim(),
    description = description.trim(),
    type = type.name
)

fun WorkoutEntity.toDomain(exercises: List<WorkoutExercise> = emptyList()): Workout = Workout(
    id = id,
    name = name,
    description = description,
    exercises = exercises
)

fun Workout.toEntity(): WorkoutEntity = WorkoutEntity(
    id = id,
    name = name.trim(),
    description = description.trim()
)

fun WorkoutExerciseRow.toDomain(): WorkoutExercise = WorkoutExercise(
    id = we_id,
    workoutId = we_workout_id,
    exercise = Exercise(
        id = e_id,
        name = e_name,
        description = e_description,
        type = ExerciseType.fromString(e_type)
    ),
    sets = we_sets,
    reps = we_reps,
    suggestedWeightKg = we_weight_kg,
    durationSeconds = we_duration_seconds,
    orderIndex = we_order_index
)

fun WorkoutExercise.toEntity(workoutId: Long): WorkoutExerciseEntity = WorkoutExerciseEntity(
    id = id,
    workoutId = workoutId,
    exerciseId = exercise.id,
    sets = sets,
    reps = reps,
    weightKg = suggestedWeightKg,
    durationSeconds = durationSeconds,
    orderIndex = orderIndex
)

fun ScheduleEntryEntity.toDomain(workout: Workout): WeeklyScheduleEntry = WeeklyScheduleEntry(
    id = id,
    dayOfWeek = DayOfWeekBr.fromIso(dayOfWeek),
    workout = workout,
    orderIndex = orderIndex
)

fun WorkoutSessionEntity.toDomain(workout: Workout, logs: List<ExerciseLog>): WorkoutSession =
    WorkoutSession(
        id = id,
        date = LocalDate.ofEpochDay(dateEpochDay),
        workout = workout,
        status = runCatching { SessionStatus.valueOf(status) }.getOrDefault(SessionStatus.PENDENTE),
        originalDate = originalDateEpochDay?.let(LocalDate::ofEpochDay),
        notes = notes,
        exerciseLogs = logs
    )

fun ExerciseLogEntity.toDomain(exercise: Exercise): ExerciseLog = ExerciseLog(
    id = id,
    sessionId = sessionId,
    workoutExerciseId = workoutExerciseId,
    exercise = exercise,
    performed = performed,
    actualSets = actualSets,
    actualReps = actualReps,
    actualWeightKg = actualWeightKg,
    actualDurationSeconds = actualDurationSeconds,
    notes = notes
)

fun ExerciseLog.toEntity(): ExerciseLogEntity = ExerciseLogEntity(
    id = id,
    sessionId = sessionId,
    workoutExerciseId = workoutExerciseId,
    performed = performed,
    actualSets = actualSets,
    actualReps = actualReps,
    actualWeightKg = actualWeightKg,
    actualDurationSeconds = actualDurationSeconds,
    notes = notes
)
