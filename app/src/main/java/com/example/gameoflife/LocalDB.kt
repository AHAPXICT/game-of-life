package com.example.gameoflife

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class LocalDB private constructor(context: Context?) : SQLiteOpenHelper(context, "GamesDB.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val sql = "CREATE TABLE data (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT, " +
                "width INTEGER NOT NULL, " +
                "height INTEGER NOT NULL, " +
                "mapData TEXT NOT NULL, " +
                "created DATETIME NOT NULL, " +
                "modified DATETIME NOT NULL);"

        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun addRow(name: String?, width: Int, height: Int, mapData: String?) {
        var dateTime: String?
        val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            dateTime = now.format(formatter)

        val sql = "Insert into data (name, width, height, mapData, created, modified) " +
                "Values ('" + name + "', " + width + ", " + height + ", '" + mapData + "', '" + dateTime +
                "', '" + dateTime + "');"

        val db = getWritableDatabase()
        try {
            db.execSQL(sql)
        } catch (ex: Exception) {
            ex.printStackTrace()
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
            width = getInt(getColumnIndexOrThrow("width")),
            height = getInt(getColumnIndexOrThrow("height")),
            created = parseDate(getString(getColumnIndexOrThrow("created"))),
            mapData = getString(getColumnIndexOrThrow("mapData")),
            modified = parseDate(getString(getColumnIndexOrThrow("modified")))
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


    private fun parseDate(dateStr: String?): Date? {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
            return format.parse(dateStr)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun lastId(): Int {
        val sel = "Select id from data ;"
        val db = getReadableDatabase()
        val cur = db.rawQuery(sel, null)
        if (!cur.moveToFirst()) {
            return 0
        }

        var i = 1

        while (cur.moveToNext()) {
            i++
        }
        return i
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
