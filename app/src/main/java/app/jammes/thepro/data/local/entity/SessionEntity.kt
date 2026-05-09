package app.jammes.thepro.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workout_id"), Index("date")]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "date") val dateEpochDay: Long,
    @ColumnInfo(name = "workout_id") val workoutId: Long,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "original_date") val originalDateEpochDay: Long?,
    @ColumnInfo(name = "notes") val notes: String
)

@Entity(
    tableName = "exercise_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workout_exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("session_id"), Index("workout_exercise_id")]
)
data class ExerciseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "workout_exercise_id") val workoutExerciseId: Long,
    @ColumnInfo(name = "performed") val performed: Boolean,
    @ColumnInfo(name = "actual_sets") val actualSets: Int?,
    @ColumnInfo(name = "actual_reps") val actualReps: Int?,
    @ColumnInfo(name = "actual_weight_kg") val actualWeightKg: Double?,
    @ColumnInfo(name = "actual_duration_seconds") val actualDurationSeconds: Int?,
    @ColumnInfo(name = "notes") val notes: String
)
