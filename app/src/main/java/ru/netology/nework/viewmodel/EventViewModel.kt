package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.*
import androidx.paging.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.*
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.model.AudioModel
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.PhotoModel
import ru.netology.nework.model.VideoModel
import ru.netology.nework.repository.EventRepository
import ru.netology.nework.util.SingleLiveEvent
import java.io.IOException
import javax.inject.Inject

private val empty = Event(
    id = 0,
    authorId = 0,
    author = "",
    content = "",
    datetime = "",
    published = "",
    likeOwnerIds = ArrayList(),
    participantsIds = ArrayList(),
    speakerIds = ArrayList(),
    link = null,
    participatedByMe = false,
    likedByMe = false,
    ownedByMe = false,
    authorAvatar = null,
    authorJob = null,
    coords = null,
    type = EventType.OFFLINE,
)

private val noPhoto = PhotoModel(null)
private val noAudio = AudioModel(null)
private val noVideo = VideoModel(null)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
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


    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(empty)
    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

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
        loadEvents()
    }

    fun loadEvents() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            // repository.stream.cachedIn(viewModelScope).
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

                    _eventCreated.value = Unit
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

    fun edit(event: Event) {
        edited.value = event
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

                    _eventCreated.value = Unit
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

    fun changeContent(content: String, dateTime: String, isOnline: Boolean) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        //проверяем тип события
        val type = when (isOnline) {
            true -> EventType.ONLINE
            else -> EventType.OFFLINE
        }
        edited.value = edited.value?.copy(content = text, datetime = dateTime, type = type)
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
    suspend fun getLikedUsers(event: Event): List<Users> {
        val list = event.likeOwnerIds
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

    suspend fun getEvent(id: Long): Event = viewModelScope.async {
        repository.getEventById(id)
    }.await()
}
