package com.zekart.trackensurequbesdk.qubesdk.custom

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.zekart.trackensurequbesdk.qubesdk.AbstractWherequbeStateObserver
import com.zekart.trackensurequbesdk.qubesdk.BaseRequest
import com.zekart.trackensurequbesdk.qubesdk.BaseResponse
import com.zekart.trackensurequbesdk.qubesdk.CustomWrapperQubeSDK
import com.zekart.trackensurequbesdk.qubesdk.GeoData
import com.zekart.trackensurequbesdk.qubesdk.MsgHandlerThread
import com.zekart.trackensurequbesdk.qubesdk.RequestHandler
import com.zekart.trackensurequbesdk.qubesdk.ResponseHandler
import com.zekart.trackensurequbesdk.qubesdk.WQError
import com.zekart.trackensurequbesdk.qubesdk.WQSmartService
import com.zekart.trackensurequbesdk.qubesdk.WherequbeService

abstract class AbstractQubeSDKImpl : WherequbeService(){

    private val wrapperSDKModel = CustomWrapperQubeSDK()

    abstract fun closeService()
    abstract fun connectDevice(address:String?)
    abstract fun disconnectDevice()
    abstract fun successConnectedDevice()
    abstract fun successDisconnectDevice()
    abstract fun onGetData(data:GeoData)
    abstract fun onDeviceConnectionError(error: WQError?)
    abstract fun onServiceSuccessConnected()
    abstract fun onServiceErrorConnected(message:String)

    protected fun registerBroadcast(context:Context){
        this.mBroadcastReceiverState.register(context)
    }

    private val mBroadcastReceiverState = object :AbstractWherequbeStateObserver(){
        override fun onDiscovered() {
            println()
        }

        override fun onConnected() {
            successConnectedDevice()
        }

        override fun onSynced() {
            println()
        }

        override fun onDisconnected() {
            successDisconnectDevice()
        }

        override fun onError(var1: WQError?) {
            onError(var1)
        }
    }

    /**
     * Handler to get main data, like position etc.
     * Get all OBD parameters - BaseRequest.OBD_MEASUREMENT
     * Provide app id - .WRITE_APP_IDENTIFIER
     * Start getting unidentified driving events - .REQUEST_START_UDEVENTS
     * Stop getting unidentified driving events - .REQUEST_STOP_UDEVENTS
     * Purge saved unidentified driving events - .PURGE_UDEVENTS
     * Get device address - .REQUEST_DEVICE_ADDRESS
     */
    private val mDataHandler = RequestHandler { var1, data ->
        if(data.requestId == BaseRequest.OBD_MEASUREMENT){
            data.`object`?.let {
                val geo = it as GeoData
                if (it.isDataSet) onGetData (geo)
            }
        }
    }

    /**
     * Handler to get response data, param like RequestHandler.
     */
    private val mResponseHandler = object :ResponseHandler{
        override fun onRecv(ctx: Context, data: BaseResponse) {
            data.let {
                when(it.requestId){
                    BaseRequest.OBD_MEASUREMENT->{ }
                    BaseRequest.WRITE_APP_IDENTIFIER->{ }
                    BaseRequest.REQUEST_START_UDEVENTS->{ }
                    BaseRequest.REQUEST_STOP_UDEVENTS->{ }
                    BaseRequest.PURGE_UDEVENTS->{ }
                    BaseRequest.REQUEST_DEVICE_ADDRESS->{ }
                    else -> {}
                }
            }
        }

        override fun onError(var1: Context) {

        }

    }
    private var mServiceConnectionState: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, rawBinder: IBinder) {
            mService = (rawBinder as WQSmartService.LocalBinder).service
            //Log.d(WherequbeService.TAG, "TS: onServiceConnected mService= $mService")
            if (!mService.initialize(this@AbstractQubeSDKImpl)) {
                //Log.e(WherequbeService.TAG, "TS: Unable to initialize Bluetooth")
                onServiceErrorConnected("TS: Unable to initialize Bluetooth")
            }else onServiceSuccessConnected()

        }

        override fun onServiceDisconnected(classname: ComponentName) {
            //Log.e(WherequbeService.TAG, "TS: onServiceDisconnected; Cleanup not performed")
            onServiceErrorConnected("TS: onServiceDisconnected; Cleanup not performed")
        }
    }

    /**
     * Registers the service with the application context,
     * initializes all data and starts the message processing thread.
     */
    protected fun initService(context:Context?){
        context?.let {
            val bindIntent = Intent(
                it.applicationContext,
                WQSmartService::class.java
            )
            it.applicationContext?.bindService(bindIntent, mServiceConnectionState, Context.BIND_AUTO_CREATE)

            mModel = wrapperSDKModel.qubeModelIntents
            mMHT = MsgHandlerThread(it.applicationContext, this)

            it.applicationContext?.startService(bindIntent)
            Log.d(TAG, "WherequbeService: initialize")
        }
    }


    /**
     * Destroys / Cleans up the WherequbeService object
     * @param context Context in which the service runs.
     */
    protected fun stopService(context: Context){
        disconnect()
        this.destroy(context)
        this.mBroadcastReceiverState.unregister(context)
        setReqHandler(BaseRequest.OBD_MEASUREMENT, null)
    }

    /**
     * Connects to the Whereqube at the given address.
     * @param address address of the whereqube to connect to
     * @return true if the connection suceeds
     * @throws IllegalArgumentException if the service was not properly initialized.
     */
    protected fun deviceToConnect(address:String?){
        if (mService == null) {
            //throw IllegalStateException("Service is not initialized")
        } else if (address.isNullOrEmpty()) {
            //throw IllegalArgumentException("Invalid Address")
        } else {
            super.mService.connect(address)
        }
    }

    protected fun deviceDisconnect(){
        if (mService!=null){
            mService.disconnect()
        }
    }

    protected fun setDataHandler(){
        setReqHandler(BaseRequest.OBD_MEASUREMENT, mDataHandler)
    }

    protected fun sendRequest(request:BaseRequest){
        sendRequest(request, mResponseHandler,5000)
    }

    protected fun getDeviceConnectedAddress(): String = mService.mGattClient.device.address
}