package com.aulbachscheuerpflug.mobileVrCamera.uiComponents

import InfoPopUp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulbachscheuerpflug.mobileVrCamera.BLUETOOTH_SELECTION_DESC
import com.aulbachscheuerpflug.mobileVrCamera.BLUETOOTH_SELECTION_GUIDE_DESC
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import com.aulbachscheuerpflug.mobileVrCamera.bluetooth.BluetoothDevice
import com.aulbachscheuerpflug.mobileVrCamera.bluetooth.BluetoothUiState
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.LightGrey
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Shapes
import com.aulbachscheuerpflug.mobileVrCamera.utils.playSystemSound

@Composable
fun BluetoothScreen(
    state: BluetoothUiState,
    mainViewModel: MainViewModel,
    onDeviceClick: (BluetoothDevice) -> Unit,
    onDisconnectClick: () -> Unit,
    onCloseBluetoothEditor: () -> Unit,
    rotation: Int,
    bluetoothState: BluetoothUiState,
) {

    val configuration = LocalConfiguration.current

    val animatedRotationDegrees by animateFloatAsState(
        targetValue = rotation.toFloat(),
        animationSpec = TweenSpec(durationMillis = 500, easing = LinearOutSlowInEasing)
    )

    TopBar(
        onCloseBluetoothEditor,
        rotation = rotation,
        mainViewModel = MainViewModel(),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),
        ) {
            AnimatedVisibility(
                visible = mainViewModel.showDescPopUp,
                enter = slideInVertically(),
                exit = slideOutVertically(),
            ) {
                InfoPopUp(
                    titleText = "Bluetooth Device Selection",
                    descriptionText = if (mainViewModel.guidedModeSteps == null) BLUETOOTH_SELECTION_DESC else BLUETOOTH_SELECTION_GUIDE_DESC,
                    offsetX = 10,
                    offsetY = 10,
                    width = configuration.screenWidthDp - 20,
                    height = configuration.screenHeightDp / 2,
                    titleFontSize = 22,
                    fontSize = 18,
                    rotateDegrees = animatedRotationDegrees,
                )
            }
            BluetoothDeviceList(
                pairedDevices = state.pairedDevices,
                scannedDevices = state.scannedDevices,
                onClick = onDeviceClick,
                onDisconnectClick = onDisconnectClick,
                isScanning = state.isScanning,
                modifier = Modifier
                    .fillMaxWidth(),
                bluetoothState = bluetoothState,
            )
        }
    }
}

@Composable
fun BluetoothDeviceList(
    pairedDevices: List<BluetoothDevice>,
    scannedDevices: List<BluetoothDevice>,
    onClick: (BluetoothDevice) -> Unit,
    onDisconnectClick: () -> Unit,
    modifier: Modifier = Modifier,
    isScanning: Boolean,
    bluetoothState: BluetoothUiState,
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Paired Devices",
            fontSize = 20.sp,
            modifier = Modifier.padding(16.dp)
        )
        if (bluetoothState.isConnected) {
            TextButton(onClick = onDisconnectClick) {
                Text(text = "Disconnect from Device", fontSize = 14.sp)
            }
        }
    }
    LazyColumn(
        modifier = modifier
            .padding(5.dp)
            .clip(shape = Shapes.small)
            .background(LightGrey)
            .height((LocalConfiguration.current.screenHeightDp / 3).dp),
    ) {
        items(pairedDevices) { device ->
            if (device.name != null) {
                Text(
                    text = device.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onClick(device)
                            playSystemSound(context)
                        }
                        .padding(16.dp)
                )
            }
        }
    }
    Text(
        text = if (isScanning) "Scanning Devices..." else "Scanned Devices",
        fontSize = 20.sp,
        modifier = Modifier.padding(16.dp)
    )
    LazyColumn(
        modifier = modifier
            .padding(5.dp)
            .clip(shape = Shapes.small)
            .background(LightGrey)
    ) {
        items(scannedDevices) { device ->
            if (device.name != null) {
                Text(
                    text = device.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            playSystemSound(context)
                            onClick(device)
                        }
                        .padding(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BluetoothScreenPreview() {
    val dummyState = BluetoothUiState(
        scannedDevices = listOf(
            BluetoothDevice("Felix OnePlus 8t", "00:11:22:33:44:55"),
            BluetoothDevice("Christian Pixel 6a", "01:23:45:67:89:AB"),
            BluetoothDevice("Felix OnePlus 7t", "00:11:22:33:44:55"),
            BluetoothDevice("Christian Pixel 6a", "01:23:45:67:89:AB"),
            BluetoothDevice("Felix OnePlus 6t", "00:11:22:33:44:55"),
            BluetoothDevice("Christian Pixel 5a", "01:23:45:67:89:AB"),
        ),
        pairedDevices = listOf(
            BluetoothDevice("Paired Device 1", "AB:CD:EF:12:34:56"),
            BluetoothDevice("Paired Device 2", "AB:CD:EF:12:34:56"),

        )
    )
    val dummyMainViewModel = MainViewModel()

    BluetoothScreen(
        state = dummyState,
        dummyMainViewModel,
        onDeviceClick = {},
        onDisconnectClick = {},
        onCloseBluetoothEditor = {},
        rotation = 0,
        bluetoothState = BluetoothUiState()
    )
}