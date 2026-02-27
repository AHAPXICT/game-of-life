package com.example.gameoflife.pages

import GameField
import android.R.attr.height
import android.R.attr.name
import android.util.Log
import android.util.Log.e
import android.widget.Toast
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.gameoflife.API
import com.example.gameoflife.DeletePayload
import com.example.gameoflife.GameData
import com.example.gameoflife.HttpMethod
import com.example.gameoflife.InsertPayload
import com.example.gameoflife.LocalDB
import com.example.gameoflife.R
import com.example.gameoflife.SaveType
import com.example.gameoflife.Screen
import com.example.gameoflife.UpdatePayload
import com.example.gameoflife.components.DialogButton
import com.example.gameoflife.components.DialogTextField
import com.example.gameoflife.components.Parsers
import com.example.gameoflife.components.ValidateNumberTextField
import com.example.gameoflife.ui.theme.GreenColor
import com.example.gameoflife.ui.theme.GreyColor
import com.example.gameoflife.ui.theme.PrimaryColor
import com.example.gameoflife.ui.theme.RedColor
import com.example.gameoflife.ui.theme.SecondaryColor
import com.example.gameoflife.ui.theme.WhiteColor
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun Game(navController: NavController = rememberNavController(), gameData: GameData) {
    val isSettingsDialogVisible = remember { mutableStateOf(false) }
    var isSaveDialogVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = CoroutineScope(Dispatchers.Main)

    val rows: MutableState<Int> = remember { mutableIntStateOf(gameData.rows) }
    val columns: MutableState<Int> = remember { mutableIntStateOf(gameData.cols) }

    val cells = remember(gameData.id) {
        mutableStateMapOf<Pair<Int, Int>, Boolean>().apply {
            putAll(Parsers.stringToMap(gameData.mapData))
        }
    }

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

        GameField(modifier = Modifier.weight(0.86f), rows = rows, cols = columns, cells = cells)

        fun deleteClick() {
            val db = LocalDB.getInstance(context.applicationContext)
            if (gameData.saveType == SaveType.Local) {
                try {
                    db.deleteRow(gameData.id ?: -1)
                }
                catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Exception while deleting", Toast.LENGTH_LONG).show()
                }
            }
            else if(gameData.saveType == SaveType.Server) {
                val payload = DeletePayload(gameData.id!!)
                scope.launch {
                    val apiResponse = withContext(Dispatchers.IO) {
                        API.callApi(
                            "/deleteRow",
                            httpMethod = HttpMethod.POST,
                            requestModel = payload,
                        )
                    }
                }
            }
            navController.navigate(Screen.Main.name)
        }
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
                ActionElement("Delete", R.drawable.outline_delete_24, { deleteClick() })
                ActionElement("Save", R.drawable.outline_save_as_24, { isSaveDialogVisible = true })
            }
        }
    }

    fun saveLocal() {
        isSaveDialogVisible = false
        gameData.mapData = Parsers.mapToString(cells)

        val db = LocalDB.getInstance(context.applicationContext)
        if (gameData.saveType == SaveType.Local && gameData.id != null) {
            db.updateRow(gameData)
        } else {
            if (gameData.saveType == SaveType.None)
            {
                gameData.saveType = SaveType.Local
                db.addRow(gameData)
            }
            else {
                Toast.makeText(context, "File already saved in another way", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun saveOnServer() {
        isSaveDialogVisible = false
        gameData.mapData = Parsers.mapToString(cells)

        if (gameData.saveType == SaveType.Server && gameData.id != null) {
            scope.launch {
                val apiResponse = withContext(Dispatchers.IO) {
                    val updatePayload = UpdatePayload(
                        id = gameData.id!!,
                        width = gameData.cols,
                        height = gameData.rows,
                        name = gameData.name,
                        mapData = gameData.mapData
                    )
                    API.callApi(
                        "/updateRow",
                        httpMethod = HttpMethod.POST,
                        requestModel = updatePayload,
                    )
                }
            }
        } else {
            if (gameData.saveType == SaveType.None)
            {
                gameData.saveType = SaveType.Server
                val addPayload = InsertPayload(
                    width = gameData.cols,
                    height = gameData.rows,
                    name = gameData.name,
                    mapData = gameData.mapData,
                    created = gameData.created
                )

                scope.launch {
                    val apiResponse = withContext(Dispatchers.IO) {
                        API.callApi(
                            "/addRow",
                            httpMethod = HttpMethod.POST,
                            requestModel = addPayload
                        )
                    }
                }
            }
            else {
                Toast.makeText(context, "File already saved in another way", Toast.LENGTH_LONG).show()
            }
        }
    }

    if (isSettingsDialogVisible.value) {
        SettingsDialog(
            onDismiss = { isSettingsDialogVisible.value = false },
            rows,
            columns,
            isSettingsDialogVisible,
            gameData
        )
    }
    if (isSaveDialogVisible) {
        SaveDialog(
            onDismiss = { isSaveDialogVisible = false },
            saveOnServer = { saveOnServer() },
           // saveOnFile = { isSaveDialogVisible = false },
            saveLocal = { saveLocal() }
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
    isSettingsDialogVisible: MutableState<Boolean>,
    gameData: GameData
) {
    val name = rememberTextFieldState(gameData.name)
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

                    gameData.name = name.text.toString()
                    gameData.cols = columnsCount.value.toInt()
                    gameData.rows = columnsCount.value.toInt()
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
  //  saveOnFile: () -> Unit,
    saveOnServer: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Game") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                //DialogButton(saveOnFile, SecondaryColor, text = "Save on file")
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