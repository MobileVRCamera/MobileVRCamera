package com.aulbachscheuerpflug.mobileVrCamera.bluetooth

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aulbachscheuerpflug.mobileVrCamera.RECEIVE_IMAGE
import com.aulbachscheuerpflug.mobileVrCamera.STATE_TIMEOUT_MILLIS
import com.aulbachscheuerpflug.mobileVrCamera.TAKE_PICTURE_REQUEST
import com.aulbachscheuerpflug.mobileVrCamera.camera.CameraSynchronizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {

    private val _receiveTakePhotoEvent = MutableSharedFlow<Unit>()
    val receiveTakePhotoEvent: SharedFlow<Unit> = _receiveTakePhotoEvent

    private val _receivePhotoEvent = MutableSharedFlow<Bitmap>()
    val receivePhotoEvent: SharedFlow<Bitmap> = _receivePhotoEvent

    private val _receiveSettingsEvent = MutableSharedFlow<SettingsMessage>()
    val receiveSettingsEvent: SharedFlow<SettingsMessage> = _receiveSettingsEvent

    private val _transferProgress =
        bluetoothController.transferProgress.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _messageReceivedEvent = MutableSharedFlow<String>()
    val messageReceivedEvent: SharedFlow<String> = _messageReceivedEvent.asSharedFlow()

    private val _state = MutableStateFlow(BluetoothUiState())
    val state = combine(
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        _transferProgress,
        _state
    ) { scannedDevices, pairedDevices, transferProgress, state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
            transferProgress = transferProgress,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATE_TIMEOUT_MILLIS.toLong()),
        _state.value
    )

    private var deviceConnectionJob: Job? = null

    init {
        bluetoothController.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)
        bluetoothController.errors.onEach { error ->
            _state.update {
                it.copy(
                    errorMessage = error
                )
            }
        }.launchIn(viewModelScope)
    }

    fun updateBluetoothUiState(newState: BluetoothUiState) = _state.update { newState }

    fun connectToDevice(device: BluetoothDeviceDomain) {
        deviceConnectionJob = bluetoothController.connectToDevice(device).listen()
        _state.update { it.copy(isNewConnected = true) }
    }

    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update {
            it.copy(
                isConnecting = false,
                isConnected = false,
            )
        }
    }

    fun waitForIncomingConnections() {
        deviceConnectionJob = bluetoothController.startBluetoothServer().listen()
    }

    fun sendMessage(
        message: String,
        bitmapImage: Bitmap,
        quality: Int,
        focus: Float,
        exposure: Long,
        sensitivity: Int,
        stabilization: Boolean,
        equirectangular: Boolean,
        manualMode: Boolean,
    ) {
        viewModelScope.launch {
            bluetoothController.trySendMessage(
                message = message,
                bitmapImage = bitmapImage,
                quality = quality,
                manualMode = manualMode,
                focus = focus,
                exposure = exposure,
                sensitivity = sensitivity,
                stabilization = stabilization,
                equirectangular = equirectangular,
            )
        }
    }

    fun sendSettingsMessage(
        grid: Boolean,
        flashlight: Boolean,
        stabilization: Boolean,
        manualSensor: Boolean,
        equirectangular: Boolean,
        focus: Float,
        exposure: Long,
        sensitivity: Int,
        antiAliasing: Int,
        imageScaling: Int,
    ) {
        viewModelScope.launch {
            bluetoothController.trySendSettingsMessage(
                grid = grid,
                flashlight = flashlight,
                stabilization = stabilization,
                manualSensor = manualSensor,
                equirectangular = equirectangular,
                focus = focus,
                exposure = exposure,
                sensitivity = sensitivity,
                antiAliasing = antiAliasing,
                imageScaling = imageScaling
            )
        }
    }

    fun startScan() {
        bluetoothController.startDiscovery()
        _state.update { it.copy(isScanning = !_state.value.isScanning) }
    }

    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when (result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update {
                        it.copy(
                            isConnected = true,
                            isConnecting = false,
                            errorMessage = null
                        )
                    }
                }

                is ConnectionResult.TransferSucceeded -> {
                    if (result.transferMessage.message.contains(TAKE_PICTURE_REQUEST))
                        @androidx.camera.camera2.interop.ExperimentalCamera2Interop {
                            CameraSynchronizer.updateFocusExposureSensitivityStabilization(
                                result.transferMessage.manualMode,
                                result.transferMessage.focus,
                                result.transferMessage.exposure,
                                result.transferMessage.sensitivity,
                                result.transferMessage.stabilization,
                            )
                            viewModelScope.launch {
                                _receiveTakePhotoEvent.emit(Unit)
                            }
                        }
                    if (result.transferMessage.message.contains(RECEIVE_IMAGE)) {
                        viewModelScope.launch {
                            _receivePhotoEvent.emit(result.transferMessage.bitmapImage)
                        }
                    }
                }

                is ConnectionResult.SettingsTransferSucceeded -> {
                    _receiveSettingsEvent.emit(result.settings)
                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            isConnecting = false,
                            isConnected = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }.catch {
            bluetoothController.closeConnection()
            _state.update {
                it.copy(
                    isConnected = false,
                    isConnecting = false,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }
}