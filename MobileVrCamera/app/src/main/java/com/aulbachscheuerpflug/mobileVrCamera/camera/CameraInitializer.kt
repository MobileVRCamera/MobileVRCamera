package com.aulbachscheuerpflug.mobileVrCamera.camera

import android.content.Context
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel

class CameraInitializer {
    @androidx.camera.camera2.interop.ExperimentalCamera2Interop
    fun startCamera(
        context: Context,
        cameraSetup: CameraSetup,
        lifecycleOwner: LifecycleOwner,
        mainViewModel: MainViewModel
    ) {
        CameraDataSingleton.getInstance().cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)
        CameraDataSingleton.getInstance().cameraProviderFuture.addListener({

            CameraDataSingleton.getInstance().imageCapture =
                ImageCapture.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build()
            cameraSetup.setupCamera(context, lifecycleOwner)
            mainViewModel.exposureSliderValue =
                CameraDataSingleton.getInstance().exposureRange.run { lower.toFloat() + (upper.toFloat() - lower.toFloat()) / 2 }
            mainViewModel.sensitivitySliderValue =
                CameraDataSingleton.getInstance().sensitivityRange.run { lower.toFloat() + (upper.toFloat() - lower.toFloat()) / 2 }
            mainViewModel.focusSliderValue =
                CameraDataSingleton.getInstance().minFocusDistance / 2

        }, ContextCompat.getMainExecutor(context))
    }
}