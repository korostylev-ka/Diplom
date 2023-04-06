package ru.netology.nework.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.MediaController
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import hilt_aggregated_deps._ru_netology_nework_api_ApiServiceModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentEditEventBinding

import ru.netology.nework.databinding.FragmentEditPostBinding
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Post
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.ui.AuthFragment.Companion.textArg
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.LongArg
import ru.netology.nework.util.StringArg
import ru.netology.nework.util.UriPathHelper
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.MediaLifecycleObserver
import ru.netology.nework.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.measureNanoTime

private const val ID = "id"

//фрагмент редактирования поста
@AndroidEntryPoint
class EditEventFragment: Fragment() {

    //функция установки даты и времени события
    @SuppressLint("SimpleDateFormat")
    private fun pickDateTime(editText: EditText) {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)
        DatePickerDialog(requireContext(), { _, year, month, day ->
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

    //для передачи id события
    companion object {
        var Bundle.longArgs: Long by LongArg
        fun createArguments(id: Long?): Bundle {
            return bundleOf(ID to id)
        }
    }

    private val viewModel: EventViewModel by activityViewModels()

    private var fragmentBinding: FragmentEditEventBinding? = null

    private val mediaObserver = MediaLifecycleObserver()

    //переменная для редактируемого события
    var eventId: Long? = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEditEventBinding.inflate(
            inflater,
            container,
            false
        )

        //в зависимости от положения переключателя типа события, меняем текст
        binding.switchOnline.setOnClickListener {
            if (binding.switchOnline.isChecked) {
                binding.switchOnline.setText(R.string.event_online)
            } else binding.switchOnline.setText(R.string.event_offline)

        }

        //обработка нажатия установки даты
        binding.setDate.setOnClickListener {
            pickDateTime(binding.dateTime)
        }

        //id редактируемого события
        eventId = requireArguments().getLong(ID)
        lifecycleScope.async {
            requireActivity().setTitle(R.string.editing_event)
            //получаем событие
            val event = viewModel.getEvent(eventId!!)
            binding.apply {
                edit.setText(event.content)
                dateTime.setText(event.datetime)
                when (event.attachment?.type) {
                    AttachmentType.IMAGE -> {
                        photoContainer.isVisible = true
                        Glide.with(binding.photo)
                            .load(event.attachment.url)
                            .timeout(10_000)
                            .into(binding.photo)
                    }
                    AttachmentType.AUDIO -> {
                        photoContainer.isVisible = true
                        viewModel.changeAudio(event.attachment.url.toUri())
                        binding.photo.setImageResource(R.drawable.ic_audio_48dp)

                    }
                    AttachmentType.VIDEO -> {
                        photoContainer.isVisible = true
                        viewModel.changeVideo(event.attachment.url.toUri())
                        photo.setImageResource(R.drawable.ic_video_48dp)

                    }
                    else -> photoContainer.isVisible = false
                }
            }

            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_new_post, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.save -> {
                            //формируем измененное событие
                            val content = binding.edit.text.toString()
                            val dateTime = binding.dateTime.text.toString()
                            val type = when (binding.switchOnline.isChecked) {
                                true -> EventType.ONLINE
                                else -> EventType.OFFLINE
                            }
                            val eventChanged = event.copy(content = content, datetime = dateTime, type = type, attachment = null)
                            viewModel.edit(eventChanged)
                            AndroidUtils.hideKeyboard(requireView())
                            true
                        }
                        else -> false
                    }

            }, viewLifecycleOwner)

        }
        //интент по фото
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

        //выбрать фото
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

        //сделать фото
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

        //удалить все вложения
        binding.fabRemove.setOnClickListener {
            viewModel.changePhoto(null)
            viewModel.changeAudio(null)
            viewModel.changeVideo(null)
        }

        viewModel.eventCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        //подписка на изменения вложений изображения
        viewModel.photo.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }

            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageURI(it.uri)
        }
        //подписка на изменения вложений аудио
        viewModel.audio.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }

            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageResource(R.drawable.ic_audio_48dp)
        }
        //подписка на изменения вложений видео
        viewModel.video.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }
            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageResource(R.drawable.ic_video_48dp)
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}