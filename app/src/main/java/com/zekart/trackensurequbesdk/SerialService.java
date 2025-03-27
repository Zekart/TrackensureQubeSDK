package com.zekart.trackensurequbesdk;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayDeque;

import com.zekart.trackensurequbesdk.ble.Tracker;
import com.zekart.trackensurequbesdk.sdk.frame.DeviceInfo;
import com.zekart.trackensurequbesdk.sdk.frame.EventParam;

/**
 * create notification and queue serial data while activity is not in the foreground
 * use listener chain: EldBleManager -> SerialService -> UI fragment
 */
public class SerialService extends Service implements TrackerWrapper {

    class SerialBinder extends Binder {
        SerialService getService() { return SerialService.this; }
    }

    private enum QueueType {Connect, ConnectError, Device, ReadRaw, IoError}

    private static class QueueItem {
        QueueType type;
        ArrayDeque<byte[]> datas;

        ArrayDeque<DeviceInfo> deviceInfoArray;
        ArrayDeque<EventParam> eventParamArray;

        Exception e;

        Pair<BluetoothDevice, State> currentState;

        QueueItem(){}
        QueueItem(QueueType type) {
            this.type=type;
            if(type==QueueType.ReadRaw) {
                rawInit();
                infoReadInit();
                eventParamInit();
            }
        }
        QueueItem(QueueType type, Exception e) { this.type=type; this.e=e; }

        QueueItem(QueueType type, Pair<BluetoothDevice, State> connection) { this.type=type; this.currentState=connection; }

        QueueItem(QueueType type, ArrayDeque<byte[]> datas) { this.type=type; this.datas=datas; }

        QueueItem updateInfo(ArrayDeque<DeviceInfo> datas){
            this.type=QueueType.ReadRaw;
            this.deviceInfoArray =datas;
            return this;
        }

        QueueItem updateEvents(ArrayDeque<EventParam> params){
            this.type=QueueType.ReadRaw;
            this.eventParamArray = params;
            return this;
        }


        void rawInit() { datas = new ArrayDeque<>(); }
        void infoReadInit() { deviceInfoArray = new ArrayDeque<>(); }
        void eventParamInit() { eventParamArray = new ArrayDeque<>(); }

        void rawAdd(byte[] data) { datas.add(data); }
        void infoAdd(DeviceInfo data) { deviceInfoArray.add(data); }
        void eventsAdd(EventParam data) { eventParamArray.add(data); }
    }

    private final Handler mainLooper;
    private final IBinder binder;
    private final ArrayDeque<QueueItem> queue1, queue2;
    private final QueueItem lastRead;

    private Tracker mBleManager;
    private TrackerWrapper listener;
    private boolean connected;

    /**
     * Lifecylce
     */
    public SerialService() {
        mainLooper = new Handler(Looper.getMainLooper());
        binder = new SerialBinder();
        queue1 = new ArrayDeque<>();
        queue2 = new ArrayDeque<>();
        lastRead = new QueueItem(QueueType.ReadRaw);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        disconnect();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Api
     */
    public void connect(BluetoothDevice device) {
        this.mBleManager = new Tracker(getApplicationContext(), this);
        this.mBleManager.connect(device);
        connected = true;
    }

    public void disconnect() {
        connected = false; // ignore data,errors while disconnecting
        stopForeground(true);
        if(mBleManager != null) {
            mBleManager.disconnect();
            mBleManager = null;
        }
    }

    public void write(byte[] data) throws IOException {
        if(!connected)
            throw new IOException("not connected");
        mBleManager.write(data);
    }

    public void attach(TrackerWrapper listener) {
        if(Looper.getMainLooper().getThread() != Thread.currentThread())
            throw new IllegalArgumentException("not in main thread");
        stopForeground(true);
        // use synchronized() to prevent new items in queue2
        // new items will not be added to queue1 because mainLooper.post and attach() run in main thread
        synchronized (this) {
            this.listener = listener;
        }

        for(QueueItem item : queue1) {
            switch(item.type) {
                case Device:
                    Pair<BluetoothDevice, State> device = item.currentState;
                    listener.onConnectionState(device.first, device.second);
                    break;
                case Connect:       listener.onTrackerConnect(); break;
                case ConnectError:  listener.onTrackerConnectError(item.e); break;
                case ReadRaw:
                    listener.onTrackerRawRead(item.datas);
                    for(DeviceInfo info: item.deviceInfoArray) {
                        listener.onTrackerDeviceInfoRead(info);
                    }
                    for(EventParam eventParam: item.eventParamArray) {
                        listener.onTrackerEventRead(eventParam);
                    }
                break;
                case IoError:       listener.onTrackerIoError(item.e); break;
            }
        }
        for(QueueItem item : queue2) {
            switch(item.type) {
                case Device:
                    Pair<BluetoothDevice, State> device = item.currentState;
                    listener.onConnectionState(device.first, device.second);
                    break;
                case Connect:       listener.onTrackerConnect(); break;
                case ConnectError:  listener.onTrackerConnectError(item.e); break;
                case ReadRaw:
                    listener.onTrackerRawRead(item.datas);
                    for(DeviceInfo info: item.deviceInfoArray) {
                        listener.onTrackerDeviceInfoRead(info);
                    }
                    for(EventParam eventParam: item.eventParamArray) {
                        listener.onTrackerEventRead(eventParam);
                    }
                    break;
                case IoError:       listener.onTrackerIoError(item.e); break;
            }
        }
        queue1.clear();
        queue2.clear();
    }

    public void detach() {
        // items already in event queue (posted before detach() to mainLooper) will end up in queue1
        // items occurring later, will be moved directly to queue2
        // detach() and mainLooper.post run in the main thread, so all items are caught
        listener = null;
    }


    @Override
    public void onTrackerConnect() {
        if(connected) {
            synchronized (this) {
                if (listener != null) {
                    mainLooper.post(() -> {
                        if (listener != null) {
                            listener.onTrackerConnect();
                        } else {
                            queue1.add(new QueueItem(QueueType.Connect));
                        }
                    });
                } else {
                    queue2.add(new QueueItem(QueueType.Connect));
                }
            }
        }
    }

    @Override
    public void onTrackerConnectError(Exception e) {
        if(connected) {
            synchronized (this) {
                if (listener != null) {
                    mainLooper.post(() -> {
                        if (listener != null) {
                            listener.onTrackerConnectError(e);
                        } else {
                            queue1.add(new QueueItem(QueueType.ConnectError, e));
                            disconnect();
                        }
                    });
                } else {
                    queue2.add(new QueueItem(QueueType.ConnectError, e));
                    disconnect();
                }
            }
        }
    }

    @Override
    public void onTrackerRawRead(ArrayDeque<byte[]> datas) { throw new UnsupportedOperationException(); }

    /**
     * reduce number of UI updates by merging data chunks.
     * Data can arrive at hundred chunks per second, but the UI can only
     * perform a dozen updates if receiveText already contains much text.
     *
     * On new data inform UI thread once (1).
     * While not consumed (2), add more data (3).
     */
    @Override
    public void onTrackerRawRead(byte[] data) {
        if(connected) {
            synchronized (this) {
                if (listener != null) {
                    boolean first;
                    synchronized (lastRead) {
                        first = lastRead.datas.isEmpty(); // (1)
                        lastRead.rawAdd(data); // (3)
                    }
                    if(first) {
                        mainLooper.post(() -> {
                            ArrayDeque<byte[]> datas;
                            synchronized (lastRead) {
                                datas = lastRead.datas;
                                lastRead.rawInit(); // (2)
                            }
                            if (listener != null) {
                                listener.onTrackerRawRead(datas);
                            } else {
                                queue1.add(new QueueItem(QueueType.ReadRaw, datas));
                            }
                        });
                    }
                } else {
                    if(queue2.isEmpty() || queue2.getLast().type != QueueType.ReadRaw)
                        queue2.add(new QueueItem(QueueType.ReadRaw));
                    queue2.getLast().rawAdd(data);
                }
            }
        }
    }

    @Override
    public void onTrackerIoError(Exception e) {
        if(connected) {
            synchronized (this) {
                if (listener != null) {
                    mainLooper.post(() -> {
                        if (listener != null) {
                            listener.onTrackerIoError(e);
                        } else {
                            queue1.add(new QueueItem(QueueType.IoError, e));
                            disconnect();
                        }
                    });
                } else {
                    queue2.add(new QueueItem(QueueType.IoError, e));
                    disconnect();
                }
            }
        }
    }

    @Override
    public void onConnectionState(@NonNull BluetoothDevice device, @NonNull State state) {
        synchronized (this) {
            if (listener != null) {
                mainLooper.post(() -> {
                    if (listener != null) {
                        listener.onConnectionState(device, state);
                    } else {
                        queue1.add(new QueueItem(QueueType.Device, new Pair<>(device, state)));
                    }
                });
            } else {
                queue2.add(new QueueItem(QueueType.ConnectError, new Pair<>(device, state)));
            }
        }
    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device, int reason) {}

    @Override
    public void onDeviceFailedToConnect(@NonNull BluetoothDevice device, int reason) {}

    @Override
    public void onTrackerDeviceInfoRead(@Nullable DeviceInfo info) {
        if(connected) {
            synchronized (this) {
                if (listener != null) {
                    boolean first;
                    synchronized (lastRead) {
                        first = lastRead.deviceInfoArray.isEmpty(); // (1)
                        lastRead.infoAdd(info); // (3)
                    }
                    if(first) {
                        mainLooper.post(() -> {
                            ArrayDeque<DeviceInfo> datas;
                            synchronized (lastRead) {
                                datas = lastRead.deviceInfoArray;
                                lastRead.infoReadInit(); // (2)
                            }
                            if (listener != null) {
                                for(DeviceInfo inf : datas) {
                                    listener.onTrackerDeviceInfoRead(inf);
                                }
                            } else {
                                queue1.add(new QueueItem().updateInfo(datas));
                            }
                        });
                    }
                } else {
                    if(queue2.isEmpty() || queue2.getLast().type != QueueType.ReadRaw) {
                        queue2.add(new QueueItem(QueueType.ReadRaw));
                    }
                    queue2.getLast().infoAdd(info);
                }
            }
        }
    }

    @Override
    public void onTrackerEventRead(@Nullable EventParam event) {
        if(connected) {
            synchronized (this) {
                if (listener != null) {
                    boolean first;
                    synchronized (lastRead) {
                        first = lastRead.datas.isEmpty(); // (1)
                        lastRead.eventsAdd(event); // (3)
                    }
                    if(first) {
                        mainLooper.post(() -> {
                            ArrayDeque<EventParam> datas;
                            synchronized (lastRead) {
                                datas = lastRead.eventParamArray;
                                lastRead.eventParamInit(); // (2)
                            }
                            if (listener != null) {
                                for(EventParam inf : datas) {
                                    listener.onTrackerEventRead(inf);
                                }
                            } else {
                                queue1.add(new QueueItem().updateEvents(datas));
                            }
                        });
                    }
                } else {
                    if(queue2.isEmpty() || queue2.getLast().type != QueueType.ReadRaw) {
                        queue2.add(new QueueItem(QueueType.ReadRaw));
                    }
                    queue2.getLast().eventsAdd(event);
                }
            }
        }
    }

    @Override
    public void onFileUpdateProgress(int progress) {}
}
