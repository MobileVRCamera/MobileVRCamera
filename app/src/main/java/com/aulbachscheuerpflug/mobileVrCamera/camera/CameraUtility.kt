package com.aulbachscheuerpflug.mobileVrCamera.camera

import android.hardware.camera2.CaptureRequest
import com.aulbachscheuerpflug.mobileVrCamera.utils.showDebugError

class CameraUtility {
    @androidx.camera.camera2.interop.ExperimentalCamera2Interop
    fun toggleFlashlight(isActive: Boolean) {
        try {
            if (isActive) {
                CameraSynchronizer.updateCaptureRequestConfig(CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_TORCH)
            } else {
                CameraSynchronizer.updateCaptureRequestConfig(CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_OFF)
            }
        } catch (exc: Exception) {
            showDebugError("Flashlight not available: ${exc.message}", exc)
        }
    }

    @androidx.camera.camera2.interop.ExperimentalCamera2Interop
    fun toggleVideoStabilization(isActive: Boolean) {
        try {
            if (isActive) {
                CameraSynchronizer.updateCaptureRequestConfig(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON)
            } else {
                CameraSynchronizer.updateCaptureRequestConfig(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF)
            }
        } catch (exc: Exception) {
            showDebugError("CameraControl not available: ${exc.message}", exc)
        }
    }
    @androidx.camera.camera2.interop.ExperimentalCamera2Interop
    fun toggleManualModes(isActive: Boolean) {
        try {
            CameraSynchronizer.updateCaptureRequestConfig(
                CaptureRequest.CONTROL_AE_MODE,
                if (isActive) CaptureRequest.CONTROL_AE_MODE_OFF else CaptureRequest.CONTROL_AE_MODE_ON
            )
            CameraSynchronizer.updateCaptureRequestConfig(
                CaptureRequest.CONTROL_AF_MODE,
                if (isActive) CaptureRequest.CONTROL_AF_MODE_OFF else CaptureRequest.CONTROL_AF_MODE_AUTO
            )
        } catch (exc: Exception) {
            showDebugError("ManualModes not available: ${exc.message}", exc)
        }
    }
}