package ru.netology.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.*

interface EventRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getAll()
    suspend fun getEventById(id: Long): Event
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun save(event: Event, upload: MediaUpload?)
    suspend fun removeById(id: Long)
    suspend fun like(id: Long, isLiked: Boolean)
    suspend fun upload(upload: MediaUpload): Media
}