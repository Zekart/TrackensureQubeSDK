package com.zekart.trackensurequbesdk.qubesdk.custom

import com.zekart.trackensurequbesdk.qubesdk.GeoData

interface QubeManagerSDKWrapper {
    fun onServiceStarted()
    fun onServiceStopped()
    fun onDeviceSuccessConnected(device:String)
    fun onDeviceErrorConnection(device:String, message:String)
    fun onError(message:String)
    fun onDataReceive(data:GeoData)
}