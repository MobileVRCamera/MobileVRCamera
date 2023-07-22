package com.aulbachscheuerpflug.mobileVrCamera.camera

import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.util.Range
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.common.util.concurrent.ListenableFuture


class CameraDataSingleton private constructor() {
    lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    var cameraProvider: ProcessCameraProvider? = null
    var cameraSelector: CameraSelector? = null

    var imageCapture: ImageCapture? = null
    var previewView: PreviewView? = null
    var preview: Preview? = null

    var activeCamera: Camera? = null
    var activeCameraInfo: CameraInfo? = null
    var activeCameraControl: CameraControl? = null

    var activeCamera2CameraControl: Camera2CameraControl? = null
    var activeCameraCharacteristics: CameraCharacteristics? = null

    var mostRecentBitmap: Bitmap? = null
    var mostRecentEquirectangularBitmap: Bitmap? = null

    var mostRecentPhotoOrientation: Int = 0
    var mostRecentBitmapOrientation: Int = 0

    var minFocusDistance: Float = 0.0f

    var sensitivityRange: Range<Int> = Range(0, 0)
    var exposureRange: Range<Long> = Range(0L, 0L)

    var configMapInt: MutableMap<CaptureRequest.Key<Int>, Int>? = mutableMapOf(
        CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE to CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF
    )
    var configMapFloat: MutableMap<CaptureRequest.Key<Float>, Float>? =
        mutableMapOf(CaptureRequest.LENS_FOCUS_DISTANCE to 0.0f)
    var configMapLong: MutableMap<CaptureRequest.Key<Long>, Long>? =
        mutableMapOf(CaptureRequest.SENSOR_EXPOSURE_TIME to 50)

    companion object {

        @Volatile
        private var instance: CameraDataSingleton? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: CameraDataSingleton().also { instance = it }
            }
    }

}