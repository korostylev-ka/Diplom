package ru.netology.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nework.dao.*
import ru.netology.nework.entity.*

//сообщаем Room о новой таблице
@Database(entities = [PostEntity::class, PostRemoteKeyEntity::class, EventEntity::class, EventRemoteKeyEntity::class, JobEntity::class, WallEntity::class, WallRemoteKeyEntity::class, UsersEntity::class], version = 1, exportSchema = false)
@TypeConverters(ListIdsConverter::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    //и новых запросах
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun wallRemoteKeyDao(): WallRemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
    abstract fun jobDao(): JobDao
    abstract fun usersDao(): UsersDao
}