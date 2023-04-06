package ru.netology.nework.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.crazylegend.audiopicker.audios.AudioModel
import com.crazylegend.audiopicker.pickers.MultiAudioPicker
import com.crazylegend.audiopicker.pickers.SingleAudioPicker
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.StringArg
import ru.netology.nework.util.UriPathHelper
import ru.netology.nework.viewmodel.PostViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()

    private var fragmentBinding: FragmentNewPostBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        arguments?.textArg
            ?.let(binding.edit::setText)

        binding.edit.requestFocus()

        lifecycleScope.launchWhenCreated {
            requireActivity().setTitle(R.string.new_post)
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

        //прикрепить фото
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

        //удалить вложенные файлы
        binding.fabRemove.setOnClickListener {
            viewModel.changePhoto(null)
            viewModel.changeAudio(null)
            viewModel.changeVideo(null)
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

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        fragmentBinding?.let {
                            val content = it.edit.text.toString()
                            var link: String? = null
                            //есть или нет текст ссылки
                            if (!it.link.text.isEmpty()) {
                                link = it.link.text.toString()
                            }
                            viewModel.changeContent(content, link)
                            viewModel.save()
                            AndroidUtils.hideKeyboard(requireView())
                        }
                        true
                    }
                    else -> false
                }

        }, viewLifecycleOwner)

        binding.addUsers.setOnClickListener {
            /*val myDialogFragment = DialogUsersSelectFragment()
            val manager = parentFragmentManager
            myDialogFragment.show(manager, "myDialog")*/
            lifecycleScope.launchWhenCreated {
                println("ЗАПРОС ${viewModel.getUsersNames()}")
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }

}