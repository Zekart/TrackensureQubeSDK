package com.zekart.trackensurequbesdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayDeque;

import com.zekart.trackensurequbesdk.sdk.frame.DeviceInfo;
import com.zekart.trackensurequbesdk.sdk.frame.EventParam;
import com.zekart.trackensurequbesdk.sdk.frame.Position;
import com.zekart.trackensurequbesdk.sdk.frame.Telemetry;

public class TerminalFragment extends Fragment implements ServiceConnection, TrackerWrapper {



    private enum Connected { False, Pending, True }

    private String deviceAddress;
    private SerialService service;

    private TextView receiveText;
    private TextView sendText;

    private TextView connectionState;
    private TextView firmware;
    private TextView motion;

    private TextView truckTelemetry;
    private TextView truckPosition;

    private TextView truckTime;
    private TextView truckEvent;

    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        requireActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            requireActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if(service != null && !requireActivity().isChangingConfigurations()) service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        requireActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { requireActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(initialStart && service != null) {
            initialStart = false;
            requireActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart && isResumed()) {
            initialStart = false;
            requireActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);
        receiveText = view.findViewById(R.id.receive_text);                          // TextView performance decreases with number of spans
        receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());
        sendText = view.findViewById(R.id.send_text);
        view.findViewById(R.id.send_btn).setOnClickListener(v -> send(sendText.getText().toString()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        connectionState = view.findViewById(R.id.state);
        firmware = view.findViewById(R.id.firmware);
        motion = view.findViewById(R.id.motion);
        truckTelemetry = view.findViewById(R.id.telemetry);
        truckPosition = view.findViewById(R.id.position);
        truckTime = view.findViewById(R.id.time);
        truckEvent = view.findViewById(R.id.event);

    }

    private void connect() {
        try {
            status("connecting...");
            connected = Connected.Pending;
            service.connect(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress));
        } catch (Exception e) {
            onTrackerConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        if(connected != Connected.True) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String msg;
            byte[] data;
            msg = str;
            data = (str + newline).getBytes();
            SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            receiveText.append(spn);
            service.write(data);
        } catch (Exception e) {
            onTrackerIoError(e);
        }
    }

    private void receive(ArrayDeque<byte[]> datas) {
        SpannableStringBuilder spn = new SpannableStringBuilder();
        for (byte[] data : datas) {
            String msg = new String(data);
            if (newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                // don't show CR as ^M if directly before LF
                msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
                if(!msg.contains(TextUtil.newline_lf)) msg = TextUtil.newline_lf + msg;
                // special handling if CR and LF come in separate fragments
                if (pendingNewline && msg.charAt(0) == '\n') {
                    if(spn.length() >= 2) {
                        spn.delete(spn.length() - 2, spn.length());
                    } else {
                        Editable edt = receiveText.getEditableText();
                        if (edt != null && edt.length() >= 2)
                            edt.delete(edt.length() - 2, edt.length());
                    }
                }
                pendingNewline = msg.charAt(msg.length() - 1) == '\r';
            }
            spn.append(TextUtil.toCaretString(msg, newline.length() != 0));
        }
        receiveText.append(spn);
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        receiveText.append(spn);
    }

    @Override
    public void onTrackerConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onTrackerConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onTrackerRawRead(@NonNull byte[] data) {
        ArrayDeque<byte[]> datas = new ArrayDeque<>();
        datas.add(data);
        receive(datas);
    }

    public void onTrackerRawRead(@NonNull ArrayDeque<byte[]> datas) {
        receive(datas);
    }

    @Override
    public void onTrackerIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onConnectionState(@NonNull BluetoothDevice device, @NonNull State state) {
        if(connectionState!=null){
            connectionState.setText(String.format("Bluetooth Device is: %s", state.name()));
        }
    }

    @Override
    public void onTrackerDeviceInfoRead(@Nullable DeviceInfo info) {
        if(firmware!=null && info != null){
            String information = String.format("Firmware:   %s\nHardware:  %s\nProtocol:    %s",
                    info.getFirmwareVersion(),
                    info.getHardwareVersion(),
                    info.getProtocolVersion());
            firmware.setText(information);
        }
    }

    @Override
    public void onTrackerEventRead(@Nullable EventParam event) {
        if(event!=null){
            Telemetry telemetry = event.getTelemetry();
            if(motion!=null) motion.setText(telemetry.getVelocity() > 0 ? String.format("truck in motion\nspeed:%s km/h", telemetry.getVelocity()): "track stopped");
            if(truckTelemetry!=null) {
                StringBuilder telemetryBuilder = new StringBuilder(">Telemetry\n");
                if(event.hasVin())telemetryBuilder.append(String.format("VIN: %s\n", event.getVin()));
                telemetryBuilder.append(String.format("Odometer: %s km\n", telemetry.getOdometer()))
                        .append(String.format("Distance: %s km\n", telemetry.getTripDistance()))
                        .append(String.format("Engine Hours: %s", telemetry.getEngineHours()));
                truckTelemetry.setText(telemetryBuilder);
            }

            if(!event.getEvent().isUnknown()) {
                if (truckTime != null) {
                    StringBuilder timeBuilder = new StringBuilder(">Time\n")
                            .append(String.format("%s\n", event.getTime("yyyy-MM-dd HH:mm:ss")))
                            .append(String.format("Millisec: %s\n", event.getTime()));
                    truckTime.setText(timeBuilder);

                }

                if(truckEvent!=null){
                    StringBuilder infoBuilder = new StringBuilder(">Frame\n")
                            .append(String.format("Type: %s\n", event.frameProtocol()))
                            .append(String.format("Event: %s", event.frameType()));
                    truckEvent.setText(infoBuilder);
                }
            }



            Position position = event.getPosition();
            if(truckPosition!=null) {
                StringBuilder positionBuilder = new StringBuilder(">Position by Satellites\n")
                        .append(String.format("Quality: %s\n", position.getQuality().name()))
                        .append(String.format("Lat: %s / Lng: %s\n", position.getLat(), position.getLng()))
                        .append(String.format("Satellites: %s (visible: %s)\n", position.getSatellitesTotal(), position.getSatellites()))
                        .append(String.format("Course: %s (speed: %s)", position.getCourse(), position.getSpeed()));
                truckPosition.setText(positionBuilder);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_terminal, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {
            receiveText.setText("");
            return true;
        } else if (id == R.id.newline) {
            String[] newlineNames = getResources().getStringArray(R.array.newline_names);
            String[] newlineValues = getResources().getStringArray(R.array.newline_values);
            int pos = java.util.Arrays.asList(newlineValues).indexOf(newline);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Newline");
            builder.setSingleChoiceItems(newlineNames, pos, (dialog, item1) -> {
                newline = newlineValues[item1];
                dialog.dismiss();
            });
            builder.create().show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    //================== NOT USED
    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device, int reason) {}

    @Override
    public void onDeviceFailedToConnect(@NonNull BluetoothDevice device, int reason) {

    }

    @Override
    public void onFileUpdateProgress(int progress) {

    }
    //================== NOT USED
}
