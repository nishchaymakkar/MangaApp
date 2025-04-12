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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toRectF
import com.app.manga.ui.components.camera.CameraPreview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mediapipe.formats.proto.DetectionProto.Detection
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.OutputHandler
import com.google.mediapipe.tasks.core.OutputHandler.ResultListener
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import org.koin.core.component.getScopeName
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun FaceStreamScreen(
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val context = LocalContext.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Add state for face detections
    val detections = remember { mutableStateOf<List<Detection>>(emptyList()) }

    // Initialize FaceDetector
    val faceDetector = remember {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("face_detection_short_range.tflite")
            .build()
        
        val options = FaceDetector.FaceDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result: FaceDetectorResult, input: MPImage ->
                detections.value = convertToProtoDetections(result.detections())
            }
            .build()
        
        FaceDetector.createFromOptions(context, options)
    }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_ANALYSIS or
                CameraController.IMAGE_CAPTURE
            )
            
            setImageAnalysisAnalyzer(
                cameraExecutor,
                FaceDetectionAnalyzer(faceDetector) { result ->
                    detections.value = convertToProtoDetections(result.detections())
                }
            )
        }
    }
    controller.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (cameraPermissionState.status.isGranted) {
            Box {
                CameraPreview(
                    controller = controller
                )
                
                // Add face detection boxes overlay
                detections.value.map { detection ->
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .drawBehind {
                                val boundingBox = convertFromProtoDetection(detection)
                                val rect = boundingBox.boundingBox()
                                drawRect(
                                    color = Color.Green,
                                    topLeft = Offset(rect.left, rect.top),
                                    size = Size(rect.width(), rect.height()),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                    )
                }
            }
        }
        else if (!cameraPermissionState.status.isGranted){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
            ) {
                Text("Camera Permission is not granted. Grant Permission to access Camera.",
                    textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = {
                    cameraPermissionState.launchPermissionRequest()
                }) {
                    Text("Grant Permission")
                }
            }
        } else {
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
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
    private val faceDetector: FaceDetector,
    private val onResults: (FaceDetectorResult) -> Unit
) : ImageAnalysis.Analyzer {

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage != null) {
            val mpImage = BitmapUtils.convertYuvToRgb(mediaImage)

            val imageOptions = ImageProcessingOptions.builder()
                .setRotationDegrees(image.imageInfo.rotationDegrees)
                .build()

            try {
                faceDetector.detectAsync(
                    mpImage,
                    imageOptions,
                    image.imageInfo.timestamp
                )
                onResults

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                mpImage.close()
            }
        }
        image.close()
    }
}
fun convertToProtoDetections(detections: List<com.google.mediapipe.tasks.components.containers.Detection>): List<com.google.mediapipe.formats.proto.DetectionProto.Detection> {
    return detections.map { detection ->
        // You'll need to manually construct the protobuf Detection object
        com.google.mediapipe.formats.proto.DetectionProto.Detection.newBuilder()
            .addAllLabel(detection.categories().map { it.categoryName() }) // adjust as per available fields
            .build()
    }
}
fun convertFromProtoDetection(protoDetection: com.google.mediapipe.formats.proto.DetectionProto.Detection): com.google.mediapipe.tasks.components.containers.Detection {
    val categories = protoDetection.labelList.map { label ->
        com.google.mediapipe.tasks.components.containers.Category.create(0f,1,label,label) // Set score to 0f (or fetch from proto if available)
    }

    val boundingBox = if (protoDetection.hasLocationData() && protoDetection.locationData.hasBoundingBox()) {
        val box = protoDetection.locationData.boundingBox
        android.graphics.Rect(box.xmin, box.ymin, box.xmin + box.width, box.ymin + box.height)
    } else {
        android.graphics.Rect()
    }

    return com.google.mediapipe.tasks.components.containers.Detection.create(
        categories,
        boundingBox.toRectF()
    )
}
