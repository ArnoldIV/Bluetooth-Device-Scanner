package com.taras.pet.bluetoothdevicescanner.presentation

sealed class UiEffect {
    data class ShowError(val message: String) : UiEffect()
    data object RequestBluetoothEnable : UiEffect()
    data object RequestPermissions : UiEffect()
}