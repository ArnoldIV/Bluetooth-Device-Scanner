package com.taras.pet.bluetoothdevicescanner.domain.usecase

import com.taras.pet.bluetoothdevicescanner.domain.repository.BluetoothRepository
import javax.inject.Inject

class ObserveIsScanningUseCase @Inject constructor(
    private val repository: BluetoothRepository
) {
    operator fun invoke() = repository.isScanningFlow
}