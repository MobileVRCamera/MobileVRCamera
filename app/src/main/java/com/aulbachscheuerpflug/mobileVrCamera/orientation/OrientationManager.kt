package com.aulbachscheuerpflug.mobileVrCamera.orientation

import android.content.Context
import android.view.OrientationEventListener
import android.view.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aulbachscheuerpflug.mobileVrCamera.camera.CameraDataSingleton

class OrientationManager {

    var currentExactRotation: Int by mutableStateOf(0)
    var currentRoundedRotation: Int by mutableStateOf(0)

    fun setupOrientationEventListener(context: Context): OrientationEventListener {
        return object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                currentExactRotation = orientation
                currentRoundedRotation = calculateRoundedRotation(orientation)
                CameraDataSingleton.getInstance().mostRecentPhotoOrientation = getPhotoRotation()
                CameraDataSingleton.getInstance().mostRecentBitmapOrientation = getCameraRotation()
            }
        }
    }

    private fun calculateRoundedRotation(newRotationDeg: Int): Int {
        return when {
            newRotationDeg <= 45 || newRotationDeg > 315 -> 0
            newRotationDeg in 46..135 -> 90
            newRotationDeg in 136..225 -> 180
            else -> 270
        }
    }

    fun getCameraRotation(): Int {
        return when (currentRoundedRotation) {
            0 -> 90
            90 -> 180
            180 -> 270
            270 -> 0
            else -> 90
        }
    }

    fun getPhotoRotation(): Int {
        return when (currentRoundedRotation) {
            0 -> Surface.ROTATION_0
            90 -> Surface.ROTATION_270
            180 -> Surface.ROTATION_180
            270 -> Surface.ROTATION_90
            else -> Surface.ROTATION_0
        }
    }
}