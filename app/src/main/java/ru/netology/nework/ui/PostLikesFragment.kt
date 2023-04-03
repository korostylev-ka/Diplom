package ru.netology.nework.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import ru.netology.nework.R
import ru.netology.nework.adapter.UsersAdapter

import ru.netology.nework.databinding.FragmentPostLikesBinding
import ru.netology.nework.dto.Users
import ru.netology.nework.util.LongArg
import ru.netology.nework.viewmodel.PostViewModel

private const val ID = "id"

//Фрагмент просмотра списка пользователей, поставивших лайк
class PostLikesFragment: Fragment() {
    //для передачи id поста
    companion object {
        var Bundle.longArgs: Long by LongArg
        fun createArguments(id: Long?): Bundle {
            return bundleOf(ID to id)
        }


    }

    private val viewModel: PostViewModel by activityViewModels()

    private var fragmentBinding: FragmentPostLikesBinding? = null

    //переменная для редактируемого поста
    var postId: Long? = 0

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostLikesBinding.inflate(inflater, container, false)

        //id поста
        postId = requireArguments().getLong(ID)
        val text: String = getString(R.string.post) + " " + postId.toString() + " " + getString(R.string.liked_users)
        binding.likeId.text = text
        lifecycleScope.launchWhenCreated {
            val post = viewModel.getPost(postId!!)
            val list = viewModel.getLikedUsers(post)
            val adapter = UsersAdapter()
            adapter.submitList(list)

            binding.listLikes.adapter = adapter
        }




        return binding.root
    }
}
