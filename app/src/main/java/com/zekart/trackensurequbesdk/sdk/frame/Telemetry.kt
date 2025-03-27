package com.zekart.trackensurequbesdk.sdk.frame


class Telemetry{
    var odometer: Long = 0          //Vehicle odometer reading
    var engineHours: Double = 0.0       //Last know engine hours
    var rpm: Long = 0               //Last known engine speed rpm
    var velocity: Long = 0          //Last known speed km/h
    var tripDistance: Long = 0      //Last known trip distance
    var acc: Long = 0

    constructor()
    constructor(telemetry: BaseParse.BaseTelemetry){
        telemetry.let {
            this.odometer = it.odometer
            this.engineHours = it.EH
            this.rpm = it.ESRPM
            this.velocity = it.velocity
            this.tripDistance = it.tripDistance
            this.acc = it.acc
        }
    }



    override fun toString(): String {
        return String.format("{odometer:%s, engineHours:%s, speed:%s, rpm:%s, distance:%s}", odometer, engineHours, velocity, rpm, tripDistance)
    }
}