// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.kontrol.mkii;

import com.ktemkin.controller.ni.core.AbstractNIHostInterop;
import com.ktemkin.controller.ni.core.NIGraphicDisplay;
import com.ktemkin.controller.ni.kontrol.mkii.command.trigger.StartClipOrSceneCommand;
import com.ktemkin.controller.ni.kontrol.mkii.controller.KontrolProtocolColorManager;
import com.ktemkin.controller.ni.kontrol.mkii.controller.KontrolProtocolControlSurface;
import com.ktemkin.controller.ni.kontrol.mkii.mode.ParamsMode;
import com.ktemkin.controller.ni.kontrol.mkii.mode.SendMode;
import com.ktemkin.controller.ni.kontrol.mkii.mode.VolumeMode;
import com.ktemkin.controller.ni.kontrol.mkii.view.ControlView;
import de.mossgrabers.controller.mackie.mcu.controller.MCUDisplay;
import de.mossgrabers.framework.command.continuous.KnobRowModeCommand;
import de.mossgrabers.framework.command.core.ContinuousCommand;
import de.mossgrabers.framework.command.core.NopCommand;
import de.mossgrabers.framework.command.core.TriggerCommand;
import de.mossgrabers.framework.command.trigger.ShiftCommand;
import de.mossgrabers.framework.command.trigger.application.DeleteCommand;
import de.mossgrabers.framework.command.trigger.application.RedoCommand;
import de.mossgrabers.framework.command.trigger.application.UndoCommand;
import de.mossgrabers.framework.command.trigger.clip.NewCommand;
import de.mossgrabers.framework.command.trigger.clip.QuantizeCommand;
import de.mossgrabers.framework.command.trigger.clip.StartSceneCommand;
import de.mossgrabers.framework.command.trigger.clip.StopClipCommand;
import de.mossgrabers.framework.command.trigger.mode.ButtonRowModeCommand;
import de.mossgrabers.framework.command.trigger.mode.KnobRowTouchModeCommand;
import de.mossgrabers.framework.command.trigger.mode.ModeSelectCommand;
import de.mossgrabers.framework.command.trigger.track.MuteCommand;
import de.mossgrabers.framework.command.trigger.track.SoloCommand;
import de.mossgrabers.framework.command.trigger.transport.*;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.AbstractControllerSetup;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.ContinuousID;
import de.mossgrabers.framework.controller.ISetupFactory;
import de.mossgrabers.framework.controller.hardware.BindType;
import de.mossgrabers.framework.controller.hardware.IHwButton;
import de.mossgrabers.framework.controller.hardware.IHwRelativeKnob;
import de.mossgrabers.framework.controller.valuechanger.RelativeEncoding;
import de.mossgrabers.framework.controller.valuechanger.TwosComplementValueChanger;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.ITransport;
import de.mossgrabers.framework.daw.ModelSetup;
import de.mossgrabers.framework.daw.constants.DeviceID;
import de.mossgrabers.framework.daw.data.ICursorTrack;
import de.mossgrabers.framework.daw.data.ISlot;
import de.mossgrabers.framework.daw.data.ISpecificDevice;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ISceneBank;
import de.mossgrabers.framework.daw.data.bank.ISlotBank;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.daw.midi.IMidiAccess;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.IMidiOutput;
import de.mossgrabers.framework.featuregroup.IMode;
import de.mossgrabers.framework.featuregroup.ModeManager;
import de.mossgrabers.framework.featuregroup.ViewManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.utils.FrameworkException;
import de.mossgrabers.framework.utils.LatestTaskExecutor;
import de.mossgrabers.framework.utils.OperatingSystem;
import de.mossgrabers.framework.view.Views;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;


/**
 * Setup for the Komplete Kontrol NIHIA protocol.
 *
 * @author Jürgen Moßgraber
 * @author Kate Temkin
 */
public class KontrolProtocolControllerSetup extends AbstractControllerSetup<KontrolProtocolControlSurface, KontrolProtocolConfiguration> {
    // TODO(ktemkin): support the more-keys models, too
    private static final int DEVICE_ID = 0x1610;

    private final int version;

    private final Object navigateLock = new Object();
    private final LatestTaskExecutor slotScrollExecutor = new LatestTaskExecutor();
    private long lastEdit;


    /**
     * Constructor.
     *
     * @param host             The DAW host
     * @param factory          The factory
     * @param globalSettings   The global settings
     * @param documentSettings The document (project) specific settings
     * @param version          The version number of the NIHIA protocol to support
     */
    public KontrolProtocolControllerSetup(final IHost host, final ISetupFactory factory, final ISettingsUI globalSettings, final ISettingsUI documentSettings, final int version) {
        super(factory, host, globalSettings, documentSettings);

        this.version = version;
        this.colorManager = new KontrolProtocolColorManager();
        this.valueChanger = new TwosComplementValueChanger(1024, 4);
        this.configuration = new KontrolProtocolConfiguration(host, this.valueChanger, factory.getArpeggiatorModes());

        // Create a global NI host interop in the background.
        // We don't need to hold on to this -- just having it created once is enough to allow serial autodetection to work later.
        try {
            AbstractNIHostInterop.createInterop(DEVICE_ID, "", null, null, false);
        } catch (IOException ex) {
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        if (OperatingSystem.get() == OperatingSystem.LINUX)
            throw new FrameworkException("Komplete Kontrol MkII is not supported on Linux since there is no Native Instruments DAW Integration Host.");

        super.init();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() {
        final KontrolProtocolControlSurface surface = this.getSurface();

        surface.updateButtonLights();


        super.flush();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void createScales() {
        this.scales = new Scales(this.valueChanger, 0, 128, 128, 1);
        this.scales.setChromatic(true);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void createSurface() {
        final IMidiAccess midiAccess = this.factory.createMidiAccess();
        final IMidiOutput output = midiAccess.createOutput();
        final IMidiInput pianoInput = midiAccess.createInput(1, "Keyboard", "8?????" /* Note off */,
                "9?????" /* Note on */, "B?????" /* Sustain pedal + Modulation + Strip */,
                "D?????" /* Channel After-touch */, "E?????" /* Pitch-bend */);
        final KontrolProtocolControlSurface surface = new KontrolProtocolControlSurface(this.host, this.colorManager, this.configuration, output, midiAccess.createInput(null), this.version);
        this.surfaces.add(surface);

        surface.addPianoKeyboard(49, pianoInput, true);

        try {
            // Currently, if there's more than one of the same device connected, the NIServices
            // always just return information about the first one. We _should_ be able to work around that by
            // fetching the USB serial number, but this requires us to match to the USB device, even if we don't
            // open it. That seems messy, but that may be necessary.
            //
            // For now, we'll just let the user specify which serial they want to talk to in settings.
            // If they don't provide one, we'll try to figure out what serial is around.
            //
            String serial = this.configuration.getSerialForDisplay();

            // If we have a single device of this type, just use it.
            if ((serial == null) || serial.isEmpty()) {
                serial = AbstractNIHostInterop.getSingleDeviceSerial(DEVICE_ID);
                if (serial != null) {
                    this.host.println("Auto-detected serial " + serial + ".");
                }
            }

            if ((serial != null) && !serial.isEmpty()) {
                final var nihiaConnection = AbstractNIHostInterop.createInterop(DEVICE_ID, serial, surface, host, false);
                surface.addNiConnection(nihiaConnection);

                final NIGraphicDisplay display = new NIGraphicDisplay(this.host, this.valueChanger.getUpperBound(), this.configuration, nihiaConnection);
                surface.addGraphicsDisplay(display);

                this.host.println("Graphics display set up on Kontrol with serial " + serial + ".");
            } else {
                this.host.error("Couldn't auto-detect the device serial corresponding to this controller. Try providing it in settings.");
            }
        } catch (IOException ex) {
            this.host.error("Couldn't create NI service connection. Falling back to MCU display.");
            this.host.error(ex.toString());
            // If we can't create a graphics display, don't panic: we'll fall back to MCU display.
        }

        // If we don't have a grahpic display, but we can create an MCU text one, do so.
        if ((surface.getGraphicsDisplay() == null)) {
            final MCUDisplay display = new MCUDisplay(this.host, output, true, false, false);
            display.setCenterNotification(false);
            surface.addTextDisplay(display);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void createModel() {
        final ModelSetup ms = new ModelSetup();
        ms.enableMainDrumDevice(false);
        ms.enableDevice(DeviceID.NI_KOMPLETE);
        ms.setHasFullFlatTrackList(true);
        ms.setNumFilterColumnEntries(0);
        ms.setNumResults(0);
        ms.setNumDeviceLayers(0);
        ms.setNumDrumPadLayers(0);
        ms.setNumMarkers(0);
        this.model = this.factory.createModel(this.configuration, this.colorManager, this.valueChanger, this.scales, ms);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void createViews() {
        final KontrolProtocolControlSurface surface = this.getSurface();
        final ViewManager viewManager = surface.getViewManager();
        viewManager.register(Views.CONTROL, new ControlView(surface, this.model));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void createModes() {
        final KontrolProtocolControlSurface surface = this.getSurface();
        final ModeManager modeManager = surface.getModeManager();

        final List<ContinuousID> controls = ContinuousID.createSequentialList(ContinuousID.KNOB1, 8);

        modeManager.register(Modes.VOLUME, new VolumeMode(surface, this.model));
        modeManager.register(Modes.DEVICE_PARAMS, new ParamsMode(surface, this.model));

        // FIXME: register modes for the various sends
        modeManager.register(Modes.SEND, new SendMode(0, surface, this.model));

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void createObservers() {
        super.createObservers();

        this.configuration.registerDeactivatedItemsHandler(this.model);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerTriggerCommands() {
        final KontrolProtocolControlSurface surface = this.getSurface();
        final ITransport t = this.model.getTransport();

        this.addButton(ButtonID.SHIFT, "Shift", new ShiftCommand<KontrolProtocolControlSurface, KontrolProtocolConfiguration>(this.model, surface));

        this.addButton(ButtonID.PLAY, "Play", new PlayCommand<>(this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_PLAY, t::isPlaying);
        this.addButton(ButtonID.NEW, "Shift+\nPlay", new NewCommand<>(this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_RESTART);
        final ConfiguredRecordCommand<KontrolProtocolControlSurface, KontrolProtocolConfiguration> recordCommand = new ConfiguredRecordCommand<>(false, this.model, surface);
        this.addButton(ButtonID.RECORD, "Record", recordCommand, 15, KontrolProtocolControlSurface.KONTROL_RECORD, (BooleanSupplier) recordCommand::isLit);
        final ConfiguredRecordCommand<KontrolProtocolControlSurface, KontrolProtocolConfiguration> shiftedRecordCommand = new ConfiguredRecordCommand<>(true, this.model, surface);
        this.addButton(ButtonID.REC_ARM, "Shift+\nRecord", shiftedRecordCommand, 15, KontrolProtocolControlSurface.KONTROL_COUNT_IN, (BooleanSupplier) shiftedRecordCommand::isLit);
        this.addButton(ButtonID.STOP, "Stop", new StopCommand<>(this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_STOP, () -> !t.isPlaying());

        this.addButton(ButtonID.LOOP, "Loop", new ToggleLoopCommand<>(this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_LOOP, t::isLoop);
        this.addButton(ButtonID.METRONOME, "Metronome", new MetronomeCommand<>(this.model, surface, false), 15, KontrolProtocolControlSurface.KONTROL_METRO, t::isMetronomeOn);
        this.addButton(ButtonID.TAP_TEMPO, "Tempo", new TapTempoCommand<>(this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_TAP_TEMPO);

        // Note: Since there is no pressed-state with this device, in the simulator-GUI the
        // following buttons are always on
        this.addButton(ButtonID.UNDO, "Undo", new UndoCommand<>(this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_UNDO, () -> this.model.getApplication().canUndo());
        this.addButton(ButtonID.REDO, "Redo", new RedoCommand<>(this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_REDO, () -> this.model.getApplication().canRedo());
        this.addButton(ButtonID.QUANTIZE, "Quantize", new QuantizeCommand<>(this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_QUANTIZE, () -> true);
        this.addButton(ButtonID.AUTOMATION, "Automation", new WriteArrangerAutomationCommand<>(this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_AUTOMATION, t::isWritingArrangerAutomation);

        this.addButton(ButtonID.DELETE, "Clear", new DeleteCommand<>(this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_CLEAR, () -> true);

        this.addButton(ButtonID.CLIP, "Start Clip", new StartClipOrSceneCommand(this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_PLAY_SELECTED_CLIP);
        this.addButton(ButtonID.STOP_CLIP, "Stop Clip", new StopClipCommand<>(this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_STOP_CLIP);
        // Not implemented in NIHIA
        this.addButton(ButtonID.SCENE1, "Play Scene", new StartSceneCommand<>(this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_PLAY_SCENE);

        // KONTROL_RECORD_SESSION - Not implemented in NIHIA

        this.addButton(ButtonID.MUTE, "Mute", new MuteCommand<>(this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_SELECTED_TRACK_MUTE, () -> {
            final ITrackBank tb = this.model.getCurrentTrackBank();
            final Optional<ITrack> selectedTrack = tb.getSelectedItem();
            return selectedTrack.isPresent() && selectedTrack.get().isMute() ? 1 : 0;
        });
        this.addButton(ButtonID.SOLO, "Solo", new SoloCommand<>(this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_SELECTED_TRACK_SOLO, () -> {
            final ITrackBank tb = this.model.getCurrentTrackBank();
            final Optional<ITrack> selectedTrack = tb.getSelectedItem();
            return selectedTrack.isPresent() && selectedTrack.get().isSolo() ? 1 : 0;
        });

        this.addButtons(surface, 0, 8, ButtonID.ROW_SELECT_1, "Select", (event, index) -> {
            if (event == ButtonEvent.DOWN)
                this.model.getCurrentTrackBank().getItem(index).selectOrExpandGroup();
        }, 15, KontrolProtocolControlSurface.KONTROL_TRACK_SELECTED, index -> this.model.getTrackBank().getItem(index).isSelected() ? 1 : 0);

        this.addButton(ButtonID.F1, "", NopCommand.INSTANCE, 15, KontrolProtocolControlSurface.KONTROL_SELECTED_TRACK_AVAILABLE);
        this.addButton(ButtonID.F2, "", NopCommand.INSTANCE, 15, KontrolProtocolControlSurface.KONTROL_SELECTED_TRACK_MUTED_BY_SOLO);

        // Rightmost set: mode buttons.
        //this.addButton(ButtonID.BROWSE, "Browser", new BrowserCommand<KontrolProtocolControlSurface,KontrolProtocolConfiguration>(this.model, surface));
        this.addButton(ButtonID.PARAM_PAGE1, "Instance", new ModeSelectCommand<KontrolProtocolControlSurface, KontrolProtocolConfiguration>(this.model, surface, Modes.DEVICE_PARAMS));
        this.addButton(ButtonID.VOLUME, "Midi", new ModeSelectCommand<KontrolProtocolControlSurface, KontrolProtocolConfiguration>(this.model, surface, Modes.VOLUME));
    }


    /**
     * Adds a non-MIDI button with a simple trigger handler.
     */
    protected void addButton(ButtonID buttonId, String label, TriggerCommand action) {
        this.addButton(this.getSurface(), buttonId, label, action);
    }


    /**
     * Adds a non-MIDI button with a simple trigger handler.
     */
    protected void addButton(KontrolProtocolControlSurface surface, ButtonID buttonId, String label, TriggerCommand action) {
        final IHwButton button = surface.createButton(buttonId, label);
        button.bind(action);

        surface.createLight(null, () -> {
            return this.colorManager.getColor(this.getButtonColor(surface, buttonId), buttonId);
        }, color -> surface.setButtonColor(buttonId, color));
    }


    /**
     * {@inheritDocs}
     */
    @Override
    protected void addButton(final ButtonID buttonId, final String label, final TriggerCommand command, final int midiChannel, final int midiControl, final BooleanSupplier supplier) {
        KontrolProtocolControlSurface surface = (KontrolProtocolControlSurface) this.getSurface();

        super.addButton(buttonId, label, command, midiChannel, midiControl, () -> supplier.getAsBoolean() ? KontrolProtocolColorManager.COLOR_WHITE : KontrolProtocolColorManager.COLOR_DARK_GREY);
        surface.createLight(null, () -> {
            return this.colorManager.getColor(this.getButtonColor(surface, buttonId), buttonId);
        }, color -> surface.setButtonColor(buttonId, color));
    }


    /**
     * Create a hardware knob proxy on a controller, which sends relative values, and bind a continuous command to it.
     */
    protected IHwRelativeKnob addRelativeKnob(final KontrolProtocolControlSurface surface, final ContinuousID continuousID, final String label, final ContinuousCommand command) {
        final IHwRelativeKnob knob = surface.createRelativeKnob(continuousID, label, RelativeEncoding.TWOS_COMPLEMENT);
        knob.bind(command);
        return knob;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerContinuousCommands() {
        final KontrolProtocolControlSurface surface = this.getSurface();

        this.addFader(ContinuousID.HELLO, "Hello", surface::handshakeSuccess, BindType.CC, 15, KontrolProtocolControlSurface.CMD_HELLO);

        // Add our modal buttons (the buttons above the screen).
        for (int i = 0; i < 8; ++i) {
            this.addButton(surface, ButtonID.get(ButtonID.ROW2_1, i), "Mode " + Integer.toString(i), new ButtonRowModeCommand<KontrolProtocolControlSurface, KontrolProtocolConfiguration>(0, i, this.model, surface));
        }

        this.addRelativeKnob(ContinuousID.MOVE_TRANSPORT, "Move Transport", value -> this.changeTransportPosition(value, 0), BindType.CC, 15, KontrolProtocolControlSurface.KONTROL_NAVIGATE_MOVE_TRANSPORT);
        this.addRelativeKnob(ContinuousID.MOVE_LOOP, "Move Loop", this::changeLoopPosition, BindType.CC, 15, KontrolProtocolControlSurface.KONTROL_NAVIGATE_MOVE_LOOP);

        // Only on S models
        this.addRelativeKnob(ContinuousID.NAVIGATE_VOLUME, "Navigate Volume", value -> this.changeTransportPosition(value, 1), BindType.CC, 15, KontrolProtocolControlSurface.KONTROL_CHANGE_SELECTED_TRACK_VOLUME);
        this.addRelativeKnob(ContinuousID.NAVIGATE_PAN, "Navigate Pan", value -> this.changeTransportPosition(value, 2), BindType.CC, 15, KontrolProtocolControlSurface.KONTROL_CHANGE_SELECTED_TRACK_PAN);

        for (int i = 0; i < 8; i++) {
            final var index = i;
            final IHwRelativeKnob knob = this.addRelativeKnob(surface, ContinuousID.get(ContinuousID.KNOB1, i), "Knob " + (i + 1), new KnobRowModeCommand<>(index, this.model, surface));

            // Add the touch sensor for the knob.
            this.addButton(surface, ButtonID.get(ButtonID.KNOB1_TOUCH, i), "Knob " + Integer.toString(i), new KnobRowTouchModeCommand<KontrolProtocolControlSurface, KontrolProtocolConfiguration>(i, this.model, surface));
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void addButton(final KontrolProtocolControlSurface surface, final ButtonID buttonID, final String label, final TriggerCommand command, final int midiChannel, final int midiControl, final IntSupplier supplier, final String... colorIds) {
        super.addButton(surface, buttonID, label, (event, velocity) -> {

            // Since there is only a down event from the device, long has no meaning
            if (event == ButtonEvent.LONG)
                return;

            // Add missing UP event
            command.execute(ButtonEvent.DOWN, velocity);
            command.execute(ButtonEvent.UP, velocity);

        }, midiChannel, midiControl, supplier, colorIds);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void layoutControls() {
        final KontrolProtocolControlSurface surface = this.getSurface();

        surface.getButton(ButtonID.PLAY).setBounds(20.25, 149.5, 31.75, 22.75);
        surface.getButton(ButtonID.NEW).setBounds(20.25, 179.5, 31.75, 22.75);
        surface.getButton(ButtonID.RECORD).setBounds(63.0, 149.25, 31.75, 22.75);
        surface.getButton(ButtonID.REC_ARM).setBounds(63.0, 179.25, 31.75, 22.75);
        surface.getButton(ButtonID.STOP).setBounds(105.75, 149.5, 31.75, 22.75);
        surface.getButton(ButtonID.LOOP).setBounds(20.25, 120.5, 31.75, 22.75);
        surface.getButton(ButtonID.METRONOME).setBounds(63.0, 120.5, 31.75, 22.75);
        surface.getButton(ButtonID.TAP_TEMPO).setBounds(105.75, 120.5, 31.75, 22.75);
        surface.getButton(ButtonID.UNDO).setBounds(21.0, 43.0, 31.75, 22.75);
        surface.getButton(ButtonID.REDO).setBounds(21.0, 75.5, 31.75, 22.75);
        surface.getButton(ButtonID.QUANTIZE).setBounds(63.75, 43.0, 31.75, 22.75);
        surface.getButton(ButtonID.AUTOMATION).setBounds(106.5, 43.0, 31.75, 22.75);
        surface.getButton(ButtonID.DELETE).setBounds(225.75, 120.5, 31.75, 22.75);
        surface.getButton(ButtonID.MUTE).setBounds(194.0, 43.0, 24.25, 22.75);
        surface.getButton(ButtonID.SOLO).setBounds(226.25, 43.0, 24.25, 22.75);

        surface.getButton(ButtonID.ROW_SELECT_1).setBounds(276.0, 43.0, 39.75, 16.0);
        surface.getButton(ButtonID.ROW_SELECT_2).setBounds(330.5, 43.0, 39.75, 16.0);
        surface.getButton(ButtonID.ROW_SELECT_3).setBounds(385.0, 43.0, 39.75, 16.0);
        surface.getButton(ButtonID.ROW_SELECT_4).setBounds(439.5, 43.0, 39.75, 16.0);
        surface.getButton(ButtonID.ROW_SELECT_5).setBounds(494.0, 43.0, 39.75, 16.0);
        surface.getButton(ButtonID.ROW_SELECT_6).setBounds(548.5, 43.0, 39.75, 16.0);
        surface.getButton(ButtonID.ROW_SELECT_7).setBounds(602.75, 43.0, 39.75, 16.0);
        surface.getButton(ButtonID.ROW_SELECT_8).setBounds(657.25, 43.0, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW1_1).setBounds (276.0, 67.5, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW1_2).setBounds (330.5, 67.5, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW1_3).setBounds (385.0, 67.5, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW1_4).setBounds (439.5, 67.5, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW1_5).setBounds (494.0, 67.5, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW1_6).setBounds (548.5, 67.5, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW1_7).setBounds (602.75, 67.5, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW1_8).setBounds (657.25, 67.5, 39.75, 16.0);
        surface.getButton(ButtonID.ROW2_1).setBounds(276.0, 92.25, 39.75, 16.0);
        surface.getButton(ButtonID.ROW2_2).setBounds(330.5, 92.25, 39.75, 16.0);
        surface.getButton(ButtonID.ROW2_3).setBounds(385.0, 92.25, 39.75, 16.0);
        surface.getButton(ButtonID.ROW2_4).setBounds(439.5, 92.25, 39.75, 16.0);
        surface.getButton(ButtonID.ROW2_5).setBounds(494.0, 92.25, 39.75, 16.0);
        surface.getButton(ButtonID.ROW2_6).setBounds(548.5, 92.25, 39.75, 16.0);
        surface.getButton(ButtonID.ROW2_7).setBounds(602.75, 92.25, 39.75, 16.0);
        surface.getButton(ButtonID.ROW2_8).setBounds(657.25, 92.25, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW3_1).setBounds (276.0, 116.75, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW3_2).setBounds (330.5, 116.75, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW3_3).setBounds (385.0, 116.75, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW3_4).setBounds (439.5, 116.75, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW3_5).setBounds (494.0, 116.75, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW3_6).setBounds (548.5, 116.75, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW3_7).setBounds (602.75, 116.75, 39.75, 16.0);
        //surface.getButton (ButtonID.ROW3_8).setBounds (657.25, 116.75, 39.75, 16.0);

		/*
        surface.getButton (ButtonID.BANK_LEFT).setBounds (188.5, 78.5, 29.75, 20.5);
        surface.getButton (ButtonID.BANK_RIGHT).setBounds (225.75, 78.5, 29.75, 20.5);
        surface.getButton (ButtonID.MOVE_TRACK_LEFT).setBounds (705.5, 188.5, 29.75, 20.5);
        surface.getButton (ButtonID.MOVE_TRACK_RIGHT).setBounds (751.0, 188.5, 29.75, 20.5);
        surface.getButton (ButtonID.ARROW_UP).setBounds (727.25, 163.25, 29.75, 20.5);
        surface.getButton (ButtonID.ARROW_DOWN).setBounds (727.25, 211.5, 29.75, 20.5);
		*/

        surface.getButton(ButtonID.CLIP).setBounds(512.75, 0.75, 31.75, 22.75);
        surface.getButton(ButtonID.STOP_CLIP).setBounds(550.25, 0.75, 31.75, 22.75);
        surface.getButton(ButtonID.SCENE1).setBounds(588.0, 0.75, 31.75, 22.75);

        surface.getButton(ButtonID.F1).setBounds(637.5, 0.75, 31.75, 22.75);
        surface.getButton(ButtonID.F2).setBounds(675.25, 0.75, 31.75, 22.75);

        surface.getContinuous(ContinuousID.MOVE_TRANSPORT).setBounds(713.5, 40.75, 27.75, 29.75);
        surface.getContinuous(ContinuousID.MOVE_LOOP).setBounds(752.25, 40.75, 27.75, 29.75);
        surface.getContinuous(ContinuousID.NAVIGATE_VOLUME).setBounds(713.5, 80.75, 27.75, 29.75);
        surface.getContinuous(ContinuousID.NAVIGATE_PAN).setBounds(752.25, 80.75, 27.75, 29.75);

        surface.getContinuous(ContinuousID.KNOB1).setBounds(284.0, 143.25, 28.0, 29.25);
        surface.getContinuous(ContinuousID.KNOB2).setBounds(338.25, 143.25, 28.0, 29.25);
        surface.getContinuous(ContinuousID.KNOB3).setBounds(392.5, 143.25, 28.0, 29.25);
        surface.getContinuous(ContinuousID.KNOB4).setBounds(446.75, 143.25, 28.0, 29.25);
        surface.getContinuous(ContinuousID.KNOB5).setBounds(501.25, 143.25, 28.0, 29.25);
        surface.getContinuous(ContinuousID.KNOB6).setBounds(555.5, 143.25, 28.0, 29.25);
        surface.getContinuous(ContinuousID.KNOB7).setBounds(609.75, 143.25, 28.0, 29.25);
        surface.getContinuous(ContinuousID.KNOB8).setBounds(664.25, 143.25, 28.0, 29.25);

        surface.getContinuous(ContinuousID.MODULATION_WHEEL).setBounds(100.0, 222.75, 22.75, 67.5);
        surface.getContinuous(ContinuousID.PITCHBEND_WHEEL).setBounds(65.5, 222.75, 22.75, 67.5);

        surface.getPianoKeyboard().setBounds(162.75, 218.5, 531.5, 79.75);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void startup() {
        final KontrolProtocolControlSurface surface = this.getSurface();
        surface.getViewManager().setActive(Views.CONTROL);
        surface.getModeManager().setActive(Modes.VOLUME);
        surface.initHandshake();
    }


    /**
     * Get the name of an Komplete Kontrol instance on the current track.
     *
     * @return The instance name, which is the actual label of the first parameter (e.g. NIKB01). An
     * empty string if none is present
     */
    private String getKompleteInstance() {
        final ISpecificDevice kkDevice = this.model.getSpecificDevice(DeviceID.NI_KOMPLETE);
        return kkDevice.doesExist() ? kkDevice.getID() : "";
    }


    /**
     * Navigate to the previous or next scene (if any).
     *
     * @param isLeft Select the previous scene if true
     */
    private void navigateScenes(final boolean isLeft) {
        final ISceneBank sceneBank = this.model.getSceneBank();
        if (sceneBank == null)
            return;
        if (isLeft)
            sceneBank.selectPreviousItem();
        else
            sceneBank.selectNextItem();
    }


    /**
     * Navigate to the previous or next clip of the selected track (if any).
     *
     * @param isLeft Select the previous clip if true
     */
    private void navigateClips(final boolean isLeft) {
        final ITrack cursorTrack = this.model.getCursorTrack();
        if (!cursorTrack.doesExist())
            return;
        final ISlotBank slotBank = cursorTrack.getSlotBank();
        if (isLeft)
            slotBank.selectPreviousItem();
        else
            slotBank.selectNextItem();
    }


    /**
     * Navigate to the previous or next track (if any). Contains complex workaround to make sure
     * that the same slot is selected a newly selected track as well.
     *
     * @param isLeft Select the previous track if true
     */
    private void navigateTracks(final boolean isLeft) {
        final ICursorTrack cursorTrack = this.model.getCursorTrack();
        if (!cursorTrack.doesExist()) {
            this.model.getTrackBank().getItem(0).select();
            return;
        }

        final Optional<ISlot> selectedSlot = cursorTrack.getSlotBank().getSelectedItem();
        final int slotIndex = selectedSlot.isPresent() ? selectedSlot.get().getIndex() : -1;

        if (isLeft)
            cursorTrack.selectPrevious();
        else
            cursorTrack.selectNext();

        synchronized (this.navigateLock) {
            this.lastEdit = System.currentTimeMillis();
        }

        this.slotScrollExecutor.execute(() -> selectSlot(slotIndex));
    }


    private void selectSlot(final int slotIndex) {
        if (slotIndex < 0)
            return;

        try {
            Thread.sleep(50);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            return;
        }

        synchronized (this.navigateLock) {
            if (System.currentTimeMillis() - this.lastEdit > 200)
                this.model.getCursorTrack().getSlotBank().getItem(slotIndex).select();
            else
                this.slotScrollExecutor.execute(() -> selectSlot(slotIndex));
        }
    }


    private void changeTransportPosition(final int value, final int mode) {
        final boolean increase = mode == 0 ? value == 1 : value <= 63;
        this.model.getTransport().changePosition(increase, false);
    }


    private void changeLoopPosition(final int value) {
        // Changing of loop position is not possible. Therefore, change position fine grained
        this.model.getTransport().changePosition(value <= 63, true);
    }


    // These are the left/right buttons
    private void moveTrackBank(final ButtonEvent event, final boolean isLeft) {
        if (event != ButtonEvent.DOWN)
            return;
        final IMode activeMode = this.getSurface().getModeManager().getActive();
        if (activeMode == null)
            return;
        if (isLeft)
            activeMode.selectPreviousItemPage();
        else
            activeMode.selectNextItemPage();
    }


    // This is encoder left/right
    private void moveTrack(final ButtonEvent event, final boolean isLeft) {
        if (event != ButtonEvent.DOWN)
            return;

        if (this.getSurface().getModeManager().isActive(Modes.VOLUME)) {
            if (this.configuration.isFlipTrackClipNavigation()) {
                if (this.configuration.isFlipClipSceneNavigation())
                    this.navigateScenes(isLeft);
                else
                    this.navigateClips(isLeft);
            } else
                this.navigateTracks(isLeft);
            return;
        }

        final IMode activeMode = this.getSurface().getModeManager().getActive();
        if (activeMode == null)
            return;
        if (isLeft)
            activeMode.selectPreviousItem();
        else
            activeMode.selectNextItem();
    }


    // This is encoder up/down
    private void moveClips(final ButtonEvent event, final boolean isLeft) {
        if (event != ButtonEvent.DOWN)
            return;

        if (this.getSurface().getModeManager().isActive(Modes.VOLUME)) {
            if (this.configuration.isFlipTrackClipNavigation()) {
                this.navigateTracks(isLeft);
                return;
            }

            if (this.configuration.isFlipClipSceneNavigation())
                this.navigateScenes(isLeft);
            else
                this.navigateClips(isLeft);
            return;
        }

        this.moveTrackBank(event, isLeft);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void exit() {
        this.slotScrollExecutor.shutdown();

        super.exit();
    }
}
