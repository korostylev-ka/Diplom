package ru.netology.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.PostRemoteKeyEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError

//экспериментальная аннотация, чтобы код скомплеировался
@OptIn(ExperimentalPagingApi::class)
//переименовываем PostPagingSource в
class PostRemoteMediator(
    private val service: ApiService,
    private val db: AppDb,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        //действие, которое хочет совершить пользователь(обновить, скроллить вверх или вниз)
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            //val body1 = service.getLatest(state.config.pageSize).body()!!.size
            val response = when (loadType) {
                //достаем размер страниц из экземпляра PagingState, config.pagesize
                LoadType.REFRESH -> {
                    service.getLatest(state.config.pageSize)
                }
                //скроллинг вверх(запрос на получение верхней страницы). Ключ достаем из аргумента state(достаем последний элемент)
                LoadType.PREPEND -> {
                    //меняем на чтение из базы данных
                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(
                        //конец страницы не достигнут
                        endOfPaginationReached = false
                    )
                    service.getAfter(id, state.config.pageSize)
                }
                //скроллинг вниз. Запрос на получение нижней страницы
                LoadType.APPEND -> {
                    //берем из базы данных
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = false)
                    service.getBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message(),
            )

            //заполняем таблицу ключей данными которые приходят про сети
            db.withTransaction {
                //тип входных данных
                when (loadType) {
                    LoadType.REFRESH -> {
                        //получаем id последнего поста в базе из базы данных кючей
                        val idLast = postRemoteKeyDao.max()
                        //очищаем базу данных с ключами
                        //postRemoteKeyDao.removeAll()
                        //записываем список ключей, первого и последнего поста
                        postRemoteKeyDao.insert(
                            listOf(
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.AFTER,
                                    id = body.first().id
                                ),
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.BEFORE,
                                    /* Меняем ключ самого старого поста в базе ключей с самого старого
                                     во всем списке, пришедшего с сервера, на ключ самого свежего поста,
                                     кторый есть в базе данных кдючей*/
                                    id = body.last().id,
                                ),
                            )
                        )
                    }
                    //при скролле наверх запишем только ключ after
                    LoadType.PREPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.AFTER,
                                id = body.first().id,
                            )
                        )
                        println("КЛЮЧ_MAX" + postRemoteKeyDao.max())
                        println("КЛЮЧ_MIN" + postRemoteKeyDao.min())

                    }
                    LoadType.APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                id = body.last().id,
                            )
                        )
                    }
                }
                //запишем в базу список элементов, который "пришел"
                postDao.insert(body.toEntity())
            }
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}