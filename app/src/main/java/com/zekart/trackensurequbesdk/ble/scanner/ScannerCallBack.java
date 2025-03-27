package com.zekart.trackensurequbesdk.ble.scanner;

import android.bluetooth.BluetoothDevice;

import java.util.List;

public interface ScannerCallBack {
 void scanResult(List<BluetoothDevice> result);
}
