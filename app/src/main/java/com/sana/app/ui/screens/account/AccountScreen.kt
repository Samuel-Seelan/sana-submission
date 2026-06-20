package com.sana.app.ui.screens.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sana.app.model.InjuryProfile
import com.sana.app.model.SampleData
import com.sana.app.model.UserProfile
import com.sana.app.ui.components.SectionHeader
import com.sana.app.ui.theme.SanaTheme

/*
 * AccountScreen.kt — profile management.
 * What: edit name/email, toggle the user's injuries, change password, and sign out. Edits are saved
 *       to Firestore through the ViewModel; injuries drive exercise recommendations and the plan.
 * Who: Mimo.
 * When: Goal 7 — Firebase integration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit = {},
    user: UserProfile = SampleData.user,
    injuryProfiles: List<InjuryProfile> = SampleData.injuryProfiles,
    initialSelectedInjuryIds: Set<String> = SampleData.selectedInjuryIds,
    onSave: (name: String, email: String, injuryIds: Set<String>) -> Unit = { _, _, _ -> },
    onChangePassword: (String) -> Unit = {},
    message: String? = null,
    onMessageShown: () -> Unit = {},
) {
    var name by rememberSaveable { mutableStateOf(user.name) }
    var email by rememberSaveable { mutableStateOf(user.email) }
    var selectedInjuryIds by remember { mutableStateOf(initialSelectedInjuryIds) }
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(user.name, user.email, initialSelectedInjuryIds) {
        name = user.name
        email = user.email
        selectedInjuryIds = initialSelectedInjuryIds
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileSection(
                name = name,
                onNameChange = { name = it },
                email = email,
                onEmailChange = { email = it },
                onSave = { onSave(name, email, selectedInjuryIds) },
            )
            InjuriesSection(
                injuryProfiles = injuryProfiles,
                selectedInjuryIds = selectedInjuryIds,
                onToggleInjury = { id ->
                    selectedInjuryIds =
                        if (id in selectedInjuryIds) selectedInjuryIds - id
                        else selectedInjuryIds + id
                },
                onSave = { onSave(name, email, selectedInjuryIds) },
            )
            PasswordSection(
                currentPassword = currentPassword,
                onCurrentPasswordChange = { currentPassword = it },
                newPassword = newPassword,
                onNewPasswordChange = { newPassword = it },
                onChangePassword = {
                    onChangePassword(newPassword)
                    currentPassword = ""
                    newPassword = ""
                },
            )
            SignOutButton(onSignOut = onSignOut)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ProfileSection(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    SectionCard {
        SectionHeader(title = "Profile")
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = onSave,
            enabled = name.isNotBlank() && email.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save profile")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun InjuriesSection(
    injuryProfiles: List<InjuryProfile>,
    selectedInjuryIds: Set<String>,
    onToggleInjury: (String) -> Unit,
    onSave: () -> Unit,
) {
    SectionCard {
        SectionHeader(title = "Injuries")
        Text(
            text = "Your injuries drive exercise recommendations and your curated plan.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            injuryProfiles.forEach { profile ->
                val selected = profile.id in selectedInjuryIds
                FilterChip(
                    selected = selected,
                    onClick = { onToggleInjury(profile.id) },
                    label = { Text(profile.name) },
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
        if (selectedInjuryIds.isEmpty()) {
            Text(
                text = "Select at least one injury.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Button(
            onClick = onSave,
            enabled = selectedInjuryIds.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save injuries")
        }
    }
}

@Composable
private fun PasswordSection(
    currentPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    onChangePassword: () -> Unit,
) {
    SectionCard {
        SectionHeader(title = "Password")
        OutlinedTextField(
            value = currentPassword,
            onValueChange = onCurrentPasswordChange,
            label = { Text("Current password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            label = { Text("New password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            supportingText = { Text("At least 6 characters") },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = onChangePassword,
            enabled = currentPassword.isNotEmpty() && newPassword.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Change password")
        }
    }
}

@Composable
private fun SignOutButton(onSignOut: () -> Unit) {
    OutlinedButton(
        onClick = onSignOut,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text("Sign out")
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}

@Preview(name = "Account", showBackground = true, backgroundColor = 0xFF0E1420, heightDp = 1100)
@Composable
private fun AccountScreenPreview() {
    SanaTheme {
        AccountScreen(onBack = {})
    }
}
