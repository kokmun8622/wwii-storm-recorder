package com.wanshi.wwiistorm.export

import android.content.Context
import android.os.Environment
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.wanshi.wwiistorm.model.CoordinateRecord

object CoordinateExporter {

    private fun exportDirectory(context: Context): File {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
    }

    fun exportCsv(context: Context, records: List<CoordinateRecord>): File? {
        return try {
            val directory = exportDirectory(context)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(directory, "wwii_storm_$timestamp.csv")
            val writer = BufferedWriter(FileWriter(file))
            writer.write("id,title,x,y,map_name,note,created_at\n")

            records.forEach { record ->
                val safeTitle = record.title.replace("\"", "\"\"")
                val safeNote = record.note.replace("\"", "\"\"")
                writer.write("${record.id},\"$safeTitle\",${record.x},${record.y},\"${record.mapName}\",\"$safeNote\",${record.createdAt}\n")
            }

            writer.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    fun exportExcel(context: Context, records: List<CoordinateRecord>): File? {
        return try {
            val directory = exportDirectory(context)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(directory, "wwii_storm_$timestamp.xlsx")
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("WWII Storm Coordinates")

            val headers = arrayOf("id", "title", "x", "y", "map_name", "note", "created_at")
            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { index, value ->
                headerRow.createCell(index).setCellValue(value)
            }

            records.forEachIndexed { index, record ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(record.id.toDouble())
                row.createCell(1).setCellValue(record.title)
                row.createCell(2).setCellValue(record.x.toDouble())
                row.createCell(3).setCellValue(record.y.toDouble())
                row.createCell(4).setCellValue(record.mapName)
                row.createCell(5).setCellValue(record.note)
                row.createCell(6).setCellValue(record.createdAt.toDouble())
            }

            val output = FileOutputStream(file)
            workbook.write(output)
            output.close()
            workbook.close()
            file
        } catch (e: Exception) {
            null
        }
    }
}
