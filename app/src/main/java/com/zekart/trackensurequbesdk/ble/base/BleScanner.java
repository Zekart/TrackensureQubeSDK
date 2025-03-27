package com.zekart.trackensurequbesdk.ble.base;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;

import com.zekart.trackensurequbesdk.ble.base.BleProfile;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.PhyRequest;
import no.nordicsemi.android.ble.observer.ConnectionObserver;

public abstract class BleScanner extends ContextWrapper implements ConnectionObserver {

    protected abstract BleManager updateManager();
    protected abstract void onServiceStarted();

    /** The parameter passed when creating the service. Must contain the address of the sensor that we want to connect to */
    public static final String EXTRA_DEVICE_ADDRESS = "no.nordicsemi.android.nrftoolbox.EXTRA_DEVICE_ADDRESS";
    public static final String EXTRA_DEVICE_NAME = "no.nordicsemi.android.nrftoolbox.EXTRA_DEVICE_NAME";

    private BluetoothDevice mBluetoothDevice;

//    protected BleTrackingManager mBleManager;
    protected BleManager mBleManager;
    private String mDeviceName;


    public BleScanner(Context base) {
        super(base);
    }

    private boolean isInitialized = false;

    public boolean isInitialized() {
        return isInitialized;
    }

    protected void initializeManager(){
        // Initialize the manager
        mBleManager = updateManager();//new BleTrackingManager(getApplicationContext(), listener);
        mBleManager.setConnectionObserver(this);
        isInitialized = mBleManager!=null;
    }

    public void connect(Bundle bundle){
        final String deviceAddress = bundle.getString(EXTRA_DEVICE_ADDRESS);
        mDeviceName = bundle.getString(EXTRA_DEVICE_NAME);

        if(!isConnected()) {
            if (deviceAddress != null && !deviceAddress.isEmpty()) {
                mBluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                if (mBleManager != null) {
                    onServiceStarted();
                    mBleManager.connect(mBluetoothDevice)
                            .useAutoConnect(true)
                            .retry(3, 100)
                            .enqueue();
                }
            }
        }
    }

    protected void onDestroy() {
        // shutdown the manager
        if(mBleManager!=null)mBleManager.close();
        isInitialized = false;
        mBleManager = null;
        mBluetoothDevice = null;
        mDeviceName = null;

    }

    /**
     * Method called when Bluetooth Adapter has been disabled.
     */
    protected void onBluetoothDisabled() {
        String address = getDeviceAddress();
        if (address.isEmpty() || mBleManager == null) return;
        if (isConnected())
            mBleManager.disconnect().enqueue();
    }

    /**
     * This method is called when Bluetooth Adapter has been enabled and
     * after the service was created if Bluetooth Adapter was enabled at that moment.
     * This method could initialize all Bluetooth related features, for example open the GATT server.
     */
    protected final void onBluetoothEnabled() {
        String address = getDeviceAddress();
        if (address.isEmpty() || mBleManager == null) return;
        if (!isConnected())
            mBleManager.connect(getBluetoothDevice())
                    .useAutoConnect(true)
                    .usePreferredPhy(PhyRequest.PHY_LE_1M_MASK)
                    .retry(20, 200)
                    .enqueue();
    }

    /**
     * Returns the device address
     *
     * @return device address
     */
    protected String getDeviceAddress() {
        return (mBluetoothDevice != null) ? mBluetoothDevice.getAddress(): "";
    }

    /**
     * Returns the Bluetooth device object
     *
     * @return bluetooth device
     */
    protected BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

    /**
     * Returns the device name
     *
     * @return the device name
     */
    public String getDeviceName() {
        return (mDeviceName != null) ? mDeviceName : "";
    }

    /**
     * Returns <code>true</code> if the device is connected to the sensor.
     *
     * @return <code>true</code> if device is connected to the sensor, <code>false</code> otherwise
     */
    public boolean isConnected() {
        if (mBleManager != null && mBleManager.isConnected()) {
            String deviceAddress = getDeviceAddress();
            String managerAddress = (mBleManager != null && mBleManager.getBluetoothDevice() != null) ? mBleManager.getBluetoothDevice().getAddress() : null;
            return (!deviceAddress.isEmpty()
                    && managerAddress != null
                    && !managerAddress.isEmpty() && deviceAddress.equals(managerAddress));
        } else return false;
    }

    /**
     * Returns the connection state of given device.
     * @return the connection state, as in {@link BleManager#getConnectionState()}.
     */
    public int getConnectionState() {
        return (mBleManager != null) ? mBleManager.getConnectionState() : BleProfile.STATE_DISCONNECTED;
    }

    /**
     * Disconnects from the device.
     * For connect, see onStartCommand
     */
    public final void  disconnect() {
        if(mBleManager != null) {
            final int state = getConnectionState(); //mBleManager.getConnectionState();
            if(state == BluetoothProfile.STATE_CONNECTING || state == BluetoothProfile.STATE_CONNECTED) {
                mBleManager.disconnect().enqueue();
                onDeviceDisconnected(mBluetoothDevice, ConnectionObserver.REASON_TERMINATE_LOCAL_HOST);
                mBluetoothDevice = null;
                return;
            }
            mBleManager.disconnect().enqueue();
            mBluetoothDevice = null;
        }
    }
}

