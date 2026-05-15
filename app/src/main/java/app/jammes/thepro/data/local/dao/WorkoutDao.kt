package app.jammes.thepro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.jammes.thepro.data.local.entity.ExerciseEntity
import app.jammes.thepro.data.local.entity.WorkoutEntity
import app.jammes.thepro.data.local.entity.WorkoutExerciseEntity
import kotlinx.coroutines.flow.Flow

data class WorkoutExerciseJoin(
    val we: WorkoutExerciseEntity,
    val exercise: ExerciseEntity
)

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    fun observeById(id: Long): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getById(id: Long): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): WorkoutEntity?

    @Query("SELECT COUNT(*) FROM workouts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WorkoutEntity): Long

    @Update
    suspend fun update(entity: WorkoutEntity)

    @Delete
    suspend fun delete(entity: WorkoutEntity)

    @Query("DELETE FROM workout_exercises WHERE workout_id = :workoutId")
    suspend fun deleteExercisesOf(workoutId: Long)

    @Query("DELETE FROM workout_exercises WHERE id = :id")
    suspend fun deleteWorkoutExerciseById(id: Long)

    @Query("UPDATE workout_exercises SET active_in_workout = 0 WHERE id = :id")
    suspend fun markWorkoutExerciseInactive(id: Long)

    @Update
    suspend fun updateWorkoutExercise(entity: WorkoutExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercises(items: List<WorkoutExerciseEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(item: WorkoutExerciseEntity): Long

    @Query("SELECT * FROM workout_exercises WHERE workout_id = :workoutId AND active_in_workout = 1 ORDER BY order_index ASC")
    suspend fun getActiveWorkoutExerciseEntities(workoutId: Long): List<WorkoutExerciseEntity>

    @Transaction
    @Query(
        """
        SELECT we.id AS we_id, we.workout_id AS we_workout_id, we.exercise_id AS we_exercise_id,
               we.sets AS we_sets, we.reps AS we_reps, we.weight_kg AS we_weight_kg,
               we.duration_seconds AS we_duration_seconds, we.order_index AS we_order_index,
               e.id AS e_id, e.name AS e_name, e.description AS e_description, e.type AS e_type
        FROM workout_exercises we
        INNER JOIN exercises e ON e.id = we.exercise_id
        WHERE we.workout_id = :workoutId AND we.active_in_workout = 1
        ORDER BY we.order_index ASC
        """
    )
    fun observeWorkoutExercises(workoutId: Long): Flow<List<WorkoutExerciseRow>>

    @Transaction
    @Query(
        """
        SELECT we.id AS we_id, we.workout_id AS we_workout_id, we.exercise_id AS we_exercise_id,
               we.sets AS we_sets, we.reps AS we_reps, we.weight_kg AS we_weight_kg,
               we.duration_seconds AS we_duration_seconds, we.order_index AS we_order_index,
               e.id AS e_id, e.name AS e_name, e.description AS e_description, e.type AS e_type
        FROM workout_exercises we
        INNER JOIN exercises e ON e.id = we.exercise_id
        WHERE we.workout_id = :workoutId
          AND (we.active_in_workout = 1
               OR we.id IN (SELECT workout_exercise_id FROM exercise_logs WHERE session_id = :sessionId))
        ORDER BY we.order_index ASC, we.id ASC
        """
    )
    fun observeWorkoutExercisesForSession(workoutId: Long, sessionId: Long): Flow<List<WorkoutExerciseRow>>

    @Query("SELECT * FROM workout_exercises WHERE id = :id")
    suspend fun getWorkoutExerciseById(id: Long): WorkoutExerciseEntity?

    @Query("SELECT id FROM workout_exercises WHERE workout_id = :workoutId AND active_in_workout = 1")
    suspend fun getActiveWorkoutExerciseIds(workoutId: Long): List<Long>
}

data class WorkoutExerciseRow(
    val we_id: Long,
    val we_workout_id: Long,
    val we_exercise_id: Long,
    val we_sets: Int,
    val we_reps: Int?,
    val we_weight_kg: Double?,
    val we_duration_seconds: Int?,
    val we_order_index: Int,
    val e_id: Long,
    val e_name: String,
    val e_description: String,
    val e_type: String
)
