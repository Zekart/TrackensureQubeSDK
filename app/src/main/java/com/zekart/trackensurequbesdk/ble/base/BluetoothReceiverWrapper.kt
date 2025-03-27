package com.zekart.trackensurequbesdk.ble.base

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

internal class BluetoothReceiverWrapper(private val mContext: Context) {
    private var mCallback: BluetoothCallback? = null
    private var mReceiver: BluetoothReceiver? = null

    /**
     * Register this bluetooth listener to the context with a callback.
     */
    fun register(callback: BluetoothCallback?) {
        mCallback = callback
        if (mReceiver == null) {
            mReceiver = BluetoothReceiver()
            val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            mContext.registerReceiver(mReceiver, filter)
        }
    }

    /**
     * Unregister this bluetooth listener from the context.
     */
    fun unregister() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver)
            mReceiver = null
        }
    }

    /**
     * Receives the [android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED].
     */
    private inner class BluetoothReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.ERROR
            )
            when (state) {
                BluetoothAdapter.STATE_OFF -> mCallback!!.onBluetoothOff()
                BluetoothAdapter.STATE_TURNING_ON -> mCallback!!.onBluetoothTurningOn()
                BluetoothAdapter.STATE_ON -> mCallback!!.onBluetoothOn()
                BluetoothAdapter.STATE_TURNING_OFF -> mCallback!!.onBluetoothTurningOff()
            }
        }
    }
}