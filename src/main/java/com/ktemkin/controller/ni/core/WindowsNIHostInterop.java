// Written by Kate Temkin - ktemk.in
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.core;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.utils.FrameworkException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Windows backend code for interfacing with the NIHostIntegrationAgent.
 * Heavily influenced by <a href="http://github.com/terminar/rebellion">librebellion<a>.
 *
 * @author Kate Temkin
 */
public class WindowsNIHostInterop extends AbstractNIHostInterop {

    /**
     * The maximum packet size we'll allow over our pipe.
     */
    private static final int MAX_IO_SIZE = 1024;
    /**
     * Executor that handles notifications off of the main callback thread.
     */
    private final ExecutorService notificationExecutor = Executors.newCachedThreadPool();
    /**
     * The port we'll use to send requests to the NIHostIntegrationAgent.
     */
    private WinNT.HANDLE requestPort;
    /**
     * The port we'll use to receive notifications from the NIHostIntegrationAgent.
     */
    private NotificationPipe notificationPort;


    /**
     * Creates a new interface for connecting to the NIHostIntegrationAgent.
     *
     * @param deviceId     The DeviceID for the relevant NI device.
     * @param deviceSerial The device's serial; or null / empty string for a non-device-specific connection.
     * @param eventHandler The set of callbacks to use to handle notification events. May be null.
     * @param host         The host to use for debug printing and scheduling. Cannot be null if eventHandler is provided.
     * @param tryToUseMidi If set, we'll try to use MIDI on Maschine devices, instead of claiming all events.
     *                     Will break display, unless you have a hacked NIHostIntegrationAgent (mk3/+) or NIHardwareAgent (studio).
     */
    WindowsNIHostInterop(int deviceId, String deviceSerial, INIEventHandler eventHandler, IHost host, boolean tryToUseMidi) throws IOException {
        super(deviceId, deviceSerial, eventHandler, host, tryToUseMidi);
    }

    /**
     * Opens a named pipe by name.
     */
    private WinNT.HANDLE openPortByName(String name) throws IOException {
        String pipeName = "\\\\.\\pipe\\" + name;

        // First, open the pipe itself...
        WinNT.HANDLE file = Kernel32.INSTANCE.CreateFile(pipeName, WinNT.GENERIC_READ | WinNT.GENERIC_WRITE, 0, null, WinNT.OPEN_EXISTING, WinNT.FILE_ATTRIBUTE_NORMAL, null);
        if (file == WinNT.INVALID_HANDLE_VALUE) {
            throw new IOException("Failed to open WinNT handle for named pipe " + pipeName + "!");
        }

        // ... and set it up.
        IntByReference dwMode = new IntByReference(WinNT.PIPE_READMODE_MESSAGE);
        boolean success = Kernel32.INSTANCE.SetNamedPipeHandleState(file, dwMode, null, null);
        if (!success) {
            throw new IOException("Failed to configure named pipe!");
        }

        return file;
    }

    /**
     * Creates a named pipe with the given name.
     */
    private NotificationPipe createPortWithName(String name) throws IOException {
        NotificationPipe pipe = new NotificationPipe();
        String pipeName = "\\\\.\\pipe\\" + name;

        // First, create an event source we'll use to hande incoming events.
        pipe.event = Kernel32.INSTANCE.CreateEvent(null, true, true, null);

        // First, open the pipe itself...
        pipe.port = Kernel32.INSTANCE.CreateNamedPipe(pipeName, WinNT.PIPE_ACCESS_DUPLEX, WinNT.PIPE_TYPE_MESSAGE | WinNT.PIPE_READMODE_MESSAGE | WinNT.PIPE_WAIT, 1, MAX_IO_SIZE, MAX_IO_SIZE, 1000, null);
        if (pipe.port == WinNT.INVALID_HANDLE_VALUE) {
            throw new IOException("Failed to create WinNT handle for named pipe " + pipeName + "!");
        }

        return pipe;
    }

    /**
     * Bootstraps a per-device connection to the NIHostIntegrationAgent.
     */
    @Override
    protected void bootstrapConnections() throws IOException {
        byte[] deviceSerial = this.deviceSerialBytes.array();

        // Create a bootstrap port connection, which we'll use to send a handshake.
        WinNT.HANDLE bootstrapPort = this.openPortByName(NI_BOOTSTRAP_PORT);

        // Build the message we'll use.
        byte[] rawMessage = new byte[NI_MSG_HANDSHAKE_LENGTH + deviceSerial.length + 1];
        ByteBuffer messageBuffer = ByteBuffer.allocateDirect(NI_MSG_HANDSHAKE_LENGTH + deviceSerial.length + 1);
        messageBuffer.order(ByteOrder.LITTLE_ENDIAN);

        if (deviceSerial.length == 0) {
            messageBuffer.putInt(NI_MSG_HANDSHAKE);           // Connect to the server, but not to specific hardware.
        } else {
            messageBuffer.putInt(NI_MSG_CONNECT);             // Connect to a port for a specific piece of hardware.
        }
        //
        messageBuffer.putInt(this.deviceId);                  // The device type.
        messageBuffer.putInt(NI_SOFTWARE_ID_MASCHINE2);       // The "NI software" that's connecting.
        messageBuffer.putInt(NI_HEADER_CONSTANT);             // Unknown. Possibly protocol version?
        messageBuffer.putInt(deviceSerial.length + 1);        // The length of the serial number that follows, plus a NULL.

        if (deviceSerial.length != 0) {
            messageBuffer.put(deviceSerial);                    // The serial number of the device we want to control, if any.
            messageBuffer.put((byte) 0);                          // A null terminator for after the device serial.
        }

        // Ensure we have a byte array.
        messageBuffer.rewind();
        messageBuffer.get(rawMessage);

        // ... and perform our exchange.
        byte[] rawResponse = this.sendOnPort(bootstrapPort, rawMessage, true);
        if ((rawResponse == null) || rawResponse.length == 0) {
            throw new IOException("NIHostIntegrationAgent did not reply. Failing out.");
        }

        // Interpret our response a variety of ways...
        ByteBuffer response = ByteBuffer.wrap(rawResponse);
        response.order(ByteOrder.LITTLE_ENDIAN);
        if (rawResponse.length == 4) {
            throw new IOException("NIHostIntegrationAgent reports an error. Failing out.");
        }

        // ... so we can extract our target data.
        int checkVal = response.getInt();
        int requestPortLength = response.getInt();

        if (checkVal != NI_SUCCESS) {
            throw new IOException("Failed to communicate!");
        }

        // Finally, extract the core port name we need...
        CharBuffer responseChars = StandardCharsets.US_ASCII.decode(response);
        String requestPortName = responseChars.subSequence(0, requestPortLength - 1).toString();

        int notificationPortLength = response.getInt(8 + requestPortLength);
        String notificationPortName = responseChars.subSequence(requestPortLength + 4, requestPortLength + 4 + notificationPortLength - 1).toString();

        //// ... open our ports...
        this.requestPort = this.openPortByName(requestPortName);
        this.notificationPort = this.createPortWithName(notificationPortName);

        // ... and finish our bootstrapping.
        this.subscribeToNotifications(notificationPortName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUsableForDisplay() {
        return (this.requestPort != WinNT.INVALID_HANDLE_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pushRequest(byte[] data) {
        this.sendOnPort(this.requestPort, data, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] sendRequest(byte[] data) {
        return this.sendOnPort(this.requestPort, data, true);
    }

    /**
     * Sends simple data on a named pipe, and receive the response.
     * Cannot read more than 1024B, currently; but NI never sends that much.
     */
    private byte[] sendOnPort(WinNT.HANDLE port, byte[] message, boolean collectResponse) {
        int retries = 10;
        boolean sent = false;

        if (port == WinNT.INVALID_HANDLE_VALUE) {
            throw new FrameworkException("Internal consistency: trying to send on an invalid handle");
        }
        if (port == null) {
            throw new FrameworkException("Internal consistency: trying to send on an uninitialized handle");
        }

        // Optimization: if we're not looking for a response, don't read one.
        if (!collectResponse) {
            IntByReference actualWriteSize = new IntByReference(0);
            sent = Kernel32.INSTANCE.WriteFile(port, message, message.length, actualWriteSize, null);

            if (!sent || (actualWriteSize.getValue() != message.length)) {
                this.debugPrint("Failed to push message!");
            }
            return null;
        }

        // Windows will often fail with "pipe busy, try again". We'll keep reading from the pipe
        // until we either get the data we want, or get a real error.
        byte[] response = new byte[MAX_IO_SIZE];
        IntByReference bytesRead = new IntByReference(0);

        while (!sent && retries > 0) {
            sent = Kernel32.INSTANCE.TransactNamedPipe(port, message, message.length, response, response.length, bytesRead, null);
            if (sent) {
                break;
            }

            // If we failed to transact on the pipe, check the error.
            // If it's anything other than "try again", fail out.
            int result = Kernel32.INSTANCE.GetLastError();
            if (result != Kernel32.ERROR_PIPE_BUSY) {
                this.debugPrint(String.format("Failed to send on %s pipe (%d)", port == this.requestPort ? "request" : "bootstrap", result));
                return null;
            }

            // Give the pipe a little bit to be less busy.
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
                // Acceptable and ignored. :)
            }

            --retries;
        }

        // If we still haven't transacted, fail out, since we've stalled things for a second, already.
        if (!sent) {
            this.debugPrint("Retried 10 times, but no successful pipe write!");
            return null;
        }

        return Arrays.copyOfRange(response, 0, bytesRead.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void pollForNotifications() {
        NotificationPipe pipe = this.notificationPort;

        // The first time through, set up our thread state.
        // Technically, this can be done from any thread, but why take risks?
        if (!this.notificationPort.hasStarted.get()) {
            this.initializePipeServer();
        }

        // Poll for new data on our pipe.
        int status = Kernel32.INSTANCE.WaitForSingleObject(pipe.event, 1000);

        // If this wait failed, return. We'll immediately be called again to give it another try.
        if (status != WinNT.WAIT_OBJECT_0) {
            return;
        }

        // If we have asynchronous I/O in process, handle it before moving on.
        if (pipe.hasIoPending) {
            try {
                this.handleAsyncIo();
            } catch (PipeBusyException _ex) {
                // Our pipe was busy, and wants us to try again.
                // We'll get it on the next iteration.
                return;
            }
        }

        //
        // Primary read.
        //

        IntByReference bytesRead = new IntByReference(0);

        // Start a read, attempting to fill our local buffer.
        var readDone = Kernel32.INSTANCE.ReadFile(pipe.port, pipe.readBuffer, MAX_IO_SIZE, bytesRead, pipe.overlappedIo);

        // If data was waiting for us, we can actually read something immediately.
        // Handle it, and then move on to the writing state.
        if (readDone && (bytesRead.getValue() != 0)) {
            // Extract just the bytes we've actually read ...
            final byte[] notificationData = Arrays.copyOfRange(pipe.readBuffer, 0, bytesRead.getValue());

            // ... and submit them as a notification.
            this.notificationExecutor.submit(() -> this.handleNotification(notificationData));

            return;
        }
        // Otherwise, on en error, we'll have to handle our return code.
        else if (!readDone) {
            int rc = Kernel32.INSTANCE.GetLastError();

            // If the I/O was marked as pending, we're still waiting on the read
            // Mark our I/O as pending, and loop back around to our handler on the next call.
            if (rc == WinNT.ERROR_IO_PENDING) {
                pipe.hasIoPending = true;
                return;
            } else {
                throw new FrameworkException(String.format("Failed to read on notification pipe (%d)", rc));
            }

        }
        // Otherwise, we got a zero-length read.
        // Print a warning.
        else {
            this.debugPrint("READ: got a zero-length pipe read");
        }
    }

    //
    // Low-level helpers.
    //

    /**
     * Handles any I/O pending on our notification pipe.
     */
    private void handleAsyncIo() throws PipeBusyException {
        NotificationPipe pipe = this.notificationPort;
        IntByReference bytesTransferred = new IntByReference(0);

        // Get the actual result of our overlapped I/O.
        // Note that we don't yet handle failures -- we handle them on a per-state basis.
        var resultOkay = Kernel32Extended.INSTANCE.GetOverlappedResult(pipe.port, pipe.overlappedIo.getPointer(), bytesTransferred, false);

        if (pipe.waitingOnConnect) {
            // If we were waiting for a GetOverlappedResult to connect, a success means we're now connected.
            // Move to READING.
            if (resultOkay) {
                pipe.waitingOnConnect = false;
                pipe.hasIoPending = false;
            }
            // Otherwise, we'll move on depending on what our error is.
            else {
                var error = Kernel32.INSTANCE.GetLastError();

                // If we still have pending I/O, we'll need to try again.
                if (error == WinNT.ERROR_IO_PENDING) {
                    throw new PipeBusyException();
                }
                // Any other error is Bad. Fail out.
                else {
                    throw new FrameworkException(String.format("Failed to read overlapped I/O (%d).", error));
                }
            }
        } else {

            // If we were waiting on a read, and we got successful I/O, mark ourselves as having
            // read data to handle, and then move on to waiting for an async write operation.
            //
            // Our main polling loop will handle the actual read.
            if (resultOkay && (bytesTransferred.getValue() > 0)) {
                pipe.hasIoPending = false;
            } else {
                throw new FrameworkException("Failed to handle overlapped I/O (expecting read)!");
            }
        }
    }

    /**
     * Sets up our notification thread to be able to listen for NIHIA messages.
     */
    private void initializePipeServer() {
        NotificationPipe pipe = this.notificationPort;

        // Connect up to the notification port.
        var connectionIssue = Kernel32.INSTANCE.ConnectNamedPipe(pipe.port, pipe.overlappedIo);
        if (connectionIssue) {
            throw new FrameworkException("Failed to connect to our notification pipe!");
        }

        // Check to see if we've actually started.
        //
        // Apparently, we expect ERROR_IO_PENDING or ERROR_PIPE_CONNECTED, which is some kind of
        // Windows ERROR_SUCCESS bullshit.
        var status = Kernel32.INSTANCE.GetLastError();

        // If the pipe is now (or already) connceted, we've done it!
        //
        // Kick off an event to get things started, so our first wait doesn't sit waiting for an
        // event that will never come, and instead goes directly into read/write.
        if (status == WinNT.ERROR_PIPE_CONNECTED) {
            var eventSet = Kernel32.INSTANCE.SetEvent(pipe.event);
            if (!eventSet) {
                throw new FrameworkException("Failed to issue a notification pipe event.");
            }
        }
        // If we have pending I/O already, mark us as needing to handle it.
        else if (status == WinNT.ERROR_IO_PENDING) {
            pipe.hasIoPending = true;

            // Start off our poll loop by finishing this connection.
            pipe.waitingOnConnect = true;
        }

        // Otherwise, something's gone wrong.
        else {
            throw new FrameworkException(String.format("Failed to initialize our pipe server (%d).", status));
        }
    }


    /**
     * Extension that adds missing methods to the JNA Kernel32.
     */
    interface Kernel32Extended extends Kernel32 {
        Kernel32Extended INSTANCE = (Kernel32Extended) Native.load("kernel32", Kernel32Extended.class);

        boolean GetOverlappedResult(WinNT.HANDLE file, Pointer over, IntByReference actual, boolean wait);
    }

    /**
     * Simple wrapper that groups the notification port's state.
     */
    private static class NotificationPipe {

        /**
         * Stores whether we've initialized a server for the relevant pipe.
         */
        public AtomicBoolean hasStarted = new AtomicBoolean(false);

        /**
         * The core notification port pipe itself.
         */
        public WinNT.HANDLE port;

        /**
         * The event handler we'll use for reads.
         */
        public WinNT.HANDLE event;

        /**
         * Asynchronous state for our pipe.
         */
        public WinNT.OVERLAPPED overlappedIo;

        /**
         * True if we know the pipe has pending IO.
         */
        public boolean hasIoPending;

        /**
         * True if Windows has asked us to wait on this connect.
         */
        public boolean waitingOnConnect;

        /**
         * The buffer for any read operation; avoids re-creating an unnecessary buffer. Silly optimization.
         */
        public byte[] readBuffer = new byte[MAX_IO_SIZE];
    }

    //
    // JNA
    //

    /**
     * Exception indicating that the local pipe was busy.
     */
    class PipeBusyException extends IOException {
    }

}
