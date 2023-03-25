package ru.netology.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.entity.ListIdsConverter
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.PostRemoteKeyEntity

//сообщаем Room о новой таблице
@Database(entities = [PostEntity::class, PostRemoteKeyEntity::class], version = 1, exportSchema = false)
@TypeConverters(ListIdsConverter::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    //и новых запросах
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
}