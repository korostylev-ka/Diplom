package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nework.entity.WallRemoteKeyEntity

@Dao
interface WallRemoteKeyDao {
    @Query("SELECT COUNT(*) == 0 FROM WallRemoteKeyEntity")
    suspend fun isEmpty(): Boolean

    //функция получения мксимального id из базы данных
    @Query("SELECT MAX(id) FROM WallRemoteKeyEntity")
    suspend fun max(): Long?

    //функция получения миним id из базы данных(самый старый пост)
    @Query("SELECT MIN(id) FROM WallRemoteKeyEntity")
    suspend fun min(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: WallRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keys: List<WallRemoteKeyEntity>)

    @Query("DELETE FROM WallRemoteKeyEntity")
    suspend fun removeAll()
}