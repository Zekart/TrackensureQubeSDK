package com.zekart.trackensurequbesdk.sdk.frame

import com.zekart.trackensurequbesdk.sdk.types.Command
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.json.JSONObject
import java.util.TreeMap
import kotlin.jvm.Throws

class BaseParse {
    var type: Int = Command.UNKNOWN
    var info: DeviceInfo? = null
    var eventParam: EventParam? = null
    private var queue : TreeMap<Int, BaseProtocol> = TreeMap<Int, BaseProtocol>()

    fun invalidate(){
        queue = TreeMap<Int, BaseProtocol>()
        type = Command.UNKNOWN
        info = null
        eventParam = null
    }

    fun parse(string: String): BaseParse {
        this.type = Command.UNKNOWN
        this.info = null
        this.eventParam = null
        isValidJson(string)?.let {json ->
            if(json.has("type")) {
                this.type = json.optInt("type") ?: Command.UNKNOWN
                when (this.type) {
                    Command.INFO ->{
                        this.info = DeviceInfo(BaseInfo.toObject(string), string).also {
                            queue[this.type] = it
                        }
                    }

                    Command.FRAME -> {
                        this.eventParam = EventParam(BaseFrame.toObject(string), string).also {
                            queue[this.type] = it
                        }
                    }
                    else -> {
                        queue[this.type] = BaseProtocol()
                    }
                }
            }
        }
        return this
    }

    @Throws(IllegalArgumentException::class)
    fun hasType(key:Int):Boolean{
        if(queue.isEmpty()) throw IllegalArgumentException("Parse method need to be called first")
        return queue[key]!=null && !queue[key]!!.isEmpty()
    }

    private fun isValidJson(stringValue: String?): JSONObject? {
        if(!stringValue.isNullOrEmpty()) return JSONObject(stringValue)
        return null
    }

    @Serializable
    data class BaseFrame(
        val type: Int,              //Contains a complete description of the vehicle's condition
        val time: Long,             //Epoch event generation time in GMT
        val event: Int = 0,         //One of the possible [event types](###event-types)
        val stored: Boolean = false,        //Indicates whether the event was generated before the _Mobile_ connected
        val vin: String = "",       //Vehicle Identification Number
        val position: BasePosition,
        val telemetry: BaseTelemetry,
        val spn: BaseSpn
    ){

        /*
        Example response:
        {
            "type":3,
            "receiveState":true
        }
        */
        companion object{
            fun toObject(stringValue: String): BaseFrame = Json.decodeFromString(serializer(), stringValue)
        }
    }

    @Serializable
    data class BaseInfo(
        val type: Int,                      //Invitation to work
        val time: Long,                     //Epoch time in GMT
        val countOfStoredMessages: Int = 0, //Number of accumulated important messages before connection starts
        val protocolVersion: String = "",   //Version of the implementation of the communication protocol between devices
        val HWVersion: String = "",         //Current _Device_ hardware version
        val FWVersion: String = ""          //Current _Device_ firmware version
    ){

        /*
        Example response:
        {
            "type":1,
            "reportEach":2,
            "minTruckSpeed":10
        }
        */
        companion object{
            fun toObject(stringValue: String): BaseInfo = Json.decodeFromString(serializer(), stringValue)
        }
    }


    @Serializable
    data class BasePosition(
        val lat: Double = 0.0,      //Last known latitude
        val lng: Double = 0.0,      //Last known longtitude
        val course: Double = 0.0,   //Last known course over ground
        val speed: Double = 0.0,    //Vehicle speed according GPS in km/h
        val NAS: Int = 0,           //Number of satellites involved in calculating coordinates from the NMEA GSA package
        val PFI: Int = 0,           //Indicator of the [quality](###type-of-quality-fixation) of position fixation
        val NSV: Int = 0            //Total number of satellites in view, from NMEA GSV package
    ){
        companion object{
            fun toObject(stringValue: String): BasePosition = Json.decodeFromString(serializer(), stringValue)
        }
    }

    @Serializable
    data class BaseTelemetry(
        val odometer: Long = 0,     //Vehicle odometer reading
        val EH: Double = 0.0,           //Last know engine hours
        val ESRPM: Long = 0,        //Last known engine speed rpm
        val velocity: Long = 0,     //Last known speed km/h
        val tripDistance: Long = 0,  //Last known trip distance
        val acc:Long = 0
    ){
        companion object{
            fun toObject(stringValue: String): BaseTelemetry = Json.decodeFromString(serializer(), stringValue)
        }
    }

    @Serializable
    data class BaseSpn(
        val protocolType: Int = 0,      //Current connection and diagnostic standard
        val ECT: Double = 0.0,          //Last known engine coolant temperature
        val FTmp: Double = 0.0,         //Last know fuel temperature
        val OTmp: Double = 0.0,         //Last known oil temperature
        val EOL: Double = 0.0,          //Last known engine oil level
        val CL: Double = 0.0,           //Last known coolant level
        val WFL: Double = 0.0,          //Last known washer fluid level
        val fuelLevel: Double = 0.0,    //Last known fuel level from first tank
        val fuelLevel2: Double = 0.0,   //Last known fuel level from second tank
        @Deprecated("Need to remove") val TVS: Double = 0.0,          //Last known tachograph vehicle speed
        @Deprecated("Need to remove") val WVS: Double = 0.0,          //Last know wheel based vehicle speed
        val fuelRate: Double = 0.0,     //Last know fuel rate
        val DEFT:Int = 0,               //Last know diesel exhaust fluid
        val DMI:Int = 0,                //Malfunction code when there is
        val FMI:Int = 0                 //Failure Mode Identifier
    ){
        companion object{
            fun toObject(stringValue: String): BaseSpn = Json.decodeFromString(serializer(), stringValue)
        }
    }
}