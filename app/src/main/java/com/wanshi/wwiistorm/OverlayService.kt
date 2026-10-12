package com.wanshi.wwiistorm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.wanshi.wwiistorm.data.CoordinateDbHelper
import com.wanshi.wwiistorm.model.CoordinateRecord

class OverlayService : Service() {

    private lateinit var dbHelper: CoordinateDbHelper
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        dbHelper = CoordinateDbHelper(this)
        createNotificationChannel()
        startForeground(1, buildNotification())
        showFloatingWindow()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        overlayView?.let { view ->
            windowManager?.removeView(view)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "wwii_storm_overlay",
                "WWII Storm Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "wwii_storm_overlay")
            .setContentTitle("WWII Storm")
            .setContentText("Floating coordinate recorder is running")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
    }

    private fun showFloatingWindow() {
        windowManager = getSystemService(WindowManager::class.java)
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_window, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 32
        params.y = 200

        windowManager?.addView(overlayView, params)

        overlayView?.findViewById<Button>(R.id.btnRecordOverlay)?.setOnClickListener {
            saveFromOverlay()
        }

        overlayView?.findViewById<Button>(R.id.btnCloseOverlay)?.setOnClickListener {
            stopSelf()
        }
    }

    private fun saveFromOverlay() {
        val titleInput = overlayView?.findViewById<EditText>(R.id.editOverlayTitle)
        val xInput = overlayView?.findViewById<EditText>(R.id.editOverlayX)
        val yInput = overlayView?.findViewById<EditText>(R.id.editOverlayY)
        val noteInput = overlayView?.findViewById<EditText>(R.id.editOverlayNote)

        val title = titleInput?.text?.toString()?.trim().orEmpty()
        val xText = xInput?.text?.toString()?.trim().orEmpty()
        val yText = yInput?.text?.toString()?.trim().orEmpty()
        val note = noteInput?.text?.toString()?.trim().orEmpty()

        if (title.isEmpty() || xText.isEmpty() || yText.isEmpty()) {
            Toast.makeText(this, "Please enter title, X and Y.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val record = CoordinateRecord(
                title = title,
                x = xText.toInt(),
                y = yText.toInt(),
                mapName = "Overlay",
                note = note,
                createdAt = System.currentTimeMillis()
            )
            dbHelper.insertRecord(record)
            Toast.makeText(this, "Saved from floating window.", Toast.LENGTH_SHORT).show()

            titleInput?.text?.clear()
            xInput?.text?.clear()
            yInput?.text?.clear()
            noteInput?.text?.clear()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "X and Y must be numeric.", Toast.LENGTH_SHORT).show()
        }
    }
}
