package com.kxnst.bugsgame.presentation.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.ContextCompat

import com.kxnst.bugsgame.R

class GameBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(context, R.color.primary),
            PorterDuff.Mode.SRC_IN
        )
    }
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val bitmapLoader = BugBitmapLoader(context)
    private val bitmaps = BugType.entries.associateWith(bitmapLoader::load)
    private var gameState = GameState()
    private var downX = 0f
    private var downY = 0f

    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.surfaceContainerLow))
        contentDescription = context.getString(R.string.game_cd_board)
    }

    fun render(state: GameState) {
        gameState = state
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val bugSize = minOf(width, height) * BUG_SIZE_RATIO
        val outlineOffset = (bugSize * OUTLINE_RATIO).coerceAtLeast(MIN_OUTLINE_OFFSET)

        gameState.bugs.forEach { bug ->
            val bitmap = bitmaps.getValue(bug.type)
            val centerX = bug.x * width
            val centerY = bug.y * height
            val left = centerX - bugSize / 2f
            val top = centerY - bugSize / 2f
            val destination = RectF(
                left,
                top,
                left + bugSize,
                top + bugSize
            )

            drawOutline(canvas, bitmap, destination, outlineOffset)
            canvas.drawBitmap(bitmap, null, destination, paint)
        }
    }

    private fun drawOutline(
        canvas: Canvas,
        bitmap: android.graphics.Bitmap,
        destination: RectF,
        offset: Float
    ) {
        val offsets = arrayOf(
            -offset to -offset,
            0f to -offset,
            offset to -offset,
            -offset to 0f,
            offset to 0f,
            -offset to offset,
            0f to offset,
            offset to offset
        )

        offsets.forEach { (x, y) ->
            canvas.drawBitmap(bitmap, null, RectF(destination).apply { offset(x, y) }, outlinePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                return true
            }

            MotionEvent.ACTION_UP -> {
                val distanceX = event.x - downX
                val distanceY = event.y - downY
                val isTap =
                    distanceX * distanceX + distanceY * distanceY <= touchSlop * touchSlop

                if (isTap && width > 0 && height > 0) {
                    performClick()

                    val normalizedX = (event.x / width).coerceIn(0f, 1f)
                    val normalizedY = (event.y / height).coerceIn(0f, 1f)
                    onTap?.invoke(normalizedX, normalizedY)
                }

                return true
            }
        }

        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    var onTap: ((Float, Float) -> Unit)? = null

    private companion object {
        const val BUG_SIZE_RATIO = 0.14f
        const val OUTLINE_RATIO = 0.018f
        const val MIN_OUTLINE_OFFSET = 1f
    }
}
