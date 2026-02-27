package com.example.gameoflife

import android.R.attr.name
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gameoflife.pages.Game
import com.example.gameoflife.pages.Main

enum class Screen {
    Main,
    Game
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val gameData: MutableState<GameData> = remember { mutableStateOf(GameData()) }
    NavHost(
        navController = navController,
        startDestination = Screen.Main.name
    ) {
        composable(Screen.Main.name) { Main(navController ,gameData) }
        composable(Screen.Game.name) { Game(navController, gameData.value) }
    }
}
