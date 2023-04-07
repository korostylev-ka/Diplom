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
    val datetime: String,
    val published: String,
    @Embedded
    val coords: CoordinatesEmbeddable?,
    val likeOwnerIds: MutableList<Long>,
    val likedByMe: Boolean,
    val speakerIds: MutableList<Long>,
    val participantsIds: MutableList<Long>,
    val participatedByMe: Boolean,
    val link: String?,
    val ownedByMe: Boolean,
    @Embedded
    var type: TypeEmbeddable,
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
        datetime,
        published,
        coords?.toDto(),
        type.toDto(),
        likeOwnerIds,
        likedByMe,
        speakerIds = ArrayList(),
        participantsIds = ArrayList(),
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
                dto.datetime,
                dto.published,
                CoordinatesEmbeddable.fromDto(dto.coords),
                dto.likeOwnerIds,
                dto.likedByMe,
                dto.speakerIds,
                dto.participantsIds,
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


