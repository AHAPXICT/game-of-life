package com.example.gameoflife.pages

import android.R.attr.password
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.gameoflife.API
import com.example.gameoflife.GameData
import com.example.gameoflife.HttpMethod
import com.example.gameoflife.LocalDB
import com.example.gameoflife.SaveType
import com.example.gameoflife.Screen
import com.example.gameoflife.components.DialogButton
import com.example.gameoflife.components.DialogTextField
import com.example.gameoflife.components.ValidateNumberTextField
import com.example.gameoflife.ui.theme.GreenColor
import com.example.gameoflife.ui.theme.PrimaryColor
import com.example.gameoflife.ui.theme.RedColor
import com.example.gameoflife.ui.theme.SecondaryColor
import com.example.gameoflife.ui.theme.WhiteColor
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun Main(navController: NavController = rememberNavController(), gameData: MutableState<GameData>) {
    var games by remember { mutableStateOf<List<GameData>>(emptyList()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {

        val localGames: List<GameData> = withContext(Dispatchers.IO) {
            LocalDB
                .getInstance(context.applicationContext)
                .getAll
        }

        val apiResponse: String = withContext(Dispatchers.IO) {
            API.callApi(
                "/getAll",
                httpMethod = HttpMethod.GET,
            )
        }

        val apiGames: List<GameData> =
            if (apiResponse.isNotEmpty()) {
                Gson().fromJson(
                    apiResponse,
                    Array<GameData>::class.java
                ).toList().map {
                    it.saveType = SaveType.Server
                    it
                }
            } else {
                emptyList()
            }

        games = localGames + apiGames
    }


    var isDialogVisible by remember { mutableStateOf(false) }
    Column {
        Box(
            modifier = Modifier
                .background(
                    color = PrimaryColor,
                    shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
                )
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                .weight(0.08f),
            contentAlignment = Alignment.CenterStart
        )
        {
            Text(text = "Game Of Life", Modifier.padding(5.dp), color = WhiteColor)
        }
        LazyColumn(
            modifier = Modifier
                .padding(vertical = 20.dp)
                .fillMaxHeight(0.84f)
                .weight(0.84f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(games) { index, item ->
                GameItem(index, item, navController, gameData)
            }
        }
        Box(
            modifier = Modifier
                .background(
                    color = PrimaryColor,
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                )
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .weight(0.08f)
                .clickable(onClick = { isDialogVisible = true }),
            contentAlignment = Alignment.Center
        )
        {
            Text(text = "Add new", Modifier.padding(5.dp), color = WhiteColor)
        }
    }

    @Composable
    fun GameDialog(
        onDismiss: () -> Unit,
//        onCreate: () -> Unit,
//        onDownloadFromServer: () -> Unit,
//        onDownloadFromFile: () -> Unit
    ) {
        val name = rememberTextFieldState("JustName")
        val rowsCount = remember { mutableStateOf("${10}") }
        val columnsCount = remember { mutableStateOf("${5}") }

        val hasRowsFieldErrors = remember { mutableStateOf(false) }
        val hasColumnsFieldErrors = remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Create Game") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DialogTextField(name, "Name")
                    ValidateNumberTextField(hasRowsFieldErrors, rowsCount, "Y size")
                    ValidateNumberTextField(hasColumnsFieldErrors, columnsCount, "X size")

//                    DialogButton(onDownloadFromServer, SecondaryColor, text = "Download from server")
//                    DialogButton(onDownloadFromFile, SecondaryColor, text = "Download from file")
                }
            },
            dismissButton = {
                DialogButton(onDismiss, RedColor, 0.48f, "Cancel")
            },
            confirmButton = {
                val nowFormatted = LocalDate.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                )
                DialogButton({
                    if (!hasRowsFieldErrors.value && !hasColumnsFieldErrors.value) {
                        gameData.value = GameData(
                            name = name.text.toString(),
                            rows = rowsCount.value.toInt(),
                            cols = columnsCount.value.toInt(),
                            created = nowFormatted
                        )
                        navController.navigate(Screen.Game.name)
                        isDialogVisible = false
                    }
                }, GreenColor, 0.48f, "Create")
            }
        )
    }
    if (isDialogVisible) {
        GameDialog(
            onDismiss = { isDialogVisible = false },
            //onCreate = { onCreate() },
            //onDownloadFromFile = {
            //    isDialogVisible = false
            //},
            //onDownloadFromServer = {
            //    isDialogVisible = false
            //}
        )
    }
}

@Composable
fun GameItem(
    number: Int,
    gameData: GameData,
    navController: NavController,
    mainGameData: MutableState<GameData>
) {
    Box(
        modifier = Modifier
            .background(
                color = SecondaryColor,
                shape = RoundedCornerShape(10.dp)
            )
            .fillMaxWidth()
            .padding(10.dp)
            .fillMaxHeight(0.06f)
            .clickable { mainGameData.value = gameData; navController.navigate(Screen.Game.name) },
        contentAlignment = Alignment.CenterStart,
    )
    {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "${number}. ${gameData.name}", Modifier.padding(10.dp), color = WhiteColor)
            Text(text = "${gameData.saveType}", Modifier.padding(10.dp), color = WhiteColor)
            Text(text = gameData.created ?: "N/a", Modifier.padding(10.dp), color = WhiteColor)
        }
    }
}