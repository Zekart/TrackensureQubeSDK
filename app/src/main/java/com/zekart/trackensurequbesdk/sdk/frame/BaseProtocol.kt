package com.zekart.trackensurequbesdk.sdk.frame

import com.zekart.trackensurequbesdk.sdk.types.Command
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class BaseProtocol(protected var type:Int = Command.UNKNOWN, var time:Long = 0L) {
    fun isEmpty(): Boolean = (type == Command.UNKNOWN)

    companion object {
        fun reset(): ByteArray = Json.encodeToString(
            Response.serializer(), Response(type = Command.RESET, resetConnection = true)
        ).encodeToByteArray()

        @Serializable
        private data class Response(
            val type: Int,
            val resetConnection: Boolean
        )
    }

    fun getTime(pattern:String = "yyyy-MM-dd HH:mm:ss"):String?{
        return if(type!= Command.UNKNOWN && time>0) {
            SimpleDateFormat(pattern, Locale.getDefault()).format(Date(time))
        } else null
    }
}