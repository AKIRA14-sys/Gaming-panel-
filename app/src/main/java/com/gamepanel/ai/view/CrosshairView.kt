package com.gamepanel.ai.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.io.InputStream

class CrosshairView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var crosshairBitmap: Bitmap? = null
    private var crosshairAssetPath: String = ""

    // Settings
    var crosshairColor: Int = Color.CYAN
        set(value) {
            field = value
            invalidate()
        }

    var crosshairSizeDp: Float = 36f
        set(value) {
            field = value.coerceIn(12f, 120f)
            requestLayout()
            invalidate()
        }

    var crosshairOpacity: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.0f, 1.0f)
            invalidate()
        }

    var crosshairThickness: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.5f, 4.0f)
            invalidate()
        }

    var crosshairRotation: Float = 0f
        set(value) {
            field = value % 360f
            invalidate()
        }

    // Center Dot settings
    var centerDotEnabled: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var centerDotSizeDp: Float = 4f
        set(value) {
            field = value.coerceIn(1f, 20f)
            invalidate()
        }

    var centerDotColor: Int = Color.RED
        set(value) {
            field = value
            invalidate()
        }

    var centerDotOpacity: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.0f, 1.0f)
            invalidate()
        }

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun setCrosshairAsset(assetPath: String) {
        if (crosshairAssetPath == assetPath && crosshairBitmap != null) return
        crosshairAssetPath = assetPath
        try {
            val isStream: InputStream = context.assets.open(assetPath)
            crosshairBitmap = BitmapFactory.decodeStream(isStream)
            isStream.close()
        } catch (e: Exception) {
            crosshairBitmap = null
        }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        val sizePx = (crosshairSizeDp * density).toInt()
        val padding = (8 * density).toInt()
        val totalSize = sizePx + padding * 2
        setMeasuredDimension(
            resolveSize(totalSize, widthMeasureSpec),
            resolveSize(totalSize, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val density = resources.displayMetrics.density
        val cx = width / 2f
        val cy = height / 2f

        val bmp = crosshairBitmap
        if (bmp != null) {
            canvas.save()
            canvas.rotate(crosshairRotation, cx, cy)

            val sizePx = crosshairSizeDp * density
            val targetRect = RectF(
                cx - sizePx / 2f,
                cy - sizePx / 2f,
                cx + sizePx / 2f,
                cy + sizePx / 2f
            )

            // Apply color filter and opacity
            bitmapPaint.colorFilter = PorterDuffColorFilter(crosshairColor, PorterDuff.Mode.SRC_IN)
            bitmapPaint.alpha = (crosshairOpacity * 255).toInt().coerceIn(0, 255)

            // Drawing bitmap scale & thickness effect
            if (crosshairThickness > 1.05f) {
                // Draw slightly expanded behind for thickness boost
                val offset = (crosshairThickness - 1f) * density
                val thickRect = RectF(
                    targetRect.left - offset,
                    targetRect.top - offset,
                    targetRect.right + offset,
                    targetRect.bottom + offset
                )
                bitmapPaint.alpha = (crosshairOpacity * 120).toInt().coerceIn(0, 255)
                canvas.drawBitmap(bmp, null, thickRect, bitmapPaint)
                bitmapPaint.alpha = (crosshairOpacity * 255).toInt().coerceIn(0, 255)
            }

            canvas.drawBitmap(bmp, null, targetRect, bitmapPaint)
            canvas.restore()
        }

        // Render Center Dot if enabled
        if (centerDotEnabled) {
            val dotRadiusPx = (centerDotSizeDp * density) / 2f
            dotPaint.color = centerDotColor
            dotPaint.alpha = (centerDotOpacity * crosshairOpacity * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx, cy, dotRadiusPx, dotPaint)
        }
    }
}
