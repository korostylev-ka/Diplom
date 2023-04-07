package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.UsersEntity

@Dao
interface UsersDao {

    @Query("SELECT * FROM UsersEntity")
    fun getUsers(): Flow<List<UsersEntity>>

    @Query("SELECT name FROM UsersEntity")
    fun getNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: UsersEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(events: List<UsersEntity>)
}