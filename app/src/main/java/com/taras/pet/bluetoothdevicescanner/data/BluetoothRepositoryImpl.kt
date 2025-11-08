package com.taras.pet.bluetoothdevicescanner.data

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.taras.pet.bluetoothdevicescanner.domain.model.DiscoveredDevice
import com.taras.pet.bluetoothdevicescanner.domain.repository.BluetoothRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
) : BluetoothRepository {

    private val _devices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    override val devicesFlow: StateFlow<List<DiscoveredDevice>> = _devices

    private val _isScanning = MutableStateFlow(false)
    override val isScanningFlow: StateFlow<Boolean> = _isScanning

    private val _errorFlow = MutableSharedFlow<String>()
    override val errorFlow: SharedFlow<String> = _errorFlow

    private val receiver = BluetoothReceiver(
        context = context,
        onDeviceFound = { device, rssi -> handleDeviceFound(device, rssi) },
        onDiscoveryFinished = { handleDiscoveryFinished() },
        onError = { message -> _errorFlow.tryEmit(message) }
    )

    private fun handleDeviceFound(device: BluetoothDevice, rssi: Int) {
        val hasConnectPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                // On Android ≤ 11 this permission does not exist, so we assume that it is true
                true
            }

        if (!hasConnectPermission) {
            _errorFlow.tryEmit("Missing BLUETOOTH_CONNECT permission")
            return
        }

        try {
            val discovered = DiscoveredDevice(
                name = device.name ?: "Unknown",
                address = device.address,
                rssi = rssi,
                isConnectable = false,
                bondState = device.bondState
            )
            if (_devices.value.none { it.address == discovered.address }) {
                _devices.value = _devices.value + discovered
            }
        } catch (e: SecurityException) {
            _errorFlow.tryEmit("No permission to access device info")
        }
    }

    private fun handleDiscoveryFinished() {
        _isScanning.value = false
    }

    override suspend fun startScan() {
        // 1. Bluetooth is off → we do nothing
        if (!bluetoothAdapter.isEnabled) {
            _errorFlow.emit("Bluetooth is disabled")
            return
        }

        // 2. Permission checking
        val hasScanPermission: Boolean
        val hasConnectPermission: Boolean

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val scanPermission = Manifest.permission.BLUETOOTH_SCAN
            val connectPermission = Manifest.permission.BLUETOOTH_CONNECT

            hasScanPermission = ContextCompat.checkSelfPermission(
                context, scanPermission
            ) == PackageManager.PERMISSION_GRANTED

            hasConnectPermission = ContextCompat.checkSelfPermission(
                context, connectPermission
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            hasScanPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            hasConnectPermission = true
        }

        if (!hasScanPermission || !hasConnectPermission) {
            _errorFlow.emit("Missing Bluetooth permissions")
            return
        }

        _isScanning.value = true
        _devices.value = emptyList()
        receiver.register()
        bluetoothAdapter.startDiscovery()
    }

    override suspend fun stopScan() {
        // 1. Safely cancel discovery
        try {
            // BluetoothAdapter.cancelDiscovery() requires BLUETOOTH_SCAN starting with API 31
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val hasScanPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED

                if (hasScanPermission) {
                    bluetoothAdapter.cancelDiscovery()
                } else {
                    _errorFlow.emit("Missing BLUETOOTH_SCAN permission")
                }
            } else {
                bluetoothAdapter.cancelDiscovery()
            }
        } catch (e: SecurityException) {
            _errorFlow.emit("Permission denied to stop discovery")
        } catch (e: Exception) {
            _errorFlow.emit("Failed to stop discovery: ${e.message}")
        }

        // 2. Secure receiver unsubscription
        try {
            context.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            // Receiver is already unsubscribed - ignore
        }

        // 3. Completing the scan
        _isScanning.value = false
    }

}