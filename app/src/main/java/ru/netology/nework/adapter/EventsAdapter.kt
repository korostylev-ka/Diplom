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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.BuildConfig
import ru.netology.nework.R
import ru.netology.nework.databinding.CardEventBinding
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.databinding.HeaderBinding
import ru.netology.nework.databinding.SeparatorDateItemBinding
import ru.netology.nework.dto.*
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.ui.AuthFragment.Companion.textArg
import ru.netology.nework.ui.EditPostFragment.Companion.longArgs
import ru.netology.nework.view.loadCircleCrop
import ru.netology.nework.viewmodel.MediaLifecycleObserver

interface OnInteractionListenerEvent {
    fun onLike(event: Event) {}
    fun onEdit(event: Event) {}
    fun onRemove(event: Event) {}
    fun onShare(event: Event) {}
}

private val typeEvent = 1


private val mediaObserver = MediaLifecycleObserver()

class EventsAdapter(
    private val onInteractionListener: OnInteractionListenerEvent,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(FeedItemDiffCallback()) {
    //получаем тип элемента
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            //если тип разделитель, то ссылка на макет с разделителем
            //is DateSeparator -> typeSepararor
            is Event -> typeEvent
            //is Header -> typeHeader
            else -> throw IllegalArgumentException("unknown item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            typeEvent -> EventViewHolder(
                CardEventBinding.inflate(layoutInflater, parent, false),
                onInteractionListener
            )
            else -> throw IllegalArgumentException("unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            when (it) {
                // Holder'a событий
                is Event -> (holder as? EventViewHolder)?.bind(it)
                else -> throw IllegalArgumentException("unknown item type")
            }
        }
    }

    class EventViewHolder(
        private val binding: CardEventBinding,
        private val onInteractionListener: OnInteractionListenerEvent,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {

                author.text = event.author
                published.text = event.published
                content.text = event.content
                like.text = event.likeOwnerIds.size.toString()
                //если вложений нет, view невидима и не занимает места
                if (event.attachment == null) {
                    attachment.isVisible = false
                    //если есть вложение
                } else {
                    when (event.attachment.type) {
                        //если вложение - изображение
                        AttachmentType.IMAGE -> {
                            attachment.isVisible = true
                            Glide.with(binding.attachment)
                                .load(event.attachment.url)
                                .timeout(10_000)
                                .into(binding.attachment)
                        }
                        //если вложение - аудио
                        AttachmentType.AUDIO -> {
                            attachment.isVisible = true
                            attachment.setImageResource(R.drawable.audio_icon)
                            attachment.setOnClickListener {
                                mediaObserver.apply {
                                    player?.setDataSource(
                                        event.attachment.url
                                    )
                                }.play()
                            }
                        }
                        AttachmentType.VIDEO -> {
                            video.apply {
                                setMediaController(MediaController(context))
                                setVideoURI(
                                    Uri.parse(event.attachment.url)
                                )
                                setOnPreparedListener {
                                    start()
                                }
                                setOnCompletionListener {
                                    stopPlayback()
                                }
                            }

                            //attachment.setImageURI(Uri.parse(post.attachment.url))
                            attachment.apply {

                            }

                        }
                        else -> attachment.isVisible = false
                    }
                    //если тип вложения - изображение
                    /*if (post.attachment.type == AttachmentType.IMAGE) {

                }*/


                }
                if (event.authorAvatar != null) avatar.loadCircleCrop(event.authorAvatar)
                else avatar.setImageResource(R.drawable.person_empty)
                like.isChecked = event.likedByMe
                //like.text = "${post.likes}"

                menu.visibility = if (event.ownedByMe) View.VISIBLE else View.INVISIBLE

                menu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.options_post)
                        // TODO: if we don't have other options, just remove dots
                        menu.setGroupVisible(R.id.owned, event.ownedByMe)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    onInteractionListener.onRemove(event)
                                    true
                                }
                                R.id.edit -> {
                                    onInteractionListener.onEdit(event)
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }

                like.setOnClickListener {
                    onInteractionListener.onLike(event)
                }

                share.setOnClickListener {
                    onInteractionListener.onShare(event)
                }
            }
        }
    }

    class FeedItemDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
        override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            //проверяем ситуацию, когда у поста и разделителя может быть одинаковый id
            if (oldItem::class != newItem::class) {
                return false
            }

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {

            return oldItem == newItem
        }
    }
}

