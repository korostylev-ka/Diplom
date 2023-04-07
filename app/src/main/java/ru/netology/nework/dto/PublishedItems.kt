package ru.netology.nework.dto

import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.EventType

//объединяем объекты  общим классом
sealed class FeedItem{
    abstract val id: Long
}

//data класс для разделителей
data class DateSeparator(
    override val id: Long,
) : FeedItem()

//data класс для верхнего колонтитула для даты постов
data class Header(
    override val id: Long,
) : FeedItem()

//Пост
data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published: String,
    val coords: Coordinates?,
    val link: String?,
    val likeOwnerIds: MutableList<Long> = ArrayList(),
    val mentionIds: List<Long> = emptyList(),
    val mentionedMe: Boolean,
    val likedByMe: Boolean,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
    //val users: List<Users>? = null
): FeedItem()

//события
data class Event(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: Coordinates?,
    val type: EventType,
    val likeOwnerIds: MutableList<Long> = ArrayList(),
    val likedByMe: Boolean = false,
    val speakerIds: MutableList<Long> = ArrayList(),
    val participantsIds: MutableList<Long> = ArrayList(),
    val participatedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val link: String?,
    val ownedByMe: Boolean = false,
    //val users: List<Users>? = null
): FeedItem()

data class Job(
    override val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?
): FeedItem()

data class Attachment(
    val url: String,
    val type: AttachmentType,
)


data class Coordinates(
    val lat: String,
    val long: String,
)

