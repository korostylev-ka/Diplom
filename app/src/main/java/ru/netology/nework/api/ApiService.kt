package ru.netology.nework.api

import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.netology.nework.BuildConfig
import ru.netology.nework.auth.AuthState
import ru.netology.nework.dto.*
import java.io.File

private const val BASE_URL = "${BuildConfig.BASE_URL}/api/"

fun okhttp(vararg interceptors: Interceptor): OkHttpClient = OkHttpClient.Builder()
    .apply {
        interceptors.forEach {
            this.addInterceptor(it)
        }
    }
    .build()

fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(client)
    .build()

interface ApiService {
    @POST("users/push-tokens")
    suspend fun save(@Body pushToken: PushToken): Response<Unit>

    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    //получаем посты "старше", чем переданный id
    @GET("posts/{id}/before")
    suspend fun getBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    //получаем посты новее, чем переданный id
    @GET("posts/{id}/after")
    suspend fun getAfter(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Response<Post>

    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Post>

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>

    @Multipart
    @POST("users/registration/")
    suspend fun registrationCreate(@Part("login") login: String, @Part("password") pass: String, @Part("name") name: String, @Part file: MultipartBody.Part?): Response<Token>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<Users>

    @GET("users/")
    suspend fun getUsers(): Response<List<Users>>

    //получаем токен при корректной аутентификации
    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun getAuthentication(@Field("login") login: String, @Field("password") password: String): Response<Token>

    //Для запросов Событий(Events)
    @GET("events/latest")
    suspend fun getLatestEvents(@Query("count") count: Int): Response<List<Event>>

    //получаем события новее, чем переданный id
    @GET("events/{id}/after")
    suspend fun getAfterEvents(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("events/{id}/before")
    suspend fun getBeforeEvents(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: Long): Response<Event>

    @GET("events/{id}/newer")
    suspend fun getNewerEvent(@Path("id") id: Long): Response<List<Event>>

    @POST("events")
    suspend fun saveEvent(@Body event: Event): Response<Event>

    @DELETE("events/{id}")
    suspend fun removeEventById(@Path("id") id: Long): Response<Unit>

    @POST("events/{id}/likes")
    suspend fun likeEventById(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/likes")
    suspend fun dislikeEventById(@Path("id") id: Long): Response<Event>

    //Для запросов Job's
    @GET("my/jobs")
    suspend fun getMyJobs(): Response<List<Job>>

    @POST("my/jobs")
    suspend fun saveJob(@Body job: Job): Response<Job>


}

