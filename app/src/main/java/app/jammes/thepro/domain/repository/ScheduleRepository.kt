package app.jammes.thepro.domain.repository

import app.jammes.thepro.domain.model.DayOfWeekBr
import app.jammes.thepro.domain.model.WeeklyScheduleEntry
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun observeWeek(): Flow<Map<DayOfWeekBr, List<WeeklyScheduleEntry>>>
    fun observeForDay(day: DayOfWeekBr): Flow<List<WeeklyScheduleEntry>>
    suspend fun assign(day: DayOfWeekBr, workoutId: Long): Long
    suspend fun unassign(entryId: Long)
    suspend fun moveEntry(entryId: Long, newDay: DayOfWeekBr)
}
