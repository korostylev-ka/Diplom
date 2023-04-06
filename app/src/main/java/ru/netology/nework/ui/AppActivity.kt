package ru.netology.nework.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.ui.NewPostFragment.Companion.textArg
import ru.netology.nework.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AppActivity : AppCompatActivity(R.layout.activity_app) {
    @Inject
    lateinit var repository: PostRepository

    @Inject
    lateinit var auth: AppAuth
    private val viewModel: AuthViewModel by viewModels()


    override fun onStart() {
        super.onStart()
        MapKitFactory.initialize(this)
        MapKitFactory.getInstance().onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = text
                    }
                )
        }

        viewModel.data.observe(this) {
            invalidateOptionsMenu()
            //в зависимости от того, авторизованы или нет, присваиваем имя пользователя в поле Title
            lifecycleScope.launchWhenCreated {
                //если авторизованы, запрашиваем данные пользователя
                if (viewModel.authenticated) {
                    val user = viewModel.getUser()
                    setTitle(user.name)

                } else setTitle(R.string.not_autorized)
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("some stuff happened: ${task.exception}")
                return@addOnCompleteListener
            }

            val token = task.result
            println(token)
        }

        checkGoogleApiAvailability()

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)

                menu.let {
                    it.setGroupVisible(R.id.unauthenticated, !viewModel.authenticated)
                    it.setGroupVisible(R.id.authenticated, viewModel.authenticated)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.signin -> {
                    //переход на страницу аутентентификации
                        findNavController(R.id.nav_host_fragment)
                            .navigate(R.id.authFragment)
                        true
                    }
                    R.id.signup -> {
                        //переход на страницу регистрации
                        findNavController(R.id.nav_host_fragment)
                            .navigate(R.id.registrationFragment)
                        true
                    }
                    R.id.signout -> {
                        //удаляем авторизацию
                        auth.removeAuth()
                        true
                    }
                    else -> false
                }

        })
    }

    private fun checkGoogleApiAvailability() {
        with(GoogleApiAvailability.getInstance()) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
        }
    }
}