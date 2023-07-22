package com.aulbachscheuerpflug.mobileVrCamera.bluetooth

sealed interface ConnectionResult {
    object ConnectionEstablished : ConnectionResult
    data class Error(val message: String) : ConnectionResult
    data class TransferSucceeded(val transferMessage: BluetoothMessage) : ConnectionResult
    data class SettingsTransferSucceeded(val settings: SettingsMessage) : ConnectionResult
}