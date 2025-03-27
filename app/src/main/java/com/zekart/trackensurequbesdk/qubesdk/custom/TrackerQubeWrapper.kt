package com.zekart.trackensurequbesdk.qubesdk.custom

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.os.IBinder
import java.util.ArrayDeque

interface TrackerQubeWrapper {
    fun onServiceConnected(className: ComponentName, rawBinder: IBinder)
    fun onServiceDisconnected(className: ComponentName)
    fun onServiceConnectionError()
    fun onDeviceFailedToConnect(deviceAddress:String)
    fun onDeviceConnected(deviceAddress:String?)
    fun onTrackerRawRead(data: ArrayDeque<ByteArray>)

    enum class State{
        Connecting, Connected, Ready, Failed, Disconnecting, Disconnected
    }
}