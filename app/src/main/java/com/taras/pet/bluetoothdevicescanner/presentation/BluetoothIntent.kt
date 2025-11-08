package com.taras.pet.bluetoothdevicescanner.presentation

sealed class BluetoothIntent {
    data object CheckBluetoothState : BluetoothIntent()
    data object RequestPermissions : BluetoothIntent()
    data object StartScan : BluetoothIntent()
    data object StopScan : BluetoothIntent()
    data class DeviceClicked(val address: String) : BluetoothIntent()
    data object ClearError : BluetoothIntent()
}