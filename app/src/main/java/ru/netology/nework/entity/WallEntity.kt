package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.nework.dto.Post

@Entity
@TypeConverters(ListIdsConverter::class)
data class WallEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published: String,
    @Embedded
    val coords: CoordinatesEmbeddable?,
    val link: String?,
    val likeOwnerIds: MutableList<Long>,
    val mentionedMe: Boolean,
    val likedByMe: Boolean,

    @Embedded
    var attachment: AttachmentEmbeddable?,
) {
    fun toDto() = Post(
        id,
        authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        published,
        coords?.toDto(),
        link,
        likeOwnerIds,
        mentionIds = emptyList(),
        mentionedMe,
        likedByMe,
        attachment?.toDto(),

        )

    companion object {
        fun fromDto(dto: Post) =
            WallEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.published,
                CoordinatesEmbeddable.fromDto(dto.coords),
                dto.link,
                dto.likeOwnerIds,
                dto.mentionedMe,
                dto.likedByMe,
                AttachmentEmbeddable.fromDto(dto.attachment),
            )
    }
}

fun List<WallEntity>.toWallDto(): List<Post> = map(WallEntity::toDto)
fun List<Post>.toWallEntity(): List<WallEntity> = map(WallEntity::fromDto)