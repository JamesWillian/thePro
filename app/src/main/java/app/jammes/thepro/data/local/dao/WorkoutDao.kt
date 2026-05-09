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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercises(items: List<WorkoutExerciseEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(item: WorkoutExerciseEntity): Long

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
        ORDER BY we.order_index ASC
        """
    )
    fun observeWorkoutExercises(workoutId: Long): Flow<List<WorkoutExerciseRow>>

    @Query("SELECT * FROM workout_exercises WHERE id = :id")
    suspend fun getWorkoutExerciseById(id: Long): WorkoutExerciseEntity?
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
