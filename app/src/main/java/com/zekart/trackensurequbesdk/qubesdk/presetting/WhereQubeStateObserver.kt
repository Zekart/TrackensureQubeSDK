package com.zekart.trackensurequbesdk.qubesdk.presetting

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.zekart.trackensurequbesdk.qubesdk.WQError
import com.zekart.trackensurequbesdk.qubesdk.WherequbeModel

interface WhereQubeStateObserver {
    /**
     * Receives the intent and routes the data to the appropriate method call,
     * including onConnection(), onDisconnected(), onDiscovered(), onError(),
     * onSynced() -- these must be overridden in a derived class
     * @param intent The Intent being received.
     */
    fun onReceive(intent: Intent) {
        val action = intent.action
        if (action == "com.geometris.WQ.ACTION_GATT_CONNECTED") {
            onConnected()
        } else if (action == "com.geometris.WQ.ACTION_GATT_DISCONNECTED") {
            onDisconnected()
        } else if (action == "com.geometris.WQ.ACTION_GATT_SERVICES_DISCOVERED") {
            onDiscovered()
        } else {
            val code: Int
            val cause: String?
            if (action == "com.geometris.WQ.DEVICE_DOES_NOT_SUPPORT_DATA") {
                code = intent.getIntExtra("errorCode", -8)
                cause = intent.getStringExtra("errorCause")
                onError(WQError(code, cause))
            } else if (action == "com.geometris.WQ.DEVICE_SYNC_OK") {
                onSynced()
            } else if (action == "com.geometris.WQ.DEVICE_SYNC_FAIL") {
                onError(WQError())
            }
        }
    }

    /**
     * Will be called when a WhereQube device is discovered on the Bluetooth radio.
     */
    fun onDiscovered()

    /**
     * Will be called when a WhereQube device connects.
     */
    fun onConnected()

    /**
     * Will be called when a WhereQube device synchronizes data.
     */
    fun onSynced()

    /**
     * Will be called when a WhereQube device disconnects.
     */
    fun onDisconnected()

    /**
     * Will be called when retrieval of data from a WhereQube device fails.
     * @param var1 Identifies the specific error.
     * @see WQError
     */
    fun onError(var1: WQError?)

    /**
     * Registers this broadcast receiver with the intents filter from WherequbeModel.
     * @param context The context in which the receiver is running.
     * @see WherequbeModel
     */
    fun register() {
        //LocalBroadcastManager.getInstance(context).registerReceiver(this, WherequbeModel.getWherequbeEventsIntentFilter());
    }

    /**
     * Unregisters this broadcast receiver.
     * @param context The context in which the receiver is running.
     */
    fun unregister() {
        //LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }
}