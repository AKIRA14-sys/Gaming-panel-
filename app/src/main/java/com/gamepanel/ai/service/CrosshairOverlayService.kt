package com.gamepanel.ai.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.gamepanel.ai.MainActivity
import com.gamepanel.ai.R
import com.gamepanel.ai.data.GameProfile
import com.gamepanel.ai.data.ProfileRepository
import com.gamepanel.ai.view.CrosshairView

class CrosshairOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var repository: ProfileRepository

    private var crosshairOverlayView: View? = null
    private var crosshairView: CrosshairView? = null
    private var crosshairParams: WindowManager.LayoutParams? = null

    private var quickPanelView: View? = null
    private var quickPanelParams: WindowManager.LayoutParams? = null

    private var currentProfile: GameProfile? = null

    companion object {
        const val CHANNEL_ID = "gamepanel_ai_overlay_channel"
        const val NOTIF_ID = 1001
        const val ACTION_STOP = "com.gamepanel.ai.ACTION_STOP"
        const val ACTION_UPDATE_PROFILE = "com.gamepanel.ai.ACTION_UPDATE_PROFILE"
        const val ACTION_SHOW_PANEL = "com.gamepanel.ai.ACTION_SHOW_PANEL"
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        repository = ProfileRepository(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            repository.setOverlayActive(false)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = createNotification()
        startForeground(NOTIF_ID, notification)
        repository.setOverlayActive(true)

        // Load active game profile
        val activeGameId = repository.getActiveGameId()
        currentProfile = repository.getActiveProfileForGame(activeGameId)

        if (crosshairOverlayView == null) {
            setupCrosshairOverlay()
        } else {
            updateCrosshairView()
        }

        if (intent?.action == ACTION_SHOW_PANEL && quickPanelView == null) {
            setupQuickPanel()
        }

        return START_STICKY
    }

    private fun setupCrosshairOverlay() {
        val profile = currentProfile ?: return
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        crosshairOverlayView = inflater.inflate(R.layout.layout_crosshair_overlay, null)
        crosshairView = crosshairOverlayView?.findViewById(R.id.crosshairView)

        updateCrosshairView()

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        crosshairParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = profile.offsetX
            y = profile.offsetY
        }

        try {
            windowManager.addView(crosshairOverlayView, crosshairParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateCrosshairView() {
        val profile = currentProfile ?: return
        val cv = crosshairView ?: return

        cv.setCrosshairAsset(profile.crosshairAsset)
        cv.crosshairColor = profile.color
        cv.crosshairSizeDp = profile.sizeDp
        cv.crosshairOpacity = profile.opacity
        cv.crosshairThickness = profile.thickness
        cv.crosshairRotation = profile.rotation
        cv.centerDotEnabled = profile.centerDotEnabled
        cv.centerDotSizeDp = profile.centerDotSizeDp
        cv.centerDotColor = profile.centerDotColor
        cv.centerDotOpacity = profile.centerDotOpacity

        crosshairParams?.let { params ->
            params.x = profile.offsetX
            params.y = profile.offsetY
            try {
                if (crosshairOverlayView?.isAttachedToWindow == true) {
                    windowManager.updateViewLayout(crosshairOverlayView, params)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupQuickPanel() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        quickPanelView = inflater.inflate(R.layout.layout_quick_panel, null)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        quickPanelParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        val panelHeader = quickPanelView?.findViewById<View>(R.id.panelHeader)
        val tvActiveGame = quickPanelView?.findViewById<TextView>(R.id.tvActiveGame)
        val btnToggle = quickPanelView?.findViewById<Button>(R.id.btnToggleCrosshair)
        val btnClose = quickPanelView?.findViewById<ImageButton>(R.id.btnClosePanel)
        val btnOpenApp = quickPanelView?.findViewById<Button>(R.id.btnOpenApp)
        val btnStopOverlay = quickPanelView?.findViewById<Button>(R.id.btnStopOverlay)
        val sbOpacity = quickPanelView?.findViewById<SeekBar>(R.id.sbPanelOpacity)
        val sbSize = quickPanelView?.findViewById<SeekBar>(R.id.sbPanelSize)

        tvActiveGame?.text = "Game: ${currentProfile?.gameName ?: "Free Fire"}"

        btnToggle?.setOnClickListener {
            val isVisible = crosshairOverlayView?.visibility == View.VISIBLE
            crosshairOverlayView?.visibility = if (isVisible) View.GONE else View.VISIBLE
            btnToggle.text = if (isVisible) "OFF" else "ON"
        }

        btnClose?.setOnClickListener {
            removeQuickPanel()
        }

        btnOpenApp?.setOnClickListener {
            val appIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(appIntent)
        }

        btnStopOverlay?.setOnClickListener {
            repository.setOverlayActive(false)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        currentProfile?.let { p ->
            sbOpacity?.progress = (p.opacity * 100).toInt()
            sbSize?.progress = p.sizeDp.toInt()
        }

        sbOpacity?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentProfile?.let { p ->
                        p.opacity = progress / 100f
                        repository.saveProfile(p)
                        updateCrosshairView()
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbSize?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentProfile?.let { p ->
                        p.sizeDp = progress.coerceAtLeast(12).toFloat()
                        repository.saveProfile(p)
                        updateCrosshairView()
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Touch listener for dragging Quick Panel
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        panelHeader?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = quickPanelParams?.x ?: 0
                    initialY = quickPanelParams?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    quickPanelParams?.x = initialX + (event.rawX - initialTouchX).toInt()
                    quickPanelParams?.y = initialY + (event.rawY - initialTouchY).toInt()
                    try {
                        windowManager.updateViewLayout(quickPanelView, quickPanelParams)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    true
                }
                else -> false
            }
        }

        try {
            windowManager.addView(quickPanelView, quickPanelParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeQuickPanel() {
        quickPanelView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            quickPanelView = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_description)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingAppIntent = PendingIntent.getActivity(
            this, 0, appIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, CrosshairOverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStopIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val activeGame = repository.getActiveGameItem().name

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GamePanel AI — Crosshair Active")
            .setContentText("Active Game: $activeGame")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingAppIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP", pendingStopIntent)
            .addAction(android.R.drawable.ic_menu_view, "OPEN APP", pendingAppIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.setOverlayActive(false)
        crosshairOverlayView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) {}
        }
        removeQuickPanel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
