package ru.netology.nework.entity

import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Type
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.EventType

data class TypeEmbeddable(
    var url: String,
    var type: EventType,
) {
    fun toDto() = Type(url, type)

    companion object {
        fun fromDto(dto: Type?) = dto?.let {
            TypeEmbeddable(it.url, it.type)
        }
    }
}