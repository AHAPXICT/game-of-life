package com.example.gameoflife

import android.R.attr.name
import androidx.compose.runtime.Composable
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
    NavHost(
        navController = navController,
        startDestination = Screen.Game.name
    ) {
        composable(Screen.Main.name) { Main(navController) }
        composable(Screen.Game.name) { Game(navController) }
    }
}
