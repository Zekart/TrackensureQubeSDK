package com.zekart.trackensurequbesdk

import android.app.Activity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zekart.trackensurequbesdk.qubesdk.GeoData
import com.zekart.trackensurequbesdk.qubesdk.custom.QubeManagerSDKWrapper
import com.zekart.trackensurequbesdk.qubesdk.custom.QubeSDKManager

class TerminalQubeFragment: Fragment(), QubeManagerSDKWrapper {


    private enum class Connected {
        False,
        Pending,
        True
    }

    private var connected = Connected.False
    private var initialStart = true
    private val pendingNewline = false
    private val newline = TextUtil.newline_crlf

    private var receiveText: TextView? = null
    private var sendText: TextView? = null

    private var connectionState: TextView? = null
    private var firmware: TextView? = null
    private var motion: TextView? = null

    private var truckTelemetry: TextView? = null
    private var truckPosition: TextView? = null
    private var truckTime: TextView? = null
    private var truckEvent: TextView? = null
    private var deviceAddress: String? = null
    private var qubeSDKManager: QubeSDKManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
        deviceAddress = arguments?.getString("device")
        // Init Device
        qubeSDKManager = QubeSDKManager(requireContext().applicationContext,this)

//        val filters = qubeSDKManager?.mFilters
//        requireContext().registerReceiver(receiver,filters, Context.RECEIVER_NOT_EXPORTED)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_qube_terminal, container, false)
        receiveText = view.findViewById(R.id.qube_receive_text) // TextView performance decreases with number of spans
        receiveText?.setTextColor(resources.getColor(R.color.colorRecieveText)) // set as default color to reduce number of spans
        receiveText?.movementMethod = ScrollingMovementMethod.getInstance()
        sendText = view.findViewById(R.id.qube_send_text)
        view?.findViewById<View>(R.id.qube_send_btn)?.setOnClickListener {
            send(sendText?.text.toString())
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectionState = view.findViewById(R.id.qube_state)
        firmware = view.findViewById(R.id.qube_firmware)
        motion = view.findViewById(R.id.qube_motion)
        truckTelemetry = view.findViewById(R.id.qube_telemetry)
        truckPosition = view.findViewById(R.id.qube_position)
        truckTime = view.findViewById(R.id.qube_time)
        truckEvent = view.findViewById(R.id.qube_event)
    }

    private fun send(str: String) {
        if (connected != Connected.True) {
            Toast.makeText(activity, "not connected", Toast.LENGTH_SHORT).show()
            return
        }

        //send command
    }

    private fun status(str: String) {
        val spn = SpannableStringBuilder(str + '\n')
        spn.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.colorStatusText)),
            0,
            spn.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        receiveText?.append(spn)
    }

//    override fun onConnectionState(device: BluetoothDevice, state: TrackerWrapper.State) {
//        if (connectionState != null) {
//            connectionState!!.text = String.format("Bluetooth Device is: %s", state.name)
//        }
//    }

    override fun onServiceStarted() {
        status("connecting...")
        connected = Connected.Pending
        qubeSDKManager?.connectDevice(deviceAddress)
    }

    override fun onServiceStopped() {
        println()
    }

    override fun onDeviceSuccessConnected(device: String) {
        connected = Connected.True
        connectionState!!.text = String.format("Bluetooth Device is: %s", device)
        status("connected to: $device")
    }

    override fun onDeviceErrorConnection(device: String, message: String) {
        connected = Connected.False
    }

    override fun onError(message: String) {
        status("connection lost: $message")
    }

    override fun onDataReceive(data: GeoData) {
        receiveText?.text = data.toString()
    }

    override fun onDestroy() {
        qubeSDKManager?.closeService()
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
//        if (service != null) service.attach(this) else requireActivity().startService(
//            Intent(
//                activity,
//                SerialService::class.java
//            )
//        ) // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    override fun onStop() {
        //if (service != null && !requireActivity().isChangingConfigurations) service.detach()
        super.onStop()
    }

    @Suppress("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
//        requireActivity().bindService(
//            Intent(getActivity(), SerialService::class.java), this, Context.BIND_AUTO_CREATE
//        )
    }

    override fun onDetach() {
        try {
            //requireActivity().unbindService(this)
        } catch (ignored: java.lang.Exception) {
        }
        super.onDetach()
    }

    override fun onResume() {
        super.onResume()
//        if (initialStart && service != null) {
//            initialStart = false
//            requireActivity().runOnUiThread { this.connect() }
//        }
    }

}