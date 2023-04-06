package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.*
import androidx.paging.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.Users
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.model.AudioModel
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.PhotoModel
import ru.netology.nework.model.VideoModel
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.ui.EditPostFragment
import ru.netology.nework.util.SingleLiveEvent
import ru.netology.nework.util.UriPathHelper
import java.io.File
import java.io.IOException
import javax.inject.Inject

private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    content = "",
    published = "",
    mentionedMe = false,
    likedByMe = false,
    ownedByMe = false,
    authorAvatar = null,
    authorJob = null,
    coords = null,
    likeOwnerIds = ArrayList(),
    link = null,
    mentionIds = emptyList(),
    //users = null,
)

private val noPhoto = PhotoModel(null)
private val noAudio = AudioModel(null)
private val noVideo = VideoModel(null)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    auth: AppAuth,
) : ViewModel() {
    private val cached = repository
        .data
        .cachedIn(viewModelScope)

    val data: Flow<PagingData<FeedItem>> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached
                .map { pagingData ->
                    pagingData.map { post ->
                        //если объект является постом, то копируем
                        if (post is Post) {
                            post.copy(ownedByMe = post.authorId == myId)
                        } else {
                            post
                        }
                    }
                }
        }


    //запрос поста по id
    /*suspend fun getPost(id: Long): Post{
        println("Запрос из ViewModel")
        val post = repository.getPostById(id)
        return post
    }*/


    suspend fun getPost(id: Long): Post = viewModelScope.async {
        //EditPostFragment.getPost(repository.getPostById(id))
        repository.getPostById(id)

    }.await()


    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    //медиавложение(изображение)
    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo
    //медиавложение(аудио)
    private val _audio = MutableLiveData(noAudio)
    val audio: LiveData<AudioModel>
        get() = _audio
    //медиавложение(видео)
    private val _video = MutableLiveData(noVideo)
    val video: LiveData<VideoModel>
        get() = _video

    init {
        //загружаем посты
        loadPosts()
        //загружаем пользователей
        getUsers()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            // repository.stream.cachedIn(viewModelScope).
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
             repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun save() {
        edited.value?.let {
            viewModelScope.launch {
                //определяем тип вложения
                val upload: Uri? = when {
                    _audio.value?.uri != null -> _audio.value?.uri
                    _video.value?.uri != null -> _video.value?.uri
                    _photo.value?.uri != null -> _photo.value?.uri
                    else -> null
                }
                try {
                    repository.save(
                        it, upload?.let { MediaUpload(upload.toFile()) }
                    )
                    _postCreated.value = Unit
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
        _audio.value = noAudio
        _video.value = noVideo


    }
    //редактируем пост
    fun edit(post: Post) {
        //обновленный пост с контентом и без вложений
        edited.value = post
        edited.value?.let {
            viewModelScope.launch {
                //определяем тип вложения
                val upload: Uri? = when {
                    _audio.value?.uri != null -> _audio.value?.uri
                    _video.value?.uri != null -> _video.value?.uri
                    _photo.value?.uri != null -> _photo.value?.uri
                    else -> null
                }
                try {
                    repository.save(
                        it, upload?.let { MediaUpload(upload.toFile()) }
                    )
                    _postCreated.value = Unit
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
        _audio.value = noAudio
        _video.value = noVideo
    }

    fun changeContent(content: String, link: String?) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text, link = link)
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }

    fun changeAudio(uri: Uri?) {
        _audio.value = AudioModel(uri)
    }

    fun changeVideo(uri: Uri?) {
        _video.value = VideoModel(uri)
    }

    fun likeById(id: Long, isLiked: Boolean) = viewModelScope.launch {
        try {
            repository.likeById(id, isLiked)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun like(id: Long, isLiked: Boolean) = viewModelScope.launch {
        try {
            repository.like(id, isLiked)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }

    }
    //список пользователей, поставивших лайк
    suspend fun getLikedUsers(post: Post): List<Users> {
        val list = post.likeOwnerIds
        val listUsers: MutableList<Users> = ArrayList<Users>()
        try {
            for (id in list) {
                val user = repository.getUserById(id)
                listUsers.add(user)
            }
            return listUsers.toList()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    //список пользователей
    fun getUsers() = viewModelScope.launch{
        try {
            repository.getUsers()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun getUsersNames(): List<String> {
        val list = repository.usersNames.flatMapConcat { it.asFlow() }.toList()
        return list


    }







}
