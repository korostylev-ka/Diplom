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

private val typeEvent = 1

private val mediaObserver = MediaLifecycleObserver()

class EventsAdapter(
    private val onInteractionListener: OnInteractionListener,
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
            //если разделитель по дате поста, создадим его viewHolder
            /*typeSepararor -> DateSeparatorViewHolder(
                SeparatorDateItemBinding.inflate(layoutInflater, parent, false),
                onInteractionListener
            )*/
            typeEvent -> PostViewHolder(
                CardEventBinding.inflate(layoutInflater, parent, false),
                onInteractionListener
            )
            /*typeHeader -> HeaderViewHolder(
                HeaderBinding.inflate(layoutInflater, parent, false),

            )*/
            else -> throw IllegalArgumentException("unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            when (it) {
                //если пост, приводим к view
                // Holder'a поста
                is Post -> (holder as? PostViewHolder)?.bind(it)
                //is DateSeparator -> (holder as? DateSeparatorViewHolder)?.bind(it)
                //is Header -> (holder as? HeaderViewHolder)?.bind(it)
                else -> throw IllegalArgumentException("unknown item type")
            }
        }
    }

    class PostViewHolder(
        private val binding: CardEventBinding,
        private val onInteractionListener: OnInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.apply {

                author.text = post.author
                published.text = post.published
                content.text = post.content
                like.text = post.likeOwnerIds.size.toString()
                //если вложений нет, view невидима и не занимает места
                if (post.attachment == null) {
                    attachment.isVisible = false
                    //если есть вложение
                } else {
                    when (post.attachment.type) {
                        //если вложение - изображение
                        AttachmentType.IMAGE -> {
                            attachment.isVisible = true
                            Glide.with(binding.attachment)
                                .load(post.attachment.url)
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
                                        post.attachment.url
                                    )
                                }.play()
                            }
                        }
                        AttachmentType.VIDEO -> {
                            video.apply {
                                setMediaController(MediaController(context))
                                setVideoURI(
                                    Uri.parse(post.attachment.url)
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
                if (post.authorAvatar != null) avatar.loadCircleCrop(post.authorAvatar)
                else avatar.setImageResource(R.drawable.person_empty)
                like.isChecked = post.likedByMe
                //like.text = "${post.likes}"

                menu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

                menu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.options_post)
                        // TODO: if we don't have other options, just remove dots
                        menu.setGroupVisible(R.id.owned, post.ownedByMe)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    onInteractionListener.onRemove(post)
                                    true
                                }
                                R.id.edit -> {

                                    onInteractionListener.onEdit(post)
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }

                like.setOnClickListener {
                    onInteractionListener.onLike(post)
                }

                share.setOnClickListener {
                    onInteractionListener.onShare(post)
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

