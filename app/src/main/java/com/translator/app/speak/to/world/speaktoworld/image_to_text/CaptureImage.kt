package com.translator.app.speak.to.world.speaktoworld.image_to_text

import android.Manifest
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.SparseIntArray
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.translator.app.speak.to.world.speaktoworld.R
import com.translator.app.speak.to.world.speaktoworld.databinding.FragmentCaptureImageBinding
import com.translator.app.speak.to.world.speaktoworld.view_model.ShareViewModel
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService


class CaptureImage : Fragment() {

    lateinit var binding: FragmentCaptureImageBinding
    val TAG = "CameraXApp"
    private val shareViewModel: ShareViewModel by activityViewModels()

    var cameraControlGlobal:CameraControl? = null


    private val ORIENTATIONS = SparseIntArray()

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 0)
        ORIENTATIONS.append(Surface.ROTATION_90, 90)
        ORIENTATIONS.append(Surface.ROTATION_180, 180)
        ORIENTATIONS.append(Surface.ROTATION_270, 270)
    }



    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    var isCameraGranted = false
    var isTorchOn = false


    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ p->
            p.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value

                if (isGranted){
                    isCameraGranted = true
                    startCamera()
                }


            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val cameraXBinding = FragmentCaptureImageBinding.inflate(inflater, container, false)
        binding = cameraXBinding
        return cameraXBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityResultLauncher.launch(arrayOf(Manifest.permission.CAMERA ))


        binding.imageCaptureButton.setOnClickListener {
            if (isCameraGranted) takePhoto()
            else activityResultLauncher.launch(arrayOf(Manifest.permission.CAMERA ))
            binding.viewFinder.visibility = View.INVISIBLE
        }


        with(binding) {

            imageCaptureButton.visibility = View.VISIBLE
            flashButton.visibility = View.VISIBLE
            imageViewCase.visibility = View.INVISIBLE
            retake.visibility = View.INVISIBLE
            crop.visibility = View.INVISIBLE
            done.visibility = View.INVISIBLE


            retake.setOnClickListener {

                binding.viewFinder.visibility = View.VISIBLE

                flashButton.visibility = View.VISIBLE
                imageCaptureButton.visibility = View.VISIBLE
                imageViewCase.visibility = View.INVISIBLE
                retake.visibility = View.INVISIBLE
                crop.visibility = View.INVISIBLE
                done.visibility = View.INVISIBLE
            }


        }
    }



    private fun takePhoto() {


        Log.d(TAG, "Photo capture 1")
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        Log.d(TAG, "Photo capture 2")
        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraXHome-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = activity?.let {
            ImageCapture.OutputFileOptions
                .Builder(
                    it.contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                .build()
        }

        // Set up image capture listener, which is triggered after photo has
        // been taken

        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()), object :
            ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                if (cameraControlGlobal!=null){
                    isTorchOn = false
                    cameraControlGlobal!!.enableTorch(false)
                    binding.flashButton.setImageResource(R.drawable.ic_baseline_flash_off)
                }



                with(binding) {

                    imageCaptureButton.visibility = View.INVISIBLE
                    flashButton.visibility = View.INVISIBLE

                    imageViewCase.visibility = View.VISIBLE
                    retake.visibility = View.VISIBLE
                    crop.visibility = View.VISIBLE
                    done.visibility = View.VISIBLE


                    val rotation = image.imageInfo.rotationDegrees
                    Log.d(TAG, "onCaptureSuccess 22: $rotation")
                    val matrix = Matrix()
                    matrix.postRotate(rotation.toFloat())
                    val bitmap: Bitmap = imageProxyToBitmap(image)
                    val rotateBitmap = Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                    )
                    imageViewCase.setImageBitmap(rotateBitmap)

                    image.close()

                    crop.setOnClickListener {

                        if (rotateBitmap==null) return@setOnClickListener

                        shareViewModel.bitmap1 = rotateBitmap
                        findNavController().navigate(R.id.action_captureImage2_to_cropImage)
                    }

                    done.setOnClickListener {
                        if (rotateBitmap==null) return@setOnClickListener

                        val width = binding.imageViewCase.width.toFloat()
                        val bm = rotateBitmap

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
                            val bitmap2 = Bitmap.createScaledBitmap(bm, imageWidth.toInt(), imageHeight.toInt(), true)
                            shareViewModel.bitmap1 = bitmap2
                        }else {

                            imageHeight = h
                            screenWidth = ((imageHeight.toFloat()/bm.height)*bm.width)
                            imageWidth = screenWidth
                            val bitmap2 = Bitmap.createScaledBitmap(bm, imageWidth.toInt(), imageHeight.toInt(), true)
                            shareViewModel.bitmap1 = bitmap2
                        }


                        findNavController().popBackStack(R.id.imageToTextFragment2,false)

                    }

                }

            }
        })



//        imageCapture.takePicture(
//            outputOptions,
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageSavedCallback {
//
//
//                override fun onError(exc: ImageCaptureException) {
//                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
//                }
//
//                override fun
//                        onImageSaved(output: ImageCapture.OutputFileResults){
//                    val msg = "Photo capture succeeded: ${output.savedUri}"
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                    Log.d(TAG, msg)
//                }
//            }
//        )

    }


    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun captureVideo() {}

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val orientationEventListener = object : OrientationEventListener(requireContext() ) {
                override fun onOrientationChanged(orientation : Int) {
                    // Monitors orientation values to determine the target rotation value
                    val rotation : Int = when (orientation) {
                        in 45..134 -> Surface.ROTATION_270
                        in 135..224 -> Surface.ROTATION_180
                        in 225..314 -> Surface.ROTATION_90
                        else -> Surface.ROTATION_0
                    }

                    imageCapture!!.targetRotation = rotation
                    Log.d(TAG, "onOrientationChanged: $rotation")
                }
            }
            orientationEventListener.enable()



            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
              val camera =  cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )


                val cameraControl = camera.cameraControl
                cameraControlGlobal = cameraControl

                binding.flashButton.setOnClickListener {
                    if (isTorchOn){
                        isTorchOn = false
                        cameraControl.enableTorch(false)
                        binding.flashButton.setImageResource(R.drawable.ic_baseline_flash_off)
                    }else {
                        isTorchOn = true
                        cameraControl.enableTorch(true)
                        binding.flashButton.setImageResource(R.drawable.ic_baseline_flash_on)
                    }
                }

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ActivityCompat.getMainExecutor(requireContext()))
    }



    override fun onDestroy() {
        super.onDestroy()
        if (::cameraExecutor.isInitialized) cameraExecutor.shutdown()
    }



    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10

        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).toTypedArray()

//        private val REQUIRED_PERMISSIONS =
//            mutableListOf(
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO
//            ).apply {
//                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
//                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                }
//            }.toTypedArray()
    }

}