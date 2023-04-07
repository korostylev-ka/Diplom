package ru.netology.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.WallRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.entity.*
import ru.netology.nework.error.ApiError
//Класс для пагинации Wall

//экспериментальная аннотация, чтобы код скомплеировался
@OptIn(ExperimentalPagingApi::class)
class MyWallRemoteMediator(
    private val service: ApiService,
    private val db: AppDb,
    private val postDao: PostDao,
    private val wallRemoteKeyDao: WallRemoteKeyDao,
) : RemoteMediator<Int, WallEntity>() {
    override suspend fun load(
        //действие, которое хочет совершить пользователь(обновить, скроллить вверх или вниз)
        loadType: LoadType,
        state: PagingState<Int, WallEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                //достаем размер страниц из экземпляра PagingState, config.pagesize
                LoadType.REFRESH -> {
                    service.myWallGetLatest(state.config.pageSize)
                }
                //скроллинг вверх(запрос на получение верхней страницы). Ключ достаем из аргумента state(достаем последний элемент)
                LoadType.PREPEND -> {
                    //меняем на чтение из базы данных
                    val id = wallRemoteKeyDao.max() ?: return MediatorResult.Success(
                        //конец страницы не достигнут
                        endOfPaginationReached = false
                    )
                    service.myWallGetAfter(id, state.config.pageSize)
                }
                //скроллинг вниз. Запрос на получение нижней страницы
                LoadType.APPEND -> {
                    //берем из базы данных
                    val id = wallRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = false)
                    service.myWallgetBefore(id, state.config.pageSize)
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
                        val idLast = wallRemoteKeyDao.max()
                        //очищаем базу данных с ключами
                        //wallRemoteKeyDao.removeAll()
                        //записываем список ключей, первого и последнего поста
                        wallRemoteKeyDao.insert(
                            listOf(
                                WallRemoteKeyEntity(
                                    type = WallRemoteKeyEntity.KeyType.AFTER,
                                    id = body.first().id
                                ),
                                WallRemoteKeyEntity(
                                    type = WallRemoteKeyEntity.KeyType.BEFORE,
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
                        wallRemoteKeyDao.insert(
                           WallRemoteKeyEntity(
                                type = WallRemoteKeyEntity.KeyType.AFTER,
                                id = body.first().id,
                            )
                        )

                    }
                    LoadType.APPEND -> {
                        wallRemoteKeyDao.insert(
                            WallRemoteKeyEntity(
                                type = WallRemoteKeyEntity.KeyType.BEFORE,
                                id = body.last().id,
                            )
                        )
                    }
                }
                //запишем в базу список элементов, который "пришел"
                postDao.wallInsert(body.toWallEntity())
            }
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}