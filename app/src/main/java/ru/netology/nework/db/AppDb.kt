package ru.netology.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.entity.*

//сообщаем Room о новой таблице
@Database(entities = [PostEntity::class, PostRemoteKeyEntity::class, EventEntity::class, EventRemoteKeyEntity::class], version = 1, exportSchema = false)
@TypeConverters(ListIdsConverter::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    //и новых запросах
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
}