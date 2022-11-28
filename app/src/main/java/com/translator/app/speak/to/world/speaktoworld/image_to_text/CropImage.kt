package com.translator.app.speak.to.world.speaktoworld.image_to_text

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.translator.app.speak.to.world.speaktoworld.R
import com.translator.app.speak.to.world.speaktoworld.databinding.FragmentCropImageBinding
import com.translator.app.speak.to.world.speaktoworld.view_model.ShareViewModel

class CropImage : Fragment() {

    lateinit var binding: FragmentCropImageBinding
    val TAG = "CameraXApp"
    private val shareViewModel: ShareViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val cameraXBinding = FragmentCropImageBinding.inflate(inflater, container, false)
        binding = cameraXBinding
        return cameraXBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding){

            if (shareViewModel.bitmap1!=null) {

                val x = cv4.test(shareViewModel.bitmap1)
                imageCropButton.setOnClickListener {
                    if (shareViewModel.bitmap1 == null) return@setOnClickListener
                    val bitmap = cv4.test(shareViewModel.bitmap1,1)
                    shareViewModel.bitmap1 = bitmap

                    if (bitmap != null) {
                        shareViewModel.bitmap1 = getResizedBitmap(bitmap,shareViewModel.screenWidth.toInt(),((bitmap.height/bitmap.width.toFloat())*shareViewModel.screenWidth).toInt())

                        findNavController().popBackStack(R.id.imageToTextFragment2,false)
                    }


                }
            }
        }
    }


    fun getResizedBitmap(bm: Bitmap?, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm?.width
        val height = bm?.height
        val scaleWidth = newWidth.toFloat() / width!!
        val scaleHeight = newHeight.toFloat() / height!!
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
    }

}