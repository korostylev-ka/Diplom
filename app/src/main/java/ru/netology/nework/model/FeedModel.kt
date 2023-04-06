package ru.netology.nework.model

import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
)

data class JobFeedModel(
    val jobs: List<Job> = emptyList(),
)
