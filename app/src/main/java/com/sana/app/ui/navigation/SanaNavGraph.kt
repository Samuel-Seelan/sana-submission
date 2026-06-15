package com.sana.app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/*
 * WHO: Developers working on the Sana app skeleton.
 * WHAT: Temporary placeholder navigation graph while real routes and screens are not created yet.
 * WHEN: Used during early UI setup to prevent missing screen imports from crashing the build.
 */
@Composable
fun SanaNavGraph() {
    SanaNavGraphPlaceholder()
}

/*
 * WHO: Developers previewing or running the app before navigation is fully wired.
 * WHAT: Displays a simple placeholder instead of real navigation routes.
 * WHEN: Used until HomeScreen, AccountScreen, SessionScreen, and other route screens exist.
 */
@Composable
private fun SanaNavGraphPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Sana",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Navigation placeholder",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = "Screens and routes will be connected later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}