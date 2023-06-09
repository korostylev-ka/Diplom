package ru.netology.nework.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewJobBinding
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.StringArg
import ru.netology.nework.viewmodel.JobViewModel
import java.text.SimpleDateFormat
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
        //устанавливаем только дату
        DatePickerDialog(requireContext(),{ _, year, month, day ->
                val pickedDateTime = Calendar.getInstance()
                pickedDateTime.set(year, month, day)
                val dateTimeLong = pickedDateTime.timeInMillis
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(dateTimeLong)
                //присваиваем полю значение времени
                editText.setText(date)
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


        binding.name.requestFocus()
        binding.position.requestFocus()
        binding.link.requestFocus()


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
            requireActivity().setTitle(R.string.new_job)
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
                            //передаем текст события и даты
                            val name = it.name.text.toString()
                            val position = it.position.text.toString()
                            var link: String? = null
                            val dateStart = it.dateTimeStart.text.toString()
                            var dateFinish: String? = null
                            if (!it.dateTimeFinish.text.isEmpty()) {
                                dateFinish = it.dateTimeFinish.text.toString()
                            }
                            if (!it.link.text.isEmpty()) {
                                link = it.link.text.toString()
                            }
                            viewModel.changeJob(name, position, dateStart, dateFinish, link)
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