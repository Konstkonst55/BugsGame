package com.kxnst.bugsgame.presentation.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.content.res.AppCompatResources
import com.kxnst.bugsgame.R

class BugBitmapLoader(
    private val context: Context
) {
    fun load(type: BugType): Bitmap {
        val resourceId = when (type) {
            BugType.BEETLE -> R.drawable.ph_bug_first
            BugType.ANT -> R.drawable.ph_bug_second
            BugType.FLY -> R.drawable.ph_bug_third
        }

        val drawable = AppCompatResources.getDrawable(context, resourceId)
            ?: error("Bug drawable is not available")

        val width = drawable.intrinsicWidth.coerceAtLeast(MIN_BITMAP_SIZE)
        val height = drawable.intrinsicHeight.coerceAtLeast(MIN_BITMAP_SIZE)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        return bitmap
    }

    private companion object {
        const val MIN_BITMAP_SIZE = 1
    }
}
