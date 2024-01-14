package com.aulbachscheuerpflug.mobileVrCamera

import android.os.Bundle
import android.view.OrientationEventListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aulbachscheuerpflug.mobileVrCamera.bitmap.BitmapMerger
import com.aulbachscheuerpflug.mobileVrCamera.bitmap.BitmapSaver
import com.aulbachscheuerpflug.mobileVrCamera.bluetooth.BluetoothViewModel
import com.aulbachscheuerpflug.mobileVrCamera.camera.CameraDataSingleton
import com.aulbachscheuerpflug.mobileVrCamera.camera.CameraSynchronizer
import com.aulbachscheuerpflug.mobileVrCamera.camera.CameraUtility
import com.aulbachscheuerpflug.mobileVrCamera.camera.MobileVRCameraController
import com.aulbachscheuerpflug.mobileVrCamera.orientation.OrientationManager
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.MobileVrCameraTheme
import com.aulbachscheuerpflug.mobileVrCamera.uiComponents.MainUiHandler
import com.aulbachscheuerpflug.mobileVrCamera.utils.showDebugInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var mainActivityLifeCycleOwner: LifecycleOwner = this
    private var isPaused: Boolean = false
    private val mobileVRCameraController: MobileVRCameraController = MobileVRCameraController()

    private val bluetoothViewModel: BluetoothViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    private val orientationManager: OrientationManager = OrientationManager()
    private var orientationEventListener: OrientationEventListener? = null

    @androidx.camera.camera2.interop.ExperimentalCamera2Interop
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        orientationEventListener =
            orientationManager.setupOrientationEventListener(this@MainActivity)
        orientationEventListener?.enable()

        mobileVRCameraController.cameraInitializer.startCamera(
            this@MainActivity,
            mobileVRCameraController.cameraSetup,
            mainActivityLifeCycleOwner,
            mainViewModel,
        )

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    bluetoothViewModel.receiveTakePhotoEvent.collect {
                        mobileVRCameraController.cameraCapture.takePhoto(
                            false,
                            CameraDataSingleton.getInstance().imageCapture!!,
                            contentResolver,
                            context = this@MainActivity,
                            bluetoothViewModel = bluetoothViewModel,
                            mainViewModel = mainViewModel,
                        )
                    }
                }
                launch {
                    bluetoothViewModel.receivePhotoEvent.collect { bitmap ->
                        if (mainViewModel.equirectangularTransformation) {
                            BitmapSaver.createAndSaveJpegFromBitmap(
                                bitmap = BitmapMerger.createSingleImageFromTwoImages(
                                    CameraDataSingleton.getInstance().mostRecentEquirectangularBitmap!!,
                                    bitmap,
                                    mainViewModel
                                ),
                                context = this@MainActivity,
                                mainViewModel = mainViewModel,
                                toastText = "Bitmap merge succeeded"
                            )
                        } else {
                            BitmapSaver.createAndSaveJpegFromBitmap(
                                bitmap = BitmapMerger.createSingleImageFromTwoImages(
                                    CameraDataSingleton.getInstance().mostRecentBitmap!!,
                                    bitmap,
                                    mainViewModel
                                ),
                                context = this@MainActivity,
                                mainViewModel = mainViewModel,
                                toastText = "Bitmap merge succeeded"
                            )
                        }

                    }
                }
                launch {
                    bluetoothViewModel.receiveSettingsEvent.collect { settings ->
                        showDebugInfo("receiveSettingsEvent")
                        val cameraUtility = CameraUtility()
                        mainViewModel.gridEnabled = settings.grid
                        mainViewModel.imageStabilization = settings.stabilization
                        mainViewModel.equirectangularTransformation = settings.equirectangular
                        mainViewModel.useManualCameraSettings = settings.manualSensor
                        mainViewModel.flashlightEnabled = settings.flashlight
                        mainViewModel.focusSliderValue = settings.focus
                        mainViewModel.exposureSliderValue = settings.exposure.toFloat()
                        mainViewModel.sensitivitySliderValue = settings.sensitivity.toFloat()
                        mainViewModel.antiAliasing = settings.antiAliasing
                        mainViewModel.outputImageScaling = settings.imageScaling

                        CameraSynchronizer.updateFocusExposureSensitivityStabilization(
                            focusDistance = settings.focus,
                            exposureTime = settings.exposure,
                            sensitivity = settings.sensitivity,
                            stabilization = settings.stabilization,
                            manualMode = settings.manualSensor,
                        )
                        CameraDataSingleton.getInstance().cameraProvider!!.unbindAll()
                        cameraUtility.toggleVideoStabilization(settings.stabilization)
                        cameraUtility.toggleFlashlight(settings.flashlight)
                        cameraUtility.toggleManualModes(settings.manualSensor)
                        CameraDataSingleton.getInstance().activeCamera2CameraControl!!.captureRequestOptions =
                            CameraSynchronizer.getCaptureRequestOptions()

                        mobileVRCameraController.cameraSetup.setupCamera(
                            this@MainActivity,
                            mainActivityLifeCycleOwner
                        )
                    }
                }
            }
        }

        setContent {
            MobileVrCameraTheme {
                MainUiHandler(
                    bluetoothViewModel = bluetoothViewModel,
                    mobileVRCameraController = mobileVRCameraController,
                    mainViewModel = mainViewModel,
                    mainActivityLifeCycleOwner = mainActivityLifeCycleOwner,
                    context = this@MainActivity,
                    activity = this,
                    rotation = if (orientationManager.currentRoundedRotation >= 180) (orientationManager.currentRoundedRotation - 360) * -1 else orientationManager.currentRoundedRotation * -1,
                    exactRotation = orientationManager.currentExactRotation,
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        CameraDataSingleton.getInstance().cameraProvider!!.unbindAll()
        orientationEventListener?.disable()
        isPaused = true
    }

    @androidx.camera.camera2.interop.ExperimentalCamera2Interop
    override fun onResume() {
        super.onResume()
        if (isPaused) {
            mobileVRCameraController.cameraSetup.setupCamera(
                this@MainActivity,
                mainActivityLifeCycleOwner
            )
            orientationEventListener?.enable()
            isPaused = false
        }
    }
}