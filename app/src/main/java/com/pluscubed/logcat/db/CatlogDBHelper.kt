package com.pluscubed.logcat.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pluscubed.logcat.util.UtilLogger

class CatlogDBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    private val db: SQLiteDatabase = writableDatabase

    override fun onCreate(db: SQLiteDatabase) {
        val createSql = "create table if not exists $TABLE_NAME ($COLUMN_ID integer not null primary key autoincrement, $COLUMN_TEXT text);"
        val indexSql = "create unique index if not exists index_game_id on $TABLE_NAME ($COLUMN_TEXT);"
        db.execSQL(createSql)
        db.execSQL(indexSql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // do nothing
    }

    fun findFilterItems(): List<FilterItem> {
        synchronized(CatlogDBHelper::class.java) {
            val filters = mutableListOf<FilterItem>()
            db.query(TABLE_NAME, arrayOf(COLUMN_ID, COLUMN_TEXT), null, null, null, null, null).use { cursor ->
                while (cursor.moveToNext()) {
                    val filterItem = FilterItem.create(cursor.getInt(0), cursor.getString(1))
                    filters.add(filterItem)
                }
            }
            log.d("fetched %d filters", filters.size)
            return filters
        }
    }

    fun deleteFilter(id: Int) {
        synchronized(CatlogDBHelper::class.java) {
            val rows = db.delete(TABLE_NAME, "$COLUMN_ID=$id", null)
            log.d("deleted %d filters with id %d", rows, id)
        }
    }

    fun addFilter(text: String): FilterItem? {
        synchronized(CatlogDBHelper::class.java) {
            val contentValues = ContentValues()
            contentValues.put(COLUMN_TEXT, text)
            val result = db.insert(TABLE_NAME, null, contentValues)
            log.d("inserted filter with text %s: %d", text, result)
            if (result == -1L) {
                log.d("attempted to insert duplicate filter")
                return null
            }
            db.query(TABLE_NAME, arrayOf(COLUMN_ID, COLUMN_TEXT), "$COLUMN_TEXT=?", arrayOf(text), null, null, null).use { cursor ->
                cursor.moveToNext()
                return FilterItem.create(cursor.getInt(0), cursor.getString(1))
            }
        }
    }

    companion object {
        private const val DB_NAME = "catlog.db"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "Filters"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_TEXT = "filterText"
        private val log = UtilLogger(CatlogDBHelper::class.java)
    }
}
