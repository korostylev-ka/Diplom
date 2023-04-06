package ru.netology.nework.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nework.R
import ru.netology.nework.adapter.*
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentFeedEventBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedEventFragment : Fragment() {
    @Inject
    lateinit var repository: PostRepository

    @Inject
    lateinit var auth: AppAuth
    private val viewModel: EventViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedEventBinding.inflate(inflater, container, false)
        //кнопка События нажата
        binding.eventsButton.isPressed = true

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
                    requireActivity().setTitle(R.string.events_list)
                } else binding.userBar.visibility = View.GONE
            }
        }

        val adapter = EventsAdapter(object : OnInteractionListenerEvent {
            //редактирование поста
            override fun onEdit(event: Event) {
                val bundle = Bundle()
                bundle.putLong("id", event.id)
                //переходим на страницу редактирования, передавая в поле логин значение id события
                findNavController().navigate(R.id.editEventFragment, EditPostFragment.createArguments(event.id)
                )

            }

            override fun onLike(event: Event) {
                viewModel.like(event.id, event.likedByMe)
            }

            override fun onRemove(event: Event) {
                viewModel.removeById(event.id)
            }

            override fun onShare(event: Event) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, event.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onOpenLikes(event: Event) {
                findNavController().navigate(R.id.action_feedEventFragment_to_eventLikesFragment, EventLikesFragment.createArguments(event.id))
            }
        })


        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
            footer = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
        )

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest(adapter::submitData)
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
            findNavController().navigate(R.id.action_feedEventFragment_to_newEventFragment)
        }

        return binding.root
    }
}
