package com.aulbachscheuerpflug.mobileVrCamera.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.util.Range
import androidx.camera.camera2.internal.Camera2CameraInfoImpl
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.aulbachscheuerpflug.mobileVrCamera.utils.showDebugError

class CameraSetup {
    @androidx.camera.camera2.interop.ExperimentalCamera2Interop
    fun setupCamera(context: Context, lifeCycleOwner: LifecycleOwner) {
        if (CameraDataSingleton.getInstance().cameraProvider == null) {
            CameraDataSingleton.getInstance().cameraSelector =
                CameraSelector.Builder().addCameraFilter { cameraInfos ->
                    val backCameras = cameraInfos.filterIsInstance<Camera2CameraInfoImpl>()
                        .filter {
                            val pixelWidth =
                                it.cameraCharacteristicsCompat.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)?.width
                                    ?: 0
                            it.lensFacing == CameraSelector.LENS_FACING_BACK && pixelWidth >= 2000
                        }

                    backCameras.minByOrNull {
                        val focalLengths =
                            it.cameraCharacteristicsCompat.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                        focalLengths?.getOrNull(0) ?: 0f
                    }
                        ?.let { listOf(it) } ?: backCameras
                }.build()


            CameraDataSingleton.getInstance().cameraProvider =
                CameraDataSingleton.getInstance().cameraProviderFuture.get()
            CameraDataSingleton.getInstance().previewView = PreviewView(context)
            CameraDataSingleton.getInstance().preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .also { it.setSurfaceProvider(CameraDataSingleton.getInstance().previewView!!.surfaceProvider) }
        }
        CameraDataSingleton.getInstance().activeCamera =
            CameraDataSingleton.getInstance().cameraSelector?.let {
                CameraDataSingleton.getInstance().cameraProvider?.bindToLifecycle(
                    lifeCycleOwner,
                    it,
                    CameraDataSingleton.getInstance().preview,
                    CameraDataSingleton.getInstance().imageCapture
                )
            }

        CameraDataSingleton.getInstance().activeCameraControl =
            CameraDataSingleton.getInstance().activeCamera?.cameraControl

        CameraDataSingleton.getInstance().activeCameraInfo =
            CameraDataSingleton.getInstance().activeCamera?.cameraInfo

        if (CameraDataSingleton.getInstance().activeCameraControl != null) {
            try {
                CameraDataSingleton.getInstance().activeCameraControl?.setZoomRatio(
                    CameraDataSingleton.getInstance().activeCameraInfo?.zoomState?.value?.minZoomRatio!!
                )
            } catch (exc: Exception) {
                showDebugError("No CameraControl assigned: ${exc.message}", exc)
            }
        }
        CameraDataSingleton.getInstance().activeCamera2CameraControl =
            Camera2CameraControl.from(CameraDataSingleton.getInstance().activeCameraControl!!)

        CameraDataSingleton.getInstance().activeCameraCharacteristics =
            Camera2CameraInfo.extractCameraCharacteristics(CameraDataSingleton.getInstance().activeCamera!!.cameraInfo)

        CameraDataSingleton.getInstance().sensitivityRange =
            CameraDataSingleton.getInstance().activeCameraCharacteristics?.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
                ?: Range(0, 0)
        CameraDataSingleton.getInstance().exposureRange =
            CameraDataSingleton.getInstance().activeCameraCharacteristics?.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
                ?: Range(0L, 0L)

        CameraDataSingleton.getInstance().activeCamera2CameraControl!!.captureRequestOptions =
            CameraSynchronizer.getCaptureRequestOptions()

        try {
            CameraDataSingleton.getInstance().minFocusDistance =
                CameraDataSingleton.getInstance().activeCameraCharacteristics!!.get(
                    CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE
                )!!
        } catch (exc: Exception) {
            showDebugError("Minimal lens focus distance not set ${exc.message}", exc)
        }
    }
}