package com.taras.pet.bluetoothdevicescanner.domain.model

data class DiscoveredDevice(
    val name: String,
    val address:String,
    val rssi: Int?,
    val isConnectable: Boolean,
    val bondState:Int,
)