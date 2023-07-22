package com.aulbachscheuerpflug.mobileVrCamera.uiComponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aulbachscheuerpflug.mobileVrCamera.BLUETOOTH_DECLINED_DESC
import com.aulbachscheuerpflug.mobileVrCamera.CAMERA_DECLINED_DESC
import com.aulbachscheuerpflug.mobileVrCamera.GRANTED_PERMISSION
import com.aulbachscheuerpflug.mobileVrCamera.GRANT_PERMISSION
import com.aulbachscheuerpflug.mobileVrCamera.MICROPHONE_DECLINED_DESC
import com.aulbachscheuerpflug.mobileVrCamera.PERMANENTLY_DECLINED_DESC_1
import com.aulbachscheuerpflug.mobileVrCamera.PERMANENTLY_DECLINED_DESC_2
import com.aulbachscheuerpflug.mobileVrCamera.PERMISSION_DIALOG_HEADER
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Black
import com.aulbachscheuerpflug.mobileVrCamera.utils.playSystemSound

// General structure idea: https://github.com/philipplackner/PermissionsGuideCompose
@Composable
fun PermissionDialog(
    permissionTextProvider: PermissionTextProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    AlertDialog(onDismissRequest = onDismiss, buttons = {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isPermanentlyDeclined) {
                        GRANT_PERMISSION
                    } else {
                        GRANTED_PERMISSION
                    },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            playSystemSound(context)
                            if (isPermanentlyDeclined) {
                                onGoToAppSettingsClick()
                            } else {
                                onOkClick()
                            }
                        }
                        .padding(16.dp)
                )
                IconButton(onClick = {
                    playSystemSound(context)
                    onDismiss()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Black,
                    )
                }
            }
        }
    }, title = {
        Text(text = PERMISSION_DIALOG_HEADER)
    }, text = {
        Text(
            text = permissionTextProvider.getDescription(isPermanentlyDeclined)
        )
    }, modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun PermissionDialogPreview() {
    PermissionDialog(permissionTextProvider = CameraPermissionTextProvider(),
        isPermanentlyDeclined = true,
        onDismiss = {},
        onOkClick = {},
        onGoToAppSettingsClick = {})
}

interface PermissionTextProvider {
    fun getDescription(isPermanentlyDeclined: Boolean): String
}

fun permanentlyDeclinedDescription(permissionName: String): String {
    return "$PERMANENTLY_DECLINED_DESC_1$permissionName$PERMANENTLY_DECLINED_DESC_2"
}

class CameraPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            permanentlyDeclinedDescription("camera")
        } else {
            CAMERA_DECLINED_DESC
        }
    }
}

class AudioPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            permanentlyDeclinedDescription("microphone")
        } else {
            MICROPHONE_DECLINED_DESC
        }
    }
}

class BluetoothPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            permanentlyDeclinedDescription("Bluetooth")
        } else {
            BLUETOOTH_DECLINED_DESC
        }
    }
}