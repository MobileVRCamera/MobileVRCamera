package com.aulbachscheuerpflug.mobileVrCamera.bluetooth

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isNewConnected: Boolean = false,
    val errorMessage: String? = null,
    val transferProgress: Int = 0,
    val isScanning: Boolean = false,
)
