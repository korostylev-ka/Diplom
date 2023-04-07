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
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.StringArg
import ru.netology.nework.util.UriPathHelper
import ru.netology.nework.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*


//фрагмент создания нового события
@AndroidEntryPoint
class NewEventFragment: Fragment() {

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
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(dateTimeLong)
                //присваиваем полю значение времени
                editText.setText(date)
            }, startHour, startMinute, true).show()
        }, startYear, startMonth, startDay).show()

    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: EventViewModel by activityViewModels()

    private var fragmentBinding: FragmentNewEventBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewEventBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        arguments?.textArg
            ?.let(binding.edit::setText)

        binding.edit.requestFocus()

        //обработка нажатия установки даты
        binding.setDate.setOnClickListener {
            pickDateTime(binding.dateTime)
        }
        //в зависимости от положения переключателя типа события, меняем текст
        binding.switchOnline.setOnClickListener {
            if (binding.switchOnline.isChecked) {
                binding.switchOnline.setText(R.string.event_online)
            } else binding.switchOnline.setText(R.string.event_offline)

        }

        lifecycleScope.launchWhenCreated {
            //устанока заголовка
            requireActivity().setTitle(R.string.new_event)
        }

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> viewModel.changePhoto(it.data?.data)
                }
            }

        //запуск по аудио интенту
        val pickAudioLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    Activity.RESULT_OK -> {
                        //конвертируем путь файла к uri
                        val filePath = UriPathHelper().fileFromContentUri(requireContext(),it.data?.data!! )
                        viewModel.changeAudio(filePath.toUri())
                    }
                }
            }

        //запуск по видео интенту
        val pickVideoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    Activity.RESULT_OK -> {
                        //конвертируем путь файла к uri
                        val filePath = UriPathHelper().fileFromContentUri(requireContext(),it.data?.data!! )
                        viewModel.changeVideo(filePath.toUri())
                    }
                }
            }


        //обработка нажатия на выбрать фото из галереи
        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }

        //прикрепить аудио
        binding.pickAudio.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*")
            pickAudioLauncher.launch(intent)
        }
        //прикрепить видео
        binding.pickVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*")
            pickVideoLauncher.launch(intent)
        }

        binding.fabRemove.setOnClickListener {
            viewModel.changePhoto(null)
            viewModel.changeAudio(null)
            viewModel.changeVideo(null)
        }

        viewModel.eventCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }

            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageURI(it.uri)
        }
        viewModel.audio.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }

            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageResource(R.drawable.ic_audio_48dp)
        }
        viewModel.video.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }
            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageResource(R.drawable.ic_video_48dp)
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
                            val content = it.edit.text.toString()
                            val dateTime = it.dateTime.text.toString()
                            val isOnline = binding.switchOnline.isChecked
                            viewModel.changeContent(content, dateTime, isOnline)
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