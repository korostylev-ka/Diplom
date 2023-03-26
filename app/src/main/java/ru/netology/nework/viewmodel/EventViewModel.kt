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
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.PhotoModel
import ru.netology.nework.repository.EventRepository
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.ui.EditPostFragment
import ru.netology.nework.util.SingleLiveEvent
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
    //users = null,
)

private val noPhoto = PhotoModel(null)

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

    suspend fun getEvent(id: Long): Event = viewModelScope.async {
        //EditPostFragment.getPost(repository.getPostById(id))
        repository.getEventById(id)
    }.await()


    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    //медиавложение
    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

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
                try {
                    repository.save(
                        it, _photo.value?.uri?.let { MediaUpload(it.toFile()) }
                    )

                    _postCreated.value = Unit
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
    }

    fun edit(event: Event) {
        edited.value = event
        edited.value?.let {
            viewModelScope.launch {
                try {
                    repository.save(
                        it, _photo.value?.uri?.let { MediaUpload(it.toFile()) }
                    )

                    _postCreated.value = Unit
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
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
}
