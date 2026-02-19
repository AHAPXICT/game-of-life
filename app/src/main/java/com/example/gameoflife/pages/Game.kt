package com.example.gameoflife.pages

import GameField
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.gameoflife.R
import com.example.gameoflife.Screen
import com.example.gameoflife.components.DialogButton
import com.example.gameoflife.components.DialogTextField
import com.example.gameoflife.components.ValidateNumberTextField
import com.example.gameoflife.ui.theme.GreenColor
import com.example.gameoflife.ui.theme.GreyColor
import com.example.gameoflife.ui.theme.PrimaryColor
import com.example.gameoflife.ui.theme.RedColor
import com.example.gameoflife.ui.theme.SecondaryColor
import com.example.gameoflife.ui.theme.WhiteColor
import kotlinx.coroutines.delay

@Preview
@Composable
fun Game(navController: NavController = rememberNavController()) {
    val isSettingsDialogVisible = remember { mutableStateOf(false) }
    var isSaveDialogVisible by remember { mutableStateOf(false) }

    val rows: MutableState<Int> = remember { mutableIntStateOf(10) }
    val columns: MutableState<Int> = remember { mutableIntStateOf(5) }

    Column(
        Modifier.background(color = GreyColor)
    ) {
        Row(
            modifier = Modifier
                .background(
                    color = PrimaryColor,
                    shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
                )
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                .weight(0.08f),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Text(
                text = "Game Of Life",
                Modifier
                    .padding(5.dp)
                    .weight(0.82f),
                color = WhiteColor
            )
            IconButton(onClick = { isSettingsDialogVisible.value = true }) {
                Icon(
                    modifier = Modifier.weight(0.08f),
                    painter = painterResource(id = R.drawable.outline_settings_24),
                    contentDescription = "Settings",
                    tint = WhiteColor
                )
            }
            IconButton(onClick = { navController.navigate(Screen.Main.name) }) {
                Icon(
                    modifier = Modifier.weight(0.1f),
                    painter = painterResource(id = R.drawable.outline_close_24),
                    contentDescription = "Close",
                    tint = WhiteColor
                )
            }
        }

        GameField(modifier = Modifier.weight(0.86f), rows = rows, cols = columns)

        Box(
            modifier = Modifier
                .background(
                    color = PrimaryColor,
                )
                .fillMaxWidth()
                .weight(0.06f),
            contentAlignment = Alignment.Center
        )
        {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ActionElement("Delete", R.drawable.outline_delete_24, {})
                ActionElement("Save", R.drawable.outline_save_as_24, { isSaveDialogVisible = true })
            }
        }
    }

    if (isSettingsDialogVisible.value) {
        SettingsDialog(
            onDismiss = { isSettingsDialogVisible.value = false },
            rows,
            columns,
            isSettingsDialogVisible
        )
    }
    if (isSaveDialogVisible) {
        SaveDialog(
            onDismiss = { isSaveDialogVisible = false },
            saveOnServer = { isSaveDialogVisible = false },
            saveOnFile = { isSaveDialogVisible = false },
            saveLocal = { isSaveDialogVisible = false }
        )
    }
}

@Composable
fun ActionElement(text: String, icon: Int, onClickAction: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .clickable(onClick = onClickAction),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, Modifier.padding(5.dp), color = WhiteColor)
        Icon(
            painter = painterResource(icon),
            contentDescription = text,
            tint = WhiteColor
        )
    }
}

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    rows: MutableState<Int>,
    columns: MutableState<Int>,
    isSettingsDialogVisible: MutableState<Boolean>
) {
    val name = rememberTextFieldState("")
    val rowsCount = remember { mutableStateOf("${rows.value}") }
    val columnsCount = remember { mutableStateOf("${columns.value}") }

    val hasRowsFieldErrors = remember { mutableStateOf(false) }
    val hasColumnsFieldErrors = remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DialogTextField(name, "Name")
                ValidateNumberTextField(hasRowsFieldErrors, rowsCount, "Rows Count")
                ValidateNumberTextField(hasColumnsFieldErrors, columnsCount, "Columns Count")
            }
        },
        dismissButton = {
            DialogButton(onDismiss, RedColor, 0.48f, "Cancel")
        },
        confirmButton = {
            DialogButton({
                if (!hasRowsFieldErrors.value && !hasColumnsFieldErrors.value) {
                    rows.value = rowsCount.value.toInt()
                    columns.value = columnsCount.value.toInt()
                    isSettingsDialogVisible.value = false
                }
            }, GreenColor, 0.48f, "Save")
        }
    )
}

@Composable
fun SaveDialog(
    onDismiss: () -> Unit,
    saveLocal: () -> Unit,
    saveOnFile: () -> Unit,
    saveOnServer: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Game") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DialogButton(saveOnFile, SecondaryColor, text = "Save on file")
                DialogButton(saveOnServer, SecondaryColor, text = "Save on server")
                DialogButton(saveLocal, SecondaryColor, text = "Save local")
            }
        },
        dismissButton = {
            DialogButton(onDismiss, RedColor, text = "Cancel")
        },
        confirmButton = {

        }
    )
}