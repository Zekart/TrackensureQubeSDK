package com.zekart.trackensurequbesdk.sdk.types

enum class EventType {
    /*
    Power ON            - 1   The event is generated by the bus
    Power OFF           - 2   The event is generated by the bus
    Ignition ON         - 3   The event is generated by the bus
    Ignition OFF        - 4   The event is generated by the bus
    Engine ON           - 5   The event is generated by the bus
    Engine OFF          - 6   The event is generated by the bus
    Trip START          - 7   Event generated by comparing bus data and preset parameters
    Trip STOP           - 8   Event generated by comparing bus data and preset parameters
    Periodic            - 9   The event is generated automatically if there are no other events. The event is overwritten when a new one occurs.
    Harsh Acceleration  - 10  Event generated by comparing GNSS modul data and preset parameters
    Harsh Harsh Brake   - 11  Event generated by comparing GNSS modul data and preset parameters
    Harsh Cornering     - 12  Event generated by comparing GNSS modul data and preset parameters
    */
    EV_UNKNOWN,
    EV_POWER_ON,
    EV_POWER_OFF,
    EV_IGNITION_ON,
    EV_IGNITION_OFF,
    EV_ENGINE_ON,
    EV_ENGINE_OFF,
    EV_TRIP_START,
    EV_TRIP_STOP,
    EV_PERIODIC,
    EV_MEMS_ACC,
    EV_MEMS_BRK,
    EV_MEMS_COR;

    fun isUnknown():Boolean = this== EV_UNKNOWN
}