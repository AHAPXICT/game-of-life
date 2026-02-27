package com.example.gameoflife.components

class Parsers {
    companion object {
        fun mapToString(cells: Map<Pair<Int, Int>, Boolean>): String {
            return cells.entries.joinToString(";") { (key, value) ->
                "${key.first},${key.second},$value"
            }
        }
        fun stringToMap(str: String?): Map<Pair<Int, Int>, Boolean> {
            if (str.isNullOrBlank()) return emptyMap()

            return str.split(";")
                .associate { entry ->
                    val parts = entry.split(",")
                    val x = parts[0].toInt()
                    val y = parts[1].toInt()
                    val value = parts[2].toBoolean()
                    Pair(x, y) to value
                }
        }
    }
}