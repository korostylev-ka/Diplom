package ru.netology.nework.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentEditPostBinding
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.LongArg
import ru.netology.nework.util.UriPathHelper
import ru.netology.nework.viewmodel.MediaLifecycleObserver
import ru.netology.nework.viewmodel.PostViewModel


private const val ID = "id"

//фрагмент редактирования поста
@AndroidEntryPoint
class EditPostFragment : Fragment() {
    //для передачи id поста
    companion object {
        var Bundle.longArgs: Long by LongArg
        fun createArguments(id: Long?): Bundle {
            return bundleOf(ID to id)
        }
    }

    private val viewModel: PostViewModel by activityViewModels()

    private var fragmentBinding: FragmentEditPostBinding? = null

    //переменная для редактируемого поста
    var postId: Long? = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEditPostBinding.inflate(
            inflater,
            container,
            false
        )

        //id редактируемого поста
        postId = requireArguments().getLong(ID)
        lifecycleScope.launchWhenCreated {
            requireActivity().setTitle(R.string.editing_post)
            //получаем поста
            val post = viewModel.getPost(postId!!)
            //заполняем данными
            binding.apply {
                edit.setText(post.content)
                link.setText(post.link)
                when (post.attachment?.type) {
                    AttachmentType.IMAGE -> {
                        viewModel.changePhoto(post.attachment.url.toUri())
                        photo.isVisible = true
                        Glide.with(binding.photo)
                            .load(post.attachment.url)
                            .timeout(10_000)
                            .into(binding.photo)

                    }
                    AttachmentType.AUDIO -> {
                        viewModel.changeAudio(post.attachment.url.toUri())
                        photo.isVisible = true
                        binding.photo.setImageResource(R.drawable.ic_audio_48dp)

                    }
                    AttachmentType.VIDEO -> {
                        viewModel.changeVideo(post.attachment.url.toUri())
                        photo.isVisible = true
                        photo.setImageResource(R.drawable.ic_video_48dp)

                    }
                    else -> photo.isVisible = true
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
                            val content = binding.edit.text.toString()
                            var link: String? = null
                            //есть или нет текст ссылки
                            if (!binding.link.text.isEmpty()) {
                                link = binding.link.text.toString()
                            }
                            val postChanged = post.copy(content = content, link = link, attachment = null)
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


        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.fabRemove.setOnClickListener {
            viewModel.changePhoto(null)
            viewModel.changeAudio(null)
            viewModel.changeVideo(null)
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

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}