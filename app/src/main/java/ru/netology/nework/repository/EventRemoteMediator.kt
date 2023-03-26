package ru.netology.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.entity.*
import ru.netology.nework.error.ApiError

//экспериментальная аннотация, чтобы код скомплеировался
@OptIn(ExperimentalPagingApi::class)
//переименовываем PostPagingSource в
class EventRemoteMediator(
    private val service: ApiService,
    private val db: AppDb,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
) : RemoteMediator<Int, EventEntity>() {
    override suspend fun load(
        //действие, которое хочет совершить пользователь(обновить, скроллить вверх или вниз)
        loadType: LoadType,
        state: PagingState<Int, EventEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                //достаем размер страниц из экземпляра PagingState, config.pagesize
                LoadType.REFRESH -> {
                    service.getLatestEvents(state.config.pageSize)
                }
                //скроллинг вверх(запрос на получение верхней страницы). Ключ достаем из аргумента state(достаем последний элемент)
                LoadType.PREPEND -> {
                    //меняем на чтение из базы данных
                    val id = eventRemoteKeyDao.max() ?: return MediatorResult.Success(
                        //конец страницы не достигнут
                        endOfPaginationReached = false
                    )
                    service.getAfterEvents(id, state.config.pageSize)
                }
                //скроллинг вниз. Запрос на получение нижней страницы
                LoadType.APPEND -> {
                    //берем из базы данных
                    val id = eventRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = false)
                    service.getBeforeEvents(id, state.config.pageSize)
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
                        //получаем id последнего события в базе из базы данных кючей
                        val idLast = eventRemoteKeyDao.max()
                        //записываем список ключей, первого и последнего события
                        eventRemoteKeyDao.insert(
                            listOf(
                                EventRemoteKeyEntity(
                                    type = EventRemoteKeyEntity.KeyType.AFTER,
                                    id = body.first().id
                                ),
                                EventRemoteKeyEntity(
                                    type = EventRemoteKeyEntity.KeyType.BEFORE,
                                    /* Меняем ключ самого старого события в базе ключей с самого старого
                                     во всем списке, пришедшего с сервера, на ключ самого свежего события,
                                     кторый есть в базе данных кдючей*/
                                    id = body.last().id,
                                ),
                            )
                        )
                    }
                    //при скролле наверх запишем только ключ after
                    LoadType.PREPEND -> {
                        eventRemoteKeyDao.insert(
                            EventRemoteKeyEntity(
                                type = EventRemoteKeyEntity.KeyType.AFTER,
                                id = body.first().id,
                            )
                        )
                    }
                    LoadType.APPEND -> {
                        eventRemoteKeyDao.insert(
                            EventRemoteKeyEntity(
                                type = EventRemoteKeyEntity.KeyType.BEFORE,
                                id = body.last().id,
                            )
                        )
                    }
                }
                //запишем в базу список элементов, который "пришел"
                eventDao.insert(body.toEntity())
            }
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}