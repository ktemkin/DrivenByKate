// Written by Kate Temkin - ktemk.in
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.core;

import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.utils.OperatingSystem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Code for communicating with the NIHostIntegrationAgent.
 * Allows for functionality like screen control that's not provided for via MIDI/OSC.
 *
 * @author Kate Temkin
 */
public abstract class AbstractNIHostInterop {
    /**
     * Constant used to indicate success in NI's RPC protocol. ASCII for 'true'.
     */
    protected final static int NI_SUCCESS = 0x74727565;
    /**
     * Constant used to indicate a device was found in power-on position.
     */
    protected final static int NI_DEVICE_ON = 0x5a48a720;
    /**
     * The software identifier for Maschine 2 ('2MhN').
     */
    protected final static int NI_SOFTWARE_ID_MASCHINE2 = 0x4e684d32;
    /**
     * The software identifier for Komplete Kontrol ('KKiN').
     */
    protected final static int NI_SOFTWARE_ID_KONTROL = 0x4e694b4b;
    /**
     * Unknown constant (all ascii) necessary for comms.
     */
    protected final static int NI_HEADER_CONSTANT = 0x70726d79;
    /**
     * The well-known mach port we'll use to communicate with the NIHostIntegrationAgent.
     */
    protected final static String NI_BOOTSTRAP_PORT = "com.native-instruments.NIHostIntegrationAgent";


    //
    // Constants.
    //
    /**
     * The message ID for a "create ports for me" handshake, in a general / non-device context.
     */
    protected final static int NI_MSG_HANDSHAKE = 0x03447500;
    /**
     * The message ID for a "create ports for me" handshake, in a per-device contxt.
     */
    protected final static int NI_MSG_CONNECT = 0x03444900;
    /**
     * The base length of a handshake message.
     */
    protected final static int NI_MSG_HANDSHAKE_LENGTH = 5 * 4;
    /**
     * The message ID for a "acknowlege notification port" handshake.
     */
    protected final static int NI_MSG_ACKNOLWEDGE_NOTIFICATION_PORT = 0x03404300;
    /**
     * The base length of an ACK message.
     */
    protected final static int NI_MSG_ACKNOLWEDGE_NOTIFICATION_PORT_LENGTH = 4 * 4;
    /**
     * The whole message for requesting our device state .
     */
    protected final static byte[] NI_WHOLE_MSG_GET_DEVICE_STATE = {(byte) 0x43, (byte) 0x71, (byte) 0x44, (byte) 0x03};

    //
    // Outgoing messages.
    //
    /**
     * The message ID for a "talk only via this protocol to me" message ('strt').
     */
    protected final static byte[] NI_WHOLE_MSG_ACQUIRE = {0x00, 0x43, 0x43, 0x03, 0x74, 0x72, 0x74, 0x73};
    /**
     * The message ID for a "please let me have focus" message ('user').
     */
    protected final static byte[] NI_WHOLE_MSG_REQUEST_FOCUS = {0x00, 0x43, 0x43, 0x03, 0x72, 0x65, 0x73, 0x75};
    /**
     * The message ID for a "set our LED colors" message.
     */
    protected final static int NI_MSG_SET_LEDS = 0x036c7500;
    /**
     * The message ID for a "set the LEDs on the keybed" message.
     */
    protected final static int NI_MSG_CONFIGURE_KEYZONE = 0x0345736b;
    /**
     * The message ID for a "send display data" handshake. 'Dsd'
     */
    protected final static int NI_MSG_DISPLAY = 0x03647344;
    /**
     * Notification that a device has connected or disconnected.
     */
    protected final static int NI_NOTIFICATION_DEVICE_STATE = 0x03444e2b;
    /**
     * Notification that occurs when the client state changes; such as when getting focus.
     */
    protected final static int NI_NOTIFICATION_CLIENT = 0x3564e66;
    /**
     * Argument to the CLIENT notification that indicates that we've gotten or lost focus.
     */
    protected final static int NI_NOTIFICATION_CLIENT_FOCUS_CHANGED = 0x41434365;
    /**
     * Argument to the CLIENT notification that indicates that we've succesfully acquried the device.
     */
    protected final static int NI_NOTIFICATION_CLIENT_ACQUIRE = 0x4D435565;
    /**
     * Argument to the CLIENT notification that indicates that the device has changed octaves.
     */
    protected final static int NI_NOTIFICATION_CLIENT_OCTAVE_CHANGED = 0x524b6579;
    /**
     * Notification received to acknowledge the connection has come up.
     */
    protected final static int NI_NOTIFICATION_ACK = 0x3444e00;
    /**
     * Notification received when a subscribed button is pressed.
     */
    protected final static int NI_NOTIFICATION_BUTTON = 0x3734e00;
    /**
     * Argument to the BUTTON notification that says this is a main encoder touch.
     */
    protected final static int NI_NOTIFICATION_BUTTON_TIMESTAMP = 0;

    //
    // Incoming messages.
    //
    /**
     * Argument to the BUTTON notification that says this is a simple button event.
     */
    protected final static int NI_NOTIFICATION_BUTTON_STATE = 1;
    /**
     * Argument to the BUTTON notification that says this is a simultaneous second button event.
     */
    protected final static int NI_NOTIFICATION_BUTTON_STATE_MULTI = 2;
    /**
     * Notification received when a subscribed knob is turned.
     */
    protected final static int NI_NOTIFICATION_KNOB = 0x3654e00;
    /**
     * Notification received when the main encoder is turned.
     */
    protected final static int NI_NOTIFICATION_ENCODER = 0x3774e00;
    /**
     * Notification received when the touch strip is touched.
     */
    protected final static int NI_NOTIFICATION_TOUCHSTRIP = 0x3744e00;
    /**
     * Notification received when our claim status has changed.
     */
    protected final static int NI_NOTIFICATION_CLAIM_CHANGED = 0x3434e00;

    /**
     * Notification received when one or more pads is touched.
     */
    protected final static int NI_NOTIFICATION_PAD_TOUCH = 0x3504e00;

    /**
     * A comprehensive list of supported Kontrol devices.
     * These are the devices for which we should report being the Kontrol software.
     */
    protected final static Set<Integer> KONTROL_DEVICE_IDS = Set.of(0x1610, 0x1620, 0x1630);


    /**
     * Maps DeviceIDs to the relevant global NIHostIntegrationAgent.
     */
    protected final static Map<Integer, AbstractNIHostInterop> globalNIConnections = new HashMap<>();
    /**
     * Stores a collection of serials known to a relevant device type.
     */
    protected final static Map<Integer, Set<String>> knownSerials = new IdentityHashMap<>();
    //
    // Global background connection to the NIHostIntegrationAgent.
    //
    protected static final Object globalStateLock = new Object();
    /**
     * True iff this is a 'global' connection, rather than a per-device one.
     */
    protected final boolean isGlobalConnection;
    /**
     * The set of callbacks issued when events happen.
     */
    protected final INIEventHandler eventHandler;
    /**
     * True iff the given device is a Kontrol device.
     */
    protected final boolean isKontrol;

    /**
     * True if we want to use MIDI for Maschine communications.
     * Will break display, unless NIHostIntegrationAgent (mk3/+) or NIHardwareAgent (studio) are hacked.
     */
    private final boolean tryToUseMidi;

    /**
     * Our host. Used to schedule event callbacks and print debug messages.
     */
    protected IHost host;
    /**
     * The device ID -- matches the device ID used elsewhere; and the USB PID.
     */
    protected int deviceId;
    /**
     * The device serial, in null-terminated ASCII. Optional.
     */
    protected ByteBuffer deviceSerialBytes;
    /**
     * Set to true iff we should terminate this connection.
     */
    protected AtomicBoolean isShutdown;
    /**
     * Thread executor for running our asynchronous notification thread.
     */
    protected ExecutorService notificationExecutor = Executors.newSingleThreadExecutor();
    /**
     * General comms lock; in case we need to panic-restart.
     */
    protected Object commsLock;


    /**
     * Creates a new interface for connecting to the NIHostIntegrationAgent.
     *
     * @param deviceId     The DeviceID for the relevant NI device.
     * @param deviceSerial The device's serial; or null / empty string for a non-device-specific connection.
     * @param eventHandler The handler to receive events from this object.
     * @param host         The host that owns this. Used to schedule callbacks.
     * @param tryToUseMidi If set, we'll try to use MIDI on Maschine devices, instead of claiming all events.
     *                     Will break display, unless you have a hacked NIHostIntegrationAgent (mk3/+) or NIHardwareAgent (studio).
     */
    public AbstractNIHostInterop(int deviceId, String deviceSerial, INIEventHandler eventHandler, IHost host, boolean tryToUseMidi) throws IOException {
        this.host = host;
        this.deviceId = deviceId;
        this.isShutdown = new AtomicBoolean(false);
        this.eventHandler = eventHandler;
        this.commsLock = new Object();

        deviceSerial = (deviceSerial == null) ? "" : deviceSerial;
        this.isGlobalConnection = deviceSerial.isEmpty();
        this.isKontrol = KONTROL_DEVICE_IDS.contains(deviceId);
        this.tryToUseMidi = tryToUseMidi && !this.isKontrol;

        // Convert the device's serial into an ASCII string.
        this.deviceSerialBytes = StandardCharsets.US_ASCII.encode(deviceSerial);
        this.bootstrapConnections();

        // If this is a global connection, issue our start-of-day requests.
        if (this.isGlobalConnection) {
            this.requestInfo();
        }

        // Otherwise, subscribe to various events.
        else if (!this.tryToUseMidi) {
            this.subscribeToEvents();
        }
    }


    /*
     * Factory method that creates the HostInterop for the relevant platform.
     *
     * @param deviceId The ID number (same as the USB PID) of the relevant device type.
     * @param serial The serial of the device to connect to, or an empty string for a non-device-specific connection.
     */
    public static AbstractNIHostInterop createInterop(int deviceId, String deviceSerial, IHost host) throws IOException {
        return createInterop(deviceId, deviceSerial, null, host, false);
    }

    /**
     * Factory method that creates the HostInterop for the relevant platform.
     *
     * @param deviceId     The ID number (same as the USB PID) of the relevant device type.
     * @param deviceSerial The serial of the device to connect to, or an empty string for a non-device-specific connection.
     */
    public static AbstractNIHostInterop createInterop(int deviceId, String deviceSerial, INIEventHandler eventHandler, IHost host, boolean tryToUseMidi) throws IOException {
        AbstractNIHostInterop interop = null;
        deviceSerial = (deviceSerial == null) ? "" : deviceSerial;

        synchronized (globalStateLock) {
            // If we're looking for a per-device connection, but we don't yet have a global one, open one first.
            if (!deviceSerial.isEmpty() && !AbstractNIHostInterop.globalNIConnections.containsKey(deviceId)) {
                AbstractNIHostInterop.createInterop(deviceId, "", host);
            }

            // If we're looking for a global connection, and we already have one, short circuit the creation process.
            if (deviceSerial.isEmpty() && AbstractNIHostInterop.globalNIConnections.containsKey(deviceId)) {
                return AbstractNIHostInterop.globalNIConnections.get(deviceId);
            }
        }


        interop = switch (OperatingSystem.get()) {
            case MAC, MAC_ARM -> new MacOSNIHostInterop(deviceId, deviceSerial, eventHandler, host, tryToUseMidi);
            case WINDOWS -> new WindowsNIHostInterop(deviceId, deviceSerial, eventHandler, host, tryToUseMidi);

            // We can't communicate with the NIHostIntegrationAgent on other platforms,
            // as NI doesn't support those platforms, yet. Return null.
            // (We shouldn't actually get here, as the plugin should abort, first.)
            default -> null;
        };

        synchronized (globalStateLock) {
            // If we just created a global connection, store it.
            if (deviceSerial.isEmpty() && (interop != null)) {
                AbstractNIHostInterop.globalNIConnections.put(deviceId, interop);
            }
        }

        return interop;
    }

    /**
     * Fetches the serialSet for the given device type. Must be called with the globalStateLock held.
     */
    private static Set<String> serialSetForDeviceType(int deviceId) {
        // Get the serial set for our device type -- creating it, if necessary.
        return AbstractNIHostInterop.knownSerials.computeIfAbsent(deviceId, k -> new HashSet<>());
    }

    /**
     * Adds a device to the list of available devices.
     */
    protected static void addAvailableDevice(int deviceId, String serial) {
        synchronized (globalStateLock) {
            var serialSet = AbstractNIHostInterop.serialSetForDeviceType(deviceId);
            serialSet.add(serial);
        }
    }

    /**
     * Removes a device from the list of available devices.
     */
    protected static void removeAvailableDevice(int deviceId, String serial) {
        synchronized (globalStateLock) {
            var serialSet = AbstractNIHostInterop.serialSetForDeviceType(deviceId);
            serialSet.remove(serial);
        }
    }

    /**
     * If one and only one of a given device type is present, returns its serial number.
     *
     * @param deviceId The NI device identifier for the device type to be fetched.
     */
    public static String getSingleDeviceSerial(int deviceId) {
        synchronized (globalStateLock) {
            var serialSet = AbstractNIHostInterop.serialSetForDeviceType(deviceId);
            if (serialSet.size() == 1) {
                return (String) serialSet.toArray(new String[1])[0];
            } else {
                return null;
            }
        }
    }

    /**
     * Sets the host used to print debug messages.
     */
    public void setHostForDebug(IHost host) {
        this.host = host;
    }

    /**
     * Prints a debug message, if possible. Same arguments as String.format().
     */
    public void debugPrint(String message, Object... toFormat) {
        if (this.host == null) {
            return;
        }

        this.host.println(String.format(message, toFormat));
    }

    /**
     * Bootstraps a per-device or per-device-type ("global") connection to the NIHostIntegrationAgent.
     */
    abstract void bootstrapConnections() throws IOException;

    /**
     * @return True iff this connection can be used for sending display data.
     */
    abstract public boolean isUsableForDisplay();

    /**
     * Sends a request over to the NIHostIntegrationAgent, but does not read a response.
     *
     * @param message The raw data to be sent as a request.
     */
    abstract public void pushRequest(byte[] message);

    /**
     * Sends a request over to the NIHostIntegrationAgent, and reads a response. Blocking.
     *
     * @param message The raw data to be sent as a request.
     * @return The raw data received as a response; or a 0-byte array if no response was received.
     */
    abstract public byte[] sendRequest(byte[] message);

    /**
     * Requests focus from the NIHostIntegrationAgent.
     * Used to request that the device display our data, specifically.
     */
    public void requestFocus() {
        // If we're trying to use MIDI, we'll skip this, as MIDI and acquisition are mutually
        // exclusive -- and we can only have focus if we're acquired ther device.
        if (!this.tryToUseMidi) {
            return;
        }

        this.pushRequest(NI_WHOLE_MSG_REQUEST_FOCUS);
    }

    /**
     * Sets the colors of the device's various button LEDs.
     */
    public void setLedColors(byte[] rawLedColors) {

        // Allocate a buffer in which we can build our LED message.
        byte[] message = new byte[8 + rawLedColors.length];
        ByteBuffer messageToSend = ByteBuffer.wrap(message);
        messageToSend.order(ByteOrder.LITTLE_ENDIAN);

        // Add our header, which contains the message ID and the non-header length...
        messageToSend.putInt(NI_MSG_SET_LEDS);
        messageToSend.putInt(rawLedColors.length);

        // ... and add in the body of raw LED colors.
        messageToSend.put(rawLedColors);

        this.pushRequest(messageToSend.array());
    }

    /**
     * Sets the colors of the device's keybed LEDs.
     * Unsurprisingly, this is only usable on devices with keybeds (so, Kontrol devices).
     *
     * @param keyColor The raw keyColor for the device.
     */
    public void configureKeyzones(int keyColor) {

        // For now, we'll mostly configure a single region, and just let
        // ourselves set the color (e.g. in our preferences.) and velocity curve.
        //
        // In the future, we may want to reverse engineer the keyzone config.
        //
        // Here's an example set-keyzone message from Komplete Kontrol, which
        // sets up a single, linear keyzone with the color Violet.
        //
        // 0x0345736b 0x00000070 0x00000000 0x00000050
        // 0x0000007f 0x00007f0d 0x007f0b00 0x7f0b0000
        // 0x0b000000 0x0000007f 0x00007f0b 0x007f0b00
        // 0x7f0b0000 0x0b000000 0x0000007f 0x00007f0b
        // 0x007f0b00 0x7f0b0000 0x0b000000 0x0000007f
        // 0x00007f0b 0x007f0b00 0x7f0b0000 0x0b000000

        final int[] premadeMessage = {
                0x0345736b, 0x00000070, 0x00000000, 0x00000050,
                0x0000007f, 0x00007f0d, 0x007f0b00, 0x7f0b0000,
                0x0b000000, 0x0000007f, 0x00007f0b, 0x007f0b00,
                0x7f0b0000, 0x0b000000, 0x0000007f, 0x00007f0b,
                0x007f0b00, 0x7f0b0000, 0x0b000000, 0x0000007f,
                0x00007f0b, 0x007f0b00, 0x7f0b0000, 0x0b000000,
        };


        // Allocate a buffer in which we can build our LED message...
        byte[] message = new byte[premadeMessage.length * 4];

        ByteBuffer messageToSend = ByteBuffer.wrap(message);
        messageToSend.order(ByteOrder.LITTLE_ENDIAN);

        // ... and, for now, just fill it with our premade buffer.
        for (var i : premadeMessage) {
            messageToSend.putInt(i);
        }

        this.pushRequest(message);
    }

    /**
     * Called to run a notification server for a short period of time (such as a second or two).
     * Runs on its own thread called by the executor; called repeatedly.
     */
    abstract void pollForNotifications();

    /**
     * Acknowledges a connection by returning the name of the notification port.
     */
    protected void subscribeToNotifications(String notificationPortName) throws IOException {

        // First, encode the notificationPortName in ASCII.
        ByteBuffer notificationPortAscii = StandardCharsets.US_ASCII.encode(notificationPortName);
        byte[] notificationPortEncoded = notificationPortAscii.array();

        // Build the message we'll use.
        byte[] rawMessage = new byte[NI_MSG_ACKNOLWEDGE_NOTIFICATION_PORT_LENGTH + notificationPortEncoded.length + 1];
        ByteBuffer messageBuffer = ByteBuffer.allocateDirect(NI_MSG_ACKNOLWEDGE_NOTIFICATION_PORT_LENGTH + notificationPortEncoded.length + 1);
        messageBuffer.order(ByteOrder.LITTLE_ENDIAN);

        messageBuffer.putInt(NI_MSG_ACKNOLWEDGE_NOTIFICATION_PORT);  // The message type.
        messageBuffer.putInt(NI_SUCCESS);                            // Success, since we're ACK'ing.
        messageBuffer.putInt(0);                                     // Padding.
        messageBuffer.putInt(notificationPortEncoded.length);        // The length of the port name that follows.
        messageBuffer.put(notificationPortEncoded);                  // Notification port we're getting.
        messageBuffer.put((byte) 0);                                  // Null terminator.

        // Ensure we have a byte array.
        messageBuffer.rewind();
        messageBuffer.get(rawMessage);

        // ... and perform our exchange.
        byte[] result = this.sendRequest(rawMessage);
        if (!this.responseWasSuccess(result)) {
            throw new IOException("NIHostIntegrationAgent did not accept our notification socket.");
        }

        // We've now subscribed to notifications -- set up a notification listener.
        notificationExecutor.submit(() -> {
            while (!this.isShutdown.get()) {
                this.pollForNotifications();
                ;
            }
        });
    }

    /**
     * Returns true iff the given response was the NI string 'true'.
     */
    private boolean responseWasSuccess(byte[] result) {
        ByteBuffer resultParser = ByteBuffer.wrap(result);
        resultParser.order(ByteOrder.LITTLE_ENDIAN);

        return (resultParser.getInt() == NI_SUCCESS);
    }

    /**
     * Requests information from the NIHIA on a successful global connection.
     */
    private void requestInfo() {

        // Request information about connected devices.
        // We'll be sent information about connected devices as a notification.
        this.pushRequest(NI_WHOLE_MSG_GET_DEVICE_STATE);
    }

    /**
     * Subscribes to input events via NIHostIntegrationAgent.
     * This consumes events that are not reported directly via MIDI; so pretty much all
     * events on Maschine devices (except the MIDI ports), and most of the non-time-critical
     * events on the Komplete Kontrol devices.
     */
    protected void subscribeToEvents() {
        // If we're not trying to use MIDI, acquire the device.
        //
        // Acquisition prevents MIDI events from being sent out, so we'll skip it
        // if we're trying to use MIDI anyway.
        if (!this.tryToUseMidi) {
            this.pushRequest(NI_WHOLE_MSG_ACQUIRE);
        }
    }

    /**
     * Handles a notification from the NIHostIntegrationAgent.
     */
    protected void handleNotification(byte[] rawNotification) {

        // We'll handle the raw data as a stream of little-endian values.
        ByteBuffer notificationData = ByteBuffer.wrap(rawNotification);
        notificationData.order(ByteOrder.LITTLE_ENDIAN);

        // Handle each possible message type.
        var notification = notificationData.getInt();
        switch (notification) {
            case NI_NOTIFICATION_DEVICE_STATE -> this.handleDeviceStateChanged(notificationData);
            case NI_NOTIFICATION_ACK -> this.debugPrint("Subscribed to events via NIHostIntegrationAgent.");
            case NI_NOTIFICATION_CLAIM_CHANGED -> this.handleClaimChanged(notificationData.getInt() == NI_SUCCESS);
            case NI_NOTIFICATION_CLIENT -> this.handleClientNotification(notificationData);
            case NI_NOTIFICATION_BUTTON -> this.handleButtonEvent(notificationData, rawNotification);
            case NI_NOTIFICATION_KNOB -> this.handleKnobEvent(notificationData);
            case NI_NOTIFICATION_ENCODER -> this.handleEncoderEvent(notificationData);
            case NI_NOTIFICATION_TOUCHSTRIP -> this.handleTouchstripEvent(notificationData);
            case NI_NOTIFICATION_PAD_TOUCH -> this.handlePadEvent(notificationData);
            default ->
                    this.debugPrint("Unknown message %x with %d bytes remaining.", notification, notificationData.remaining());
        }
    }

    /**
     * Handles receipt of a "pad touched" notification.
     */
    private void handlePadEvent(ByteBuffer notificationData) {

        // Discard the timing, which we don't care about.
        notificationData.getInt();
        notificationData.getInt();
        notificationData.getInt();

        // Parse the remainder of the message as pad data.
        while (notificationData.hasRemaining()) {
            var padNumber = notificationData.getInt();
            notificationData.getInt();
            var pressure = notificationData.getInt();

        }
    }

    /**
     * Handles receipt of a "device state changed" notification.
     */
    private void handleDeviceStateChanged(ByteBuffer notificationData) {

        // Extract the core information...
        var newState = notificationData.getInt();
        var deviceType = notificationData.getInt();
        var serialLength = notificationData.getInt();

        // ... and the serial that follows it.
        CharBuffer responseChars = StandardCharsets.US_ASCII.decode(notificationData);
        String serial = responseChars.subSequence(0, serialLength - 1).toString();

        // If we've gotten a new device, handle it.
        if (newState == NI_DEVICE_ON) {
            AbstractNIHostInterop.addAvailableDevice(deviceType, serial);
            this.debugPrint("New device %s added.", serial);
        } else {
            AbstractNIHostInterop.removeAvailableDevice(deviceType, serial);
            this.debugPrint("Device %s removed.", serial);
        }
    }

    /**
     * Handles receipt of a "client state changed" notification.
     */
    private void handleClientNotification(ByteBuffer data) {
        var number = data.getInt();
        switch (number) {
            case NI_NOTIFICATION_CLIENT_FOCUS_CHANGED:
                this.handleFocusChanged();
                return;

            case NI_NOTIFICATION_CLIENT_ACQUIRE:
                this.handleClaimChanged(true);
                return;

            case NI_NOTIFICATION_CLIENT_OCTAVE_CHANGED:
                this.handleOctaveChanged(data.getInt());
                return;

            default:
                this.debugPrint("Unknown client notification (%x); %d bytes remaining!", number, data.remaining());
                return;

        }
    }

    /**
     * Handles receipt of an 'octave changed' event.
     */
    private void handleOctaveChanged(int newBase) {
        if (this.eventHandler == null) {
            return;
        }

        this.scheduleImmediateTask(() -> this.eventHandler.handleOctaveChanged(newBase));
    }

    /**
     * Handles receipt of 'focus changed' events.
     */
    private void handleFocusChanged() {
        // Nothing to do, currently.
    }

    /**
     * Handles receipt of a button changed event.
     *
     * @param data The raw data for the relevant event.
     */
    private void handleButtonEvent(ByteBuffer data, byte[] raw) {

        if (this.eventHandler == null) {
            return;
        }


        // Discard the data that's irrelevant to us...
        data.getInt();
        data.getInt();

        // ... and handle the pieces that are.
        var type = data.getInt();
        switch (type) {
            case NI_NOTIFICATION_BUTTON_TIMESTAMP -> {
            }
            case NI_NOTIFICATION_BUTTON_STATE, NI_NOTIFICATION_BUTTON_STATE_MULTI -> {
                final int button = data.getInt();
                final int state = data.getInt();
                this.scheduleImmediateTask(() -> this.eventHandler.handleButtonEvent(button, (state % 2 == 0) ? ButtonEvent.UP : ButtonEvent.DOWN));
            }
            default ->
                    this.debugPrint("Unknown button event type (%x) -- %d of remaining data.", type, data.remaining());
        }
    }

    /**
     * Handles receipt of a 'knob' encoder event.
     *
     * @param data The raw data for the relevant event.
     */
    private void handleKnobEvent(ByteBuffer data) {

        if (this.eventHandler == null) {
            return;
        }

        // Discard the data that's irrelevant to us...
        data.getInt();
        data.getInt();
        data.getInt();

        // ... and split out the small pieces that _are_.
        final int knob = data.getInt();
        final int encoder = data.getInt();

        this.scheduleImmediateTask(() -> this.eventHandler.handleKnobEvent(knob, encoder));
    }


    //
    // Global device state helpers.
    //

    /**
     * Handles receipt of a 'primary encoder' rotation event.
     *
     * @param data The raw data for the relevant event.
     */
    private void handleEncoderEvent(ByteBuffer data) {

        if (this.eventHandler == null) {
            return;
        }

        // For now, discard the timestamp...
        data.getInt();

        // ... but keep the encoder value.
        var encoderValue = data.getInt();
        this.scheduleImmediateTask(() -> this.eventHandler.handleMainEncoderEvent(encoderValue));
    }

    /**
     * Handles receipt of a touchstrip event.
     *
     * @param data The raw data for the relevant event.
     */
    private void handleTouchstripEvent(ByteBuffer data) {
        // Currently, we only receive touchstrip events over MIDI.
        // This may change once we have good Maschine support.
    }

    /**
     * Triggered whenever our claim over the device has changed.
     * Issued when we first claim the device; and if anything tries to preempt our claim.
     *
     * @param haveClaim True iff we have a device claim after this event.
     */
    private void handleClaimChanged(boolean haveClaim) {

        // If we've just lost a claim over our buttons, re-subscribe.
        // Theoretically, we're supposed to let go when the MIDI
        if (!haveClaim) {
            this.subscribeToEvents();
        }
    }

    //
    // Generic callback helpers.
    //
    void scheduleImmediateTask(Runnable task) {
        if (this.host == null) {
            return;
        }

        // FIXME(ktemkin): The current beta of Bitwig seems to break host.scheduleTask()
        // on Windows. What we're doing is thread safe enough that we can probably just get
        // away with calling control surface functions. >.>
        if (OperatingSystem.get() == OperatingSystem.WINDOWS) {
            task.run();
        } else {
            this.host.scheduleTask(task, 0);
        }
    }

    /**
     * Used to indicate that our polling thread should stop.
     */
    public void shutdown() {
        this.isShutdown.set(true);
    }
}
