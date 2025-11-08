package com.taras.pet.bluetoothdevicescanner.domain.usecase

import com.taras.pet.bluetoothdevicescanner.domain.repository.BluetoothRepository
import javax.inject.Inject

class StopScanUseCase @Inject constructor(
    private val repository: BluetoothRepository
) {
    suspend operator fun invoke() {
        repository.stopScan()
    }
}