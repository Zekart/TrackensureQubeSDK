package com.zekart.trackensurequbesdk.ble.base

/**
 * Interface definition for a callback to be invoked when bluetooth state changed.
 */
interface BluetoothCallback {
    /**
     * Called when the bluetooth is off.
     */
    fun onBluetoothOff()

    /**
     * Called when the bluetooth is turning on.
     */
    fun onBluetoothTurningOn()

    /**
     * Called when the bluetooth is on, and ready for use.
     */
    fun onBluetoothOn()

    /**
     * Called when the bluetooth is turning off.
     */
    fun onBluetoothTurningOff()

    /**
     * This stub class provides empty implementations of the methods.
     */
    class Stub : BluetoothCallback {
        override fun onBluetoothOff() {}
        override fun onBluetoothTurningOn() {}
        override fun onBluetoothOn() {}
        override fun onBluetoothTurningOff() {}
    }
}