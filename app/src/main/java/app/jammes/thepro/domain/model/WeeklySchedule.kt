package app.jammes.thepro.domain.model

data class WeeklyScheduleEntry(
    val id: Long = 0L,
    val dayOfWeek: DayOfWeekBr,
    val workout: Workout,
    val orderIndex: Int = 0
)
