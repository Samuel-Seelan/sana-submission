package com.sana.app.ui.components

/**
 * reusable UI components shared across Sana screens.
 */

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val SanaDark = Color(0xFF13161C)
val SanaBlue = Color(0xFF4A9EBF)
val SanaCard = Color(0xFF242B3D)
val SanaMuted = Color(0xFF94A3B8)

/**
 * primary filled button used for main actions like Sign Up.
 */
@Composable
fun SanaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SanaBlue)
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

/**
 * secondary outlined button used for secondary actions like Log In.
 */
@Composable
fun SanaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * styled text field matching Sana's dark theme.
 */
@Composable
fun SanaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = SanaMuted) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SanaBlue,
            unfocusedBorderColor = SanaCard,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = SanaCard,
            unfocusedContainerColor = SanaCard
        )
    )
}

/**
 * dropdown menu for selecting from a list of options (e.g. injury type).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanaDropdown(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SanaBlue,
                unfocusedBorderColor = SanaCard,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = SanaCard,
                unfocusedContainerColor = SanaCard
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF13161C)
@Composable
fun PreviewSanaButtons() {
    Column {
        SanaPrimaryButton(text = "Sign up", onClick = {})
        SanaSecondaryButton(text = "Log in", onClick = {})
    }
}