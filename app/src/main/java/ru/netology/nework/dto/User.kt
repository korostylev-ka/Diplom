package ru.netology.nework.dto

import java.io.File

data class User (
    val login: String,
    val password: String,
    val name: String,
    val file: File?,

    )