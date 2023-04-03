package ru.netology.nework.entity

import ru.netology.nework.dto.Attachment

import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.EventType

data class TypeEmbeddable(
    var type: EventType,
) {
    fun toDto() = type

    companion object {
        fun fromDto(dto: EventType) = dto.let {
            TypeEmbeddable(it)
        }
    }
}