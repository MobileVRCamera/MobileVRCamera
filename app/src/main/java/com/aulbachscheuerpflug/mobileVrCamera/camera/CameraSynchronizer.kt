package com.aulbachscheuerpflug.mobileVrCamera.camera

import android.hardware.camera2.CaptureRequest
import androidx.camera.camera2.interop.CaptureRequestOptions
import com.aulbachscheuerpflug.mobileVrCamera.utils.showDebugError

class CameraSynchronizer {
    @androidx.camera.camera2.interop.ExperimentalCamera2Interop
    companion object {
        fun updateFocusExposureSensitivityStabilization(
            manualMode: Boolean,
            focusDistance: Float,
            exposureTime: Long,
            sensitivity: Int,
            stabilization: Boolean,
        ) {
            val cameraUtility = CameraUtility()
            if (focusDistance >= 0 && manualMode) {
                try {
                    updateCaptureRequestConfig(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_OFF
                    )
                    updateCaptureRequestConfig(
                        CaptureRequest.LENS_FOCUS_DISTANCE, focusDistance
                    )
                } catch (exc: Exception) {
                    showDebugError("Not Able to adjust Auto Focus (Mode) ${exc.message}", exc)
                }
            }
            if (exposureTime >= 0 && sensitivity >= 0 && manualMode) {
                try {
                    updateCaptureRequestConfig(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_OFF
                    )
                } catch (exc: Exception) {
                    showDebugError("Not Able to adjust Auto Exposure Mode ${exc.message}", exc)
                }
                try {
                    updateCaptureRequestConfig(
                        CaptureRequest.SENSOR_EXPOSURE_TIME, exposureTime
                    )
                } catch (exc: Exception) {
                    showDebugError("Not Able to adjust Exposure Time ${exc.message}", exc)
                }
                try {
                    updateCaptureRequestConfig(
                        CaptureRequest.SENSOR_SENSITIVITY, sensitivity
                    )
                } catch (exc: Exception) {
                    showDebugError("Not Able to adjust sensitivity ${exc.message}", exc)
                }
            }
            cameraUtility.toggleVideoStabilization(stabilization)
            CameraDataSingleton.getInstance().activeCamera2CameraControl!!.captureRequestOptions =
                getCaptureRequestOptions()
        }
        fun getCaptureRequestOptions(): CaptureRequestOptions {
            val captureRequestOptionBuilder = CaptureRequestOptions.Builder()
            for (item in CameraDataSingleton.getInstance().configMapInt!!) {
                captureRequestOptionBuilder.setCaptureRequestOption(item.key, item.value)
            }
            for (item in CameraDataSingleton.getInstance().configMapFloat!!) {
                captureRequestOptionBuilder.setCaptureRequestOption(item.key, item.value)
            }
            for (item in CameraDataSingleton.getInstance().configMapLong!!) {
                captureRequestOptionBuilder.setCaptureRequestOption(item.key, item.value)
            }
            return captureRequestOptionBuilder.build()
        }

        fun updateCaptureRequestConfig(
            captureRequestOption: CaptureRequest.Key<Float>,
            value: Float
        ) {
            CameraDataSingleton.getInstance().configMapFloat!![captureRequestOption] = value
        }

        fun updateCaptureRequestConfig(
            captureRequestOption: CaptureRequest.Key<Long>,
            value: Long
        ) {
            CameraDataSingleton.getInstance().configMapLong!![captureRequestOption] = value
        }

        fun updateCaptureRequestConfig(
            captureRequestOption: CaptureRequest.Key<Int>,
            value: Int
        ) {
            CameraDataSingleton.getInstance().configMapInt!![captureRequestOption] = value
        }
    }
}