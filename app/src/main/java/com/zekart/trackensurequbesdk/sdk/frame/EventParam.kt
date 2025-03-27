package com.zekart.trackensurequbesdk.sdk.frame

import com.zekart.trackensurequbesdk.sdk.ext.toEnum
import com.zekart.trackensurequbesdk.sdk.types.Command
import com.zekart.trackensurequbesdk.sdk.types.EventType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


class EventParam(frame: BaseParse.BaseFrame? = null, var rawValue : String = ""): BaseProtocol() {
//    private var type: Int = Command.UNKNOWN
//    var time: Long = 0L                  //Epoch event generation time in GMT
    var event: EventType = EventType.EV_UNKNOWN            //One of the possible [event types](###event-types)
    var stored: Boolean = false             //Indicates whether the event was generated before the _Mobile_ connected
    var vin: String = ""            //Vehicle Identification Number
    var position: Position = Position()
    var telemetry: Telemetry = Telemetry()
    var spn: Spn = Spn()

    fun hasVin():Boolean = vin.isNotEmpty()

    fun frameProtocol():String = spn.protocolType.name

    fun frameType():String = event.name.uppercase()

    init {
        frame?.let {
            this.type = it.type
            this.time = it.time
            this.event = it.event.toEnum<EventType>() ?: EventType.EV_UNKNOWN
            this.stored = it.stored
            this.vin = it.vin
            this.position = Position(it.position)
            this.telemetry = Telemetry(it.telemetry)
            this.spn = Spn(it.spn)
        }
    }

    override fun toString(): String {
        return if(isEmpty()) "{error:No available frame}"
        else
            String.format("{type:%s, time:%s, event=%s, stored=%s, vin=%s, position=%s, telemetry=%s, spn=%s}",
                type,
                time,
                event.name,
                stored,
                vin,
                position.toString(),
                telemetry.toString(),
                spn.toString())

    }

    companion object {
        fun response(): ByteArray = Json.encodeToString(
            Response.serializer(),
            Response(type = Command.FRAME_ACCEPT, receiveState = true)
        ).encodeToByteArray()

        @Serializable
        private data class Response(
            val type: Int,
            val receiveState: Boolean
        )
    }
}




