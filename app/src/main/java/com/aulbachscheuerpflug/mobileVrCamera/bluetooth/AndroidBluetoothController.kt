package com.aulbachscheuerpflug.mobileVrCamera.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import com.aulbachscheuerpflug.mobileVrCamera.APP_NAME
import com.aulbachscheuerpflug.mobileVrCamera.BLUETOOTH_CONNECTION_ERROR
import com.aulbachscheuerpflug.mobileVrCamera.NO_BLUETOOTH_CONNECTION_PERMISSION
import com.aulbachscheuerpflug.mobileVrCamera.STATE_RECEIVER_CONNECTION_ERROR
import com.aulbachscheuerpflug.mobileVrCamera.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context: Context
) : BluetoothController {

    private val _transferProgress = MutableStateFlow(0)
    override val transferProgress: StateFlow<Int> = _transferProgress

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private var dataTransferService: BluetoothDataTransferService? = null

    private val _isConnected = MutableStateFlow<Boolean>(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()

    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }

    private val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, bluetoothDevice ->
        if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
            _isConnected.update { isConnected }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _errors.tryEmit(STATE_RECEIVER_CONNECTION_ERROR)
            }
        }
    }

    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null


    init {
        updatePairedDevices()
        context.registerReceiver(bluetoothStateReceiver, IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        })
    }

    override fun startDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        context.registerReceiver(
            foundDeviceReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        updatePairedDevices()

        bluetoothAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        bluetoothAdapter?.cancelDiscovery()
    }

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException(NO_BLUETOOTH_CONNECTION_PERMISSION)
            }
            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                APP_NAME, UUID.fromString(MOBILE_VR_CAMERA_UUID)
            )

            var openConnection = true
            while (openConnection) {
                currentClientSocket = try {
                    currentServerSocket?.accept()
                } catch (exception: IOException) {
                    openConnection = false
                    null
                }
                emit(ConnectionResult.ConnectionEstablished)
                currentClientSocket?.let { socket ->
                    currentServerSocket?.close()
                    val service = BluetoothDataTransferService(socket) { progress ->
                        _transferProgress.value = progress
                    }
                    dataTransferService = service
                    emitAll(service.listenForIncomingMessages().map {
                        when (val message = it) {
                            is BluetoothMessage -> ConnectionResult.TransferSucceeded(
                                message
                            )

                            is SettingsMessage -> ConnectionResult.SettingsTransferSucceeded(
                                message
                            )
                        }
                    })
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException(NO_BLUETOOTH_CONNECTION_PERMISSION)
            }
            val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address)

            currentClientSocket = bluetoothDevice?.createRfcommSocketToServiceRecord(
                UUID.fromString(MOBILE_VR_CAMERA_UUID)
            )
            stopDiscovery()

            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)
                    BluetoothDataTransferService(socket) { progress ->
                        _transferProgress.value = progress
                    }.also { bluetoothDataTransferService ->
                        dataTransferService = bluetoothDataTransferService
                        emitAll(bluetoothDataTransferService.listenForIncomingMessages().map {
                            when (val message = it) {
                                is BluetoothMessage -> ConnectionResult.TransferSucceeded(
                                    message
                                )

                                is SettingsMessage -> ConnectionResult.SettingsTransferSucceeded(
                                    message
                                )
                            }
                        })
                    }
                } catch (exception: IOException) {
                    socket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error(BLUETOOTH_CONNECTION_ERROR))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun trySendMessage(
        message: String,
        bitmapImage: Bitmap,
        quality: Int,
        manualMode: Boolean,
        focus: Float,
        exposure: Long,
        sensitivity: Int,
        stabilization: Boolean,
        equirectangular: Boolean,
    ): BluetoothMessage? {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT) || dataTransferService == null) {
            return null
        }

        val bluetoothMessage = BluetoothMessage(
            message = message,
            bitmapImage = bitmapImage,
            manualMode = manualMode,
            focus = focus,
            exposure = exposure,
            sensitivity = sensitivity,
            stabilization = stabilization,
            equirectangular = equirectangular,
        )
        dataTransferService?.sendMessage(bluetoothMessage.toByteArray(quality))
        return bluetoothMessage
    }

    override suspend fun trySendSettingsMessage(
        grid: Boolean, flashlight: Boolean, stabilization: Boolean, manualSensor: Boolean, equirectangular: Boolean, focus: Float, exposure: Long, sensitivity: Int,
    ): SettingsMessage? {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT) || dataTransferService == null) {
            return null
        }
        val settingsMessage = SettingsMessage(
            grid = grid,
            flashlight = flashlight,
            stabilization = stabilization,
            manualSensor = manualSensor,
            equirectangular = equirectangular,
            focus = focus,
            exposure = exposure,
            sensitivity = sensitivity,
        )
        dataTransferService?.sendMessage(settingsMessage.toByteArray())
        return settingsMessage
    }

    override fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentClientSocket = null
        currentServerSocket = null
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
    }

    private fun updatePairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        bluetoothAdapter?.bondedDevices?.map { it.toBluetoothDeviceDomain() }?.also { devices ->
            _pairedDevices.update { devices }
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        // Rnd Generated UUID
        // Must be the same on all devices
        const val MOBILE_VR_CAMERA_UUID = "36f674f4-be8e-11ed-afa1-0242ac120002"
    }
}