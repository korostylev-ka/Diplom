package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint

import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentEditPostBinding
import ru.netology.nework.databinding.FragmentUserPageBinding
import ru.netology.nework.util.LongArg
import ru.netology.nework.view.loadCircleCrop
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.PostViewModel
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
                            userAvatar.setImageResource(R.drawable.person_empty)
                        }
                        userId.setText(auth.authStateFlow.value.id.toString())
                        //переход на страницу постов
                        postButton.setOnClickListener {
                            val fragment = requireParentFragment()
                            //проверка текущего фрагмента
                            if (fragment !is FeedPostFragment){
                                findNavController().navigate(R.id.action_feedEventFragment_to_feedFragment)
                            }

                        }
                        //переход на страницу событий
                        eventButton.setOnClickListener {
                            val fragment = requireParentFragment()
                            //проверка текущего фрагмента
                            if (fragment !is FeedEventFragment) {
                                findNavController().navigate(R.id.action_feedFragment_to_feedEventFragment)
                            }


                        }

                    }
                }
            }
        }

        return binding.root

    }

}