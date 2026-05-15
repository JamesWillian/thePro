package app.jammes.thepro.di

import android.content.Context
import androidx.room.Room
import app.jammes.thepro.data.local.AppDatabase
import app.jammes.thepro.data.local.dao.ExerciseDao
import app.jammes.thepro.data.local.dao.ScheduleDao
import app.jammes.thepro.data.local.dao.SessionDao
import app.jammes.thepro.data.local.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .addMigrations(*AppDatabase.MIGRATIONS)
            .build()

    @Provides
    fun provideExerciseDao(db: AppDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    fun provideWorkoutDao(db: AppDatabase): WorkoutDao = db.workoutDao()

    @Provides
    fun provideScheduleDao(db: AppDatabase): ScheduleDao = db.scheduleDao()

    @Provides
    fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()
}
