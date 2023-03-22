package ru.netology.nework.viewmodel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nework.api.ApiService
import ru.netology.nework.application.NMediaApplication
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.auth.AuthState
import ru.netology.nework.dto.User
import ru.netology.nework.dto.Users
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.ui.AuthFragment
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val auth: AppAuth, private val apiService: ApiService,) : ViewModel() {
    val data: LiveData<AuthState> = auth.authStateFlow
        .asLiveData(Dispatchers.Default)
    val authenticated: Boolean
        get() = auth.authStateFlow.value.id != 0L

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
        try {
            //запрос на сервер регистрации
            val response = apiService.registrationCreate(login, password, name, null)
            code = response.code()
            //если пользователь существует (не 200 ответ)
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

    suspend fun registrate(user: User) {
        println(data.value)
        /*if (data.value == AuthState()) {
            try {

                val response =
                    apiService.registrate(user.login, user.password, user.name, user.file)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }

                val body = response.body() ?: throw ApiError(response.code(), response.message())

                throw NetworkError
            } catch (e: Exception) {
                throw UnknownError
            }

        }*/
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
