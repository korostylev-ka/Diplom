package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dto.Post

@Entity
@TypeConverters(ListIdsConverter::class)
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
    val likeOwnerIds: MutableList<Long>,
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
        likeOwnerIds,
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
                dto.likeOwnerIds,
                //dto.mentionIds,
                dto.mentionedMe,
                dto.likedByMe,
                AttachmentEmbeddable.fromDto(dto.attachment),
                //CoordinatesEmbeddable.fromDto(dto.coords),

            )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)

//TypeConverters для List<Long> из ID
class ListIdsConverter {
    @TypeConverter
    fun fromLong(value: String): MutableList<Long> {
        val listType = object: TypeToken<List<Long>>(){}.type
        return Gson().fromJson(value, listType)
    }
    @TypeConverter
    fun fromList(list: MutableList<Long>): String {
        return Gson().toJson(list)
    }
}

