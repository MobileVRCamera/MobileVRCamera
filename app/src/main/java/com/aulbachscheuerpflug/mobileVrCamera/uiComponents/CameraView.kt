package com.aulbachscheuerpflug.mobileVrCamera.uiComponents

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CaptureRequest
import android.view.MotionEvent
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.DisplayOrientedMeteringPointFactory
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.MeteringPointFactory
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.startActivity
import com.aulbachscheuerpflug.mobileVrCamera.DISABLED_CAMERA
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import com.aulbachscheuerpflug.mobileVrCamera.NO_ACCESS_CAMERA
import com.aulbachscheuerpflug.mobileVrCamera.RESTART_APP
import com.aulbachscheuerpflug.mobileVrCamera.bluetooth.BluetoothViewModel
import com.aulbachscheuerpflug.mobileVrCamera.bluetooth.DummyBluetoothController
import com.aulbachscheuerpflug.mobileVrCamera.camera.CameraDataSingleton
import com.aulbachscheuerpflug.mobileVrCamera.camera.CameraSynchronizer
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Grey
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.White
import com.aulbachscheuerpflug.mobileVrCamera.utils.showDebugError
import kotlin.math.abs


@OptIn(ExperimentalComposeUiApi::class)
@androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
@Composable
fun CameraView(
    previewView: PreviewView?,
    activeCameraInfo: CameraInfo?,
    activeCameraControl: CameraControl?,
    context: Context,
    mainViewModel: MainViewModel,
    bluetoothViewModel: BluetoothViewModel,
    exactRotation: Int,
) {
    val configuration = LocalConfiguration.current
    val sizeModifier = Modifier
        .fillMaxWidth()
        .height((configuration.screenWidthDp / 3 * 4).dp)
    val rotationText =
        if (exactRotation >= 180) exactRotation - 360 else exactRotation

    Box {
        previewView?.let {
            AndroidView({ previewView },
                modifier = sizeModifier
                    .pointerInteropFilter {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN -> {
                                true
                            }

                            MotionEvent.ACTION_UP -> {
                                val factory: MeteringPointFactory =
                                    DisplayOrientedMeteringPointFactory(
                                        context.display!!,
                                        activeCameraInfo!!,
                                        previewView.width.toFloat(),
                                        previewView.height.toFloat()
                                    )

                                val autoFocusPoint = factory.createPoint(it.x, it.y)
                                try {
                                    activeCameraControl?.startFocusAndMetering(
                                        FocusMeteringAction
                                            .Builder(autoFocusPoint)
                                            .apply { disableAutoCancel() }
                                            .build())
                                    @ExperimentalCamera2Interop
                                    CameraDataSingleton.getInstance().activeCamera2CameraControl =
                                        Camera2CameraControl.from(
                                            activeCameraControl!!
                                        )
                                } catch (e: CameraInfoUnavailableException) {
                                    showDebugError(NO_ACCESS_CAMERA, error = e)
                                }
                                true
                            }

                            else -> false
                        }
                    })
            if (mainViewModel.useManualCameraSettings) {
                val floatSensitivityRange =
                    CameraDataSingleton.getInstance().sensitivityRange.lower.toFloat()..CameraDataSingleton.getInstance().sensitivityRange.upper.toFloat()
                val floatExposureRange =
                    CameraDataSingleton.getInstance().exposureRange.lower.toFloat()..CameraDataSingleton.getInstance().exposureRange.upper.toFloat()

                Slider(
                    valueRange = 0f..CameraDataSingleton.getInstance().minFocusDistance,
                    value = mainViewModel.focusSliderValue,
                    onValueChange = {
                        mainViewModel.focusSliderValue = it
                        CameraSynchronizer.updateCaptureRequestConfig(
                            CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_OFF
                        )
                        CameraSynchronizer.updateCaptureRequestConfig(
                            CaptureRequest.LENS_FOCUS_DISTANCE,
                            it
                        )
                        CameraDataSingleton.getInstance().activeCamera2CameraControl!!.captureRequestOptions =
                            CameraSynchronizer.getCaptureRequestOptions()
                    },
                    modifier = Modifier
                        .offset(x = (LocalConfiguration.current.screenWidthDp * -0.4).dp)
                        .rotate(270f)
                        .align(Alignment.CenterStart),
                    colors = SliderDefaults.colors(
                        thumbColor = White,
                        activeTrackColor = White,
                        inactiveTrackColor = Grey,
                    ),
                    onValueChangeFinished = { sendUpdatedSettings(bluetoothViewModel, mainViewModel) },
                )
                Slider(
                    valueRange = floatSensitivityRange,
                    value = mainViewModel.sensitivitySliderValue,
                    onValueChange = { newValue ->
                        mainViewModel.sensitivitySliderValue = newValue
                        CameraSynchronizer.updateCaptureRequestConfig(
                            CaptureRequest.SENSOR_SENSITIVITY,
                            newValue.toInt()
                        )
                        CameraDataSingleton.getInstance().activeCamera2CameraControl!!.captureRequestOptions =
                            CameraSynchronizer.getCaptureRequestOptions()
                    },
                    modifier = Modifier
                        .offset(x = (LocalConfiguration.current.screenWidthDp * -0.4 + 40).dp)
                        .rotate(270f)
                        .align(Alignment.CenterStart),
                    colors = SliderDefaults.colors(
                        thumbColor = White,
                        activeTrackColor = White,
                        inactiveTrackColor = Grey,
                    ),
                    onValueChangeFinished = { sendUpdatedSettings(bluetoothViewModel, mainViewModel) },
                )
                Slider(
                    valueRange = floatExposureRange,
                    value = mainViewModel.exposureSliderValue,
                    onValueChange = { newValue ->
                        mainViewModel.exposureSliderValue = newValue
                        CameraSynchronizer.updateCaptureRequestConfig(CaptureRequest.SENSOR_EXPOSURE_TIME, newValue.toLong())
                        CameraDataSingleton.getInstance().activeCamera2CameraControl!!.captureRequestOptions =
                            CameraSynchronizer.getCaptureRequestOptions()
                    },
                    modifier = Modifier
                        .offset(x = (LocalConfiguration.current.screenWidthDp * -0.4 + 80).dp)
                        .rotate(270f)
                        .align(Alignment.CenterStart),
                    colors = SliderDefaults.colors(
                        thumbColor = White,
                        activeTrackColor = White,
                        inactiveTrackColor = Grey,
                    ),
                    onValueChangeFinished = { sendUpdatedSettings(bluetoothViewModel, mainViewModel) },
                )
                Text("F", color = White, modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 30.dp, start = 30.dp))
                Text("S", color = White, modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 30.dp, start = 70.dp))
                Text("E", color = White, modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 30.dp, start = 110.dp))
            }
            Text(
                text = abs(rotationText).toString() + "°",
                color = White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .rotate(-exactRotation.toFloat())
                    .padding(bottom = 20.dp)
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val centerX = canvasWidth / 2
                val centerY = canvasHeight / 2

                val lineLength = 100
                val lineStartX = centerX - lineLength / 2
                val lineEndX = centerX + lineLength / 2

                rotate(-exactRotation.toFloat(), pivot = Offset(centerX, centerY)) {
                    drawLine(
                        color = White,
                        start = Offset(lineStartX, centerY),
                        end = Offset(lineEndX, centerY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        } ?: Box(
            modifier = sizeModifier
                .background(Grey)
        ) {
            Text(
                DISABLED_CAMERA,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp),
                textAlign = TextAlign.Center
            )
            Button(onClick = {
                val packageManager = context.packageManager
                val intent = packageManager?.getLaunchIntentForPackage(context.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (intent != null) {
                    startActivity(context, intent, null)
                }
            }, modifier = Modifier.align(Alignment.BottomCenter)) {
                Text(RESTART_APP)
            }
        }

        OverlayGrid(visible = mainViewModel.gridEnabled, sizeModifier)
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun PreviewCameraView() {
    val mainViewModelDummy = MainViewModel()
    val bluetoothControllerDummy = DummyBluetoothController()
    val bluetoothViewModelDummy = BluetoothViewModel(bluetoothControllerDummy)
    CameraView(
        previewView = null,
        activeCameraControl = null,
        activeCameraInfo = null,
        context = LocalContext.current,
        mainViewModel = mainViewModelDummy,
        bluetoothViewModel = bluetoothViewModelDummy,
        exactRotation = 0,
    )
}