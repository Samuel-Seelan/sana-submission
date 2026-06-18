package com.sana.app.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.sana.app.vision.SquatRepCounter
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.delay

@Composable
fun CameraSessionRecorder(
    shouldRecord: Boolean,
    automaticRepCountingEnabled: Boolean = false,
    squatRepCountingActive: Boolean = false,
    modifier: Modifier = Modifier,
    onRecordingFilePrepared: (String) -> Unit = {},
    onRecordingFinalized: (String) -> Unit = {},
    onRecordingError: (String) -> Unit = {},
    onAutomaticRepCount: (Int) -> Unit = {},
    onPoseStatusChanged: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val squatRepCounter = remember { SquatRepCounter() }
    val poseDetector = remember {
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        PoseDetection.getClient(options)
    }
    val poseFrameInFlight = remember { AtomicBoolean(false) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    var permissionsGranted by remember { mutableStateOf(context.cameraPermissionsGranted()) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var currentOutputFile by remember { mutableStateOf<File?>(null) }
    var statusText by remember { mutableStateOf("Camera preview") }
    var poseOverlay by remember { mutableStateOf(PoseOverlay()) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        permissionsGranted = results.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            )
        }
    }

    if (!permissionsGranted) {
        CameraPermissionPanel(
            modifier = modifier,
            onRequestPermissions = {
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                )
            },
        )
        return
    }

    LaunchedEffect(squatRepCountingActive) {
        squatRepCounter.reset()
        if (squatRepCountingActive) {
            onAutomaticRepCount(0)
            onPoseStatusChanged("Move fully into frame")
        }
    }

    LaunchedEffect(previewView, lifecycleOwner) {
        val provider = context.cameraProvider(mainExecutor)
        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    Quality.HD,
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.SD),
                ),
            )
            .build()
        val capture = VideoCapture.withOutput(recorder)
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        try {
            val cameraSelector = if (provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                capture,
            )
            videoCapture = capture
            statusText = "Camera preview"
        } catch (error: Exception) {
            statusText = "Camera unavailable"
            onRecordingError(error.message ?: "Could not start camera preview.")
        }
    }

    LaunchedEffect(automaticRepCountingEnabled, squatRepCountingActive, previewView) {
        while (automaticRepCountingEnabled && squatRepCountingActive) {
            if (poseFrameInFlight.compareAndSet(false, true)) {
                val bitmap = previewView.bitmap
                if (bitmap == null) {
                    onPoseStatusChanged("Camera preview warming up")
                    poseFrameInFlight.set(false)
                } else {
                    val inputImage = InputImage.fromBitmap(bitmap, 0)
                    poseDetector.process(inputImage)
                        .addOnSuccessListener { pose ->
                            val result = squatRepCounter.update(pose)
                            poseOverlay = pose.toPoseOverlay(
                                imageWidth = bitmap.width,
                                imageHeight = bitmap.height,
                            )
                            onAutomaticRepCount(result.reps)
                            onPoseStatusChanged(result.status)
                        }
                        .addOnFailureListener { error ->
                            onPoseStatusChanged(error.message ?: "Pose detection unavailable")
                        }
                        .addOnCompleteListener {
                            poseFrameInFlight.set(false)
                        }
                }
            }
            delay(350L)
        }
        poseOverlay = PoseOverlay()
    }

    LaunchedEffect(shouldRecord, videoCapture) {
        val capture = videoCapture ?: return@LaunchedEffect
        if (shouldRecord && activeRecording == null) {
            val outputFile = context.createSessionVideoFile()
            currentOutputFile = outputFile
            onRecordingFilePrepared(outputFile.absolutePath)
            statusText = "Recording"

            val outputOptions = FileOutputOptions.Builder(outputFile).build()
            activeRecording = capture.output
                .prepareRecording(context, outputOptions)
                .withAudioEnabled()
                .start(mainExecutor) { event ->
                    when (event) {
                        is VideoRecordEvent.Finalize -> {
                            activeRecording = null
                            val savedUri = event.outputResults.outputUri
                            val path = savedUri.toLocalPathOrFallback(outputFile)
                            if (event.hasError()) {
                                statusText = "Recording failed"
                                onRecordingError(
                                    event.cause?.message ?: "Video recording failed.",
                                )
                            } else {
                                statusText = "Recording saved"
                                onRecordingFinalized(path)
                            }
                        }
                    }
                }
        } else if (!shouldRecord && activeRecording != null) {
            activeRecording?.stop()
            activeRecording = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activeRecording?.stop()
            activeRecording = null
            poseDetector.close()
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.matchParentSize(),
        )
        PoseSkeletonOverlay(
            poseOverlay = poseOverlay,
            modifier = Modifier.matchParentSize(),
        )
        Text(
            text = statusText,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}

@Composable
private fun PoseSkeletonOverlay(
    poseOverlay: PoseOverlay,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val scaleX = if (poseOverlay.imageWidth > 0) size.width / poseOverlay.imageWidth else 1f
        val scaleY = if (poseOverlay.imageHeight > 0) size.height / poseOverlay.imageHeight else 1f

        poseOverlay.connections.forEach { connection ->
            val start = poseOverlay.landmarks[connection.start]
            val end = poseOverlay.landmarks[connection.end]
            if (start != null && end != null) {
                drawLine(
                    color = Color(0xFF7DE3B2),
                    start = start.toOffset(scaleX, scaleY),
                    end = end.toOffset(scaleX, scaleY),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round,
                )
            }
        }

        poseOverlay.landmarks.values.forEach { point ->
            drawCircle(
                color = Color(0xFFFFFFFF),
                radius = 7f,
                center = point.toOffset(scaleX, scaleY),
            )
            drawCircle(
                color = Color(0xFF20C997),
                radius = 4f,
                center = point.toOffset(scaleX, scaleY),
            )
        }
    }
}

private data class PoseOverlay(
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val landmarks: Map<Int, PoseOverlayPoint> = emptyMap(),
    val connections: List<PoseConnection> = PoseConnections,
)

private data class PoseOverlayPoint(
    val x: Float,
    val y: Float,
) {
    fun toOffset(scaleX: Float, scaleY: Float) =
        androidx.compose.ui.geometry.Offset(x * scaleX, y * scaleY)
}

private data class PoseConnection(
    val start: Int,
    val end: Int,
)

private fun com.google.mlkit.vision.pose.Pose.toPoseOverlay(
    imageWidth: Int,
    imageHeight: Int,
): PoseOverlay {
    val landmarks = OverlayLandmarkTypes.mapNotNull { type ->
        val landmark = getPoseLandmark(type)
            ?.takeIf { it.inFrameLikelihood > 0.45f }
            ?: return@mapNotNull null
        type to PoseOverlayPoint(
            x = landmark.position.x,
            y = landmark.position.y,
        )
    }.toMap()

    return PoseOverlay(
        imageWidth = imageWidth,
        imageHeight = imageHeight,
        landmarks = landmarks,
    )
}

private val OverlayLandmarkTypes = listOf(
    PoseLandmark.LEFT_SHOULDER,
    PoseLandmark.RIGHT_SHOULDER,
    PoseLandmark.LEFT_ELBOW,
    PoseLandmark.RIGHT_ELBOW,
    PoseLandmark.LEFT_WRIST,
    PoseLandmark.RIGHT_WRIST,
    PoseLandmark.LEFT_HIP,
    PoseLandmark.RIGHT_HIP,
    PoseLandmark.LEFT_KNEE,
    PoseLandmark.RIGHT_KNEE,
    PoseLandmark.LEFT_ANKLE,
    PoseLandmark.RIGHT_ANKLE,
)

private val PoseConnections = listOf(
    PoseConnection(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER),
    PoseConnection(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW),
    PoseConnection(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST),
    PoseConnection(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW),
    PoseConnection(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST),
    PoseConnection(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP),
    PoseConnection(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP),
    PoseConnection(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP),
    PoseConnection(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE),
    PoseConnection(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE),
    PoseConnection(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE),
    PoseConnection(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE),
)

@Composable
private fun CameraPermissionPanel(
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(Color(0xFF05080F)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Camera and microphone access are needed to record sessions.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRequestPermissions) {
                Text("Allow camera")
            }
        }
    }
}

private fun Context.cameraPermissionsGranted(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED

private fun Context.createSessionVideoFile(): File {
    val directory = File(getExternalFilesDir(null), "session-recordings").apply {
        mkdirs()
    }
    return File(directory, "session-${System.currentTimeMillis()}.mp4")
}

private suspend fun Context.cameraProvider(executor: Executor): ProcessCameraProvider {
    val future = ProcessCameraProvider.getInstance(this)
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        future.addListener(
            {
                continuation.resume(future.get()) { _, _, _ -> }
            },
            executor,
        )
    }
}

private fun Uri.toLocalPathOrFallback(file: File): String =
    path ?: file.absolutePath
