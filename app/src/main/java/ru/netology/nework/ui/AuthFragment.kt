package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentAuthBinding
import ru.netology.nework.util.StringArg
import ru.netology.nework.viewmodel.AuthViewModel


//класс аутентификации
@AndroidEntryPoint
class AuthFragment: Fragment() {
    private val viewModel: AuthViewModel by viewModels()

    companion object {
        //переменная для передачи значения логина из фрагмента регистрации
        private const val LOGIN = "login"
        var Bundle.textArg: String? by StringArg
        fun createArguments(login: String?): Bundle {
            return bundleOf(LOGIN to login)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuthBinding.inflate(
            inflater,
            container,
            false
        )
        //присваиваем полю логин значение из фрагмента регистрации
        arguments?.textArg
            ?.let(binding.login::setText)

        //обработка нажатия кнопки login
        binding.buttonLogin.setOnClickListener {
            //считываем логин и пароль из полей
            val login = binding.login.text
            val pass = binding.password.text
            try{
                lifecycleScope.launchWhenCreated{
                    val responseCode = viewModel.getAuthentication(login.toString(), pass.toString())
                    when (responseCode) {
                        //если 400 ответ, выводим предупреждение
                        400 -> Toast.makeText(context, R.string.wrong_login_or_password, Toast.LENGTH_LONG).show()
                        //если 200 ответ, переходим на страницу с постами
                        200 -> findNavController().navigate(R.id.action_authFragment_to_feedFragment)
                    }
                }

            } catch (e: java.lang.Exception) {
                println(e)

            }
        }
        //обработка нажатия на кнопку Присоединиться
        binding.buttonRegistration.setOnClickListener {
            findNavController().navigate(R.id.action_authFragment_to_registrationFragment)

        }

        return binding.root
    }
}