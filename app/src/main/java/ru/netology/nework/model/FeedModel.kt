package ru.netology.nework.model

import ru.netology.nework.dto.Job

data class JobFeedModel(
    val jobs: List<Job> = emptyList(),
)
