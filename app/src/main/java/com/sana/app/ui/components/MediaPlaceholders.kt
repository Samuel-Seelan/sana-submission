package com.sana.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sana.app.model.Exercise
import com.sana.app.model.ExerciseType
import com.sana.app.model.SampleData
import com.sana.app.ui.theme.SanaSurface
import com.sana.app.ui.theme.SanaSurfaceVariant
import com.sana.app.ui.theme.SanaTheme

/*
 * MediaPlaceholders.kt — stand-ins for the real video player and camera preview.
 * What: in the finished app the demo is a Media3 ExoPlayer and the mirror is a CameraX preview.
 *       Until per-exercise demo videos are produced, ExerciseDemoImage shows a real bundled photo
 *       if one exists (res/drawable named after the exercise id) or a clean placeholder built from a
 *       generic, broad-category icon (strength / mobility / balance / stretch) — never per-exercise
 *       guesswork, so it stays consistent and never looks wrong.
 * Who: Sana team (shared component).
 * When: Goal 7.
 */

/**
 * Placeholder for a demo / recorded video. Shows a play glyph and an optional label,
 * standing in for the real player while keeping the same footprint in the layout.
 */
@Composable
fun VideoPlaceholder(
    modifier: Modifier = Modifier,
    label: String = "Exercise demo video",
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(listOf(SanaSurfaceVariant, SanaSurface))
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.PlayCircle,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(56.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Placeholder for the live camera mirror used during a session. Stands in for the CameraX
 * preview surface so the split-screen session layout renders without camera permissions.
 */
@Composable
fun CameraPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(Color(0xFF05080F)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Camera preview",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f),
            )
        }
    }
}

/**
 * Demo media for an exercise. If a bundled image named after the exercise id exists in res/drawable
 * (e.g. `quad_sets.png`) it is shown; otherwise a clean placeholder is rendered: a generic icon for
 * the exercise's broad category (strength / mobility / balance / stretch) in a tinted badge, the
 * exercise name, and a "Demo video coming soon" caption — consistent and never misleading.
 */
@Composable
fun ExerciseDemoImage(
    exercise: Exercise,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val photoResId = remember(exercise.id) {
        context.resources.getIdentifier(exercise.id, "drawable", context.packageName)
    }

    Box(
        modifier = modifier.background(
            Brush.verticalGradient(listOf(SanaSurfaceVariant, SanaSurface))
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (photoResId != 0) {
            Image(
                painter = painterResource(photoResId),
                contentDescription = "${exercise.name} demonstration",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = categoryIcon(exercise.type),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(60.dp),
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Demo video coming soon",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        }
    }
}

/** A generic icon for each broad exercise category (not a per-exercise depiction). */
private fun categoryIcon(type: String): ImageVector = when (type) {
    ExerciseType.STRENGTH -> Icons.Filled.FitnessCenter
    ExerciseType.MOBILITY -> Icons.AutoMirrored.Filled.DirectionsRun
    ExerciseType.BALANCE -> Icons.Filled.AccessibilityNew
    ExerciseType.STRETCH -> Icons.Filled.SelfImprovement
    else -> Icons.Filled.FitnessCenter
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1420)
@Composable
private fun ExerciseDemoImagePreview() {
    SanaTheme {
        ExerciseDemoImage(
            exercise = SampleData.wallSit,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1420)
@Composable
private fun VideoPlaceholderPreview() {
    SanaTheme {
        VideoPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1420)
@Composable
private fun CameraPlaceholderPreview() {
    SanaTheme {
        CameraPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        )
    }
}
