package com.aulbachscheuerpflug.mobileVrCamera.uiComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.aulbachscheuerpflug.mobileVrCamera.BLUETOOTH_INFO_DESC
import com.aulbachscheuerpflug.mobileVrCamera.BLUETOOTH_ROUTE
import com.aulbachscheuerpflug.mobileVrCamera.GuidedModeSteps
import com.aulbachscheuerpflug.mobileVrCamera.INFO_CONTENT_DESC
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import com.aulbachscheuerpflug.mobileVrCamera.R
import com.aulbachscheuerpflug.mobileVrCamera.bluetooth.BluetoothUiState
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Black
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Grey
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.LightGreen
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Red
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.RoundShape
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.White
import com.aulbachscheuerpflug.mobileVrCamera.utils.playSystemSound
import kotlin.math.roundToInt

@Composable
fun BluetoothButton(
    mainViewModel: MainViewModel,
    bluetoothState: BluetoothUiState,
    onBluetoothEditorClicked: () -> Unit,
    rotation: Int,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val animatedRotationDegrees by animateFloatAsState(
        targetValue = rotation.toFloat(),
        animationSpec = TweenSpec(durationMillis = 500, easing = LinearOutSlowInEasing)
    )

    Surface(
        color = Color.Transparent,
        shape = RectangleShape,
        modifier = Modifier.size((configuration.screenWidthDp / 3).dp)
    ) {
        Surface(
            color = White,
            shape = RoundShape,
            modifier = Modifier
                .size(80.dp)
                .padding(all = 30.dp)
                .border(1.dp, Black, RoundShape)
        ) {
            IconButton(onClick = if (!mainViewModel.showInfoPopUps) {
                {
                    playSystemSound(context)
                    onBluetoothEditorClicked()
                }
            } else {
                {
                    playSystemSound(context)
                    mainViewModel.titleText = BLUETOOTH_ROUTE
                    mainViewModel.descriptionText = BLUETOOTH_INFO_DESC
                    mainViewModel.showDescPopUp = true
                }
            },
                modifier = Modifier.padding(8.dp),
                enabled = mainViewModel.guidedModeSteps == null || mainViewModel.guidedModeSteps == GuidedModeSteps.BLUETOOTH) {
                Icon(
                    painter = painterResource(id = if (bluetoothState.isConnected) R.drawable.baseline_bluetooth_24 else R.drawable.baseline_bluetooth_disabled_24),
                    contentDescription = "Bluetooth-Button",
                    tint = if (bluetoothState.isConnected) LightGreen else Red,
                    modifier = Modifier
                        .requiredSize(45.dp)
                        .onGloballyPositioned { layoutCoordinates ->
                            val position = layoutCoordinates.localToRoot(Offset(0f, 0f))
                            val size = layoutCoordinates.size
                            val centerX = position.x + size.width / 2
                            val centerY = position.y + size.height / 2
                            mainViewModel.bluetoothButtonPosition =
                                IntOffset(centerX.roundToInt(), centerY.roundToInt())
                        }
                        .rotate(animatedRotationDegrees),
                )
            }

        }
        AnimatedVisibility(
            visible = mainViewModel.showInfoPopUps,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_info_24),
                modifier = Modifier
                    .requiredSize(30.dp)
                    .offset(35.dp, 20.dp)
                    .border(1.dp, Black, RoundShape)
                    .background(Black, shape = RoundShape)
                    .rotate(animatedRotationDegrees),
                contentDescription = INFO_CONTENT_DESC,
                tint = if (mainViewModel.descriptionText == BLUETOOTH_INFO_DESC && mainViewModel.showDescPopUp) Grey else White,
            )
        }
    }
}

@Preview
@Composable
fun BluetoothButtonPreview() {
    val mainViewModelDummy = MainViewModel()
    val bluetoothUiStateDummy = BluetoothUiState(
        scannedDevices = listOf(),
        pairedDevices = listOf(),
        isConnected = false,
        isConnecting = false,
        errorMessage = null,
        transferProgress = 50,
        isScanning = false,
    )
    BluetoothButton(
        mainViewModel = mainViewModelDummy,
        bluetoothState = bluetoothUiStateDummy,
        onBluetoothEditorClicked = {},
        rotation = 0,
    )
}