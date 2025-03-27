package com.zekart.trackensurequbesdk.qubesdk.custom

import android.content.Context
import com.zekart.trackensurequbesdk.qubesdk.GeoData
import com.zekart.trackensurequbesdk.qubesdk.WQError

class QubeSDKManager(private val context: Context?, private val managerSDKWrapper: QubeManagerSDKWrapper):AbstractQubeSDKImpl() {

    init {
        context?.let {
            registerBroadcast(context)
            initService(context)
        }
    }

    override fun closeService() {
        context?.let {
            stopService(context)
        }
    }

    override fun connectDevice(address:String?) {
        if (!address.isNullOrEmpty()){
            deviceToConnect(address)
        }
    }

    override fun disconnectDevice() {
        deviceDisconnect()
    }

    override fun successConnectedDevice() {
        managerSDKWrapper.onDeviceSuccessConnected(getDeviceConnectedAddress())
        setDataHandler()
    }

    override fun successDisconnectDevice() {
        TODO("Not yet implemented")
    }

    override fun onGetData(data:GeoData) {
        managerSDKWrapper.onDataReceive(data)
    }

    override fun onDeviceConnectionError(error: WQError?) {
        if (error!=null){
            managerSDKWrapper.onError(error.mCause)
        }else{
            managerSDKWrapper.onError("Unknown error")
        }

    }

    override fun onServiceSuccessConnected() {
        managerSDKWrapper.onServiceStarted()
    }

    override fun onServiceErrorConnected(message: String) {
        managerSDKWrapper.onServiceStopped()
    }
}