package com.taras.pet.bluetoothdevicescanner.data

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class BluetoothReceiver(
    private val context: Context,
    private val onDeviceFound: (BluetoothDevice, Int) -> Unit,
    private val onDiscoveryFinished: () -> Unit,
    private val onError: (String) -> Unit
) : BroadcastReceiver() {

    fun register() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(this, filter)
    }

    fun unregister() {
        try {
            context.unregisterReceiver(this)
        } catch (e: IllegalArgumentException) {
            // Receiver already unregistered
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {

            BluetoothDevice.ACTION_FOUND -> {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val rssi =
                    intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)

                device?.let {
                    onDeviceFound(device, rssi.toInt())
                }
            }

            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                onDiscoveryFinished()
                unregister() // automatically unregister
            }
        }
    }
}