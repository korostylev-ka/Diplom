package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.auth.AuthState
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.User
import ru.netology.nework.dto.Users
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.model.PhotoModel
import java.io.IOException
import javax.inject.Inject

private val noPhoto = PhotoModel(null)
@HiltViewModel
class AuthViewModel @Inject constructor(private val auth: AppAuth, private val apiService: ApiService,) : ViewModel() {
    val data: LiveData<AuthState> = auth.authStateFlow
        .asLiveData(Dispatchers.Default)
    val authenticated: Boolean
        get() = auth.authStateFlow.value.id != 0L

    //аватар
    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }


    //авторизация по логину и паролю. В ответ получаем код ответа
    suspend fun getAuthentication(user: String, password: String): Int {
        //код ответа
        var code = 0
        try {
            val response = apiService.getAuthentication(user, password)
            code = response.code()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            body.token?.let { auth.setAuth(body.id, it) }

        } catch (e: IOException) {
            auth.removeAuth()
            throw NetworkError
        } catch (e: Exception) {
            auth.removeAuth()
            //throw UnknownError


        }
        return code
    }

    //регистрация
    suspend fun registrationCreate(login: String, password: String, name: String): Users? {
        //код ответа
        var code = 0
        //ссылка на загруженное фото аватара
        val avatarFile = _photo.value?.uri?.let { MediaUpload(it.toFile())}
        val multipartImage: MultipartBody.Part? =
            avatarFile?.file?.let {
                MultipartBody.Part.createFormData("file", avatarFile.file.name,
                    it.asRequestBody("image/*".toMediaTypeOrNull())
                )
            }

        try {
            //запрос на сервер регистрации
            println("ЗАПРОС НА РЕГИСТРАЦИЮ")
            val response = apiService.registrationCreate(login, password, name, multipartImage)
            //если пользователь существует (не 200 ответ)
            code = response.code()
            if (code == 400) {
                return Users(0,"", "", null)
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            //авторизуемся
            body.token?.let { auth.setAuth(body.id, it) }
            //возвращаем пользователя
            return Users(body.id, login, name, null)

        } catch (e: IOException) {
            //auth.removeAuth()
            throw NetworkError

        } catch (e: Exception) {
            //auth.removeAuth()
            //throw UnknownError
        }
        return null
    }

    //получаем данные зарегистрированного пользователя
    suspend fun getUser(): Users {
        try {
            //получаем id
            val id = auth.authStateFlow.value.id
            //запрос на сервер
            val response = apiService.getUserById(id)
            println("id пользователя $id + ${response.body()?.name}")
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    }

}
