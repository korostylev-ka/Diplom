package ru.netology.nework.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Job
import ru.netology.nework.model.FeedModel
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.JobFeedModel
import ru.netology.nework.repository.JobRepository
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Job(
    id = 0,
    name = "",
    position = "",
    start = "",
    finish = null,
    link = null,
)

@HiltViewModel
class JobViewModel @Inject constructor(
    private val repository: JobRepository,
    auth: AppAuth,
): ViewModel() {
    val data: LiveData<JobFeedModel> = repository.data
        .map(::JobFeedModel)
        //"Подгоняем" flow к liveData
        .asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(empty)
    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    init {
        getMyJobs()
    }
    //получаем список своих работ
    fun getMyJobs() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getMyJobs()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }
    //сохраняем
    fun save() {
        edited.value?.let {
            _jobCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.save(it)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
    }

    //изменяем
    fun changeJob(name: String, position: String, start: String) {
        if ((edited.value?.name == name) && (edited.value?.position == position) && (edited.value?.start == start))  {
            return
        }
        edited.value = edited.value?.copy(name = name, position = position, start = start)
    }
}