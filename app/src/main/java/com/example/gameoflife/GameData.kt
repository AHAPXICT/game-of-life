package com.example.gameoflife

data class GameData(
    var id: Int? = null,
    var name: String = "JustGame",
    var rows: Int = 10,
    var cols: Int = 5,
    var mapData: String? = null,
    var created: String? = null,
    var saveType: SaveType = SaveType.None
)

enum class SaveType {
    Local,
    Server,
    File,
    None
}

data class InsertPayload(
    val width: Int,
    val height: Int,
    val name: String,
    val mapData: String?,
    val created: String?
)
data class UpdatePayload(
    val id: Int,
    val width: Int,
    val height: Int,
    val name: String,
    val mapData: String?,
)
data class DeletePayload(
    val id: Int
)
