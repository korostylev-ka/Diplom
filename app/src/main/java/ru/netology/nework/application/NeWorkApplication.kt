package ru.netology.nework.application

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import javax.inject.Inject

@HiltAndroidApp
class NeWorkApplication : Application() {
    private val appScope = CoroutineScope(Dispatchers.Default)

    @Inject
    lateinit var auth: AppAuth

    override fun onCreate() {

        super.onCreate()
        //устанавливаем API ключ от яндекс карт
        MapKitFactory.setApiKey("4b0e814b-ed02-4b75-b86c-1ef414723246")

        auth = AppAuth(this)
    }
}