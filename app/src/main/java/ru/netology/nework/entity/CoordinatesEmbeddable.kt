package ru.netology.nework.entity

import ru.netology.nework.dto.Coordinates

data class CoordinatesEmbeddable(
    val latC: String,
    val longC: String,
) {
    fun toDto() = Coordinates(latC, longC)

    companion object {
        fun fromDto(dto: Coordinates?) = dto?.let {
            CoordinatesEmbeddable(it.lat, it.long)
        }
    }
}