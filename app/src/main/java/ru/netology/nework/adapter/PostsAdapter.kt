package ru.netology.nework.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import ru.netology.nework.R
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.dto.*
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.view.loadCircleCrop
import ru.netology.nework.viewmodel.MediaLifecycleObserver

interface OnInteractionListenerPost {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onOpenLikes(post: Post) {}
}
private val typePost = 1
private val mediaObserver = MediaLifecycleObserver()

class PostsAdapter(
    private val onInteractionListener: OnInteractionListenerPost,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(FeedItemDiffCallback()) {
    //получаем тип элемента
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Post -> typePost
            else -> throw IllegalArgumentException("unknown item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            typePost -> PostViewHolder(
                CardPostBinding.inflate(layoutInflater, parent, false),
                onInteractionListener)
            else -> throw IllegalArgumentException("unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            when (it) {
                is Post -> (holder as? PostViewHolder)?.bind(it)
                else -> throw IllegalArgumentException("unknown item type")
            }
        }
    }

    class PostViewHolder(
        private val binding: CardPostBinding,
        private val onInteractionListener: OnInteractionListenerPost,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.apply {
                MapKitFactory.getInstance().onStart()
                mapView.onStart()
                author.text = post.author
                published.text = post.published
                content.text = post.content
                if (post.link != null) {
                    links.isVisible = true
                    links.text = post.link
                }
                if (post.coords != null) {
                    mapView.isVisible = true
                    mapView.map.move(
                        CameraPosition(Point(post.coords.lat.toDouble(), post.coords.long.toDouble()), 11.0f, 0.0f, 0.0f),
                        Animation(Animation.Type.SMOOTH, 0F),
                        null)
                    //добавляем маркер на карту
                    mapView.map.mapObjects.addPlacemark(Point(post.coords.lat.toDouble(), post.coords.long.toDouble()))
                }
                //количество лайков
                likes.text = post.likeOwnerIds.size.toString()
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
                            attachment.setImageResource(R.drawable.ic_audio_48dp)
                            attachment.setOnClickListener {
                                mediaObserver.apply {
                                    player?.setDataSource(
                                        post.attachment.url
                                    )
                                }.play()
                            }
                        }
                        AttachmentType.VIDEO -> {
                            video.isVisible = true
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
                        }

                    }
                }
                if (post.authorAvatar != null) avatar.loadCircleCrop(post.authorAvatar)
                else avatar.setImageResource(R.drawable.ic_avatar_48dp)
                like.isChecked = post.likedByMe
                menu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE
                menu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.options_post)
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
               likes.setOnClickListener {
                    onInteractionListener.onOpenLikes(post)
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
