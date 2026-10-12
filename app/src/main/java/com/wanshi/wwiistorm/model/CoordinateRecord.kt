package com.wanshi.wwiistorm.model

import android.content.ContentValues
import android.database.Cursor

data class CoordinateRecord(
    val id: Long = 0,
    val title: String,
    val x: Int,
    val y: Int,
    val mapName: String = "World Map",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put("title", title)
        values.put("x", x)
        values.put("y", y)
        values.put("map_name", mapName)
        values.put("note", note)
        values.put("created_at", createdAt)
        return values
    }

    companion object {
        fun fromCursor(cursor: Cursor): CoordinateRecord {
            return CoordinateRecord(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                x = cursor.getInt(cursor.getColumnIndexOrThrow("x")),
                y = cursor.getInt(cursor.getColumnIndexOrThrow("y")),
                mapName = cursor.getString(cursor.getColumnIndexOrThrow("map_name")),
                note = cursor.getString(cursor.getColumnIndexOrThrow("note")),
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
            )
        }
    }
}
