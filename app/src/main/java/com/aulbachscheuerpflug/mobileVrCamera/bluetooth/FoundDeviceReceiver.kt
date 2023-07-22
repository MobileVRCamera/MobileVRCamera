package com.aulbachscheuerpflug.mobileVrCamera.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aulbachscheuerpflug.mobileVrCamera.utils.parcelable

class FoundDeviceReceiver(
    private val onDeviceFound: (BluetoothDevice) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device: BluetoothDevice? = intent.parcelable(BluetoothDevice.EXTRA_DEVICE)
                device?.let(onDeviceFound)
            }
        }
    }
}
