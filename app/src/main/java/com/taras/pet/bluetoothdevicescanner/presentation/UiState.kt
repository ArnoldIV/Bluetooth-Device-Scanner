package com.taras.pet.bluetoothdevicescanner.presentation

import com.taras.pet.bluetoothdevicescanner.domain.model.DiscoveredDevice

data class BluetoothUiState(
    val isBluetoothEnabled: Boolean = false,
    val isScanning: Boolean = false,
    val devices: List<DiscoveredDevice> = emptyList(),
    val errorMessage: String? = null
)