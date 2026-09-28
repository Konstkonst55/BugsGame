package com.kxnst.bugsgame.presentation.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.Surface

import kotlin.math.sqrt

class TiltSensorController(
    context: Context
) : SensorEventListener {
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val displayManager =
        context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var onTiltChanged: ((Float, Float) -> Unit)? = null

    fun start(onTiltChanged: (Float, Float) -> Unit) {
        this.onTiltChanged = onTiltChanged

        sensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        onTiltChanged = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val magnitude = sqrt(x * x + y * y).coerceAtLeast(MIN_GRAVITY)
        val normalizedX = x / magnitude
        val normalizedY = y / magnitude
        val rotation = displayManager
            .getDisplay(Display.DEFAULT_DISPLAY)
            ?.rotation
            ?: Surface.ROTATION_0

        val tilt = when (rotation) {
            Surface.ROTATION_90 -> normalizedY to -normalizedX
            Surface.ROTATION_180 -> -normalizedX to -normalizedY
            Surface.ROTATION_270 -> -normalizedY to normalizedX
            else -> normalizedX to normalizedY
        }

        onTiltChanged?.invoke(
            tilt.first.coerceIn(-1f, 1f),
            tilt.second.coerceIn(-1f, 1f)
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private companion object {
        const val MIN_GRAVITY = 0.001f
    }
}
