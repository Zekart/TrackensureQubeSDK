package com.zekart.trackensurequbesdk.ble.base

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import androidx.multidex.BuildConfig
import com.zekart.trackensurequbesdk.ble.BleTrackingManagerCallback
import com.zekart.trackensurequbesdk.sdk.ext.withNotNull
import com.zekart.trackensurequbesdk.sdk.frame.BaseParse
import com.zekart.trackensurequbesdk.sdk.frame.BaseProtocol
import com.zekart.trackensurequbesdk.sdk.types.Command
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.callback.WriteProgressCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.data.DataSplitter
import java.io.IOException
import java.lang.IllegalArgumentException

//How to send file - https://thelitist.medium.com/how-to-make-a-file-sharing-between-phones-using-bluetooth-or-wifi-with-kotlin-with-code-samples-cadd4f8245ef
/*
// Connect to a Bluetooth device
val device = devices[0]
val socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
socket.connect()

// Read file from storage and send it to the device
val file = File("/path/to/file.txt")
val fileInputStream = FileInputStream(file)
val outputStream = socket.getOutputStream()
val buffer = ByteArray(4096)
var bytesRead = fileInputStream.read(buffer)
while (bytesRead != -1) {
    outputStream.write(buffer, 0, bytesRead)
    bytesRead = fileInputStream.read(buffer)
}

fileInputStream.close()
outputStream.close()
socket.close()
 */
@SuppressLint("MissingPermission")
class BleTrackingManager(context: Context, private var listener: BleTrackingManagerCallback?) : BleManager(context) {
    companion object {
        private const val MAX_MTU = 512
        private const val TAG = "TrackerGattCallback"
    }

    private var parse: BaseParse = BaseParse()
    private var readCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    private var readBinCharacteristic: BluetoothGattCharacteristic? = null
    private var writeBinCharacteristic: BluetoothGattCharacteristic? = null

    override fun close() {
        super.close()
        Log.e(TAG, "Close connection")
        listener = null
    }

    var mSeq = 0
    var mNseq = 0
    fun update(data: ByteArray?){
        if (writeBinCharacteristic == null) return
        if (data!=null && data.isNotEmpty()) {
            mSeq %= 4
            mNseq %= 10
            val canceled = false
            val dataSplitter = DataSplitter { message, index, maxLength ->
                val position = index * 19
                var transfer = message.size - position
                var eof = false
                if (!canceled) {
                    if (position > message.size) {
                        Log.v("PT", "TM - End of data")
                        null
                    } else {
                        if (transfer > 19) transfer = 19 else eof = true
                        mSeq = index % 4
                        mNseq %= 10
                        val slice = ByteArray(20)
                        slice[0] = getFrameHdr(eof, mSeq, transfer)
                        System.arraycopy(message.copyOfRange(position, position + transfer), 0, slice, 1, transfer)
                        ++mNseq
                        slice
                    }
                } else {
                    Log.v("PT", "TM - Canceled")
                    null
                }
            }
            val writeProgressCallback = WriteProgressCallback { device, frame, index ->
                listener?.onFileUpdateProgress(
                    ((index * 19).toFloat() * 100.0f / data.size.toFloat()).toInt()
                )
            }
            val writeType = if (writeBinCharacteristic != null) writeBinCharacteristic!!.writeType else BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            val request: WriteRequest = this.writeCharacteristic(writeBinCharacteristic, data, writeType)
                .with({ device, data -> Log.v(TAG, "TM: -- file sent --") })
                .done { device ->
                    listener?.onFileUpdateProgress(100)
                }
            request.split(dataSplitter, writeProgressCallback).enqueue()
        }
    }

    private fun getFrameHdr(eof: Boolean, seq: Int, len: Int): Byte {
        return ((if (eof) 128 else 0) or (seq shl 5) or len).toByte()
    }

    fun request(value: ByteArray) {
        val writeType =
            if (writeCharacteristic != null) writeCharacteristic!!.writeType else BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        if (writeCharacteristic == null) {
            Log.e(TAG, "Write failed")
            listener?.onTrackerIoError(IOException("Write failed"))
            return
        }
        val writeReq = this.writeCharacteristic(writeCharacteristic, value, writeType)
            .done { device -> if(BuildConfig.DEBUG) Log.e(TAG, "Write success on device address: ${device.address}") }
            .fail { device , status -> listener?.onTrackerIoError(IOException("Write failed on device address: ${device.address}. Status: $status")) }
        writeReq.split().enqueue()
    }

    override fun getGattCallback(): BleManagerGattCallback = TrackerGattCallback()

    private inner class TrackerGattCallback : BleManagerGattCallback(), DataReceivedCallback {
        override fun initialize() {
            Log.e(TAG, "Tracker GATT initialize")
            parse.invalidate()
            setNotificationCallback(readCharacteristic).with(this)
            requestMtu(MAX_MTU).enqueue()
            enableNotifications(readCharacteristic).enqueue()
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            for (gattService in gatt.services) {
                if (gattService.uuid == Uart.BLUETOOTH_LE_TE_SERVICE) {
                    readCharacteristic = gattService.getCharacteristic(Uart.BLUETOOTH_LE_TE_CHAR_RW)
                    writeCharacteristic = gattService.getCharacteristic(Uart.BLUETOOTH_LE_TE_CHAR_W)
                    if (writeCharacteristic == null)
                        writeCharacteristic = gattService.getCharacteristic(Uart.BLUETOOTH_LE_TE_CHAR_RW)
                    break
                }
            }
            if (readCharacteristic != null && writeCharacteristic != null) {
                val writeProperties = writeCharacteristic!!.properties
                if (writeProperties and BluetoothGattCharacteristic.PROPERTY_WRITE + BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE == 0) { // HM10,TI uart,Telit have only WRITE_NO_RESPONSE
                    listener?.onTrackerConnectError(IOException("Write characteristic not writable"))
                    return false
                }

                readCharacteristic?.let {
                    if (!gatt.setCharacteristicNotification(it, true)) {
                        Log.e(TAG, IOException("No notification for read characteristic").toString())
                        listener?.onTrackerConnectError(IOException("No notification for read characteristic"))
                        return false
                    }

                    val readDescriptor = it.getDescriptor(Uart.BLUETOOTH_LE_CCCD)
                    if (readDescriptor == null) {
                        Log.e(TAG, IOException("No CCCD descriptor for read characteristic").toString())
                        listener?.onTrackerConnectError(IOException("No CCCD descriptor for read characteristic"))
                        return false
                    }
                }
            }

            if (readCharacteristic == null || writeCharacteristic == null) {
                if(BuildConfig.DEBUG) {
                    for (gattService in gatt.services) {
                        Log.d(TAG, "service " + gattService.uuid)
                        for (characteristic in gattService.characteristics)
                            Log.d(TAG, "characteristic " + characteristic.uuid)
                    }
                }
                listener?.onTrackerConnectError(IOException("No serial profile found"))
                return false
            } else listener?.onTrackerConnect()
            return true
        }

        override fun onServicesInvalidated() {
            Log.e(TAG, "Tracker GATT services invalidated")
            parse.invalidate()
            readCharacteristic = null
            writeCharacteristic = null
            readBinCharacteristic = null
            writeBinCharacteristic = null
        }

        override fun onDataReceived(device: BluetoothDevice, data: Data) {
            if (data.size() != 0) {
                data.value?.let {
                    Log.d(TAG, String(it))
                    dataReceived(it)
                }
            } else {
                listener?.onTrackerIoError(IOException("No data encountered, ignoring callback!"))
            }
        }
    }

    private fun dataReceived(data: ByteArray) {
        if(listener==null) {
            Log.e(TAG, "Callback inactive, ignoring callback!")
            return
        }
        listener?.apply {
            String(data).withNotNull { str ->
                parse.parse(str)
                try {
                    if (parse.hasType(Command.INFO)) {
                        when (parse.type) {
                            Command.INFO -> parse.info?.let {
                                onTrackerDeviceInfoRead(it)
                                Log.e(TAG, it.toString())
                                onTrackerRawRead(it.toString().encodeToByteArray())
                                return
                            }

                            Command.FRAME -> parse.eventParam?.let {
                                onTrackerEventRead(it)
                                Log.e(TAG, it.toString())
                                onTrackerRawRead(it.toString().encodeToByteArray())
                                return
                            }

                            else -> onTrackerIoError(IOException("Protocol error - parsed type unknown"))
                        }
                    } else {
                        request(BaseProtocol.reset())
                    }
                }catch (ex:IllegalArgumentException){
                    onTrackerIoError(ex)
                }
            }
            onTrackerRawRead(data)
        }
    }
}