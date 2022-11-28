package com.translator.app.speak.to.world.speaktoworld.image_to_text

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.translator.app.speak.to.world.speaktoworld.R
import com.translator.app.speak.to.world.speaktoworld.databinding.FragmentPickImageBinding
import com.translator.app.speak.to.world.speaktoworld.view_model.ShareViewModel


class PickImage : Fragment() {


    lateinit var binding: FragmentPickImageBinding
    val TAG = "CameraXApp"
    private val shareViewModel: ShareViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val cameraXBinding = FragmentPickImageBinding.inflate(inflater, container, false)
        binding = cameraXBinding
        return cameraXBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))



        with(binding) {


            crop.setOnClickListener {
                findNavController().navigate(R.id.action_pickImage2_to_cropImage)
            }

            done.setOnClickListener {
                findNavController().popBackStack(R.id.imageToTextFragment2,false)
            }

            retake.setOnClickListener {

                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }


    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            shareViewModel.uri = uri
            if (shareViewModel.uri != null) {
                val stream = activity?.contentResolver?.openInputStream(shareViewModel.uri!!)
//                shareViewModel.bitmap1 = BitmapFactory.decodeStream(stream)
                binding.imageViewCase.setImageURI(shareViewModel.uri)

                val width = binding.imageViewCase.width.toFloat()
                shareViewModel.screenWidth = width

                Log.d(TAG, "Width: $width")


                val bm = BitmapFactory.decodeStream(stream)

                val aspectRatio = bm.width/bm.height.toFloat()

                var screenWidth = 0f
                val screenHeight = 0f
                var imageWidth = 0f
                var imageHeight = 0f

                val w = width
                val h = (w/3f)*6
                val r = w/h.toFloat()

                if (r<=aspectRatio){
                    screenWidth = w
                    imageWidth = screenWidth
                    imageHeight = ((screenWidth.toFloat() / bm.width) * bm.height)
                    val bitmap = Bitmap.createScaledBitmap(bm, imageWidth.toInt(), imageHeight.toInt(), true)
                    shareViewModel.bitmap1 = bitmap
                }else {

                    imageHeight = h
                    screenWidth = ((imageHeight.toFloat()/bm.height)*bm.width)
                    imageWidth = screenWidth
                    val bitmap = Bitmap.createScaledBitmap(bm, imageWidth.toInt(), imageHeight.toInt(), true)
                    shareViewModel.bitmap1 = bitmap
                }


            }
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

}