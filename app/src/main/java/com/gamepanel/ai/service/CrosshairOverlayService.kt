package com.gamepanel.ai.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
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

        try {
            val notification = createNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIF_ID, notification)
            }
            repository.setOverlayActive(true)

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
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return START_STICKY
    }

    private fun setupCrosshairOverlay() {
        val profile = currentProfile ?: return
        try {
            val themedContext = ContextThemeWrapper(this, R.style.Theme_GamePanelAI)
            val inflater = LayoutInflater.from(themedContext)
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
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_SECURE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                x = profile.offsetX
                y = profile.offsetY
            }

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
        try {
            val themedContext = ContextThemeWrapper(this, R.style.Theme_GamePanelAI)
            val inflater = LayoutInflater.from(themedContext)
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
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_SECURE,
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

            val btnPrev = quickPanelView?.findViewById<Button>(R.id.btnPrevDesign)
            val btnNext = quickPanelView?.findViewById<Button>(R.id.btnNextDesign)
            val tvDesignLabel = quickPanelView?.findViewById<TextView>(R.id.tvCurrentDesignLabel)

            val btnColorCyan = quickPanelView?.findViewById<Button>(R.id.btnColorCyan)
            val btnColorRed = quickPanelView?.findViewById<Button>(R.id.btnColorRed)
            val btnColorGreen = quickPanelView?.findViewById<Button>(R.id.btnColorGreen)
            val btnColorYellow = quickPanelView?.findViewById<Button>(R.id.btnColorYellow)
            val btnColorWhite = quickPanelView?.findViewById<Button>(R.id.btnColorWhite)

            val btnMoveLeft = quickPanelView?.findViewById<Button>(R.id.btnMoveLeft)
            val btnMoveRight = quickPanelView?.findViewById<Button>(R.id.btnMoveRight)
            val btnMoveUp = quickPanelView?.findViewById<Button>(R.id.btnMoveUp)
            val btnMoveDown = quickPanelView?.findViewById<Button>(R.id.btnMoveDown)
            val btnMoveCenter = quickPanelView?.findViewById<Button>(R.id.btnMoveCenter)

            tvActiveGame?.text = "GAME: ${currentProfile?.gameName ?: "Free Fire"}"

            btnToggle?.setOnClickListener {
                val isVisible = crosshairOverlayView?.visibility == View.VISIBLE
                crosshairOverlayView?.visibility = if (isVisible) View.GONE else View.VISIBLE
                btnToggle.text = if (isVisible) "OFF" else "ON"
            }

            btnClose?.setOnClickListener { removeQuickPanel() }

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

            fun updateDesignLabel() {
                val num = currentProfile?.crosshairAsset?.substringAfter("crosshair_")?.substringBefore(".png") ?: "1"
                tvDesignLabel?.text = "#$num"
            }
            updateDesignLabel()

            btnPrev?.setOnClickListener {
                val curNum = currentProfile?.crosshairAsset?.substringAfter("crosshair_")?.substringBefore(".png")?.toIntOrNull() ?: 1
                val prevNum = if (curNum > 1) curNum - 1 else 219
                currentProfile?.crosshairAsset = "crosshairs/crosshair_$prevNum.png"
                currentProfile?.let { p -> repository.saveProfile(p) }
                updateDesignLabel()
                updateCrosshairView()
            }

            btnNext?.setOnClickListener {
                val curNum = currentProfile?.crosshairAsset?.substringAfter("crosshair_")?.substringBefore(".png")?.toIntOrNull() ?: 1
                val nextNum = if (curNum < 219) curNum + 1 else 1
                currentProfile?.crosshairAsset = "crosshairs/crosshair_$nextNum.png"
                currentProfile?.let { p -> repository.saveProfile(p) }
                updateDesignLabel()
                updateCrosshairView()
            }

            btnColorCyan?.setOnClickListener { currentProfile?.color = Color.CYAN; currentProfile?.let { repository.saveProfile(it) }; updateCrosshairView() }
            btnColorRed?.setOnClickListener { currentProfile?.color = Color.RED; currentProfile?.let { repository.saveProfile(it) }; updateCrosshairView() }
            btnColorGreen?.setOnClickListener { currentProfile?.color = Color.GREEN; currentProfile?.let { repository.saveProfile(it) }; updateCrosshairView() }
            btnColorYellow?.setOnClickListener { currentProfile?.color = Color.YELLOW; currentProfile?.let { repository.saveProfile(it) }; updateCrosshairView() }
            btnColorWhite?.setOnClickListener { currentProfile?.color = Color.WHITE; currentProfile?.let { repository.saveProfile(it) }; updateCrosshairView() }

            btnMoveLeft?.setOnClickListener { currentProfile?.let { it.offsetX -= 2; repository.saveProfile(it); updateCrosshairView() } }
            btnMoveRight?.setOnClickListener { currentProfile?.let { it.offsetX += 2; repository.saveProfile(it); updateCrosshairView() } }
            btnMoveUp?.setOnClickListener { currentProfile?.let { it.offsetY -= 2; repository.saveProfile(it); updateCrosshairView() } }
            btnMoveDown?.setOnClickListener { currentProfile?.let { it.offsetY += 2; repository.saveProfile(it); updateCrosshairView() } }
            btnMoveCenter?.setOnClickListener { currentProfile?.let { it.offsetX = 0; it.offsetY = 0; repository.saveProfile(it); updateCrosshairView() } }

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
