package ru.netology.nework.ui

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.*
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
import ru.netology.nework.ui.AuthFragment.Companion.textArg
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.LongArg
import ru.netology.nework.util.StringArg
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.MediaLifecycleObserver
import ru.netology.nework.viewmodel.PostViewModel
import kotlin.system.measureNanoTime

private const val ID = "id"

//фрагмент редактирования поста
@AndroidEntryPoint
class EditEventFragment: Fragment() {
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

        //id редактируемого события
        eventId = requireArguments().getLong(ID)
        lifecycleScope.async {
            requireActivity().setTitle("Редактирование поста")
            //получаем событие
            val event = viewModel.getEvent(eventId!!)
            binding.apply {
                content.setText(event.content)
                when (event.attachment?.type) {
                    AttachmentType.IMAGE -> {
                        attachment.isVisible = true
                        Glide.with(binding.attachment)
                            .load(event.attachment.url)
                            .timeout(10_000)
                            .into(binding.attachment)
                    }
                    else -> attachment.isVisible = false
                }
            }

            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_new_post, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.save -> {
                            //формируем измененный пост
                            val postChanged = event.copy(content = "binding.content.toString()")
                            viewModel.edit(postChanged)
                            AndroidUtils.hideKeyboard(requireView())
                            true
                        }
                        else -> false
                    }

            }, viewLifecycleOwner)

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

        binding.removePhoto.setOnClickListener {
            viewModel.changePhoto(null)
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
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

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}