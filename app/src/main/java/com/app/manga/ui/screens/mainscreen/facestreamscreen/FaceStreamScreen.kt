@file:OptIn(ExperimentalPermissionsApi::class)

package com.app.manga.ui.screens.mainscreen.facestreamscreen

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toRectF
import com.app.manga.ui.components.camera.CameraPreview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import java.util.concurrent.Executors

@Composable
fun FaceStreamScreen(
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val context = LocalContext.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val detections = remember { mutableStateOf<List<com.google.mediapipe.tasks.components.containers.Detection>>(emptyList()) }

    val faceDetector = remember {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("face_detection_short_range.tflite")
            .build()
        val options = FaceDetector.FaceDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result: FaceDetectorResult, _: MPImage ->
                Log.d("FaceDetection", "Received ${result.detections().size} detections")

                result.detections().forEachIndexed { index, detection ->
                    Log.d("FaceDetection", "Detection $index:")
                    Log.d("FaceDetection", "  Bounding Box: ${detection.boundingBox()}")
                    detection.categories().forEachIndexed { catIndex, category ->
                        Log.d("FaceDetection", "  Category $catIndex: ${category.categoryName()}, Score: ${category.score()}")
                    }
                }


                detections.value = result.detections()
            }
            .build()
        val detector = FaceDetector.createFromOptions(context, options)
        Log.d("FaceDetection", "FaceDetector initialized successfully")
        detector
    }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS or CameraController.IMAGE_CAPTURE)
            setImageAnalysisAnalyzer(cameraExecutor, FaceDetectionAnalyzer(faceDetector))
        }
    }
    controller.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

    var previewSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (cameraPermissionState.status.isGranted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
                        previewSize = Size(size.width.toFloat(), size.height.toFloat())
                    }
            ) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    controller = controller
                )

                if (previewSize != Size.Zero && detections.value.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .drawBehind {
                                val analysisWidth = 480f
                                val analysisHeight = 640f
                                val scaleX = previewSize.width / analysisWidth
                                val scaleY = previewSize.height / analysisHeight
                                val isFrontCamera = controller.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

                                detections.value.forEach { detection ->
                                    val boundingBox = detection.boundingBox()
                                    val left = if (isFrontCamera) {
                                        analysisWidth - boundingBox.right
                                    } else {
                                        boundingBox.left.toFloat()
                                    }
                                    val top = boundingBox.top.toFloat()
                                    val width = boundingBox.width().toFloat()
                                    val height = boundingBox.height().toFloat()

                                    val scaledLeft = left * scaleX
                                    val scaledTop = top * scaleY
                                    val scaledWidth = width * scaleX
                                    val scaledHeight = height * scaleY

                                    drawRect(
                                        color = Color.Green,
                                        topLeft = Offset(scaledLeft, scaledTop),
                                        size = Size(scaledWidth, scaledHeight),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                            }
                    )
                }
            }

        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Camera Permission is not granted. Grant Permission to access Camera.", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            faceDetector.close()
        }
    }
}

private class FaceDetectionAnalyzer(
    private val faceDetector: FaceDetector
) : ImageAnalysis.Analyzer {
    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        Log.d("FaceDetectionAnalyzer", "analyze called")
        val mediaImage = image.image
        if (mediaImage != null) {
            Log.d("FaceDetectionAnalyzer", "mediaImage is not null")
            val mpImage = BitmapUtils.convertYuvToRgb(mediaImage) // Assuming this utility exists
            val imageOptions = ImageProcessingOptions.builder()
                .setRotationDegrees(image.imageInfo.rotationDegrees)
                .build()
            try {
                Log.d("FaceDetectionAnalyzer", "Calling detectAsync with timestamp: ${image.imageInfo.timestamp}")
                faceDetector.detectAsync(mpImage, imageOptions, image.imageInfo.timestamp)
            } catch (e: Exception) {
                Log.e("FaceDetectionAnalyzer", "Error detecting faces", e)
            } finally {
                mpImage.close()
            }
        } else {
            Log.w("FaceDetectionAnalyzer", "mediaImage is null")
        }
        image.close()
    }
}
