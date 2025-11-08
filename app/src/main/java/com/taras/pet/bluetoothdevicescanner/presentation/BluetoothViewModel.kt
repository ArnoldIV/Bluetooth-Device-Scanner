package com.taras.pet.bluetoothdevicescanner.presentation

import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taras.pet.bluetoothdevicescanner.domain.usecase.BluetoothUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val useCases: BluetoothUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(BluetoothUiState())
    val state: StateFlow<BluetoothUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<UiEffect>()
    val effect = _effect.asSharedFlow()

    init {
        observeDevices()
        observeIsScanning()
        observeErrors()
    }

    // ----------- INTENTS -----------

    fun onIntent(intent: BluetoothIntent) {
        when (intent) {
            is BluetoothIntent.StartScan -> startScan()
            is BluetoothIntent.StopScan -> stopScan()
            is BluetoothIntent.ClearError -> clearError()
            is BluetoothIntent.DeviceClicked -> handleDeviceClick(intent.address)
            is BluetoothIntent.CheckBluetoothState -> checkBluetoothEnabled()
            is BluetoothIntent.RequestPermissions -> emitEffect(UiEffect.RequestPermissions)
        }
    }

    // ----------- REDUCER -----------

    private fun reduce(newState: BluetoothUiState.() -> BluetoothUiState) {
        _state.update { it.newState() }
    }

    // ----------- OBSERVERS -----------

    private fun observeDevices() {
        viewModelScope.launch {
            useCases.observeDevices().collect { devices ->
                reduce { copy(devices = devices) }
            }
        }
    }

    private fun observeIsScanning() {
        viewModelScope.launch {
            useCases.observeIsScanning().collect { isScanning ->
                reduce { copy(isScanning = isScanning) }
            }
        }
    }

    private fun observeErrors() {
        viewModelScope.launch {
            useCases.observeError().collect { message ->
                reduce { copy(errorMessage = message) }
                emitEffect(UiEffect.ShowError(message))
            }
        }
    }

    // ----------- ACTIONS -----------

    private fun startScan() {
        viewModelScope.launch {
            if (_state.value.isScanning) return@launch
            clearError()

            useCases.startScan()
        }
    }

    private fun stopScan() {
        viewModelScope.launch {
            useCases.stopScan()
        }
    }

    private fun clearError() {
        reduce { copy(errorMessage = null) }
    }

    private fun handleDeviceClick(address: String) {
        emitEffect(UiEffect.ShowError("Clicked device: $address"))
    }

    private fun checkBluetoothEnabled() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val isEnabled = adapter?.isEnabled == true

        reduce { copy(isBluetoothEnabled = isEnabled) }

        if (!isEnabled) {
            emitEffect(UiEffect.RequestBluetoothEnable)
        }
    }

    // ----------- HELPERS -----------

    private fun emitEffect(effect: UiEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}