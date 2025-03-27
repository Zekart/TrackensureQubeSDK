package com.zekart.trackensurequbesdk.ble.base;

import java.util.UUID;

public class Uart {
   public static final UUID BLUETOOTH_LE_CCCD           = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


   //------------------------------------------------------------------------------------------------------------------
   public static final UUID BLUETOOTH_LE_TE_SERVICE = UUID.fromString("6e400000-b5a3-f393-e0a9-e50e24dcca9e");
   public static final UUID BLUETOOTH_LE_TE_CHAR_RW = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
   public static final UUID BLUETOOTH_LE_TE_CHAR_W = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
}
