package com.taras.pet.bluetoothdevicescanner.data.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.taras.pet.bluetoothdevicescanner.data.BluetoothRepositoryImpl
import com.taras.pet.bluetoothdevicescanner.domain.repository.BluetoothRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BluetoothModule {

    @Binds
    @Singleton
    abstract fun bindBluetoothRepository(
        impl: BluetoothRepositoryImpl
    ): BluetoothRepository

    companion object {

        // provide BluetoothAdapter
        @Provides
        @Singleton
        fun provideBluetoothAdapter(
            @ApplicationContext context: Context
        ): BluetoothAdapter {
            val bluetoothManager =
                context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            return bluetoothManager.adapter
        }
    }

}