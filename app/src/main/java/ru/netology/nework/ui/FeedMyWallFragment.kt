package ru.netology.nework.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nework.R
import ru.netology.nework.adapter.OnInteractionListenerPost
import ru.netology.nework.adapter.PostsAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentFeedWallBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.PostViewModel
import javax.inject.Inject


@AndroidEntryPoint
class FeedMyWallFragment : Fragment() {
    @Inject
    lateinit var repository: PostRepository

    @Inject
    lateinit var auth: AppAuth
    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedWallBinding.inflate(inflater, container, false)

        //переход на страницу постов
        binding.postsButton.setOnClickListener {
            val fragment = requireParentFragment()
            //проверка текущего фрагмента
            if (fragment !is FeedPostFragment){
                findNavController().navigate(R.id.feedFragment)
            }
        }
        //переход на страницу событий
        binding.eventsButton.setOnClickListener {
            val fragment = requireParentFragment()
            //проверка текущего фрагмента
            if (fragment !is FeedEventFragment) {
                findNavController().navigate(R.id.feedEventFragment)
            }
        }

        //В зависимости от того, зарегистрированы или нет, показываем панель данных пользователя
        authViewModel.data.observe(viewLifecycleOwner) {
            lifecycleScope.launchWhenCreated {
                //если авторизованы, запрашиваем данные пользователя
                if (authViewModel.authenticated) {
                    binding.userBar.visibility = View.VISIBLE
                    binding.fab.isVisible = true
                    requireActivity().setTitle(R.string.my_posts_list)
                } else binding.userBar.visibility = View.GONE
            }
        }

        val adapter = PostsAdapter(object : OnInteractionListenerPost {
            //редактирование поста
            override fun onEdit(post: Post) {
                val bundle = Bundle()
                bundle.putLong("id", post.id)
                //переходим на страницу редактирования, передавая в поле логин значение id поста
                findNavController().navigate(R.id.editPostFragment, EditPostFragment.createArguments(post.id)
                )
            }

            override fun onLike(post: Post) {
                viewModel.like(post.id, post.likedByMe)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onOpenLikes(post: Post) {
                findNavController().navigate(R.id.action_feedMyWallFragment_to_listLikesFragment, PostLikesFragment.createArguments(post.id))
            }
        })

        binding.list.adapter = adapter

        lifecycleScope.launchWhenCreated {
            viewModel.myWallData.collectLatest(adapter::submitData)
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                //Refreshing отображается только при REFRESH
                binding.swiperefresh.isRefreshing =
                    state.refresh is LoadState.Loading

            }
        }

        binding.swiperefresh.setOnRefreshListener(adapter::refresh)

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedMyWallFragment_to_newPostFragment)
        }

        return binding.root
    }


}
