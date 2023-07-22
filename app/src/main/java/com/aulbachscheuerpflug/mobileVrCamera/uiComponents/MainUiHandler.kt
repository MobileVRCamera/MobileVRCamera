package com.aulbachscheuerpflug.mobileVrCamera.uiComponents

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aulbachscheuerpflug.mobileVrCamera.BLUETOOTH_ROUTE
import com.aulbachscheuerpflug.mobileVrCamera.CAMERA_ROUTE
import com.aulbachscheuerpflug.mobileVrCamera.GALLERY_ROUTE
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import com.aulbachscheuerpflug.mobileVrCamera.SETTINGS_INFO_ROUTE
import com.aulbachscheuerpflug.mobileVrCamera.bluetooth.BluetoothUiState
import com.aulbachscheuerpflug.mobileVrCamera.bluetooth.BluetoothViewModel
import com.aulbachscheuerpflug.mobileVrCamera.camera.CameraDataSingleton
import com.aulbachscheuerpflug.mobileVrCamera.camera.MobileVRCameraController
import com.aulbachscheuerpflug.mobileVrCamera.utils.playSystemSound
import com.aulbachscheuerpflug.mobileVrCamera.utils.showToast

@androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
@Composable
fun MainUiHandler(
    bluetoothViewModel: BluetoothViewModel,
    mobileVRCameraController: MobileVRCameraController,
    mainViewModel: MainViewModel,
    mainActivityLifeCycleOwner: LifecycleOwner,
    context: Context,
    activity: Activity,
    rotation: Int,
    exactRotation: Int,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val state by bluetoothViewModel.state.collectAsState()
    val cameraSetUpState = remember { mutableStateOf(false) }

    BackHandler(
        enabled = (backStackEntry?.destination?.route == BLUETOOTH_ROUTE) ||
                (backStackEntry?.destination?.route == SETTINGS_INFO_ROUTE) ||
                (backStackEntry?.destination?.route == GALLERY_ROUTE),
        onBack = {
            playSystemSound(context)
            mobileVRCameraController.cameraSetup.setupCamera(
                context,
                mainActivityLifeCycleOwner
            )
            navController.navigate(CAMERA_ROUTE)
        }
    )

    PermissionUiHandler(
        mainViewModel = mainViewModel,
        activity = activity,
    )

    NavHost(navController = navController, startDestination = CAMERA_ROUTE) {
        composable(CAMERA_ROUTE) {
            MainMenuScreen(
                onBluetoothEditorClicked = {
                    cameraSetUpState.value = false
                    CameraDataSingleton.getInstance().cameraProvider!!.unbindAll()
                    bluetoothViewModel.startScan()
                    bluetoothViewModel.waitForIncomingConnections()
                    navController.navigate(BLUETOOTH_ROUTE)
                },
                onTakePhotoClicked = {
                    mobileVRCameraController.cameraCapture.takePhoto(
                        true,
                        CameraDataSingleton.getInstance().imageCapture!!,
                        context.contentResolver,
                        context,
                        bluetoothViewModel,
                        mainViewModel
                    )
                },
                onSettingsInfoClicked = {
                    CameraDataSingleton.getInstance().cameraProvider!!.unbindAll()
                    navController.navigate(SETTINGS_INFO_ROUTE)
                },
                onGalleryClicked = {
                    CameraDataSingleton.getInstance().cameraProvider!!.unbindAll()
                    navController.navigate(GALLERY_ROUTE)
                },
                previewView = CameraDataSingleton.getInstance().previewView,
                activeCameraInfo = CameraDataSingleton.getInstance().activeCameraInfo,
                activeCameraControl = CameraDataSingleton.getInstance().activeCameraControl,
                context = context,
                bluetoothState = state,
                mainViewModel = mainViewModel,
                bluetoothViewModel = bluetoothViewModel,
                rotation = rotation,
                exactRotation = exactRotation,
            )
        }
        composable(SETTINGS_INFO_ROUTE) {
            SettingsInfoScreen(
                onCloseButton = {
                    mobileVRCameraController.cameraSetup.setupCamera(
                        context,
                        mainActivityLifeCycleOwner
                    )
                    navController.navigate(CAMERA_ROUTE)
                },
                mainViewModel = mainViewModel,
                rotation = rotation,
                bluetoothViewModel = bluetoothViewModel,
            )
        }
        composable(GALLERY_ROUTE) {
            GalleryScreen(
                context = context,
                rotation = rotation,
                onCloseButton = {
                    mobileVRCameraController.cameraSetup.setupCamera(
                        context,
                        mainActivityLifeCycleOwner
                    )
                    navController.navigate(CAMERA_ROUTE)
                },
                mainViewModel = mainViewModel,
            )
        }
        composable(BLUETOOTH_ROUTE) {
            LaunchedEffect(key1 = state.isConnected) {
                state.errorMessage?.let { message ->
                    showToast(context.applicationContext, message, Toast.LENGTH_LONG)
                }
            }

            LaunchedEffect(key1 = state.errorMessage) {
                state.errorMessage?.let { message ->
                    showToast(context.applicationContext, message, Toast.LENGTH_LONG)
                }
            }

            LaunchedEffect(key1 = bluetoothViewModel.messageReceivedEvent) {
                bluetoothViewModel.messageReceivedEvent.collect { message ->
                    showToast(context.applicationContext, "Received: $message")
                }
            }

            Surface(
                color = MaterialTheme.colors.background
            ) {
                when {
                    state.isConnecting -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Text(text = "Connecting...")
                        }
                    }

                    state.isNewConnected -> {
                        if (!cameraSetUpState.value) {
                            mobileVRCameraController.cameraSetup.setupCamera(
                                context,
                                mainActivityLifeCycleOwner
                            )
                            cameraSetUpState.value = true
                            bluetoothViewModel.updateBluetoothUiState(BluetoothUiState(isConnected = true))
                            navController.navigate(CAMERA_ROUTE)
                        }
                    }

                    else -> {
                        BluetoothScreen(
                            state = state,
                            mainViewModel = mainViewModel,
                            rotation = rotation,
                            onDeviceClick = bluetoothViewModel::connectToDevice,
                            onDisconnectClick = bluetoothViewModel::disconnectFromDevice,
                            onCloseBluetoothEditor = {
                                mobileVRCameraController.cameraSetup.setupCamera(
                                    context,
                                    mainActivityLifeCycleOwner
                                )
                                navController.navigate(CAMERA_ROUTE)
                            },
                            bluetoothState = state
                        )
                    }
                }
            }
        }
    }
}