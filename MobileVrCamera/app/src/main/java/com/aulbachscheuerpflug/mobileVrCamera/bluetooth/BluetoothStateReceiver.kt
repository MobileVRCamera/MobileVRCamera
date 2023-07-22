package com.aulbachscheuerpflug.mobileVrCamera.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aulbachscheuerpflug.mobileVrCamera.utils.parcelable

class BluetoothStateReceiver(
    private val onStateChanged: (isConnected: Boolean, device: BluetoothDevice) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val device: BluetoothDevice? = intent?.parcelable(BluetoothDevice.EXTRA_DEVICE)

        when (intent?.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                onStateChanged(true, device ?: return)
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                onStateChanged(false, device ?: return)
            }
        }
    }
}
