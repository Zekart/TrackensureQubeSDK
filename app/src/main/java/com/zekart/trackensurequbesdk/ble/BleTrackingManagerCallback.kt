package com.zekart.trackensurequbesdk.ble

import com.zekart.trackensurequbesdk.sdk.frame.DeviceInfo
import com.zekart.trackensurequbesdk.sdk.frame.EventParam

interface BleTrackingManagerCallback {
    fun onTrackerConnect()
    fun onTrackerConnectError(e: Exception)
    fun onTrackerDeviceInfoRead(info: DeviceInfo?)
    fun onTrackerEventRead(event: EventParam?)
    fun onTrackerRawRead(data: ByteArray)
    fun onFileUpdateProgress(progress: Int)
    fun onTrackerIoError(e: Exception)
}