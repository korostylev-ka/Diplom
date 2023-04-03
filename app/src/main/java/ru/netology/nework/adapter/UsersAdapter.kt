package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardLikesBinding
import ru.netology.nework.dto.Users
import ru.netology.nework.view.loadCircleCrop

//Адаптер для списка пользователей
class UsersAdapter(
): ListAdapter<Users, UsersViewHolder>(UsersDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder =
        UsersViewHolder(
            CardLikesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class UsersDiffCallback: DiffUtil.ItemCallback<Users>() {
    override fun areContentsTheSame(oldItem: Users, newItem: Users): Boolean = oldItem.id == newItem.id
    override fun areItemsTheSame(oldItem: Users, newItem: Users): Boolean = oldItem == newItem

}

class UsersViewHolder(
    private val binding: CardLikesBinding,
) : RecyclerView.ViewHolder(binding.root) {
    //обновляет элемент
    fun bind(user: Users) {
        with(binding) {
            userName.text = user.name
            if (user.avatar != null) {
                userAvatar.loadCircleCrop(user.avatar)
            } else userAvatar.setImageResource(R.drawable.ic_avatar_48dp)
        }
    }
}