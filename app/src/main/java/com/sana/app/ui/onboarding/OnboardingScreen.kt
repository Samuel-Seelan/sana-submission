package com.sana.app.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sana.app.model.InjuryProfile
import com.sana.app.model.SampleData
import com.sana.app.ui.components.SectionHeader
import com.sana.app.ui.theme.SanaTheme

/*
 * OnboardingScreen.kt — sign up / log in entry point.
 * What: a tabbed form that either creates an account (name, email, password, injury
 *       selection) or logs in (email, password). Selected injuries drive the recovery plan.
 * Who: Mimo.
 * When: Goal 6 — UI skeleton.
 */

private const val TAB_SIGN_UP = 0
private const val TAB_LOG_IN = 1

@Composable
fun OnboardingScreen(
    onAuthenticated: () -> Unit,
    injuryProfiles: List<InjuryProfile> = SampleData.injuryProfiles,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_SIGN_UP) }
    var name by rememberSaveable { mutableStateOf("") }
    var signUpEmail by rememberSaveable { mutableStateOf("") }
    var signUpPassword by rememberSaveable { mutableStateOf("") }
    var logInEmail by rememberSaveable { mutableStateOf("") }
    var logInPassword by rememberSaveable { mutableStateOf("") }
    var selectedInjuryIds by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(40.dp))
        Text(
            text = "Sana",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Recover with guidance",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
        ) {
            Tab(
                selected = selectedTab == TAB_SIGN_UP,
                onClick = { selectedTab = TAB_SIGN_UP },
                text = { Text("Sign up") },
            )
            Tab(
                selected = selectedTab == TAB_LOG_IN,
                onClick = { selectedTab = TAB_LOG_IN },
                text = { Text("Log in") },
            )
        }
        Spacer(Modifier.height(24.dp))

        if (selectedTab == TAB_SIGN_UP) {
            SignUpForm(
                name = name,
                onNameChange = { name = it },
                email = signUpEmail,
                onEmailChange = { signUpEmail = it },
                password = signUpPassword,
                onPasswordChange = { signUpPassword = it },
                injuryProfiles = injuryProfiles,
                selectedInjuryIds = selectedInjuryIds,
                onToggleInjury = { id ->
                    selectedInjuryIds =
                        if (id in selectedInjuryIds) selectedInjuryIds - id
                        else selectedInjuryIds + id
                },
                onSubmit = onAuthenticated,
            )
        } else {
            LogInForm(
                email = logInEmail,
                onEmailChange = { logInEmail = it },
                password = logInPassword,
                onPasswordChange = { logInPassword = it },
                onSubmit = onAuthenticated,
            )
        }

        Spacer(Modifier.height(32.dp))
        Text(
            text = "Sana supports your recovery but is not medical advice. " +
                    "Always follow your physiotherapist's guidance.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        )
    }
}

@Composable
private fun SignUpForm(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    injuryProfiles: List<InjuryProfile>,
    selectedInjuryIds: Set<String>,
    onToggleInjury: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        PasswordField(value = password, onValueChange = onPasswordChange)

        Spacer(Modifier.height(8.dp))
        SectionHeader(title = "Your injuries")
        Text(
            text = "Select all that apply — your recovery plan is built from these.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        injuryProfiles.forEach { profile ->
            InjuryCard(
                profile = profile,
                selected = profile.id in selectedInjuryIds,
                onToggle = { onToggleInjury(profile.id) },
            )
        }

        SubmitButton(label = "Create account", onClick = onSubmit)
    }
}

@Composable
private fun LogInForm(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        PasswordField(value = password, onValueChange = onPasswordChange)
        SubmitButton(label = "Log in", onClick = onSubmit)
    }
}

@Composable
private fun PasswordField(value: String, onValueChange: (String) -> Unit) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Password") },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Hide password" else "Show password",
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InjuryCard(
    profile: InjuryProfile,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        onClick = onToggle,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Checkbox(checked = selected, onCheckedChange = { onToggle() })
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = profile.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SubmitButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Text(label)
    }
}

@Preview(name = "Onboarding — sign up", showBackground = true, backgroundColor = 0xFF0E1420, heightDp = 1100)
@Composable
private fun OnboardingScreenPreview() {
    SanaTheme {
        OnboardingScreen(onAuthenticated = {})
    }
}
