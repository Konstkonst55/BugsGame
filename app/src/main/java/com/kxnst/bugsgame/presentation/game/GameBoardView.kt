package com.kxnst.bugsgame.presentation.game

import android.content.Context
import android.graphics.Bitmap
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
    private val bonusOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(context, R.color.error),
            PorterDuff.Mode.SRC_IN
        )
    }
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val bitmapLoader = BugBitmapLoader(context)
    private val bitmaps = BugType.entries.associateWith(bitmapLoader::load)
    private val bonusBitmap = bitmapLoader.loadBonus()
    private val destination = RectF()
    private val outlineDestination = RectF()
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
        val bugOutlineOffset = (bugSize * OUTLINE_RATIO).coerceAtLeast(MIN_OUTLINE_OFFSET)
        val bonusSize = minOf(width, height) * BONUS_SIZE_RATIO
        val bonusOutlineOffset = (bonusSize * OUTLINE_RATIO).coerceAtLeast(MIN_OUTLINE_OFFSET)

        gameState.bonus?.let { bonus ->
            val centerX = bonus.x * width
            val centerY = bonus.y * height

            destination.set(
                centerX - bonusSize / 2f,
                centerY - bonusSize / 2f,
                centerX + bonusSize / 2f,
                centerY + bonusSize / 2f
            )

            drawOutline(canvas, bonusBitmap, destination, bonusOutlineOffset, bonusOutlinePaint)
            canvas.drawBitmap(bonusBitmap, null, destination, paint)
        }

        gameState.bugs.forEach { bug ->
            val bitmap = bitmaps.getValue(bug.type)
            val centerX = bug.x * width
            val centerY = bug.y * height

            destination.set(
                centerX - bugSize / 2f,
                centerY - bugSize / 2f,
                centerX + bugSize / 2f,
                centerY + bugSize / 2f
            )

            drawOutline(canvas, bitmap, destination, bugOutlineOffset, outlinePaint)
            canvas.drawBitmap(bitmap, null, destination, paint)
        }
    }

    private fun drawOutline(
        canvas: Canvas,
        bitmap: Bitmap,
        destination: RectF,
        offset: Float,
        outlinePaint: Paint
    ) {
        val offsets = listOf(-offset, 0f, offset)
        for (dx in offsets) for (dy in offsets) {
            if (dx != 0f || dy != 0f) {
                drawOutlineBitmap(canvas, bitmap, destination, dx, dy, outlinePaint)
            }
        }
    }

    private fun drawOutlineBitmap(
        canvas: Canvas,
        bitmap: Bitmap,
        destination: RectF,
        offsetX: Float,
        offsetY: Float,
        outlinePaint: Paint
    ) {
        outlineDestination.set(destination)
        outlineDestination.offset(offsetX, offsetY)
        canvas.drawBitmap(bitmap, null, outlineDestination, outlinePaint)
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
        const val BONUS_SIZE_RATIO = 0.16f
        const val OUTLINE_RATIO = 0.018f
        const val MIN_OUTLINE_OFFSET = 1f
    }
}
