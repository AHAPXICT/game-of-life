package com.example.gameoflife.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gameoflife.ui.theme.WhiteColor
import java.util.regex.Matcher

@Composable
fun DialogTextField(state: TextFieldState, label: String) {
    TextField(
        state = state,
        label = { Text(label)},
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
fun DialogButton(onClick: () -> Unit, color: Color, width: Float = 1f, text: String) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(color),
        modifier = Modifier.fillMaxWidth(width)
    ) {
        Text(text = text, color = WhiteColor)
    }
}

@Composable
fun ValidateNumberTextField(hasErrors: MutableState<Boolean>, text: MutableState<String>, label: String) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun validateNumber(input: String): Boolean {
        return try {
            val number = input.toInt()
            number in 3..80
        } catch (e: NumberFormatException) {
            false
        }
    }

    val onTextChanged: (String) -> Unit = { input ->
        text.value = input
        hasErrors.value = !validateNumber(input)
        errorMessage = if (hasErrors.value) {
            "Enter a number between 3 and 80"
        } else {
            null
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = text.value,
            onValueChange = onTextChanged,
            label = { Text(label) },
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewValidateNumberTextField() {
    ValidateNumberTextField(remember {mutableStateOf(false)}, remember { mutableStateOf("") }, "label")
}
