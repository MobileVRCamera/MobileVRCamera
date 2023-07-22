package com.aulbachscheuerpflug.mobileVrCamera.uiComponents

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.aulbachscheuerpflug.mobileVrCamera.MainViewModel
import com.aulbachscheuerpflug.mobileVrCamera.utils.playSystemSound

@Composable
fun PermissionUiHandler(
    mainViewModel: MainViewModel,
    activity: Activity,
) {
    val context = LocalContext.current
    val dialogQueue = mainViewModel.visiblePermissionDialogQueue
    val permissionsToRequest = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            permissionsToRequest.forEach { permission ->
                val isGranted = permissions[permission] == true
                mainViewModel.onPermissionResult(
                    permission = permission,
                    isGranted = isGranted,
                )
            }
            val isBluetoothScanGranted = permissions[Manifest.permission.BLUETOOTH_SCAN] == true
            val isBluetoothConnectGranted =
                permissions[Manifest.permission.BLUETOOTH_CONNECT] == true

            if (isBluetoothScanGranted || isBluetoothConnectGranted) {
                checkAndRequestBluetoothEnable(activity)
            }
        }
    )

    LaunchedEffect(Unit) {
        multiplePermissionResultLauncher.launch(permissionsToRequest)
    }

    dialogQueue
        .reversed()
        .forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    Manifest.permission.CAMERA -> {
                        CameraPermissionTextProvider()
                    }

                    Manifest.permission.RECORD_AUDIO -> {
                        AudioPermissionTextProvider()
                    }

                    Manifest.permission.BLUETOOTH_ADVERTISE -> {
                        BluetoothPermissionTextProvider()
                    }

                    Manifest.permission.BLUETOOTH_CONNECT -> {
                        BluetoothPermissionTextProvider()
                    }

                    Manifest.permission.BLUETOOTH_ADMIN -> {
                        BluetoothPermissionTextProvider()
                    }

                    Manifest.permission.BLUETOOTH_SCAN -> {
                        BluetoothPermissionTextProvider()
                    }

                    else -> return@forEach
                },
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                    activity,
                    permission
                ),
                onDismiss = mainViewModel::dismissDialog,
                onOkClick = {
                    playSystemSound(context)
                    mainViewModel.dismissDialog()
                    multiplePermissionResultLauncher.launch(
                        arrayOf(permission)
                    )
                },
                onGoToAppSettingsClick = activity::openAppSettings
            )
        }
}

fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}

private fun checkAndRequestBluetoothEnable(activity: Activity) {
    if (ContextCompat.checkSelfPermission(
            activity, Manifest.permission.BLUETOOTH_ADMIN
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val bluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivity(enableBluetoothIntent)
        }
    }
}

