package ru.netology.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.PostEntity

@Dao
interface EventDao {
    //метод для получения PagingSource. Вызываем ее при создании pager'а
    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, EventEntity>

    @Query("SELECT * FROM EventEntity")
    fun getAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM EventEntity WHERE id = :id")
    fun getPost(id: Long): EventEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(events: List<EventEntity>)

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun removeById(id: Long)

}