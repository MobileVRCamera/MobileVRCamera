package com.aulbachscheuerpflug.mobileVrCamera.uiComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulbachscheuerpflug.mobileVrCamera.Duration
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import com.aulbachscheuerpflug.mobileVrCamera.NOT_AVAILABLE_DESC
import com.aulbachscheuerpflug.mobileVrCamera.R
import com.aulbachscheuerpflug.mobileVrCamera.SETTINGS_INFO_ROUTE
import com.aulbachscheuerpflug.mobileVrCamera.STEREO_PHOTO_MAKER_URL
import com.aulbachscheuerpflug.mobileVrCamera.bluetooth.BluetoothViewModel
import com.aulbachscheuerpflug.mobileVrCamera.bluetooth.DummyBluetoothController
import com.aulbachscheuerpflug.mobileVrCamera.camera.CameraUtility
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Black
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Grey
import com.aulbachscheuerpflug.mobileVrCamera.utils.openWebsite
import com.aulbachscheuerpflug.mobileVrCamera.utils.playSystemSound
import com.aulbachscheuerpflug.mobileVrCamera.utils.showToast

@androidx.camera.camera2.interop.ExperimentalCamera2Interop
@Composable
fun SettingsInfoScreen(
    onCloseButton: () -> Unit,
    rotation: Int,
    mainViewModel: MainViewModel,
    bluetoothViewModel: BluetoothViewModel,
) {
    val context = LocalContext.current
    val cameraUtility = CameraUtility()
    val focusManager = LocalFocusManager.current

    val antiAliasingText = remember { mutableStateOf(mainViewModel.antiAliasing.toString()) }
    val imageScalingText = remember { mutableStateOf(mainViewModel.outputImageScaling.toString()) }
    val deviceFovText = remember { mutableStateOf(mainViewModel.deviceFov.toString()) }
    DisposableEffect(mainViewModel.antiAliasing) {
        antiAliasingText.value = mainViewModel.antiAliasing.toString()
        onDispose { }
    }
    DisposableEffect(mainViewModel.outputImageScaling) {
        imageScalingText.value = mainViewModel.outputImageScaling.toString()
        onDispose { }
    }

    DisposableEffect(mainViewModel.deviceFov) {
        deviceFovText.value = mainViewModel.deviceFov.toString()
        onDispose { }
    }

    TopBar(
        onCloseButton = onCloseButton,
        rotation = rotation,
        mainViewModel = mainViewModel,
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
            .padding(top = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, start = 20.dp, end = 20.dp)
                .align(Alignment.TopCenter),
        ) {
            Text(
                modifier = Modifier.padding(bottom = 10.dp),
                text = SETTINGS_INFO_ROUTE,
                fontSize = 24.sp,
                color = Black
            )

            Box(
                modifier = Modifier.padding(top = 40.dp), contentAlignment = Alignment.CenterStart
            ) {
                Text(text = "Timer (s): ", fontSize = 18.sp, color = Black)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    (0 until Duration.values().size).forEach { index ->
                        val option = Duration.values()[index]
                        Column(modifier = Modifier
                            .clickable {
                                playSystemSound(context)
                                mainViewModel.selectedDuration = option
                            }
                            .padding(horizontal = 4.dp)) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_access_time_24),
                                contentDescription = option.name,
                                tint = if (option == mainViewModel.selectedDuration) Black else Grey,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = option.name, fontSize = 12.sp, color = Black
                            )
                        }
                    }
                }
            }

            DividerWithSpacing()

            SettingsSwitchRow(
                text = "Toggle Grid",
                isChecked = mainViewModel.gridEnabled,
                onCheckedChange = {
                    mainViewModel.gridEnabled = it
                    sendUpdatedSettings(bluetoothViewModel, mainViewModel)
                }
            )

            DividerWithSpacing()

            SettingsSwitchRow(
                text = "Toggle Flashlight",
                isChecked = mainViewModel.flashlightEnabled,
                onCheckedChange = {
                    mainViewModel.flashlightEnabled = it
                    cameraUtility.toggleFlashlight(mainViewModel.flashlightEnabled)
                    sendUpdatedSettings(bluetoothViewModel, mainViewModel)
                }
            )

            DividerWithSpacing()

            SettingsSwitchRow(
                text = "Toggle Image Stabilization",
                isChecked = mainViewModel.imageStabilization,
                onCheckedChange = {
                    mainViewModel.imageStabilization = it
                    cameraUtility.toggleVideoStabilization(mainViewModel.imageStabilization)
                    sendUpdatedSettings(bluetoothViewModel, mainViewModel)
                }
            )

            DividerWithSpacing()

            SettingsSwitchRow(
                text = "Toggle manual sensor settings",
                isChecked = mainViewModel.useManualCameraSettings,
                onCheckedChange = {
                    mainViewModel.useManualCameraSettings = it
                    cameraUtility.toggleManualModes(it)
                    sendUpdatedSettings(bluetoothViewModel, mainViewModel)
                },
            )

            DividerWithSpacing()

            SettingsSwitchRow(
                text = "Host Phone on the left?",
                isChecked = mainViewModel.hostPhoneLeftPlacement,
                onCheckedChange = { mainViewModel.hostPhoneLeftPlacement = it }
            )

            DividerWithSpacing()

            SettingsSwitchRow(
                text = "Use Bluetooth for Stereo Image",
                isChecked = mainViewModel.bluetoothImageCapture,
                onCheckedChange = { mainViewModel.bluetoothImageCapture = it },
            )

            DividerWithSpacing()

            SettingsSwitchRow(
                text = "Toggle Equirectangular",
                isChecked = mainViewModel.equirectangularTransformation,
                onCheckedChange = {
                    mainViewModel.equirectangularTransformation = it
                    sendUpdatedSettings(bluetoothViewModel, mainViewModel)
                },
            )

            DividerWithSpacing()

            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Device FOV",
                        fontSize = 18.sp,
                        color = Black
                    )
                }
                TextField(
                    value = deviceFovText.value,
                    onValueChange = { newValue ->
                        deviceFovText.value = newValue
                        mainViewModel.deviceFov = newValue.toIntOrNull() ?: mainViewModel.deviceFov
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() },
                    ),
                    singleLine = true,
                    modifier = Modifier.padding(start = 160.dp)
                )
            }

            DividerWithSpacing()

            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Anti-Aliasing",
                        fontSize = 18.sp,
                        color = Black
                    )
                }
                TextField(
                    value = antiAliasingText.value,
                    onValueChange = { newValue ->
                        antiAliasingText.value = newValue
                        mainViewModel.antiAliasing =
                            newValue.toIntOrNull() ?: mainViewModel.antiAliasing
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            sendUpdatedSettings(bluetoothViewModel, mainViewModel)
                        },
                    ),
                    singleLine = true,
                    modifier = Modifier.padding(start = 160.dp)
                )
            }

            DividerWithSpacing()

            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Output Image-Scaling",
                        fontSize = 18.sp,
                        color = Black
                    )
                }
                TextField(
                    value = imageScalingText.value,
                    onValueChange = { newValue ->
                        imageScalingText.value = newValue
                        mainViewModel.outputImageScaling =
                            newValue.toIntOrNull() ?: mainViewModel.outputImageScaling
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            sendUpdatedSettings(bluetoothViewModel, mainViewModel)
                        },
                    ),
                    singleLine = true,
                    modifier = Modifier.padding(start = 90.dp)
                )
            }

            DividerWithSpacing()

            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Optimise your stereo images",
                    fontSize = 18.sp,
                    color = Black
                )
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(onClick = {
                    playSystemSound(context)
                    openWebsite(context, STEREO_PHOTO_MAKER_URL)
                }) {
                    Text(
                        text = "Stereo Photo Maker",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

fun sendUpdatedSettings(bluetoothViewModel: BluetoothViewModel, mainViewModel: MainViewModel) {
    bluetoothViewModel.sendSettingsMessage(
        grid = mainViewModel.gridEnabled,
        flashlight = mainViewModel.flashlightEnabled,
        stabilization = mainViewModel.imageStabilization,
        manualSensor = mainViewModel.useManualCameraSettings,
        equirectangular = mainViewModel.equirectangularTransformation,
        focus = mainViewModel.focusSliderValue,
        exposure = mainViewModel.exposureSliderValue.toLong(),
        sensitivity = mainViewModel.sensitivitySliderValue.toInt(),
        antiAliasing = mainViewModel.antiAliasing,
        imageScaling = mainViewModel.outputImageScaling,
    )
}

@Composable
fun SettingsSwitchRow(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .clickable {
                if (!enabled) showToast(
                    context,
                    NOT_AVAILABLE_DESC
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, fontSize = 18.sp, color = Black)
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = {
                playSystemSound(context)
                onCheckedChange(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Black,
                checkedTrackColor = Black,
            ),
            enabled = enabled,
        )
    }
}

@Composable
fun DividerWithSpacing() {
    Divider(color = Black, thickness = 1.dp, modifier = Modifier.padding(top = 20.dp))
}

@androidx.camera.camera2.interop.ExperimentalCamera2Interop
@Preview(showBackground = true)
@Composable
fun SettingsInfoScreenPreview() {
    val mainViewModelDummy = MainViewModel()
    val bluetoothControllerDummy = DummyBluetoothController()
    val bluetoothViewModelDummy = BluetoothViewModel(bluetoothControllerDummy)
    SettingsInfoScreen(
        onCloseButton = {},
        rotation = 0,
        mainViewModel = mainViewModelDummy,
        bluetoothViewModel = bluetoothViewModelDummy,
    )
}