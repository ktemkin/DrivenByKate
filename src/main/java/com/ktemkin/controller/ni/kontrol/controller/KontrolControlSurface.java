// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.kontrol.controller;

import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import com.ktemkin.controller.ni.core.AbstractNIHostInterop;
import com.ktemkin.controller.ni.core.INIEventHandler;
import com.ktemkin.controller.ni.kontrol.KontrolConfiguration;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.hardware.BindType;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.IMidiOutput;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.utils.ButtonEvent;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * The Komplete Kontrol MkII control surface.
 *
 * @author Kate Temkin
 * @author Jürgen Moßgraber
 */
public class KontrolControlSurface extends CommonUIControlSurface<KontrolConfiguration> implements INIEventHandler {
    /**
     * Command to initialize the protocol handshake (and acknowledge).
     */
    public static final int CMD_HELLO = 0x01;
    /**
     * Command to stop the protocol.
     */
    public static final int CMD_GOODBYE = 0x02;

    /**
     * The play button.
     */
    public static final int KONTROL_PLAY = 0x10;
    /**
     * The restart button (Shift+Play). No LED.
     */
    public static final int KONTROL_RESTART = 0x11;
    /**
     * The record button.
     */
    public static final int KONTROL_RECORD = 0x12;
    /**
     * The count-in button (Shift+Rec).
     */
    public static final int KONTROL_COUNT_IN = 0x13;
    /**
     * The stop button.
     */
    public static final int KONTROL_STOP = 0x14;
    /**
     * The clear button.
     */
    public static final int KONTROL_CLEAR = 0x15;
    /**
     * The loop button.
     */
    public static final int KONTROL_LOOP = 0x16;
    /**
     * The metro button.
     */
    public static final int KONTROL_METRO = 0x17;
    /**
     * The tempo button. No LED.
     */
    public static final int KONTROL_TAP_TEMPO = 0x18;

    /**
     * The undo button.
     */
    public static final int KONTROL_UNDO = 0x20;
    /**
     * The redo button (Shift+Undo).
     */
    public static final int KONTROL_REDO = 0x21;
    /**
     * The quantize button.
     */
    public static final int KONTROL_QUANTIZE = 0x22;
    /**
     * The auto button.
     */
    public static final int KONTROL_AUTOMATION = 0x23;

    /**
     * Track navigation.
     */
    public static final int KONTROL_NAVIGATE_TRACKS = 0x30;
    /**
     * Track bank navigation.
     */
    public static final int KONTROL_NAVIGATE_BANKS = 0x31;
    /**
     * Clip navigation.
     */
    public static final int KONTROL_NAVIGATE_CLIPS = 0x32;

    /**
     * Transport navigation.
     */
    public static final int KONTROL_NAVIGATE_MOVE_TRANSPORT = 0x34;
    /**
     * Loop navigation.
     */
    public static final int KONTROL_NAVIGATE_MOVE_LOOP = 0x35;

    /**
     * Track available (actually the type the track, see TrackType).
     */
    public static final int KONTROL_TRACK_AVAILABLE = 0x40;
    /**
     * Name of the Komplete plugin ID on the track, if exists.
     */
    public static final int KONTROL_TRACK_INSTANCE = 0x41;
    /**
     * Select a track.
     */
    public static final int KONTROL_TRACK_SELECTED = 0x42;
    /**
     * Mute a track.
     */
    public static final int KONTROL_TRACK_MUTE = 0x43;
    /**
     * Solo a track.
     */
    public static final int KONTROL_TRACK_SOLO = 0x44;
    /**
     * Arm a track.
     */
    public static final int KONTROL_TRACK_RECARM = 0x45;
    /**
     * Volume of a track.
     */
    public static final int KONTROL_TRACK_VOLUME_TEXT = 0x46;
    /**
     * Panorama of a track.
     */
    public static final int KONTROL_TRACK_PAN_TEXT = 0x47;
    /**
     * Name of a track.
     */
    public static final int KONTROL_TRACK_NAME = 0x48;
    /**
     * VU of a track.
     */
    public static final int KONTROL_TRACK_VU = 0x49;
    /**
     * Tracl muted by solo.
     */
    public static final int KONTROL_TRACK_MUTED_BY_SOLO = 0x4A;

    /**
     * Change the volume of a track 0x50 - 0x57.
     */
    public static final int KONTROL_TRACK_VOLUME = 0x50;
    /**
     * Change the panorama of a track 0x58 - 0x5F.
     */
    public static final int KONTROL_TRACK_PAN = 0x58;

    /**
     * Play the currently selected clip.
     */
    public static final int KONTROL_PLAY_SELECTED_CLIP = 0x60;
    /**
     * Stop the clip playing on the currently selected track.
     */
    public static final int KONTROL_STOP_CLIP = 0x61;
    /**
     * Start the currently selected scene.
     */
    public static final int KONTROL_PLAY_SCENE = 0x62;
    /**
     * Record Session button pressed.
     */
    public static final int KONTROL_RECORD_SESSION = 0x63;
    /**
     * Increase/decrease volume of selected track.
     */
    public static final int KONTROL_CHANGE_SELECTED_TRACK_VOLUME = 0x64;
    /**
     * Increase/decrease pan of selected track.
     */
    public static final int KONTROL_CHANGE_SELECTED_TRACK_PAN = 0x65;
    /**
     * Toggle mute of the selected track / Selected track muted.
     */
    public static final int KONTROL_SELECTED_TRACK_MUTE = 0x66;
    /**
     * Toggle solo of the selected track / Selected track soloed.
     */
    public static final int KONTROL_SELECTED_TRACK_SOLO = 0x67;
    /**
     * Selected track available.
     */
    public static final int KONTROL_SELECTED_TRACK_AVAILABLE = 0x68;
    /**
     * Selected track muted by solo.
     */
    public static final int KONTROL_SELECTED_TRACK_MUTED_BY_SOLO = 0x69;


    private final int requiredVersion;
    private final ValueCache valueCache = new ValueCache();
    private final Object cacheLock = new Object();
    private final Object handshakeLock = new Object();
    /**
     * The color for each button.
     */
    // FIXME(ktemkin): find an elegant way to get this bound
    private final ColorEx[] colorForButton = new ColorEx[256];
    /**
     * The color for each of the keystrip keys, as raw Kontrol colors.
     */
    private final int[] colorForKey = new int[88];
    /**
     * The scale manager for this controller.
     */
    private final Scales scales;
    private int protocolVersion = KontrolProtocol.MAX_VERSION;
    private boolean isConnectedToNIHIA = false;
    private AbstractNIHostInterop niConnection = null;


    /**
     * Constructor.
     *
     * @param host          The host
     * @param colorManager  The color manager
     * @param configuration The configuration
     * @param output        The MIDI output
     * @param input         The MIDI input
     * @param version       The version number of the NIHIA protocol to request
     */
    public KontrolControlSurface(final IHost host, final ColorManager colorManager, final KontrolConfiguration configuration, final IMidiOutput output, final IMidiInput input, final int version, final Scales scales) {
        super(host, colorManager, configuration, new KontrolPseudoPadGrid(colorManager, output, scales), output, input, 800, 300);

        this.requiredVersion = version;
        this.defaultMidiChannel = 15;
        this.scales = scales;


        var padGrid = (KontrolPseudoPadGrid) this.getPadGrid();
        padGrid.setSurface(this);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalShutdown() {
        super.internalShutdown();

        synchronized (this.handshakeLock) {
            // Stop flush
            this.isConnectedToNIHIA = false;

            this.niConnection.shutdown();

            this.sendCommand(KontrolControlSurface.CMD_GOODBYE, 0);
        }
    }


    /**
     * Returns true if the handshake with the Native Instruments Host Integration was successfully
     * executed.
     *
     * @return True if connected to the NIHIA
     */
    public boolean isConnectedToNIHIA() {
        synchronized (this.handshakeLock) {
            return this.isConnectedToNIHIA;
        }
    }


    /**
     * Initialize the handshake with the NIHIA.
     */
    public void initHandshake() {
        this.sendCommand(CMD_HELLO, this.requiredVersion);
    }

    /**
     * Call if the handshake response was successfully received from the NIHIA.
     *
     * @param protocol The protocol version
     */
    public void handshakeSuccess(final int protocol) {
        synchronized (this.handshakeLock) {
            this.setProtocolVersion(protocol);

            // Initial flush of the whole DAW state...
            this.clearCache();

            this.isConnectedToNIHIA = true;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setTrigger(final BindType bindType, final int channel, final int cc, final int value) {
        this.sendCommand(cc, value);
    }


    /**
     * Send a command to the Komplete Kontrol.
     *
     * @param command The command number
     * @param value   The value
     */
    public void sendCommand(final int command, final int value) {
        this.output.sendCCEx(15, command, value);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void clearCache() {
        synchronized (this.cacheLock) {
            this.valueCache.clearCache();
        }

        super.clearCache();
    }

    /**
     * Get the protocol number of the currently connected Komplete Kontrol.
     *
     * @return The protocol number
     */
    public int getProtocolVersion() {
        return this.protocolVersion;
    }


    /**
     * Set the protocol number of the currently connected Komplete Kontrol.
     *
     * @param protocolVersion The protocol number
     */
    public void setProtocolVersion(final int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }


    /**
     * {@inheritDocs}
     */
    @Override
    public void handleButtonEvent(int rawButtonId, ButtonEvent event) {

        this.println(String.format("BUTTON: %d", rawButtonId));

        // Convert our button from a NIHIA message number to a concrete ButtonID.
        var buttonId = this.translateNIHIAButton(rawButtonId);
        var button = this.getButton(buttonId);

        // If we got null, we don't yet handle this button. Abort.
        if (button == null) {
            return;
        }

        // Otherwise, send the event to the button.
        button.trigger(event);
    }


    /**
     * {@inheritDocs}
     */
    @Override
    public void handleKnobEvent(int index, int newValue) {

        this.host.println(String.format("knob event (%d is %d).", index, newValue));

        // ... and if there's a hardware continuous control, trigger it to update.
        final var mode = this.getModeManager().getActive();
        if (mode != null) {
            int delta = newValue >> 27;
            if (!this.isShiftPressed()) {
                delta = delta >> 2;
            } else {
                delta = (delta < 0) ? delta : (delta >> 1);
            }
            if (delta < 0) {
                delta = delta >> 2;
            }

            mode.onKnobValue(index, delta);
        }
    }


    /**
     * {@inheritDocs}
     */
    @Override
    public void handleMainEncoderEvent(long newValue) {
    }


    /**
     * {@inheritDocs}
     */
    @Override
    public void handleOctaveChanged(int newBaseNote) {
        // The Kontrol reports the base note as "middle C", which it counts for the S49
        // as the second key on the keyboard. Adjust to get the key in the right position.
        // We'll also add 9 to account for the start of the 88, which isn't centered.
        // (I think this is what's going on, here.)
        //
        // FIXME(ktemkin): if I can get my hands on other hardware, I'll make this work
        // on the other devices, too.
        this.scales.setStartNote(newBaseNote - 36 + 9);
    }


    /**
     * Adds an NIHIA connection, which can be used for thins like sending LED updates.
     *
     * @param nihiaConnection Our connection abstraction.
     */
    public void addNiConnection(AbstractNIHostInterop nihiaConnection) {
        this.niConnection = nihiaConnection;
    }


    /**
     * Sets the color for the provided button.
     *
     * @param button The button to set the color for.
     * @param color  The color to set the button to; the closest equivalent will be used.
     */
    public void setButtonColor(ButtonID button, ColorEx color) {
        this.colorForButton[button.ordinal()] = color;
    }


    /**
     * Sets the color for the provided key.
     *
     * @param index The key index -- indexed as if on an 88 key keyboard.
     * @param color The color to
     */
    public void setKeyColor(int index, ColorEx color) {
        var colorManager = (KontrolColorManager) this.colorManager;
        this.colorForKey[index] = colorManager.getDeviceColor(color);
    }


    /**
     * Sets the color for the provided key.
     *
     * @param index The key index -- indexed as if on an 88 key keyboard.
     * @param color The color to
     */
    public void setKeyColor(int index, int rawColor) {
        if ((index < 0) || (index > this.colorForKey.length)) {
            return;
        }

        this.colorForKey[index] = rawColor;
    }


    /**
     * Returns the (nearest NI) color index for a given button.
     */
    public byte getColorForButton(ButtonID button) {

        var manager = (KontrolColorManager) this.colorManager;
        var color = this.colorForButton[button.ordinal()];

        // If we don't have a color set, default to OFF.
        if (color == null) {
            return KontrolColorManager.COLOR_BLACK;
        }

        return (byte) manager.getDeviceColor(color);
    }


    /**
     * Sends the LED colors associated set for each button with `setButtonColor`.
     * Does not affect the keybed colors, which are programmed via Keyzones.
     */
    public void flushLights() {
        var colorManager = (KontrolColorManager) this.colorManager;

        if (this.niConnection == null) {
            return;
        }

        // Build a packet of each button color.
        // Note: this can be up to 130; we're truncating it to what we actually use.
        byte[] colors = new byte[42 + this.colorForKey.length];
        ByteBuffer ledBuffer = ByteBuffer.wrap(colors);

        ledBuffer.put(this.getColorForButton(ButtonID.MUTE));
        ledBuffer.put(this.getColorForButton(ButtonID.SOLO));

        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_1));
        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_2));
        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_3));
        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_4));
        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_5));
        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_6));
        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_7));
        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_8));

        ledBuffer.put(this.getColorForButton(ButtonID.LEFT));
        ledBuffer.put(this.getColorForButton(ButtonID.UP));
        ledBuffer.put(this.getColorForButton(ButtonID.RIGHT));
        ledBuffer.put(this.getColorForButton(ButtonID.DOWN));

        // This byte doesn't seem to do anything.
        ledBuffer.put((byte) 0);

        ledBuffer.put(this.getColorForButton(ButtonID.SCALES));
        ledBuffer.put(this.getColorForButton(ButtonID.REPEAT));
        ledBuffer.put(this.getColorForButton(ButtonID.SCENE1));
        ledBuffer.put(this.getColorForButton(ButtonID.UNDO));
        ledBuffer.put(this.getColorForButton(ButtonID.QUANTIZE));
        ledBuffer.put(this.getColorForButton(ButtonID.AUTOMATION));
        ledBuffer.put(this.getColorForButton(ButtonID.CLIP));
        ledBuffer.put(this.getColorForButton(ButtonID.PAGE_LEFT));
        ledBuffer.put(this.getColorForButton(ButtonID.TRACK));
        ledBuffer.put(this.getColorForButton(ButtonID.LOOP));
        ledBuffer.put(this.getColorForButton(ButtonID.METRONOME));
        ledBuffer.put(this.getColorForButton(ButtonID.TAP_TEMPO));
        ledBuffer.put(this.getColorForButton(ButtonID.PAGE_RIGHT));
        ledBuffer.put(this.getColorForButton(ButtonID.CONFIGURE_PITCHBEND));
        ledBuffer.put(this.getColorForButton(ButtonID.PLAY));
        ledBuffer.put(this.getColorForButton(ButtonID.RECORD));
        ledBuffer.put(this.getColorForButton(ButtonID.STOP));
        ledBuffer.put(this.getColorForButton(ButtonID.BANK_LEFT));
        ledBuffer.put(this.getColorForButton(ButtonID.BANK_RIGHT));
        ledBuffer.put(this.getColorForButton(ButtonID.DELETE));
        ledBuffer.put(this.getColorForButton(ButtonID.BROWSE));
        ledBuffer.put(this.getColorForButton(ButtonID.DEVICE));

        // These bytes seem to be for alignment; or perhaps have meaning
        // we don't yet know. I'm going to fill them with lime green,
        // so we can see them if they do wind up doing something.
        ledBuffer.put(new byte[]{33, 33, 33, 33, 33});

        // Scan out the colors for each of our key-strip keys.
        for (var color : this.colorForKey) {
            ledBuffer.put((byte) color);
        }

        this.niConnection.setLedColors(colors);
    }


    /**
     * Translates an NIHIA button index to a local ButtonID.
     */
    public ButtonID translateNIHIAButton(int rawButton) {
        switch (rawButton) {
            case 0x00 -> {
                return ButtonID.ROW1_5;              // Above the screen.
            }
            case 0x01 -> {
                return ButtonID.ROW1_6;
            }
            case 0x02 -> {
                return ButtonID.ROW1_7;
            }
            case 0x03 -> {
                return ButtonID.ROW1_8;
            }
            case 0x04 -> {
                return ButtonID.ROW1_1;
            }
            case 0x05 -> {
                return ButtonID.ROW1_2;
            }
            case 0x06 -> {
                return ButtonID.ROW1_3;
            }
            case 0x07 -> {
                return ButtonID.ROW1_4;
            }
            case 0x0b -> {
                return ButtonID.SCALES;              // Scales
            }
            case 0x0a -> {
                return ButtonID.REPEAT;              // ARP
            }
            case 0x0f -> {
                return ButtonID.SHIFT;               // Shift
            }
            case 0x14 -> {
                return ButtonID.PAGE_LEFT;           // Preset up button
            }
            case 0x16 -> {
                return ButtonID.PAGE_RIGHT;          // Preset down button
            }
            case 0x15 -> {
                return ButtonID.BANK_RIGHT;          // Right arrow, left of screen
            }
            case 0x17 -> {
                return ButtonID.BANK_LEFT;           // Left arrow, left of screen
            }
            case 0x1c -> {
                return ButtonID.TRACK;               // Track
            }
            case 0x1e -> {
                return ButtonID.CONFIGURE_PITCHBEND; // Key mode.
            }
            case 0x21 -> {
                return ButtonID.DEVICE;              // Plugin.
            }
            case 0x22 -> {
                return ButtonID.BROWSE;              // Browser
            }
            case 0x23 -> {
                return ButtonID.SETUP;
            }
            case 0x24 -> {
                return ButtonID.PARAM_PAGE1;         // Instance
            }
            case 0x25 -> {
                return ButtonID.VOLUME;              // Midi; used as 'volume' since we can't get Mixer
            }
            case 0x37 -> {
                return ButtonID.KNOB1_TOUCH;         // Knobs below the screen.
            }
            case 0x36 -> {
                return ButtonID.KNOB2_TOUCH;
            }
            case 0x35 -> {
                return ButtonID.KNOB3_TOUCH;
            }
            case 0x34 -> {
                return ButtonID.KNOB4_TOUCH;
            }
            case 0x33 -> {
                return ButtonID.KNOB5_TOUCH;
            }
            case 0x32 -> {
                return ButtonID.KNOB6_TOUCH;
            }
            case 0x31 -> {
                return ButtonID.KNOB7_TOUCH;
            }
            case 0x30 -> {
                return ButtonID.KNOB8_TOUCH;
            }

            // Print a message for any unknown buttons, so we can implement them later.
            default -> {
                this.host.println(String.format("Unknown NIHIA button 0x%x pressed!", rawButton));
                return null;
            }
        }

    }


    /**
     * Caches the values of the system exclusive values.
     */
    private static class ValueCache {
        private final List<List<int[]>> cache = new ArrayList<>(8);


        /**
         * Constructor.
         */
        public ValueCache() {
            this.clearCache();
        }


        /**
         * Clear the cache.
         */
        public final void clearCache() {
            for (int i = 0; i < 8; i++) {
                final List<int[]> e = new ArrayList<>(128);
                for (int j = 0; j < 128; j++)
                    e.add(new int[0]);
                this.cache.add(e);
            }
        }


        /**
         * KontrolProtocolControllerSetup.java
         * Stores the value and data in the cache for the track and stateID.
         *
         * @param track   The track number
         * @param stateID The state id
         * @param value   The value
         * @param data    Further data
         * @return False if cache was updated otherwise the given value and data are already stored
         */
        public boolean store(final int track, final int stateID, final int value, final int[] data) {
            final List<int[]> trackItem = this.cache.get(track);
            final int[] values = trackItem.get(stateID);

            final int[] newValues = new int[1 + data.length];
            newValues[0] = value;
            if (data.length > 0)
                System.arraycopy(data, 0, newValues, 1, data.length);

            if (Arrays.equals(values, newValues))
                return true;

            trackItem.set(stateID, newValues);
            return false;
        }
    }


}
