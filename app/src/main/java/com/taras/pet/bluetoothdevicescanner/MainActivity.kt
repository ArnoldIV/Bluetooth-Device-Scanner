package com.taras.pet.bluetoothdevicescanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.taras.pet.bluetoothdevicescanner.presentation.BluetoothScreen
import com.taras.pet.bluetoothdevicescanner.presentation.ui.theme.BluetoothDeviceScannerTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BluetoothDeviceScannerTheme {
                BluetoothScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BluetoothDeviceScannerTheme {
        BluetoothScreen()
    }
}