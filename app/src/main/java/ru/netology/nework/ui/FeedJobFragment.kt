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
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.adapter.*
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentFeedJobBinding
import ru.netology.nework.dto.Job
import ru.netology.nework.repository.JobRepository
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.JobViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedJobFragment : Fragment() {
    @Inject
    lateinit var repository: JobRepository
    @Inject
    lateinit var auth: AppAuth
    private val viewModel: JobViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedJobBinding.inflate(inflater, container, false)


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
                    requireActivity().setTitle(R.string.jobs_list)
                } else binding.userBar.visibility = View.GONE
            }
        }

        val adapter = JobsAdapter(object : OnInteractionListener {
            //редактирование job
            override fun onEdit(job: Job) {
                val bundle = Bundle()
                bundle.putLong("id", job.id)
                //переходим на страницу редактирования, передавая в поле логин значение id поста
                findNavController().navigate(R.id.action_feedJobFragment_to_editJobFragment, EditJobFragment.createArguments(job.id)
                )
            }

            override fun onRemove(job: Job) {
                viewModel.removeById(job.id)
            }

            override fun onShare(job: Job) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, job.name)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

        })

        binding.list.adapter = adapter

        lifecycleScope.launchWhenCreated {
            viewModel.data.observe(viewLifecycleOwner) {state->
                adapter.submitList(state.jobs)
            }
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.newJobFragment)
        }

        return binding.root
    }
}
