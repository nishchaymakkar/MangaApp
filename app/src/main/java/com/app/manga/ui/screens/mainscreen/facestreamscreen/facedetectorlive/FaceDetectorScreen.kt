package com.app.manga.ui.screens.mainscreen.facestreamscreen.facedetectorlive


import androidx.compose.material3.Button

import androidx.compose.material3.Text
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FaceDetectorScreen() {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var detectorState by remember { mutableStateOf(DetectorState()) }
    var detectionResults by remember { mutableStateOf<FaceDetectorResult?>(null) }
    var inferenceTime by remember { mutableLongStateOf(0L) }
    var previewWidth by remember { mutableIntStateOf(0) }
    var previewHeight by remember { mutableIntStateOf(0) }

    val faceDetectorHelper by remember {
        mutableStateOf(
            FaceDetectorComposableHelper(
                context = context,
                threshold = detectorState.threshold,
                currentDelegate = detectorState.delegate,
                onResults = { resultBundle ->
                    detectionResults = resultBundle.results.firstOrNull()
                    inferenceTime = resultBundle.inferenceTime
                    previewWidth = resultBundle.inputImageWidth
                    previewHeight = resultBundle.inputImageHeight
                },
                onError = { error, errorCode ->
                    Log.e("FaceDetector", "Error: $error (code: $errorCode)")
                    if (errorCode == FaceDetectorComposableHelper.GPU_ERROR) {
                        detectorState = detectorState.copy(delegate = FaceDetectorComposableHelper.DELEGATE_CPU)
                    }
                }
            )
        )
    }

    LaunchedEffect(detectorState) {
        withContext(Dispatchers.IO) {
            faceDetectorHelper.apply {
                threshold = detectorState.threshold
                currentDelegate = detectorState.delegate
                setupFaceDetector()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (!cameraPermissionState.status.isGranted) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Grant Camera Permission")
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        val previewView = PreviewView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder()
                                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                                .build()
                                .also { it.surfaceProvider = previewView.surfaceProvider }

                            val imageAnalyzer = ImageAnalysis.Builder()
                                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
                                .build()
                                .also {
                                    it.setAnalyzer(
                                        Dispatchers.IO.asExecutor(),
                                        faceDetectorHelper::detectLivestreamFrame
                                    )
                                }

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                                .build()

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalyzer
                                )
                            } catch (e: Exception) {
                                Log.e("FaceDetector", "Use case binding failed", e)
                            }

                        }, ContextCompat.getMainExecutor(context))

                        previewView
                    }
                )

                detectionResults?.let { result ->
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val viewWidth = size.width
                        val viewHeight = size.height

                        val previewAspect = previewWidth.toFloat() / previewHeight
                        val viewAspect = viewWidth / viewHeight
                        val scaleX: Float
                        val scaleY: Float
                        val offsetX: Float
                        val offsetY: Float

                        if (previewAspect > viewAspect) {
                            scaleY = viewHeight / previewHeight
                            scaleX = scaleY
                            offsetX = (viewWidth - previewWidth * scaleX) / 2
                            offsetY = 0f
                        } else {

                            scaleX = viewWidth / previewWidth
                            scaleY = scaleX
                            offsetX = 0f
                            offsetY = (viewHeight - previewHeight * scaleY) / 2
                        }

                        for (detection in result.detections()) {
                            val boundingBox = detection.boundingBox()


                            val left = boundingBox.left * scaleX + offsetX
                            val top = boundingBox.top * scaleY + offsetY
                            val right = boundingBox.right * scaleX + offsetX
                            val bottom = boundingBox.bottom * scaleY + offsetY

                            drawRect(
                                color = Color.Green,
                                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                                size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                                style = Stroke(width = 8f)
                            )
                        }
                    }
                }
            }

        }
    }

    DisposableEffect(Unit) {
        onDispose {
            faceDetectorHelper.clearFaceDetector()
        }
    }
}

data class DetectorState(
    val threshold: Float = 0.6f,
    val delegate: Int = FaceDetectorComposableHelper.DELEGATE_CPU
)

class FaceDetectorComposableHelper(
    var threshold: Float = THRESHOLD_DEFAULT,
    var currentDelegate: Int = DELEGATE_CPU,
    val context: Context,
    private val onResults: (ResultBundle) -> Unit,
    private val onError: (String, Int) -> Unit
) {
    private var faceDetector: FaceDetector? = null

    init {
        setupFaceDetector()
    }

    fun clearFaceDetector() {
        faceDetector?.close()
        faceDetector = null
    }

    fun setupFaceDetector() {
        val baseOptionsBuilder = BaseOptions.builder()

        when (currentDelegate) {
            DELEGATE_CPU -> {
                baseOptionsBuilder.setDelegate(Delegate.CPU)
            }
            DELEGATE_GPU -> {
                baseOptionsBuilder.setDelegate(Delegate.GPU)
            }
        }

        val modelName = "face_detection_short_range.tflite"
        baseOptionsBuilder.setModelAssetPath(modelName)

        try {
            val optionsBuilder = FaceDetector.FaceDetectorOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setMinDetectionConfidence(threshold)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener(this::returnLivestreamResult)
                .setErrorListener(this::returnLivestreamError)

            val options = optionsBuilder.build()
            faceDetector = FaceDetector.createFromOptions(context, options)
        } catch (e: IllegalStateException) {
            onError("Face detector failed to initialize. See error logs for details", OTHER_ERROR)
            Log.e(TAG, "TFLite failed to load model with error: ${e.message}")
        } catch (e: RuntimeException) {
            onError("Face detector failed to initialize. See error logs for details", GPU_ERROR)
            Log.e(TAG, "Face detector failed to load model with error: ${e.message}")
        }
    }

    fun isClosed(): Boolean {
        return faceDetector == null
    }

    fun detectLivestreamFrame(imageProxy: androidx.camera.core.ImageProxy) {
        val frameTime = android.os.SystemClock.uptimeMillis()

        val bitmapBuffer = Bitmap.createBitmap(
            imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
        )

        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
        imageProxy.close()

        val matrix = android.graphics.Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

            postScale(
                -1f, 1f,
                imageProxy.width.toFloat(),
                imageProxy.height.toFloat()
            )
        }

        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer,
            0, 0,
            bitmapBuffer.width, bitmapBuffer.height,
            matrix, true
        )

        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        faceDetector?.detectAsync(mpImage, frameTime)
    }

    private fun returnLivestreamResult(result: FaceDetectorResult, input: com.google.mediapipe.framework.image.MPImage) {
        val finishTimeMs = android.os.SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()

        onResults(
            ResultBundle(
                listOf(result),
                inferenceTime,
                input.height,
                input.width
            )
        )
    }

    private fun returnLivestreamError(error: RuntimeException) {
        onError(error.message ?: "An unknown error has occurred", OTHER_ERROR)
    }

    data class ResultBundle(
        val results: List<FaceDetectorResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val THRESHOLD_DEFAULT = 0.5F
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1

        const val TAG = "FaceDetectorHelper"
    }
}