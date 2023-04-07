package ru.netology.nework.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentRegistrationBinding
import ru.netology.nework.dto.Users
import ru.netology.nework.ui.AuthFragment.Companion.textArg
import ru.netology.nework.view.loadCircleCrop
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
        binding.login.requestFocus()
        binding.password.requestFocus()
        binding.userName.requestFocus()
        binding.avatar.setImageResource(R.drawable.ic_avatar_48dp)

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> viewModel.changePhoto(it.data?.data)
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it.uri != null) binding.avatar.loadCircleCrop(it.uri.toString())

        }

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
            lifecycleScope.launchWhenCreated {
                try {
                    val responseUser = viewModel.registrationCreate(login.toString(), pass.toString(), name.toString())
                    when (responseUser) {
                        //если пользователь уже существует
                        Users(0,"","", null) -> Toast.makeText(context, R.string.user_exists, Toast.LENGTH_LONG).show()
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
                } catch (e: java.lang.Exception) {
                    println(e)
                }
            }

        }

        return binding.root
    }
}