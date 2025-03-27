package com.zekart.trackensurequbesdk.sdk.frame

import com.zekart.trackensurequbesdk.sdk.ext.toEnum
import com.zekart.trackensurequbesdk.sdk.types.Accuracy
import com.zekart.trackensurequbesdk.sdk.types.Quality

class Position{
    var lat: Double = 0.0      //Last known latitude
    var lng: Double = 0.0     //Last known longtitude
    var course: Double = 0.0   //Last known course over ground
    var speed: Double = 0.0    //Vehicle speed according GPS in km/h
    var satellites: Int = 0           //Number of satellites involved in calculating coordinates from the NMEA GSA package
    var quality: Quality = Quality.INVALID           //Indicator of the [quality](###type-of-quality-fixation) of position fixation
    var accuracy: Accuracy = Accuracy.NONE
    var satellitesTotal: Int = 0            //Total number of satellites in view, from NMEA GSV package

    constructor()
    constructor(position: BaseParse.BasePosition) {
        position.let {
            this.lat = it.lat
            this.lng = it.lng
            this.course = it.course
            this.speed = it.speed
            this.satellites = it.NAS
            this.quality = if(it.PFI==6) Quality.ESTIMATED else it.PFI.toEnum<Quality>() ?: Quality.INVALID
            this.satellitesTotal = it.NSV
            this.accuracy = quality.calc()
        }
    }

    fun isEmpty():Boolean = satellites==0

    override fun toString(): String {
        return String.format("{lat:%s, lng:%s, course:%s, speed:%s, satellitesConnect:%s, quality:%s, satellitesTotal:%s}",
            lat,
            lng,
            course,
            speed,
            satellites,
            quality,
            satellitesTotal)
    }
}


/*
### Type of quality fixation

|Fix status|Description|
|-----------|-----------|
|`0`|No Fix / Invalid|
|`1`|Standard GPS (2D/3D)|
|`2`|Differential GPS|
|`6`|Estimated (DR) Fix|
 */