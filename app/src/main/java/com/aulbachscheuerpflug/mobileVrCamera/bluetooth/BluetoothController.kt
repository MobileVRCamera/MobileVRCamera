package com.aulbachscheuerpflug.mobileVrCamera.bluetooth

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

interface BluetoothController {
    val isConnected: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    val errors: SharedFlow<String>
    val transferProgress: StateFlow<Int>

    fun startDiscovery()
    fun stopDiscovery()

    fun startBluetoothServer(): Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult>

    fun closeConnection()

    fun release()

    suspend fun trySendMessage(message: String, bitmapImage: Bitmap, quality: Int, manualMode: Boolean, focus: Float, exposure: Long, sensitivity: Int, stabilization: Boolean, equirectangular: Boolean): BluetoothMessage?

    suspend fun trySendSettingsMessage(grid: Boolean, flashlight: Boolean, stabilization: Boolean, manualSensor: Boolean, equirectangular: Boolean, focus: Float, exposure: Long, sensitivity: Int,): SettingsMessage?
}

class DummyBluetoothController : BluetoothController {
    override val isConnected: StateFlow<Boolean> = MutableStateFlow(false)
    override val scannedDevices: StateFlow<List<BluetoothDevice>> = MutableStateFlow(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDevice>> = MutableStateFlow(emptyList())
    override val errors: SharedFlow<String> = MutableSharedFlow()
    override val transferProgress: StateFlow<Int> = MutableStateFlow(0)
    override fun startDiscovery() {}
    override fun stopDiscovery() {}
    override fun startBluetoothServer(): Flow<ConnectionResult> = flow { emit(ConnectionResult.ConnectionEstablished) }
    override fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult> = flow { emit(ConnectionResult.ConnectionEstablished) }
    override fun closeConnection() {}
    override fun release() {}
    override suspend fun trySendMessage(message: String, bitmapImage: Bitmap, quality: Int, manualMode: Boolean, focus: Float, exposure: Long, sensitivity: Int, stabilization: Boolean, equirectangular: Boolean): BluetoothMessage? = null
    override suspend fun trySendSettingsMessage(grid: Boolean, flashlight: Boolean, stabilization: Boolean, manualSensor: Boolean, equirectangular: Boolean, focus: Float, exposure: Long, sensitivity: Int,): SettingsMessage? = null
}
