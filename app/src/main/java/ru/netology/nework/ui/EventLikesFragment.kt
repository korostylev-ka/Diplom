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
import ru.netology.nework.databinding.FragmentEventLikesBinding

import ru.netology.nework.dto.Users
import ru.netology.nework.util.LongArg
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.PostViewModel

private const val ID = "id"

//Фрагмент просмотра списка пользователей, поставивших лайк
class EventLikesFragment: Fragment() {
    //для передачи id события
    companion object {
        var Bundle.longArgs: Long by LongArg
        fun createArguments(id: Long?): Bundle {
            return bundleOf(ID to id)
        }

    }
    private val viewModel: EventViewModel by activityViewModels()

    private var fragmentBinding: FragmentEventLikesBinding? = null

    //переменная для редактируемого события
    var eventId: Long? = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEventLikesBinding.inflate(inflater, container, false)

        //id события
        eventId = requireArguments().getLong(ID)
        val text: String = getString(R.string.post) + " " + eventId.toString() + " " + getString(R.string.liked_users)
        binding.likeId.text = text
        lifecycleScope.launchWhenCreated {
            val event = viewModel.getEvent(eventId!!)
            val list = viewModel.getLikedUsers(event)
            val adapter = UsersAdapter()
            adapter.submitList(list)
            binding.listLikes.adapter = adapter
        }
        return binding.root
    }
}
