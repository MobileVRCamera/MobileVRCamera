package com.aulbachscheuerpflug.mobileVrCamera.bluetooth

import com.aulbachscheuerpflug.mobileVrCamera.ACCUMULATED_DATA_SEPARATION_CHAR
import com.aulbachscheuerpflug.mobileVrCamera.CAMERA_SETTINGS_DATA_SEPARATION_CHAR
import com.aulbachscheuerpflug.mobileVrCamera.DATA_SEPARATION
import com.aulbachscheuerpflug.mobileVrCamera.IMAGE_DATA_SEPARATION_CHAR
import com.aulbachscheuerpflug.mobileVrCamera.utils.base64ImageStringToBitmap
import com.aulbachscheuerpflug.mobileVrCamera.utils.toBase64

suspend fun BluetoothMessage.toByteArray(quality: Int): ByteArray {
    val imageString = bitmapImage.toBase64(quality)
    return "$message$DATA_SEPARATION$focus$CAMERA_SETTINGS_DATA_SEPARATION_CHAR$exposure$CAMERA_SETTINGS_DATA_SEPARATION_CHAR$sensitivity$CAMERA_SETTINGS_DATA_SEPARATION_CHAR$stabilization$CAMERA_SETTINGS_DATA_SEPARATION_CHAR$equirectangular$CAMERA_SETTINGS_DATA_SEPARATION_CHAR$manualMode$IMAGE_DATA_SEPARATION_CHAR$imageString$ACCUMULATED_DATA_SEPARATION_CHAR".encodeToByteArray()
}

fun SettingsMessage.toByteArray(): ByteArray {
    return "$grid$DATA_SEPARATION$flashlight$DATA_SEPARATION$stabilization$DATA_SEPARATION$manualSensor$DATA_SEPARATION$equirectangular$DATA_SEPARATION$focus$DATA_SEPARATION$exposure$DATA_SEPARATION$sensitivity$ACCUMULATED_DATA_SEPARATION_CHAR".encodeToByteArray()
}

suspend fun String.toBluetoothMessage(): BluetoothMessage {
    val imageData = split(IMAGE_DATA_SEPARATION_CHAR)
    val extraDataParts = imageData[0].split(DATA_SEPARATION)

    val message = extraDataParts[0]

    val cameraSettingData = extraDataParts[1].split(CAMERA_SETTINGS_DATA_SEPARATION_CHAR)
    val focus = cameraSettingData[0].toFloat()
    val exposure = cameraSettingData[1].toLong()
    val sensitivity = cameraSettingData[2].toInt()
    val stabilization = cameraSettingData[3].toBoolean()
    val equirectangular = cameraSettingData[4].toBoolean()
    val manualMode = cameraSettingData[5].toBoolean()

    val bitmapImage = base64ImageStringToBitmap(imageData[1])

    return BluetoothMessage(
        message = message,
        bitmapImage = bitmapImage,
        focus = focus,
        exposure = exposure,
        sensitivity = sensitivity,
        stabilization = stabilization,
        equirectangular = equirectangular,
        manualMode = manualMode,
    )
}

fun String.toSettingsMessage(): SettingsMessage {
    val settingsData = split(DATA_SEPARATION)
    return SettingsMessage(
        grid = settingsData[0].toBoolean(),
        flashlight = settingsData[1].toBoolean(),
        stabilization = settingsData[2].toBoolean(),
        manualSensor = settingsData[3].toBoolean(),
        equirectangular = settingsData[4].toBoolean(),
        focus = settingsData[5].toFloat(),
        exposure = settingsData[6].toLong(),
        sensitivity = settingsData[7].toInt(),
    )
}