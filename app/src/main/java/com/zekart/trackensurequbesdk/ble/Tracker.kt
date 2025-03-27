package com.zekart.trackensurequbesdk.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.zekart.trackensurequbesdk.TrackerWrapper
import com.zekart.trackensurequbesdk.ble.base.BleProfile
import com.zekart.trackensurequbesdk.ble.base.BleTrackingManager
import com.zekart.trackensurequbesdk.sdk.frame.DeviceInfo
import com.zekart.trackensurequbesdk.sdk.frame.EventParam

class Tracker(base: Context, private val listener: TrackerWrapper?) : BleProfile(base),
    BleTrackingManagerCallback {

    companion object {
        private const val TAG = "Tracker"
    }

    private val manager: BleTrackingManager by lazy {
        BleTrackingManager(context = applicationContext, this)
    }

    override fun updateManager(): BleTrackingManager {
        return manager
    }

    fun connect(device: BluetoothDevice) {
        initializeManager()
        val bluetooth = Bundle().apply {
            putString(EXTRA_DEVICE_ADDRESS, device.address)
        }
        connect(bluetooth)
    }

    //временное решение для общения с устройством
    fun write(data: ByteArray) {
        manager.request(data)
    }


    override fun onServiceStarted() {
        Log.i(TAG, "--------- BLUETOOTH SERVICE CONNECTION STARTED")
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        Log.i(TAG, "Device - CONNECTING")
        listener?.onConnectionState(device, TrackerWrapper.State.Connecting)
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        Log.i(TAG, "Device - CONNECTED COMPLETE")
        listener?.onConnectionState(device, TrackerWrapper.State.Connected)
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        Log.i(TAG, "Device - FAILED CONNECT")
        listener?.onDeviceFailedToConnect(device, reason)
        listener?.onConnectionState(device, TrackerWrapper.State.Failed)
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        Log.i(TAG, "Device - READY")
        listener?.onConnectionState(device, TrackerWrapper.State.Ready)
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        Log.i(TAG, "Device - DISCONNECTING")
        listener?.onConnectionState(device, TrackerWrapper.State.Disconnecting)
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        //BleProfile --> onDeviceDisconnected
        Log.i(TAG, "Device - DISCONNECTED COMPLETE")
        listener?.onDeviceDisconnected(device, reason)
        listener?.onConnectionState(device, TrackerWrapper.State.Disconnected)
    }

    override fun onTrackerConnect() {
        listener?.onTrackerConnect()
    }

    override fun onTrackerConnectError(e: Exception) {
        listener?.onTrackerConnectError(e)
    }

    override fun onTrackerDeviceInfoRead(info: DeviceInfo?) {
        info?.apply {
            listener?.onTrackerDeviceInfoRead(this)
            manager.request(DeviceInfo.response(2, 10))
        }
    }

    override fun onTrackerEventRead(event: EventParam?) {
        event?.apply {
            listener?.onTrackerEventRead(this)
            manager.request(EventParam.response())
        }
    }

    override fun onTrackerRawRead(data: ByteArray) {
        listener?.onTrackerRawRead(data)
    }

    override fun onFileUpdateProgress(progress: Int) {
        //TODO("Not yet implemented")
    }

    override fun onTrackerIoError(e: Exception) {
        listener?.onTrackerIoError(e)
    }
}