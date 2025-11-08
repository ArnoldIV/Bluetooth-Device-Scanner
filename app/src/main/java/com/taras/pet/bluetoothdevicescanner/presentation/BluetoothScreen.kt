package com.taras.pet.bluetoothdevicescanner.presentation

import android.Manifest
import android.R.attr.onClick
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.taras.pet.bluetoothdevicescanner.domain.model.DiscoveredDevice

@Composable
fun BluetoothScreen(
    viewModel: BluetoothViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            Toast.makeText(context, "Bluetooth permissions are required", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.onIntent(BluetoothIntent.StartScan)
        }
    }

    // listening to one-off effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UiEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                UiEffect.RequestPermissions -> {
                    // opens a system dialog
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                            )
                        )
                    } else {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                        )
                    }
                }

                UiEffect.RequestBluetoothEnable -> {
                    Toast.makeText(context, "Please enable Bluetooth", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    BluetoothContent(
        state = state,
        onStartScan = { viewModel.onIntent(BluetoothIntent.RequestPermissions) }, // 👈 міняємо
        onStopScan = { viewModel.onIntent(BluetoothIntent.StopScan) },
        onDeviceClick = { address -> viewModel.onIntent(BluetoothIntent.DeviceClicked(address)) }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothContent(
    state: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bluetooth Scanner") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // --- Scan / Stop buttons ---
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = onStartScan,
                    enabled = !state.isScanning
                ) {
                    Text("Start Scan")
                }

                Button(
                    onClick = onStopScan,
                    enabled = state.isScanning
                ) {
                    Text("Stop Scan")
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Status ---
            if (state.isScanning) {
                Text("Scanning devices...", color = Color.Gray)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (state.errorMessage != null) {
                Text(
                    text = "Error: ${state.errorMessage}",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // --- List of devices ---
            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(state.devices) { device ->
                    DeviceItem(device = device, onClick = onDeviceClick)
                }
            }

            if (state.devices.isEmpty() && !state.isScanning) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No devices found")
                }
            }
        }
    }
}

@Composable
fun DeviceItem(device: DiscoveredDevice, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(device.address) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = device.name ?: "Unknown device", fontWeight = FontWeight.Bold)
            Text(text = "Address: ${device.address}", style = MaterialTheme.typography.bodySmall)
            Text(text = "RSSI: ${device.rssi} dBm", style = MaterialTheme.typography.bodySmall)
            Text(
                text = when (device.bondState) {
                    BluetoothDevice.BOND_BONDED -> "Paired"
                    BluetoothDevice.BOND_BONDING -> "Pairing..."
                    else -> "Not paired"
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview(){
    BluetoothContent(
        BluetoothUiState(isScanning = false, devices = emptyList()),
        onStartScan = {},
        onStopScan = {},
        onDeviceClick = {}
    )
}