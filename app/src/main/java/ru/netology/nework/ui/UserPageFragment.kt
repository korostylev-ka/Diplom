package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentUserPageBinding
import ru.netology.nework.view.loadCircleCrop
import ru.netology.nework.viewmodel.AuthViewModel
import javax.inject.Inject


//Фрагмент данных пользователя
@AndroidEntryPoint
class UserPageFragment : Fragment() {
    @Inject
    lateinit var auth: AppAuth
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUserPageBinding.inflate(
            inflater,
            container,
            false

        )
        //проверяем фрагмент, и если это фрагмент работ или стены, делаем статус кнопки "нажато"
        val fragment = requireParentFragment()
        if (fragment is FeedJobFragment) binding.jobButton.isPressed = true
        if (fragment is FeedMyWallFragment) binding.wallButton.isPressed = true


        //В зависимости от того, зарегистрированы или нет, показываем панель данных пользователя
        authViewModel.data.observe(viewLifecycleOwner) {
            lifecycleScope.launchWhenCreated {
                //если авторизованы, запрашиваем данные пользователя
                if (authViewModel.authenticated) {
                    //получаем карточку пользователя
                    val user = authViewModel.getUser()
                    binding.apply {
                        if (user.avatar != null) {
                            userAvatar.loadCircleCrop(user.avatar)
                        } else {
                            userAvatar.setImageResource(R.drawable.ic_avatar_black_48dp)
                        }
                        userId.setText(user.id.toString())
                        userName.setText(user.name)

                        //переход на страницу job
                        jobButton.setOnClickListener {
                            val fragment = requireParentFragment()
                            //проверяем текущий фрагмент
                            if (fragment is FeedMyWallFragment) binding.jobButton.isPressed = true
                            findNavController().navigate(R.id.feedJobFragment)
                        }

                        //переход на страницу myWall
                        wallButton.setOnClickListener {
                            val fragment = requireParentFragment()
                            //проверяем текущий фрагмент
                            if (fragment is FeedMyWallFragment) binding.wallButton.isPressed = true
                            findNavController().navigate(R.id.feedMyWallFragment)
                        }

                    }
                }
            }
        }

        return binding.root

    }

}