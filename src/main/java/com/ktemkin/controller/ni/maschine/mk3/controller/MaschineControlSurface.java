// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.maschine.mk3.controller;

import com.ktemkin.controller.ni.core.AbstractNIHostInterop;
import com.ktemkin.controller.ni.core.INIEventHandler;
import com.ktemkin.controller.ni.kontrol.mkii.controller.KontrolProtocolColorManager;
import com.ktemkin.controller.ni.maschine.Maschine;
import com.ktemkin.controller.ni.maschine.core.AbstractMaschineControlSurface;
import com.ktemkin.controller.ni.maschine.core.MaschineColorManager;
import com.ktemkin.controller.ni.maschine.core.controller.MaschinePadGrid;
import com.ktemkin.controller.ni.maschine.mk3.MaschineConfiguration;
import com.ktemkin.controller.ni.maschine.mk3.command.trigger.MaschineStopCommand;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.hardware.BindType;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.IMidiOutput;
import de.mossgrabers.framework.daw.midi.MidiConstants;
import de.mossgrabers.framework.utils.ButtonEvent;

import java.nio.ByteBuffer;


/**
 * The NI Maschine control surface.
 *
 * @author Kate Temkin
 * @author Jürgen Moßgraber
 */
public class MaschineControlSurface extends AbstractMaschineControlSurface<MaschineConfiguration> implements INIEventHandler
{
    // MIDI CC
    public static final int TOUCHSTRIP = 1;
    public static final int TOUCHSTRIP_TOUCH = 2;

    public static final int FOOTSWITCH1_TIP = 3;
    public static final int FOOTSWITCH1_RING = 4;
    public static final int FOOTSWITCH2_TIP = 5;
    public static final int FOOTSWITCH2_RING = 6;

    public static final int ENCODER = 7;
    public static final int ENCODER_PUSH = 8;
    public static final int ENCODER_TOUCH = 9;

    /**
     * Mode buttons are from CC 22 to 29.
     */
    public static final int MODE_BUTTON_1 = 22;
    public static final int MODE_BUTTON_2 = 23;
    public static final int MODE_BUTTON_3 = 24;
    public static final int MODE_BUTTON_4 = 25;
    public static final int MODE_BUTTON_5 = 26;
    public static final int MODE_BUTTON_6 = 27;
    public static final int MODE_BUTTON_7 = 28;
    public static final int MODE_BUTTON_8 = 29;

    public static final int CURSOR_UP = 30;
    public static final int CURSOR_RIGHT = 31;
    public static final int CURSOR_DOWN = 32;
    public static final int CURSOR_LEFT = 33;

    public static final int GROUP = 34;
    public static final int AUTO = 35;
    public static final int LOCK = 36;
    public static final int NOTE_REPEAT = 37;

    public static final int PROJECT = 38;
    public static final int FAVORITES = 39;
    public static final int BROWSER = 40;

    public static final int CHANNEL = 41;
    public static final int ARRANGER = 42;
    public static final int MIXER = 43;

    public static final int VOLUME = 44;
    public static final int PLUGIN = 45;
    public static final int SWING = 46;
    public static final int SAMPLING = 47;
    public static final int TEMPO = 48;

    public static final int PITCH = 49;
    public static final int MOD = 50;
    public static final int PERFORM = 51;
    public static final int NOTES = 52;

    public static final int RESTART = 53;
    public static final int ERASE = 54;
    public static final int TAP_METRO = 55;
    public static final int FOLLOW = 56;
    public static final int PLAY = 57;
    public static final int REC = 58;
    public static final int STOP = 59;

    /**
     * Mode button touch events are from CC 60 to 67.
     */
    public static final int MODE_KNOB_TOUCH_1 = 60;

    /**
     * Mode buttons are from CC 70 to 77.
     */
    public static final int MODE_KNOB_1 = 70;

    public static final int FIXED_VEL = 80;
    public static final int PAD_MODE = 81;
    public static final int KEYBOARD = 82;
    public static final int CHORDS = 84;
    public static final int STEP = 83;
    public static final int SCENE = 85;
    public static final int PATTERN = 86;
    public static final int EVENTS = 87;
    public static final int VARIATION = 88;
    public static final int DUPLICATE = 89;
    public static final int SELECT = 90;
    public static final int SOLO = 91;
    public static final int MUTE = 92;

    /**
     * Banks are from CC 100 to 107.
     */
    public static final int BANK_1 = 100;

    public static final int PAGE_LEFT = 110;
    public static final int PAGE_RIGHT = 111;

    //
    // Specific Maschine Studio controls
    //

    public static final int EDIT_COPY = 112;
    public static final int EDIT_PASTE = 113;
    public static final int EDIT_NOTE = 114;
    public static final int EDIT_NUDGE = 115;
    public static final int EDIT_UNDO = 116;
    public static final int EDIT_REDO = 117;
    public static final int EDIT_QUANTIZE = 118;
    public static final int EDIT_CLEAR = 119;

    public static final int MONITOR_IN1 = 120;
    public static final int MONITOR_IN2 = 121;
    public static final int MONITOR_IN3 = 122;
    public static final int MONITOR_IN4 = 123;
    public static final int MONITOR_MST = 124;
    public static final int MONITOR_GRP = 125;
    public static final int MONITOR_SND = 126;
    public static final int MONITOR_CUE = 127;
    public static final int MONITOR_ENCODER = 10;

    public static final int NAVIGATE_BACK = 11;
    public static final int METRO = 14;
    /**
     * Array that translates pad indices to the MIDI notes that should send.
     */
    // @formatter:off
    private static final int NUM_PADS = 16;
    private static final int PAD_PRESSURE_MIN = 0x3b000000;
    private static final int PAD_PRESSURE_MAX = 0x3fffffff;
    /**
     * The color index for each button.
     */
    // FIXME(ktemkin): find an elegant way to get this bound
    private final ColorEx[] colorForButton = new ColorEx[512];
    private int ribbonValue = -1;
    private AbstractNIHostInterop niConnection;
    // @formatter:on

    /**
     * Tracks whether an individual pad is down,
     * so we know whether to emit NOTE_ON or aftertouch events.
     */
    private final boolean[] padDown = new boolean[NUM_PADS];


    /**
     * Constructor.
     *
     * @param host          The host
     * @param colorManager  The color manager
     * @param maschine      The maschine description
     * @param configuration The configuration
     * @param output        The MIDI output
     * @param input         The MIDI input
     */
    public MaschineControlSurface(final IHost host, final ColorManager colorManager, final Maschine maschine, final MaschineConfiguration configuration, final IMidiOutput output, final IMidiInput input)
    {
        super(host, configuration, colorManager, maschine, output, input, new MaschinePadGrid(colorManager, output), maschine.getWidth(), maschine.getHeight());

        var padGrid = (MaschinePadGrid) this.getPadGrid();
        padGrid.setSurface(this);

    }


    /**
     * Signal that the stop function should not be called on button release.
     */
    public void setStopConsumed()
    {
        ((MaschineStopCommand) this.getButton(ButtonID.STOP).getCommand()).setConsumed();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void flushHardware()
    {
        super.flushHardware();

        ((MaschinePadGrid) this.padGrid).flush();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setTrigger(final BindType bindType, final int channel, final int cc, final int state)
    {
        if (cc == MODE_BUTTON_7)
            this.output.sendNoteEx(channel, cc, state);
        else
            this.output.sendCCEx(channel, cc, state);
    }


    /**
     * Set the display value of the ribbon on the controller.
     *
     * @param value The value to set
     */
    public void setRibbonValue(final int value)
    {
        if (this.ribbonValue == value)
            return;
        this.ribbonValue = value;
        this.output.sendCC(1, value);
    }


    /**
     * Translates an NIHIA button index to a local ButtonID.
     */
    public ButtonID translateNIHIAButton(int rawButton)
    {
        this.host.println(String.format("BUTTON %x%n", rawButton));

        return switch (rawButton) {
            case 0x00 -> ButtonID.ENTER;

            case 0x02 -> ButtonID.UP;
            case 0x03 -> ButtonID.RIGHT;
            case 0x04 -> ButtonID.DOWN;
            case 0x05 -> ButtonID.LEFT;
            case 0x06 -> ButtonID.SHIFT;
            case 0x07 -> ButtonID.ROW1_8;
            case 0x08 -> ButtonID.TRACK_SELECT_1;
            case 0x09 -> ButtonID.TRACK_SELECT_2;
            case 0x0a -> ButtonID.TRACK_SELECT_3;
            case 0x0b -> ButtonID.TRACK_SELECT_4;
            case 0x0c -> ButtonID.TRACK_SELECT_5;
            case 0x0d -> ButtonID.TRACK_SELECT_6;
            case 0x0e -> ButtonID.TRACK_SELECT_7;
            case 0x0f -> ButtonID.TRACK_SELECT_8;
            case 0x10 -> ButtonID.NOTE;
            case 0x11 -> ButtonID.VOLUME;
            case 0x12 -> ButtonID.SWING;
            case 0x13 -> ButtonID.TAP_TEMPO;
            case 0x14 -> ButtonID.REPEAT;
            case 0x15 -> ButtonID.OVERDUB;

            case 0x18 -> ButtonID.DRUM;
            case 0x19 -> ButtonID.SCALES;
            case 0x1a -> ButtonID.LAYOUT;
            case 0x1b -> ButtonID.SEQUENCER;
            case 0x1c -> ButtonID.ACCENT;
            case 0x1d -> ButtonID.F1;
            case 0x1e -> ButtonID.F2;
            case 0x1f -> ButtonID.F3;

            case 0x21 -> ButtonID.F4;
            case 0x22 -> ButtonID.F5;
            case 0x23 -> ButtonID.SELECT;
            case 0x24 -> ButtonID.SOLO;
            case 0x25 -> ButtonID.MUTE;

            case 0x26 -> ButtonID.CONFIGURE_PITCHBEND;
            case 0x27 -> ButtonID.ADD_EFFECT;
            case 0x28 -> ButtonID.USER;
            case 0x29 -> ButtonID.LOOP;
            case 0x2a -> ButtonID.DELETE;
            case 0x2b -> ButtonID.TEMPO_TOUCH;
            case 0x2c -> ButtonID.FOLLOW;
            case 0x2d -> ButtonID.PLAY;
            case 0x2e -> ButtonID.RECORD;
            case 0x2f -> ButtonID.STOP;

            case 0x30 -> ButtonID.CONVERT;
            case 0x31 -> ButtonID.SETUP;
            case 0x32 -> ButtonID.PAGE_RIGHT;
            case 0x33 -> ButtonID.CLIP;
            case 0x34 -> ButtonID.LAYOUT_MIX;
            case 0x35 -> ButtonID.DEVICE;
            case 0x38 -> ButtonID.TRACK;
            case 0x39 -> ButtonID.LAYOUT_ARRANGE;
            case 0x3a -> ButtonID.BROWSE;
            case 0x3b -> ButtonID.PAGE_LEFT;
            case 0x3c -> ButtonID.PROJECT;
            case 0x3d -> ButtonID.AUTOMATION;

            case 0x40 -> ButtonID.ROW1_1;
            case 0x41 -> ButtonID.ROW1_2;
            case 0x42 -> ButtonID.ROW1_3;
            case 0x43 -> ButtonID.ROW1_4;
            case 0x44 -> ButtonID.ROW1_5;
            case 0x45 -> ButtonID.ROW1_6;
            case 0x46 -> ButtonID.ROW1_7;
            case 0x47 -> ButtonID.MASTERTRACK_TOUCH; // Standin for main encoder touch
            case 0x48 -> ButtonID.KNOB8_TOUCH;
            case 0x49 -> ButtonID.KNOB7_TOUCH;
            case 0x4a -> ButtonID.KNOB6_TOUCH;
            case 0x4b -> ButtonID.KNOB5_TOUCH;
            case 0x4c -> ButtonID.KNOB4_TOUCH;
            case 0x4d -> ButtonID.KNOB3_TOUCH;
            case 0x4e -> ButtonID.KNOB2_TOUCH;
            case 0x4f -> ButtonID.KNOB1_TOUCH;

            default -> null;
        };
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void handleButtonEvent(int rawButtonId, ButtonEvent event)
    {

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
     * {@inheritDoc}
     */
    @Override
    public void handleKnobEvent(int rawContinuousId, int newValue)
    {
        this.host.println(String.format("Knob %d has new value %x", rawContinuousId, newValue));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void handleMainEncoderEvent(long newValue)
    {
        this.host.println(String.format("MAIN ENCODER has new value %x", newValue));
    }


    /**
     * Converts a pressure into a MIDI velocity.
     *
     * @param pressure The pressure from a pad.
     */
    protected int pressureToVelocity(final long pressure)
    {
        final double maxPressure = PAD_PRESSURE_MAX - PAD_PRESSURE_MIN;

        final double maxMidiValue = 127;
        final double medianMidiValue = maxMidiValue / 2;

        final double positionOnCurve = pressure / maxPressure;
        final double linearMidiValue = positionOnCurve * 127;

        // FIXME: get this curve factor from our configuration, not manually
        // 0 gives a linear curve; positive values create a convex polynomial with its extrema
        // pointing up; negative with an extrema leveling out to the side.
        final double curveFactor = -0.85;
        final double qValue = medianMidiValue + (curveFactor * medianMidiValue);

        // Compute our curve.
        final double quadComponentA = (2 * (1 - positionOnCurve) * positionOnCurve * qValue);
        final double quadComponentB = (positionOnCurve * positionOnCurve * maxMidiValue);
        final double secondOrderComponent = Math.round(quadComponentA + quadComponentB);

        final double velocity = ((linearMidiValue - secondOrderComponent) + linearMidiValue);

        return (int) velocity;
    }


    @Override
    public void handlePadEvent(int padNumber, long newPressure)
    {
        final int note = MaschinePadGrid.GRID_TO_MIDI[padNumber];

        final long pressure = newPressure - PAD_PRESSURE_MIN;
        final int velocity = Math.max(1, this.pressureToVelocity(pressure));

        //
        // The Maschine doesn't generate MIDI events, so we'll have to generate MIDI events for it.
        //

        // If we have a pressure of 0, this is a NOTE OFF event.
        if (newPressure == 0) {
            this.sendMidiEvent(MidiConstants.CMD_NOTE_OFF, note, 0);
            this.padDown[padNumber] = false;
        }
        // If we have a pressure, and the pad is already down, this is an aftertouch event.
        else if (this.padDown[padNumber]) {
            this.sendMidiEvent(MidiConstants.CMD_POLY_AFTERTOUCH, note, velocity);
        }
        // Otherwise, this is a note-on event.
        else {
            this.sendMidiEvent(MidiConstants.CMD_NOTE_ON, note, velocity);
            this.padDown[padNumber] = true;
        }
    }


    public void addNiConnection(AbstractNIHostInterop nihiaConnection)
    {
        this.niConnection = nihiaConnection;
    }


    /**
     * Returns the (nearest NI) color index for a given button.
     */
    public byte getColorForButton(ButtonID button)
    {

        var manager = (MaschineColorManager) this.colorManager;
        var color = this.colorForButton[button.ordinal()];

        // If we don't have a color set, default to OFF.
        if (color == null) {
            return KontrolProtocolColorManager.COLOR_BLACK;
        }

        return (byte) manager.getNIColor(color);
    }


    /**
     * Sets the color for a given button.
     *
     * @param button The button whose color is to be set.
     * @param color  The color to make that button.
     */
    public void setButtonColor(ButtonID button, ColorEx color)
    {
        this.colorForButton[button.ordinal()] = color;
    }


    /**
     * Flushes the state of the Maschine's lights.
     */
    public void flushLights()
    {
        if (this.niConnection == null) {
            return;
        }

        // Build a packet of each button color.
        byte[] ledColors = new byte[103];
        ByteBuffer ledBuffer = ByteBuffer.wrap(ledColors);

        //  - CHANNEL
        //  - PLUG-IN
        //  - ARRANGER
        //  - MIXER
        //  - BROWSER
        //  - SAMPLING [color]
        //  - LEFT ARROW
        //  - RIGHT ARROW
        //  - FILE
        //  - SETTINGS
        //  - AUTO
        //  - MACRO
        ledBuffer.put(this.getColorForButton(ButtonID.TRACK)); // Channel
        ledBuffer.put(this.getColorForButton(ButtonID.DEVICE)); // Plug-in
        ledBuffer.put(this.getColorForButton(ButtonID.LAYOUT_ARRANGE)); // Arranger
        ledBuffer.put(this.getColorForButton(ButtonID.LAYOUT_MIX)); // Mixer
        ledBuffer.put(this.getColorForButton(ButtonID.BROWSE)); // Browser
        ledBuffer.put(this.getColorForButton(ButtonID.CLIP)); // Sampling
        ledBuffer.put(this.getColorForButton(ButtonID.PAGE_LEFT)); // Left arrow
        ledBuffer.put(this.getColorForButton(ButtonID.PAGE_RIGHT)); // Right arrow
        ledBuffer.put(this.getColorForButton(ButtonID.PROJECT)); // File
        ledBuffer.put(this.getColorForButton(ButtonID.SETUP)); // Settings
        ledBuffer.put(this.getColorForButton(ButtonID.AUTOMATION)); // Auto
        ledBuffer.put(this.getColorForButton(ButtonID.CONVERT)); // Auto


        //  - BUTTON 1 to BUTTON 7
        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_1));
        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_2));
        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_3));
        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_4));
        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_5));
        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_6));
        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_7));
        ledBuffer.put(this.getColorForButton(ButtonID.ROW1_8));

        //  - VOLUME
        //  - SWING
        //  - NOTE REPEAT
        //  - TEMPO
        //  - LOCK
        ledBuffer.put(this.getColorForButton(ButtonID.VOLUME));
        ledBuffer.put(this.getColorForButton(ButtonID.SWING));
        ledBuffer.put(this.getColorForButton(ButtonID.REPEAT));
        ledBuffer.put(this.getColorForButton(ButtonID.TAP_TEMPO)); // Tempo
        ledBuffer.put(this.getColorForButton(ButtonID.OVERDUB)); // Lock


        //  - PITCH
        //  - MOD
        //  - PERFORM
        //  - NOTES
        ledBuffer.put(this.getColorForButton(ButtonID.CONFIGURE_PITCHBEND));
        ledBuffer.put(this.getColorForButton(ButtonID.ADD_EFFECT));
        ledBuffer.put(this.getColorForButton(ButtonID.USER));
        ledBuffer.put(this.getColorForButton(ButtonID.NOTE));

        //  - TRACK 1 to 8 [color]
        ledBuffer.put(this.getColorForButton(ButtonID.TRACK_SELECT_1));
        ledBuffer.put(this.getColorForButton(ButtonID.TRACK_SELECT_2));
        ledBuffer.put(this.getColorForButton(ButtonID.TRACK_SELECT_3));
        ledBuffer.put(this.getColorForButton(ButtonID.TRACK_SELECT_4));
        ledBuffer.put(this.getColorForButton(ButtonID.TRACK_SELECT_5));
        ledBuffer.put(this.getColorForButton(ButtonID.TRACK_SELECT_6));
        ledBuffer.put(this.getColorForButton(ButtonID.TRACK_SELECT_7));
        ledBuffer.put(this.getColorForButton(ButtonID.TRACK_SELECT_8));

        //  - RESTART
        //  - ERASE
        //  - TAP
        //  - FOLLOW
        //  - PLAY
        //  - REC
        //  - STOP
        //  - SHIFT
        ledBuffer.put(this.getColorForButton(ButtonID.LOOP));
        ledBuffer.put(this.getColorForButton(ButtonID.DELETE));
        ledBuffer.put(this.getColorForButton(ButtonID.TEMPO_TOUCH));
        ledBuffer.put(this.getColorForButton(ButtonID.FOLLOW));
        ledBuffer.put(this.getColorForButton(ButtonID.PLAY));
        ledBuffer.put(this.getColorForButton(ButtonID.RECORD));
        ledBuffer.put(this.getColorForButton(ButtonID.STOP));
        ledBuffer.put(this.getColorForButton(ButtonID.SHIFT));


        //  - FIXED VEL
        //  - PAD MODE
        //  - KEYBOARD
        //  - CHORDS
        //  - STEP
        ledBuffer.put(this.getColorForButton(ButtonID.ACCENT));
        ledBuffer.put(this.getColorForButton(ButtonID.DRUM));
        ledBuffer.put(this.getColorForButton(ButtonID.SCALES));
        ledBuffer.put(this.getColorForButton(ButtonID.LAYOUT));
        ledBuffer.put(this.getColorForButton(ButtonID.SEQUENCER));


        //  - SCENE
        //  - PATTERN
        //  - EVENTS
        //  - VARIATION
        //  - DUPLICATE
        //  - SELECT
        //  - SOLO
        //  - MUTE
        ledBuffer.put(this.getColorForButton(ButtonID.F1));
        ledBuffer.put(this.getColorForButton(ButtonID.F2));
        ledBuffer.put(this.getColorForButton(ButtonID.F3));
        ledBuffer.put(this.getColorForButton(ButtonID.F4));
        ledBuffer.put(this.getColorForButton(ButtonID.F5));
        ledBuffer.put(this.getColorForButton(ButtonID.SELECT));
        ledBuffer.put(this.getColorForButton(ButtonID.SOLO));
        ledBuffer.put(this.getColorForButton(ButtonID.MUTE));


        //  - ENCODER TOP
        //  - ENCODER LEFT
        //  - ENCODER RIGHT
        //  - ENCODER BOTTOM
        ledBuffer.put(this.getColorForButton(ButtonID.UP));
        ledBuffer.put(this.getColorForButton(ButtonID.LEFT));
        ledBuffer.put(this.getColorForButton(ButtonID.RIGHT));
        ledBuffer.put(this.getColorForButton(ButtonID.DOWN));

        //  - TOUCHSTRIP SEGMENTS 1 to 25
        for (int i = 0; i < 25; ++i) {
            ledBuffer.put(this.getColorForButton(ButtonID.get(ButtonID.MORE_PADS1, i)));
        }

        //  - PADS FROM TOP 1 to 16
        for (int i = 0; i < 16; ++i) {
            ledBuffer.put(this.getColorForButton(ButtonID.get(ButtonID.PAD1, i)));
        }

        this.niConnection.setLedColors(ledColors);
    }
}