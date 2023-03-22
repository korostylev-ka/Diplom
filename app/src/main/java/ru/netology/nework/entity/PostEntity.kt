package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published: String,
    val link: String?,
    //val likeOwnerIds: List<Long>?,
    //val mentionIds: List<Long>?,
    val mentionedMe: Boolean,
    val likedByMe: Boolean,

    @Embedded
    var attachment: AttachmentEmbeddable?,
    //@Embedded
    //val coords: CoordinatesEmbeddable?
) {
    fun toDto() = Post(
        id,
        authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        published,
        coords = null,
        link,
        likeOwnerIds = emptyList(),
        mentionIds = emptyList(),
        mentionedMe,
        likedByMe,
        attachment?.toDto(),

    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.published,
                dto.link,
                //dto.likeOwnerIds,
                //dto.mentionIds,
                dto.mentionedMe,
                dto.likedByMe,
                AttachmentEmbeddable.fromDto(dto.attachment),
                //CoordinatesEmbeddable.fromDto(dto.coords),

            )
        /*fun toDto() = Post(id, authorId, author, authorAvatar, authorJob, content, published, coords, link, likeOwnerIds, mentionIds, mentionedMe, likedByMe, attachment?.toDto(), ownedByMe, users )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(dto.id, dto.authorId, dto.author, dto.authorAvatar, dto.authorJob, dto.content, dto.published, dto.coords, dto.link, dto.likeOwnerIds, dto.mentionIds, dto.mentionedMe, dto.likedByMe,  dto.ownedByMe, dto.users, AttachmentEmbeddable.fromDto(dto.attachment))

    }*/
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)

