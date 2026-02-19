package com.example.gameoflife.pages

import android.R.attr.name
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.gameoflife.GameData
import com.example.gameoflife.LocalDB
import com.example.gameoflife.Screen
import com.example.gameoflife.components.DialogButton
import com.example.gameoflife.components.DialogTextField
import com.example.gameoflife.ui.theme.GreenColor
import com.example.gameoflife.ui.theme.PrimaryColor
import com.example.gameoflife.ui.theme.RedColor
import com.example.gameoflife.ui.theme.SecondaryColor
import com.example.gameoflife.ui.theme.WhiteColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Preview
@Composable
fun Main(navController: NavController = rememberNavController()) {
    var games by remember { mutableStateOf<List<GameData>>(emptyList()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        games = withContext(Dispatchers.IO) {
            LocalDB
                .getInstance(context.applicationContext)
                .getAll
        }
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
                GameItem(index, item)
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
    if (isDialogVisible) {
        GameDialog(
            onDismiss = { isDialogVisible = false },
            onCreate = {
                isDialogVisible = false
                navController.navigate(Screen.Game.name)
            },
            onDownloadFromFile = {
                isDialogVisible = false
            },
            onDownloadFromServer = {
                isDialogVisible = false
            }
        )
    }
}

@Composable
fun GameItem(number: Int, gameData: GameData) {
    Box(
        modifier = Modifier
            .background(
                color = SecondaryColor,
                shape = RoundedCornerShape(10.dp)
            )
            .fillMaxWidth()
            .padding(10.dp)
            .fillMaxHeight(0.06f),
        contentAlignment = Alignment.CenterStart
    )
    {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "${number}. ${gameData.name}", Modifier.padding(10.dp), color = WhiteColor)
            Text(text = "${gameData.created}", Modifier.padding(10.dp), color = WhiteColor)
        }

    }
}

@Composable
fun GameDialog(
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    onDownloadFromServer: () -> Unit,
    onDownloadFromFile: () -> Unit
) {
    val name = rememberTextFieldState("")
    val ySize = rememberTextFieldState("")
    val xSize = rememberTextFieldState("")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Game") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DialogTextField(name, "Name")
                DialogTextField(ySize, "Y size")
                DialogTextField(xSize, "X size")

                DialogButton(onDownloadFromServer, SecondaryColor, text = "Download from server")
                DialogButton(onDownloadFromFile, SecondaryColor, text = "Download from file")
            }
        },
        dismissButton = {
            DialogButton(onDismiss, RedColor, 0.48f, "Cancel")
        },
        confirmButton = {
            DialogButton(onCreate, GreenColor, 0.48f, "Create")
        }
    )
}