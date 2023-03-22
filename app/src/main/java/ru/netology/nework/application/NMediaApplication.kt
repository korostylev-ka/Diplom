package ru.netology.nework.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import javax.inject.Inject

@HiltAndroidApp
class NMediaApplication : Application() {
    private val appScope = CoroutineScope(Dispatchers.Default)

    @Inject
    lateinit var auth: AppAuth

    override fun onCreate() {
        super.onCreate()
        //auth = AppAuth(this)
        setupAuth()
    }

    private fun setupAuth() {
        appScope.launch {
            auth.sendPushToken()
        }
    }
}