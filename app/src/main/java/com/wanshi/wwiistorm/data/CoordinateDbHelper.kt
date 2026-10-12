package com.wanshi.wwiistorm.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.wanshi.wwiistorm.model.CoordinateRecord

class CoordinateDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_NAME (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "x INTEGER NOT NULL, " +
                "y INTEGER NOT NULL, " +
                "map_name TEXT NOT NULL, " +
                "note TEXT, " +
                "created_at INTEGER NOT NULL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertRecord(record: CoordinateRecord): Long {
        val values = record.toContentValues()
        return writableDatabase.insert(TABLE_NAME, null, values)
    }

    fun getAllRecords(): List<CoordinateRecord> {
        val records = mutableListOf<CoordinateRecord>()
        val cursor = readableDatabase.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "created_at DESC"
        )

        while (cursor.moveToNext()) {
            records.add(CoordinateRecord.fromCursor(cursor))
        }
        cursor.close()
        return records
    }

    fun deleteRecord(id: Long): Int {
        return writableDatabase.delete(TABLE_NAME, "id = ?", arrayOf(id.toString()))
    }

    companion object {
        private const val DATABASE_NAME = "wwii_storm.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "coordinates"
    }
}
