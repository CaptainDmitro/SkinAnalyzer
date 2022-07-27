package ru.captaindmitro.skinanalyzer.ui

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import org.tensorflow.lite.task.vision.classifier.Classifications
import ru.captaindmitro.skinanalyzer.R
import ru.captaindmitro.skinanalyzer.databinding.FragmentCameraBinding
import ru.captaindmitro.skinanalyzer.utils.ClassificationHelper
import ru.captaindmitro.skinanalyzer.utils.decode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(), ClassificationHelper.ClassifierListener {
    private lateinit var binding: FragmentCameraBinding
    private lateinit var resultTextView: TextView
    private lateinit var openImageButton: ImageButton

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var classificationHelper: ClassificationHelper
    private lateinit var bitmapBuffer: Bitmap
    private lateinit var imageAnalyzer: ImageAnalysis
    private var imageCapture: ImageCapture? = null

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val source = ImageDecoder.createSource(activity?.contentResolver!!, it)
            val bitmap = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
            val res = classifyImage(bitmap)
            findNavController().navigate(
                CameraFragmentDirections.actionCameraFragmentToLesionFragment(
                    it.toString(),
                    res.component1().categories.map { category -> "${category.decode()} %.2f".format(category.score) }.joinToString(" ")
                )
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultTextView = binding.results
        openImageButton = binding.openImageButton

        openImageButton.setOnClickListener {
            getImage.launch("image/*")
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        classificationHelper = ClassificationHelper(context = requireContext(), imageClassifierListener = this, threshold = 0.8f)

        startCamera()
    }

    override fun onResume() {
        super.onResume()

        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                .navigate(CameraFragmentDirections.actionCameraFragmentToPermissionsFragment())
        }
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        activity?.runOnUiThread {
            resultTextView.text =
                results?.component1()?.categories?.joinToString(" ") { category -> "${category.decode()} %.2f".format(category.score) } ?: ""
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            imageAnalyzer = ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { image ->
                        if (!::bitmapBuffer.isInitialized) {
                            bitmapBuffer = Bitmap.createBitmap(
                                image.width,
                                image.height,
                                Bitmap.Config.ARGB_8888
                            )
                        }

                        classifyImage(image)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview,imageCapture, imageAnalyzer)
                val scaleGestureDetector = ScaleGestureDetector(requireContext(),
                    object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        override fun onScale(detector: ScaleGestureDetector): Boolean {
                            val scale = camera.cameraInfo.zoomState.value!!.zoomRatio * detector.scaleFactor
                            camera.cameraControl.setZoomRatio(scale)
                            return true
                        }
                    })

                binding.cameraPreview.setOnTouchListener { view, event ->
                    view.performClick()
                    scaleGestureDetector.onTouchEvent(event)
                    return@setOnTouchListener true
                }

            } catch(exc: Exception) {
                Log.e("MAIN", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))

    }

    private fun classifyImage(image: ImageProxy) {
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }

        classificationHelper.classify(bitmapBuffer)
    }

    //TODO: DRY
    private fun classifyImage(image: Bitmap): List<Classifications> {
        return classificationHelper.getClassifyResults(image)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        cameraExecutor.shutdown()
    }
}