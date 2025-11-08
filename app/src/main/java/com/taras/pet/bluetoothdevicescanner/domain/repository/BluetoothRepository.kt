package com.taras.pet.bluetoothdevicescanner.domain.repository

import com.taras.pet.bluetoothdevicescanner.domain.model.DiscoveredDevice
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothRepository {
    val devicesFlow: StateFlow<List<DiscoveredDevice>>
    val isScanningFlow: StateFlow<Boolean>
    val errorFlow: SharedFlow<String>

    suspend fun startScan()
    suspend fun stopScan()
}