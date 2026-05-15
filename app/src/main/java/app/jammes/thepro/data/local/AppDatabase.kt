package app.jammes.thepro.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun sessionDao(): SessionDao

    companion object {
        const val NAME = "thepro.db"

        /**
         * v1 → v2: adiciona a coluna `active_in_workout` em `workout_exercises`.
         * Padrão = 1 (true) → todos os exercícios existentes seguem ativos no template.
         * Sem perda de dados.
         */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE workout_exercises ADD COLUMN active_in_workout INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)
    }
}
