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
import ru.netology.nework.databinding.FragmentRegistrationBinding
import ru.netology.nework.dto.Users
import ru.netology.nework.ui.AuthFragment.Companion.textArg
import ru.netology.nework.util.StringArg
import ru.netology.nework.viewmodel.AuthViewModel


//класс регистрации
@AndroidEntryPoint
class RegFragment: Fragment() {
    private val viewModel: AuthViewModel by viewModels()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegistrationBinding.inflate(
            inflater,
            container,
            false
        )

        //обработка нажатия кнопки зарегистрироваться
        binding.buttonRegister.setOnClickListener {
            //считываем логин, пароль и имя из полей
            val login = binding.login.text
            val pass = binding.password.text
            val name = binding.userName.text
            //проверка на пустые поля
            if (login.isEmpty() && pass.isEmpty() && name.isEmpty()) {
                Toast.makeText(context, R.string.min_length, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            try{
                lifecycleScope.launchWhenCreated{
                    val responseUser = viewModel.registrationCreate(login.toString(), pass.toString(), name.toString())
                    when (responseUser) {
                        //если пользователь уже существует
                        Users(0,"","", null) -> Toast.makeText(context, "Пользователь уже зарегистрирован", Toast.LENGTH_LONG).show()
                        null -> Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show()
                        //ответ успешный
                        else -> {
                            Toast.makeText(context, R.string.registration_success, Toast.LENGTH_LONG).show()
                            val bundle = Bundle()
                            bundle.putString("login", login.toString())
                            //переходим на страницу с постами, передавая в поле логин значение логина, указанного при регистрации
                            findNavController().navigate(R.id.action_regFragment_to_authFragment, Bundle().apply {
                                textArg = login.toString()
                            })
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                println(e)
            }
        }

        return binding.root
    }
}