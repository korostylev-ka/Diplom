package ru.netology.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Media
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getAll()
    suspend fun getPostById(id: Long): Post
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun save(post: Post, upload: MediaUpload?)
    //suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long, isLiked: Boolean)
    suspend fun upload(upload: MediaUpload): Media
}
