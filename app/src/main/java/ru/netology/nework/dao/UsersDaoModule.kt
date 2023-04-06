package ru.netology.nework.dao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.db.AppDb

@InstallIn(SingletonComponent::class)
@Module
object UsersDaoModule {
    @Provides
    fun provideUsersDao(db: AppDb): UsersDao = db.usersDao()
}