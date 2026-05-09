package app.jammes.thepro.di

import app.jammes.thepro.data.repository.ExerciseRepositoryImpl
import app.jammes.thepro.data.repository.ScheduleRepositoryImpl
import app.jammes.thepro.data.repository.SessionRepositoryImpl
import app.jammes.thepro.data.repository.WorkoutRepositoryImpl
import app.jammes.thepro.domain.repository.ExerciseRepository
import app.jammes.thepro.domain.repository.ScheduleRepository
import app.jammes.thepro.domain.repository.SessionRepository
import app.jammes.thepro.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository
}
