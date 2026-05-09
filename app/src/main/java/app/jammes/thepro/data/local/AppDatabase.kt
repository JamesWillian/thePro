package app.jammes.thepro.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import app.jammes.thepro.data.local.dao.ExerciseDao
import app.jammes.thepro.data.local.dao.ScheduleDao
import app.jammes.thepro.data.local.dao.SessionDao
import app.jammes.thepro.data.local.dao.WorkoutDao
import app.jammes.thepro.data.local.entity.ExerciseEntity
import app.jammes.thepro.data.local.entity.ExerciseLogEntity
import app.jammes.thepro.data.local.entity.ScheduleEntryEntity
import app.jammes.thepro.data.local.entity.WorkoutEntity
import app.jammes.thepro.data.local.entity.WorkoutExerciseEntity
import app.jammes.thepro.data.local.entity.WorkoutSessionEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        ScheduleEntryEntity::class,
        WorkoutSessionEntity::class,
        ExerciseLogEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun sessionDao(): SessionDao

    companion object {
        const val NAME = "thepro.db"
    }
}
