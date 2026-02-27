    package com.example.gameoflife

    import android.content.ContentValues
    import android.content.Context
    import android.database.Cursor
    import android.database.sqlite.SQLiteDatabase
    import android.database.sqlite.SQLiteOpenHelper
    import java.time.LocalDateTime
    import java.time.format.DateTimeFormatter

    class LocalDB private constructor(context: Context?) :
        SQLiteOpenHelper(context, "GamesDB.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            val sql = "CREATE TABLE data (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT, " +
                    "width INTEGER NOT NULL, " +
                    "height INTEGER NOT NULL, " +
                    "mapData TEXT NOT NULL, " +
                    "created DATETIME NOT NULL)"

            db.execSQL(sql)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        }

        fun addRow(gameData: GameData) {
            val sql = """
        INSERT INTO data (name, width, height, mapData, created)
        VALUES (?, ?, ?, ?, ?)
    """.trimIndent()

            writableDatabase.use { db ->
                db.compileStatement(sql).apply {
                    bindString(1, gameData.name)
                    bindLong  (2, gameData.cols.toLong())
                    bindLong  (3, gameData.rows.toLong())
                    bindString(4, gameData.mapData ?: "")
                    bindString(5, gameData.created)
                    executeInsert()
                }
            }
        }

        fun updateRow(gameData: GameData): Boolean {
            val values = ContentValues()
            values.put("name", gameData.name)
            values.put("width", gameData.cols)
            values.put("height", gameData.rows)
            values.put("mapData", gameData.mapData)

            try {
                getWritableDatabase().use { db ->
                    val rowsAffected = db.update(
                        "data",
                        values,
                        "id = ?",
                        arrayOf(gameData.id.toString())
                    )
                    return rowsAffected > 0
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return false
            }
        }

        fun deleteRow(id: Int): Boolean {
            try {
                getWritableDatabase().use { db ->
                    db.delete("data", "id = ?", arrayOf<String>(id.toString()))
                    return true
                }
            } catch (e: java.lang.Exception) {
                return false
            }
        }

        val getAll: List<GameData>
            get() = readableDatabase.queryList("SELECT * FROM data") {
                toDBData()
            }

        private fun Cursor.toDBData(): GameData {
            return GameData(
                id = getInt(getColumnIndexOrThrow("id")),
                name = getString(getColumnIndexOrThrow("name")),
                cols = getInt(getColumnIndexOrThrow("width")),
                rows = getInt(getColumnIndexOrThrow("height")),
                created = getString(getColumnIndexOrThrow("created")),
                mapData = getString(getColumnIndexOrThrow("mapData")),
                saveType = SaveType.Local
            )
        }

        inline fun <T> SQLiteDatabase.queryList(
            sql: String,
            args: Array<String>? = null,
            mapper: Cursor.() -> T
        ): List<T> {
            return rawQuery(sql, args).use { cursor ->
                buildList {
                    if (cursor.moveToFirst()) {
                        do {
                            add(cursor.mapper())
                        } while (cursor.moveToNext())
                    }
                }
            }
        }

        companion object {
            @Volatile
            private var INSTANCE: LocalDB? = null

            fun getInstance(context: Context): LocalDB {
                return INSTANCE ?: synchronized(this) {
                    INSTANCE ?: LocalDB(context.applicationContext).also {
                        INSTANCE = it
                    }
                }
            }
        }
    }
