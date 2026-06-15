package com.sana.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sana.app.ui.theme.SanaSurface
import com.sana.app.ui.theme.SanaSurfaceVariant
import com.sana.app.ui.theme.SanaTheme

/*
 * MediaPlaceholders.kt — stand-ins for the real video player and camera preview.
 * What: in the finished app these are a Media3 ExoPlayer and a CameraX preview. For the
 *       UI skeleton (no backend, no permissions) we render styled placeholder surfaces so
 *       the layout, spacing, and previews all look correct without any device hardware.
 * Who: Sana team (shared component).
 * When: Goal 6 — UI skeleton.
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
