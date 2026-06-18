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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.Executor

@Composable
fun CameraSessionRecorder(
    shouldRecord: Boolean,
    modifier: Modifier = Modifier,
    onRecordingFilePrepared: (String) -> Unit = {},
    onRecordingFinalized: (String) -> Unit = {},
    onRecordingError: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
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
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        AndroidView(
            factory = { previewView },
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
