package com.aulbachscheuerpflug.mobileVrCamera.bluetooth

import android.graphics.Bitmap

sealed class Message

data class BluetoothMessage(
    val message: String,
    val bitmapImage: Bitmap,
    val manualMode: Boolean,
    val focus: Float,
    val exposure: Long,
    val sensitivity: Int,
    val stabilization: Boolean,
    val equirectangular: Boolean,
) : Message()

data class SettingsMessage(
    val grid: Boolean,
    val flashlight: Boolean,
    val stabilization: Boolean,
    val manualSensor: Boolean,
    val equirectangular: Boolean,
    val focus: Float,
    val exposure: Long,
    val sensitivity: Int,
) : Message()