package com.taras.pet.bluetoothdevicescanner.domain.usecase

import javax.inject.Inject

data class BluetoothUseCases @Inject constructor(
    val startScan: StartScanUseCase,
    val stopScan: StopScanUseCase,
    val observeDevices: ObserveDevicesUseCase,
    val observeIsScanning: ObserveIsScanningUseCase,
    val observeError: ObserveErrorUseCase
)
