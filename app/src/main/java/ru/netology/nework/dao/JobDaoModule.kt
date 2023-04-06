package ru.netology.nework.dao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.db.AppDb

@InstallIn(SingletonComponent::class)
@Module
object JobDaoModule {
    @Provides
    fun provideJobDao(db: AppDb): JobDao = db.jobDao()


}