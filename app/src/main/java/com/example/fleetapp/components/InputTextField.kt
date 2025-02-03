package com.example.fleetapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape


@Composable
fun InputTextField(
    text: String,
    fieldName: String,
    onChange: (String) -> Unit,
    isNumber: Boolean = false,
    canEdit: Boolean = true
) {
    val keyboardOptions = if (isNumber) {
        KeyboardOptions(keyboardType = KeyboardType.Number)
    } else {
        KeyboardOptions.Default
    }

    OutlinedTextField(
        value = if (isNumber && fieldName.isBlank()) "" else fieldName,
        onValueChange = onChange,
        placeholder = { Text(text) },
        keyboardOptions = keyboardOptions,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedContainerColor = MaterialTheme.colorScheme.onSecondary,
            disabledContainerColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f),
            focusedTextColor = MaterialTheme.colorScheme.secondary,
            unfocusedTextColor = MaterialTheme.colorScheme.secondary,
            disabledTextColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            focusedPlaceholderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            disabledPlaceholderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            cursorColor = MaterialTheme.colorScheme.secondary
        ),
        enabled = canEdit,
        textStyle = TextStyle(fontSize = 16.sp),
        singleLine = true
    )
}
