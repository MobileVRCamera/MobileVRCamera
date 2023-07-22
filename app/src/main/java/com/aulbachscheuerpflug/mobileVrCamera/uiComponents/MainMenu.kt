package com.aulbachscheuerpflug.mobileVrCamera.uiComponents

import InfoPopUp
import android.content.Context
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aulbachscheuerpflug.mobileVrCamera.EQUI_TRANSFER
import com.aulbachscheuerpflug.mobileVrCamera.GUIDED_MODE_BLOCK
import com.aulbachscheuerpflug.mobileVrCamera.GuidedModeSteps
import com.aulbachscheuerpflug.mobileVrCamera.IMAGE_TRANSFER
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import com.aulbachscheuerpflug.mobileVrCamera.bluetooth.BluetoothUiState
import com.aulbachscheuerpflug.mobileVrCamera.bluetooth.BluetoothViewModel
import com.aulbachscheuerpflug.mobileVrCamera.bluetooth.DummyBluetoothController
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.ImmerVrGradient
import com.aulbachscheuerpflug.mobileVrCamera.utils.showToast

@Composable
fun MainMenuScreen(
    onBluetoothEditorClicked: () -> Unit,
    onTakePhotoClicked: () -> Unit,
    onSettingsInfoClicked: () -> Unit,
    onGalleryClicked: () -> Unit,
    previewView: PreviewView?,
    activeCameraInfo: CameraInfo?,
    activeCameraControl: CameraControl?,
    context: Context,
    bluetoothViewModel: BluetoothViewModel,
    bluetoothState: BluetoothUiState,
    mainViewModel: MainViewModel,
    rotation: Int,
    exactRotation: Int,
) {
    val configuration = LocalConfiguration.current

    val animatedRotationDegrees by animateFloatAsState(
        targetValue = rotation.toFloat(),
        animationSpec = TweenSpec(durationMillis = 500, easing = LinearOutSlowInEasing),
    )
    val activeProgress: Int = when {
        bluetoothState.transferProgress in 1 until 99 -> bluetoothState.transferProgress
        mainViewModel.persp2EquiProgression in 1 until 99 -> mainViewModel.persp2EquiProgression
        else -> 0
    }

    val progressText: String = when (activeProgress) {
        bluetoothState.transferProgress -> IMAGE_TRANSFER
        mainViewModel.persp2EquiProgression -> EQUI_TRANSFER
        else -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = ImmerVrGradient)
    ) {
        TopBar(
            onInfoButton = {
                if (mainViewModel.guidedModeSteps != null) {
                    showToast(context, GUIDED_MODE_BLOCK)
                } else {
                    mainViewModel.showInfoPopUps = !mainViewModel.showInfoPopUps
                }
            },
            onGuidedModeButton = {
                mainViewModel.guidedModeSteps = if (mainViewModel.guidedModeSteps != null) {
                    mainViewModel.showInfoPopUps = false
                    null
                } else {
                    mainViewModel.showInfoPopUps = true
                    GuidedModeSteps.BLUETOOTH
                }
            },
            rotation = rotation,
            mainViewModel = mainViewModel,
        )
        CameraView(
            previewView,
            activeCameraInfo,
            activeCameraControl,
            context,
            mainViewModel = mainViewModel,
            bluetoothViewModel = bluetoothViewModel,
            exactRotation = exactRotation,
        )
        AnimatedProgressBar(activeProgress, progressText)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height((configuration.screenHeightDp / 4).dp)
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsGalleryButtons(
                mainViewModel = mainViewModel,
                onSettingsInfoClicked = onSettingsInfoClicked,
                onGalleryClicked = onGalleryClicked,
                context = context,
                rotation = rotation
            )
            TakePhotoButton(
                mainViewModel = mainViewModel,
                bluetoothState = bluetoothState,
                onTakePhotoClicked = onTakePhotoClicked,
                rotation = rotation
            )

            BluetoothButton(
                mainViewModel = mainViewModel,
                bluetoothState = bluetoothState,
                onBluetoothEditorClicked = onBluetoothEditorClicked,
                rotation = rotation,
            )

        }
    }

    mainViewModel.spotlightPosition?.let { position ->
        Spotlight(
            intOffset = position,
            isSpotlightVisible = true
        )
    }

    AnimatedVisibility(
        visible = mainViewModel.showDescPopUp,
        enter = slideInVertically(),
        exit = slideOutVertically(),
    ) {
        InfoPopUp(
            onCloseButton = {
                when (mainViewModel.guidedModeSteps) {
                    GuidedModeSteps.BLUETOOTH -> {
                        if (bluetoothState.isConnected) {
                            mainViewModel.showDescPopUp = false
                            mainViewModel.guidedModeSteps = GuidedModeSteps.values()[
                                    (mainViewModel.guidedModeSteps!!.ordinal + 1) % GuidedModeSteps.values().size
                            ]
                        } else {
                            onBluetoothEditorClicked()
                        }
                    }

                    GuidedModeSteps.PHOTO -> {
                        onTakePhotoClicked()
                        mainViewModel.showDescPopUp = false
                        mainViewModel.guidedModeSteps = GuidedModeSteps.values()[
                                (mainViewModel.guidedModeSteps!!.ordinal + 1) % GuidedModeSteps.values().size
                        ]
                    }

                    GuidedModeSteps.GALLERY -> {
                        mainViewModel.showInfoPopUps = false
                        mainViewModel.showDescPopUp = false
                        mainViewModel.isSpotlightVisible = false
                        mainViewModel.guidedModeSteps = null
                        onGalleryClicked()
                    }

                    else -> {
                        mainViewModel.showInfoPopUps = false
                        mainViewModel.showDescPopUp = false
                        mainViewModel.isSpotlightVisible = !mainViewModel.isSpotlightVisible
                    }
                }
            },
            descriptionText = mainViewModel.descriptionText,
            fontSize = 20,
            titleText = mainViewModel.titleText,
            offsetX = configuration.screenWidthDp / 20,
            offsetY = configuration.screenHeightDp / 7,
            width = configuration.screenWidthDp / 10 * 9,
            height = previewView!!.height / 4,
            rotateDegrees = animatedRotationDegrees
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewMainMenuScreen() {
    val bluetoothUiStateDummy = BluetoothUiState(
        scannedDevices = listOf(),
        pairedDevices = listOf(),
        isConnected = false,
        isConnecting = false,
        errorMessage = null,
        transferProgress = 50,
        isScanning = false,
    )
    val bluetoothControllerDummy = DummyBluetoothController()
    val bluetoothViewModelDummy = BluetoothViewModel(bluetoothControllerDummy)
    val mainViewModelDummy = MainViewModel()
    MainMenuScreen(
        onBluetoothEditorClicked = {},
        onTakePhotoClicked = {},
        onSettingsInfoClicked = {},
        onGalleryClicked = {},
        previewView = null,
        activeCameraInfo = null,
        activeCameraControl = null,
        context = LocalContext.current,
        bluetoothState = bluetoothUiStateDummy,
        mainViewModel = mainViewModelDummy,
        bluetoothViewModel = bluetoothViewModelDummy,
        rotation = 0,
        exactRotation = 0,
    )
}