package ru.netology.nework.adapter

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController

import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.BuildConfig
import ru.netology.nework.R
import ru.netology.nework.databinding.CardEventBinding
import ru.netology.nework.databinding.CardJobBinding
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.databinding.HeaderBinding
import ru.netology.nework.databinding.SeparatorDateItemBinding
import ru.netology.nework.dto.*
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.ui.AuthFragment.Companion.textArg
import ru.netology.nework.ui.EditPostFragment.Companion.longArgs
import ru.netology.nework.view.loadCircleCrop
import ru.netology.nework.viewmodel.MediaLifecycleObserver

interface OnInteractionListener {
    fun onEdit(job: Job) {}
    fun onRemove(job: Job) {}
    fun onShare(job: Job) {}
}

class JobsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = CardJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }
}

class JobViewHolder(
    private val binding: CardJobBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(job: Job) {
        binding.apply {
            jobName.text = job.name
            jobPosition.text = job.position
            jobStart.text = job.start
            jobFinish.text = job.finish

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(job)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(job)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }


        }
    }
}

class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}