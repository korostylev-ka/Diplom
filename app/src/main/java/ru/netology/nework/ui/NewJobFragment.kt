package ru.netology.nework.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewEventBinding
import ru.netology.nework.databinding.FragmentNewJobBinding
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.StringArg
import ru.netology.nework.util.UriPathHelper
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.JobViewModel
import ru.netology.nework.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*


//фрагмент создания нового события
@AndroidEntryPoint
class NewJobFragment: Fragment() {
    //функция установки даты и времени события
    @SuppressLint("SimpleDateFormat")
    private fun pickDateTime(editText: EditText) {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)
        DatePickerDialog(requireContext(),{ _, year, month, day ->
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val pickedDateTime = Calendar.getInstance()
                pickedDateTime.set(year, month, day, hour, minute)
                val dateTimeLong = pickedDateTime.timeInMillis
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(dateTimeLong)
                //val d = DateTimeFormatter.ofPattern()
                //присваиваем полю значение времени
                editText.setText(date)
            }, startHour, startMinute, true).show()
        }, startYear, startMonth, startDay).show()

    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: JobViewModel by activityViewModels()

    private var fragmentBinding: FragmentNewJobBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewJobBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        /*arguments?.textArg
            ?.let(binding.edit::setText)*/

        //binding.edit.requestFocus()

        //обработка нажатия установки даты start
        binding.setDateStart.setOnClickListener {
            pickDateTime(binding.dateTimeStart)

        }

        //обработка нажатия установки даты finish
        binding.setDateFinish.setOnClickListener {
            pickDateTime(binding.dateTimeFinish)

        }

        lifecycleScope.launchWhenCreated {
            //устанока заголовка
            requireActivity().setTitle(R.string.new_event)
        }


        viewModel.jobCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }


        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    //нажатие на сохранить
                    R.id.save -> {
                        fragmentBinding?.let {
                            //передаем текст события и дату
                            viewModel.changeJob(it.name.text.toString(), it.position.text.toString(), it.dateTimeStart.text.toString(), )
                            viewModel.save()
                            AndroidUtils.hideKeyboard(requireView())
                        }
                        true
                    }
                    else -> false
                }

        }, viewLifecycleOwner)

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}