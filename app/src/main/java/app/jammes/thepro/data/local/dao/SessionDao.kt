package app.jammes.thepro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.jammes.thepro.data.local.entity.ExerciseLogEntity
import app.jammes.thepro.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM workout_sessions WHERE date = :epochDay ORDER BY id ASC")
    fun observeForDate(epochDay: Long): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    fun observeRange(from: Long, to: Long): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE date = :epochDay ORDER BY id ASC")
    suspend fun listForDate(epochDay: Long): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getById(id: Long): WorkoutSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WorkoutSessionEntity): Long

    @Query("UPDATE workout_sessions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE workout_sessions SET date = :newEpochDay, original_date = COALESCE(original_date, :originalEpochDay), status = :status WHERE id = :id")
    suspend fun reschedule(id: Long, newEpochDay: Long, originalEpochDay: Long, status: String)

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM exercise_logs WHERE session_id = :sessionId")
    fun observeLogs(sessionId: Long): Flow<List<ExerciseLogEntity>>

    @Query("SELECT * FROM exercise_logs WHERE session_id = :sessionId")
    suspend fun listLogs(sessionId: Long): List<ExerciseLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(entity: ExerciseLogEntity): Long
}
