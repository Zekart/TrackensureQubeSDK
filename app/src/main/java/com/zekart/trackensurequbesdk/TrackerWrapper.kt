package com.zekart.trackensurequbesdk

import android.bluetooth.BluetoothDevice
import com.zekart.trackensurequbesdk.ble.BleTrackingManagerCallback
import java.util.ArrayDeque

interface TrackerWrapper: BleTrackingManagerCallback {
    fun onConnectionState(device: BluetoothDevice, state: State)
    fun onDeviceDisconnected(device: BluetoothDevice, reason: Int)
    fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int)

    fun onTrackerRawRead(data: ArrayDeque<ByteArray>)

    enum class State{
        Connecting, Connected, Ready, Failed, Disconnecting, Disconnected
    }
}