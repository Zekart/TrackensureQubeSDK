package com.zekart.trackensurequbesdk.ble.base

import android.bluetooth.BluetoothDevice
import android.content.Context

abstract class BleProfile(base: Context?) : BleScanner(base), BluetoothCallback {
    private val mProfileProxy: BluetoothProfileProxy?

    /**
     * Called when the service has been started. The device name and address are set.
     * The BLE Manager will try to connect to the device after this method finishes.
     */
    override fun onServiceStarted() {
        // empty default implementation
    }

    init {
        mProfileProxy = BluetoothProfileProxy(applicationContext, this)
        mProfileProxy.register(true)
        mProfileProxy.followBluetooth()
        if (isBluetoothDeviceEnabled) onBluetoothEnabled()
    }

    override fun onDestroy() {
        mProfileProxy?.unregister()
        super.onDestroy()
    }

    fun registerBluetoothProxy() {
        if (!mProfileProxy!!.isRegistered) {
            mProfileProxy.unfollowBluetooth()
            mProfileProxy.setBluetoothCallback(this)
            mProfileProxy.register(true)
        }
    }

    override fun onBluetoothOff() {
        onBluetoothDisabled()
    }

    override fun onBluetoothTurningOn() {}
    override fun onBluetoothOn() {
        onBluetoothEnabled()
    }

    override fun onBluetoothTurningOff() {}
    val isBluetoothDeviceEnabled: Boolean
        get() = mProfileProxy != null && mProfileProxy.isBluetoothDeviceEnabled

    // Обработка состояний подключения трекера по Bluetooth
    override fun onDeviceConnecting(device: BluetoothDevice) {}
    override fun onDeviceConnected(device: BluetoothDevice) {
//      TrackerVal.getInstance().isTrackerLostLink = false;
//      TrackerVal.getInstance().isConnected = isConnected();
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {}

    //Use in Tracker
    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        // Note 1: Do not use the device argument here unless you change calling onDeviceDisconnected from the binder above
        // Note 2: if BleManager#shouldAutoConnect() for this device returned true, this callback will be invoked ONLY when user requested disconnection (using Disconnect button).
//      TrackerVal.getInstance().isTrackerLostLink = (reason == ConnectionObserver.REASON_LINK_LOSS);
//      TrackerVal.getInstance().isConnected = isConnected();
    }

    //Use in Tracker
    override fun onDeviceReady(device: BluetoothDevice) {
//      TrackerVal.getInstance().isConnected = isConnected();
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
//      TrackerVal.getInstance().isConnected = isConnected();
    }

    companion object {
        const val STATE_DISCONNECTED = 0
    }
}