package com.zekart.trackensurequbesdk.ble.scanner;

import static no.nordicsemi.android.support.v18.scanner.ScanSettings.CALLBACK_TYPE_ALL_MATCHES;
import static no.nordicsemi.android.support.v18.scanner.ScanSettings.SCAN_MODE_LOW_LATENCY;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import com.zekart.trackensurequbesdk.ble.base.Uart;
import com.zekart.trackensurequbesdk.qubesdk.WQSmartService;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class BleScannerNordic extends ScanCallback  {

   private final ScanSettings scanSettings;
   private BluetoothLeScannerCompat mScannerService;
   private ScannerCallBack mScannerCallBack;

   public BleScannerNordic(ScannerCallBack scannerCallBack){
      mScannerService = BluetoothLeScannerCompat.getScanner();
      scanSettings = new ScanSettings.Builder()
              .setScanMode(SCAN_MODE_LOW_LATENCY)
              .setLegacy(true)
              .setReportDelay(5000)
              .setMatchMode(CALLBACK_TYPE_ALL_MATCHES)
              .setUseHardwareBatchingIfSupported(false)
              .build();
      mScannerCallBack = scannerCallBack;
   }

   /**
    * Scan for 5 seconds and then stop scanning when a BluetoothLE device is found then mLEScanCallback
    * is activated This will perform regular scan for custom BLE Service UUID and then filter out.
    * using class ScannerServiceParser
    */

   public void startScanDevices() {
      final List<ScanFilter> filters = new ArrayList<>();
      filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(Uart.BLUETOOTH_LE_TE_CHAR_RW)).build());

      //For Qube
      filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(WQSmartService.WQSmartUuid.OBD_SERVICE.value)).build());

      mScannerService.startScan(filters, scanSettings, this);
   }

   public void stopScanDevices() {
      if(mScannerService != null){
         mScannerService.stopScan(this);
      }
   }

   public void onDestroy() {
      mScannerCallBack = null;
      if(mScannerService != null)mScannerService.stopScan(this);
      mScannerService = null;
   }

   @Override
   public void onScanResult(int callbackType, @NonNull ScanResult result) {
      super.onScanResult(callbackType, result);
   }

   @Override
   public void onScanFailed(int errorCode) {
      super.onScanFailed(errorCode);
   }

   @Override
   public void onBatchScanResults(@NonNull List<ScanResult> results) {
      super.onBatchScanResults(results);
      List <BluetoothDevice> res = new ArrayList<>(results.size());
      for (ScanResult scanResult : results) {
         res.add(scanResult.getDevice());
      }
      mScannerCallBack.scanResult(res);
   }

   @Override
   protected void finalize() throws Throwable {
      onDestroy();
      super.finalize();
   }
}

