package ru.netology.nework.dto

data class Users(
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String?
)