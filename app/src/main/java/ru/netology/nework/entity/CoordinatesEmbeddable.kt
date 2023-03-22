package ru.netology.nework.entity

import ru.netology.nework.dto.Coordinates

data class CoordinatesEmbeddable(
    val lat: String,
    val long: String,
) {
    fun toDto() = Coordinates(lat, long)

    companion object {
        fun fromDto(dto: Coordinates?) = dto?.let {
            CoordinatesEmbeddable(it.lat, it.long)
        }
    }
}