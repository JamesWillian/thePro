package app.jammes.thepro.data.repository

import app.jammes.thepro.data.local.dao.ScheduleDao
import app.jammes.thepro.data.local.dao.WorkoutDao
import app.jammes.thepro.data.local.entity.ScheduleEntryEntity
import app.jammes.thepro.data.mapper.toDomain
import app.jammes.thepro.domain.model.DayOfWeekBr
import app.jammes.thepro.domain.model.WeeklyScheduleEntry
import app.jammes.thepro.domain.repository.ScheduleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val workoutDao: WorkoutDao
) : ScheduleRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeWeek(): Flow<Map<DayOfWeekBr, List<WeeklyScheduleEntry>>> =
        scheduleDao.observeAll().flatMapLatest { entries ->
            if (entries.isEmpty()) {
                flowOf(DayOfWeekBr.entries.associateWith { emptyList<WeeklyScheduleEntry>() })
            } else {
                combine(entries.map { entry -> entryFlow(entry) }) { pairs ->
                    val mapped = pairs.filterNotNull().toList()
                    DayOfWeekBr.entries.associateWith { day ->
                        mapped.filter { it.dayOfWeek == day }.sortedBy { it.orderIndex }
                    }
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeForDay(day: DayOfWeekBr): Flow<List<WeeklyScheduleEntry>> =
        scheduleDao.observeForDay(day.isoValue).flatMapLatest { entries ->
            if (entries.isEmpty()) flowOf(emptyList())
            else combine(entries.map { entry -> entryFlow(entry) }) { results ->
                results.filterNotNull().toList().sortedBy { it.orderIndex }
            }
        }

    private fun entryFlow(entry: app.jammes.thepro.data.local.entity.ScheduleEntryEntity): Flow<WeeklyScheduleEntry?> =
        combine(
            workoutDao.observeById(entry.workoutId),
            workoutDao.observeWorkoutExercises(entry.workoutId)
        ) { w, rows ->
            w?.let { entry.toDomain(it.toDomain(rows.map { r -> r.toDomain() })) }
        }

    override suspend fun assign(day: DayOfWeekBr, workoutId: Long): Long {
        val nextOrder = (scheduleDao.maxOrderForDay(day.isoValue) ?: -1) + 1
        return scheduleDao.insert(
            ScheduleEntryEntity(
                dayOfWeek = day.isoValue,
                workoutId = workoutId,
                orderIndex = nextOrder
            )
        )
    }

    override suspend fun unassign(entryId: Long) = scheduleDao.deleteById(entryId)

    override suspend fun moveEntry(entryId: Long, newDay: DayOfWeekBr) {
        val nextOrder = (scheduleDao.maxOrderForDay(newDay.isoValue) ?: -1) + 1
        scheduleDao.moveTo(entryId, newDay.isoValue, nextOrder)
    }
}
