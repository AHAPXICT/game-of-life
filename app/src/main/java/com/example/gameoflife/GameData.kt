package com.example.gameoflife

import java.util.Date

data class GameData(
    val id: Int,
    val name: String?,
    val width: Int,
    val height: Int,
    val mapData: String?,
    val created: Date?,
    val modified: Date?
)

