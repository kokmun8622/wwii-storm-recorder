package com.wanshi.wwiistorm

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wanshi.wwiistorm.data.CoordinateDbHelper
import com.wanshi.wwiistorm.export.CoordinateExporter
import com.wanshi.wwiistorm.model.CoordinateRecord
import com.wanshi.wwiistorm.ui.CoordinateAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: CoordinateDbHelper
    private lateinit var adapter: CoordinateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = CoordinateDbHelper(this)
        adapter = CoordinateAdapter { id ->
            dbHelper.deleteRecord(id)
            refreshHistory()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerRecords)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val editTitle = findViewById<EditText>(R.id.editTitle)
        val editX = findViewById<EditText>(R.id.editX)
        val editY = findViewById<EditText>(R.id.editY)
        val editMapName = findViewById<EditText>(R.id.editMapName)
        val editNote = findViewById<EditText>(R.id.editNote)

        findViewById<Button>(R.id.btnSaveRecord).setOnClickListener {
            val title = editTitle.text.toString().trim()
            val xText = editX.text.toString().trim()
            val yText = editY.text.toString().trim()
            val mapName = editMapName.text.toString().trim()
            val note = editNote.text.toString().trim()

            if (title.isEmpty() || xText.isEmpty() || yText.isEmpty()) {
                Toast.makeText(this, "Please fill in title, X and Y.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val record = CoordinateRecord(
                title = title,
                x = xText.toInt(),
                y = yText.toInt(),
                mapName = mapName.ifEmpty { "World Map" },
                note = note,
                createdAt = System.currentTimeMillis()
            )

            dbHelper.insertRecord(record)
            Toast.makeText(this, "Coordinate saved locally.", Toast.LENGTH_SHORT).show()
            clearFields(editTitle, editX, editY, editMapName, editNote)
            refreshHistory()
        }

        findViewById<Button>(R.id.btnOpenOverlay).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
                Toast.makeText(this, "Grant overlay permission to use the floating window.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
                Toast.makeText(this, "Notification permission is recommended for foreground overlay service.", Toast.LENGTH_SHORT).show()
            }

            OverlayService.start(this)
            Toast.makeText(this, "Floating overlay started.", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnExportCsv).setOnClickListener {
            val file = CoordinateExporter.exportCsv(this, dbHelper.getAllRecords())
            if (file != null) {
                Toast.makeText(this, "CSV exported: ${file.name}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Failed to export CSV.", Toast.LENGTH_LONG).show()
            }
        }

        findViewById<Button>(R.id.btnExportExcel).setOnClickListener {
            val file = CoordinateExporter.exportExcel(this, dbHelper.getAllRecords())
            if (file != null) {
                Toast.makeText(this, "Excel exported: ${file.name}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Failed to export Excel.", Toast.LENGTH_LONG).show()
            }
        }

        refreshHistory()
    }

    private fun refreshHistory() {
        val records = dbHelper.getAllRecords()
        adapter.submitList(records)
    }

    private fun clearFields(
        editTitle: EditText,
        editX: EditText,
        editY: EditText,
        editMapName: EditText,
        editNote: EditText
    ) {
        editTitle.text?.clear()
        editX.text?.clear()
        editY.text?.clear()
        editMapName.text?.clear()
        editNote.text?.clear()
    }
}
