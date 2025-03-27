package com.zekart.trackensurequbesdk.sdk.frame

import com.zekart.trackensurequbesdk.sdk.ext.toEnum
import com.zekart.trackensurequbesdk.sdk.types.Protocol

class Spn{
    var protocolType: Protocol = Protocol.UNKNOWN                   //Current connection and diagnostic standard
    var engineCoolantTemp: Double = 0.0         //Last known engine coolant temperature
    var fuelTemp: Double = 0.0                  //Last know fuel temperature
    var OilTemp: Double = 0.0                   //Last known oil temperature
    var engineOilLevel: Double = 0.0            //Last known engine oil level
    var coolantLevel: Double = 0.0              //Last known coolant level
    var washerFluidLevel: Double = 0.0          //Last known washer fluid level
    var fuelLevelT1: Double = 0.0               //Last known fuel level from first tank
    var fuelLevelT2: Double = 0.0               //Last known fuel level from second tank
//    var tachographVehicleSpeed: Double = 0.0    //Last known tachograph vehicle speed
//    var wheelVehicleSpped: Double = 0.0         //Last know wheel based vehicle speed
    var fuelRate: Double = 0.0                  //Last know fuel rate
    var diselExhaustFluid:Int = 0               //Last know diesel exhaust fluid
    var Malfunction:Int = 0                     //Malfunction code when there is
    var Failure:Int = 0                         //Failure Mode Identifier

    constructor()
    constructor(spn: BaseParse.BaseSpn){
        spn.let {
            this.protocolType= it.protocolType.toEnum<Protocol>() ?: Protocol.UNKNOWN
            this.engineCoolantTemp= it.ECT
            this.fuelTemp= it.FTmp
            this.OilTemp= it.OTmp
            this.engineOilLevel= it.EOL
            this.coolantLevel= it.CL
            this.washerFluidLevel= it.WFL
            this.fuelLevelT1= it.fuelLevel
            this.fuelLevelT2= it.fuelLevel2
//            this.tachographVehicleSpeed= it.TVS
//            this.wheelVehicleSpped= it.WVS
            this.fuelRate= it.fuelRate
            this.diselExhaustFluid= it.DEFT
            this.Malfunction= it.DMI
            this.Failure= it.FMI
        }
    }


    override fun toString(): String {
        return String.format("{" +
                "protocolType:%s, " +
                "engineCoolantTemperature:%s, " +
                "fuelTemperature:%s, " +
                "oilTemperature:%s, " +
                "coolantLevel:%s, " +
                "fluidLevel:%s, " +
                "fuelLevelT1:%s, " +
                "fuelLevelT2:%s, " +
//                "tachograph:%s, " +
//                "wheelSpeed:%s, " +
                "fuelRate:%s, " +
                "dieselExhaustFluid:%s, " +
                "Malfunction:%s, " +
                "Failure:%s}",
            protocolType.name,
            engineCoolantTemp,
            fuelTemp,
            OilTemp,
            engineOilLevel,
            coolantLevel,
            washerFluidLevel,
            fuelLevelT1,
            fuelLevelT2,
//            tachographVehicleSpeed,
//            wheelVehicleSpped,
            fuelRate,
            diselExhaustFluid,
            Malfunction,
            Failure
            )
    }
}