package ru.netology.nework.entity

import androidx.room.*
import ru.netology.nework.dto.Event

@Entity
@TypeConverters(ListIdsConverter::class)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val dateTime: String,
    val published: String,
    val likeOwnerIds: MutableList<Long>,
    val likedByMe: Boolean,
    val speakerIds: List<Long>,
    val partisipantsIds: List<Long> = emptyList(),
    val participatedByMe: Boolean,
    val link: String?,
    val ownedByMe: Boolean,

    @Embedded
    var type: TypeEmbeddable?,
    @Embedded(prefix = "event_")
    var attachment: AttachmentEmbeddable?,
) {
    fun toDto() = Event(
        id,
        authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        dateTime,
        published,
        coords = null,
        type?.toDto(),
        likeOwnerIds,
        likedByMe,
        speakerIds = emptyList(),
        partisipantsIds = emptyList(),
        participatedByMe,
        attachment?.toDto(),
        link,
        ownedByMe,

        )

    companion object {
        fun fromDto(dto: Event) =
            EventEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.dateTime,
                dto.published,
                dto.likeOwnerIds,
                dto.likedByMe,
                dto.speakerIds,
                dto.partisipantsIds,
                dto.participatedByMe,
                dto.link,
                dto.ownedByMe,
                TypeEmbeddable.fromDto(dto.type),
                AttachmentEmbeddable.fromDto(dto.attachment),
            )
    }
}

fun List<EventEntity>.toDto(): List<Event> = map(EventEntity::toDto)
fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)


