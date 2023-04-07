package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Users

@Entity
data class UsersEntity(
    @PrimaryKey
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String?
) {
    fun toDto() = Users(
        id,
        login,
        name,
        avatar,
    )

    companion object {
        fun fromDto(dto: Users) =
            UsersEntity(
                dto.id,
                dto.login,
                dto.name,
                dto.avatar,
            )
    }
}

fun List<UsersEntity>.toDto(): List<Users> = map(UsersEntity::toDto)
fun List<Users>.toEntity(): List<UsersEntity> = map(UsersEntity::fromDto)