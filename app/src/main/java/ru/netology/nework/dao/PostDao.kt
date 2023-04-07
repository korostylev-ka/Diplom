package ru.netology.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.WallEntity

@Dao
interface PostDao {
    //метод для получения PagingSource. Вызываем ее при создании pager'а
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, PostEntity>

    //метод для получения PagingSource для Wall. Вызываем ее при создании pager'а
    @Query("SELECT * FROM WallEntity ORDER BY id DESC")
    fun pagingSourceWall(): PagingSource<Int, WallEntity>


    @Query("SELECT * FROM PostEntity")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    fun getPost(id: Long): PostEntity

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM PostEntity")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun wallInsert(post: WallEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun wallInsert(posts: List<WallEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM WallEntity WHERE id = :id")
    suspend fun wallRemoveById(id: Long)

    /*@Query("""
        UPDATE PostEntity SET
        likeOwnerIds = likeOwnerIds + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """)
    suspend fun likeById(id: Long)*/

    //очищаем таблицу (при refresh)
    @Query("DELETE FROM PostEntity")
    suspend fun removeAll()
}