package ru.netology.nework.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.dao.UsersDao
import ru.netology.nework.dto.Users
import ru.netology.nework.viewmodel.PostViewModel
import java.util.*


class DialogUsersSelectFragment : DialogFragment() {

    private val viewModel: PostViewModel by activityViewModels()




    /*fun getUsersList(): List<String> {
        val list: MutableList<String> = ArrayList()
        val listUsers = viewModel.users
        listUsers.map {listUsers->
            for (user in listUsers) {

                list.add(user.name)
            }
        }

        return list

    }*/




    private val catNames = arrayOf("Васька", "Рыжик", "Мурзик","Васька", "Рыжик", "Мурзик")
    private val checkedItems = BooleanArray(catNames.size)



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Выберите котов")
                .setMultiChoiceItems(
                    catNames, checkedItems
                ) { _, which, isChecked ->
                    checkedItems[which] = isChecked
                    // val name = catNames[which] // Get the clicked item
                    // println(name)
                }
                .setPositiveButton(
                    "Готово"
                ) { _, _ ->
                    for (i in catNames.indices) {
                        val checked = checkedItems[i]
                        if (checked) {
                            println(catNames[i])
                        }
                    }
                }
                .setNegativeButton(
                    "Отмена"
                ) { dialog, _ ->
                    dialog.cancel()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
