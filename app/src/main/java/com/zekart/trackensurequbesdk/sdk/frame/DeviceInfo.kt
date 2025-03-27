package com.zekart.trackensurequbesdk.sdk.frame

import com.zekart.trackensurequbesdk.sdk.types.Command
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeviceInfo(info: BaseParse.BaseInfo? = null, val rawValue : String = "") : BaseProtocol() {
    var countOfStoredMessages: Int = 0 //Number of accumulated important messages before connection starts
    var protocolVersion: String = ""   //Version of the implementation of the communication protocol between devices
    var hardwareVersion: String = ""   //Current _Device_ hardware version
    var firmwareVersion: String = ""    //Current _Device_ firmware version

    init {
        info?.let {
            this.type = it.type
            this.time= it.time
            this.countOfStoredMessages = it.countOfStoredMessages
            this.protocolVersion = it.protocolVersion
            this.hardwareVersion = it.HWVersion
            this.firmwareVersion = it.FWVersion
        }
    }

    override fun toString(): String {
        return if(isEmpty()) "{error:No available information}"
        else  "{" +
                "type:$type, " +
                "time:$time, " +
                "countOfStoredMessages:$countOfStoredMessages, " +
                "protocolVersion:$protocolVersion, " +
                "hardwareVersion:$hardwareVersion, " +
                "firmwareVersion:$firmwareVersion}"
    }

    companion object {
        fun response(reportEach: Int, minTruckSpeed: Int, acc : Double = 0.0, brk: Double = 0.0, cor: Double = 0.0): ByteArray = Json.encodeToString(
            Response.serializer(),
            Response(
                type = Command.INFO_ACCEPT,
                reportEach = reportEach,
                minTruckSpeed = minTruckSpeed,
                acceleration = acc,
                brake =  brk,
                cornering = cor
            )
        ).encodeToByteArray()

        @Serializable
        private data class Response(
            val type: Int,
            val reportEach: Int,
            val minTruckSpeed: Int,
            val acceleration : Double,
            val brake:Double,
            val cornering:Double
        )
    }
}


