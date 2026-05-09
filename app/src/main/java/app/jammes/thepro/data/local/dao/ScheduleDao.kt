package app.jammes.thepro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.jammes.thepro.data.local.entity.ScheduleEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_entries ORDER BY day_of_week ASC, order_index ASC")
    fun observeAll(): Flow<List<ScheduleEntryEntity>>

    @Query("SELECT * FROM schedule_entries WHERE day_of_week = :day ORDER BY order_index ASC")
    fun observeForDay(day: Int): Flow<List<ScheduleEntryEntity>>

    @Query("SELECT * FROM schedule_entries WHERE day_of_week = :day ORDER BY order_index ASC")
    suspend fun listForDay(day: Int): List<ScheduleEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScheduleEntryEntity): Long

    @Query("DELETE FROM schedule_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE schedule_entries SET day_of_week = :newDay, order_index = :newOrder WHERE id = :id")
    suspend fun moveTo(id: Long, newDay: Int, newOrder: Int)

    @Query("SELECT MAX(order_index) FROM schedule_entries WHERE day_of_week = :day")
    suspend fun maxOrderForDay(day: Int): Int?
}
