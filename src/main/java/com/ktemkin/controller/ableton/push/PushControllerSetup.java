// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ableton.push;

import com.ktemkin.controller.ableton.push.command.continuous.ConfigurePitchbendCommand;
import com.ktemkin.controller.ableton.push.command.continuous.MastertrackTouchCommand;
import com.ktemkin.controller.ableton.push.command.pitchbend.TouchstripCommand;
import com.ktemkin.controller.ableton.push.command.trigger.*;
import com.ktemkin.controller.ableton.push.controller.Push2Display;
import com.ktemkin.controller.ableton.push.controller.PushColorManager;
import com.ktemkin.controller.ableton.push.controller.PushControlSurface;
import com.ktemkin.controller.ableton.push.view.*;
import com.ktemkin.controller.common.modes.*;
import com.ktemkin.controller.common.modes.device.*;
import com.ktemkin.controller.common.modes.track.*;
import de.mossgrabers.framework.command.aftertouch.AftertouchViewCommand;
import de.mossgrabers.framework.command.continuous.KnobRowModeCommand;
import de.mossgrabers.framework.command.core.PitchbendCommand;
import de.mossgrabers.framework.command.trigger.BrowserCommand;
import de.mossgrabers.framework.command.trigger.Direction;
import de.mossgrabers.framework.command.trigger.FootswitchCommand;
import de.mossgrabers.framework.command.trigger.application.DeleteCommand;
import de.mossgrabers.framework.command.trigger.application.DuplicateCommand;
import de.mossgrabers.framework.command.trigger.application.UndoCommand;
import de.mossgrabers.framework.command.trigger.clip.*;
import de.mossgrabers.framework.command.trigger.device.AddEffectCommand;
import de.mossgrabers.framework.command.trigger.mode.ButtonRowModeCommand;
import de.mossgrabers.framework.command.trigger.mode.KnobRowTouchModeCommand;
import de.mossgrabers.framework.command.trigger.mode.ModeSelectCommand;
import de.mossgrabers.framework.command.trigger.transport.PlayCommand;
import de.mossgrabers.framework.command.trigger.transport.RecordCommand;
import de.mossgrabers.framework.command.trigger.transport.TapTempoCommand;
import de.mossgrabers.framework.command.trigger.view.ViewButtonCommand;
import de.mossgrabers.framework.configuration.AbstractConfiguration;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.AbstractControllerSetup;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.ContinuousID;
import de.mossgrabers.framework.controller.ISetupFactory;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.hardware.BindType;
import de.mossgrabers.framework.controller.hardware.IHwFader;
import de.mossgrabers.framework.controller.hardware.IHwRelativeKnob;
import de.mossgrabers.framework.controller.valuechanger.TwosComplementValueChanger;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.ITransport;
import de.mossgrabers.framework.daw.ModelSetup;
import de.mossgrabers.framework.daw.clip.INoteClip;
import de.mossgrabers.framework.daw.data.ICursorDevice;
import de.mossgrabers.framework.daw.data.ILayer;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.daw.midi.DeviceInquiry;
import de.mossgrabers.framework.daw.midi.IMidiAccess;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.IMidiOutput;
import de.mossgrabers.framework.featuregroup.IMode;
import de.mossgrabers.framework.featuregroup.IView;
import de.mossgrabers.framework.featuregroup.ModeManager;
import de.mossgrabers.framework.featuregroup.ViewManager;
import de.mossgrabers.framework.mode.MasterVolumeMode;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.view.ColorView;
import de.mossgrabers.framework.view.ScenePlayView;
import de.mossgrabers.framework.view.TransposeView;
import de.mossgrabers.framework.view.Views;
import de.mossgrabers.framework.view.sequencer.AbstractSequencerView;
import de.mossgrabers.framework.view.sequencer.ClipLengthView;

import java.util.Optional;


/**
 * Support for the Ableton Push 1 and Push 2 controllers.
 *
 * @author Jürgen Moßgraber
 */
public class PushControllerSetup extends AbstractControllerSetup<PushControlSurface, PushConfiguration> {

    /**
     * Constructor.
     *
     * @param host             The DAW host
     * @param factory          The factory
     * @param globalSettings   The global settings
     * @param documentSettings The document (project) specific settings
     */
    public PushControllerSetup(final IHost host, final ISetupFactory factory, final ISettingsUI globalSettings, final ISettingsUI documentSettings) {
        super(factory, host, globalSettings, documentSettings);

        this.colorManager = new PushColorManager();
        this.valueChanger = new TwosComplementValueChanger(1024, 10);
        this.configuration = new PushConfiguration(host, this.valueChanger, factory.getArpeggiatorModes());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() {
        super.flush();

        final PushControlSurface surface = this.getSurface();

        final PitchbendCommand pitchbendCommand = surface.getContinuous(ContinuousID.TOUCHSTRIP).getPitchbendCommand();
        if (pitchbendCommand != null) {
            pitchbendCommand.updateValue();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void createModel() {
        final ModelSetup ms = new ModelSetup();
        ms.enableDrum64Device();
        ms.setNumFilterColumnEntries(100000); // These are set to a ridiculously large number so we can get all devices at aonce.
        ms.setNumResults(100000);
        ms.setNumSends(4);
        ms.setNumMarkers(8);
        ms.setHasFlatTrackList(false);
        this.model = this.factory.createModel(this.configuration, this.colorManager, this.valueChanger, this.scales, ms);
        this.model.getSceneBank(64);

        final ITrackBank trackBank = this.model.getTrackBank();
        trackBank.setIndication(true);
        trackBank.addSelectionObserver((index, isSelected) -> this.handleTrackChange(isSelected));
        final ITrackBank effectTrackBank = this.model.getEffectTrackBank();
        if (effectTrackBank != null) {
            effectTrackBank.addSelectionObserver((index, isSelected) -> this.handleTrackChange(isSelected));
        }
        this.model.getMasterTrack().addSelectionObserver((index, isSelected) -> {
            final PushControlSurface surface = this.getSurface();
            final ModeManager modeManager = surface.getModeManager();
            if (isSelected) {
                modeManager.setActive(Modes.MASTER);
            } else if (modeManager.isActive(Modes.MASTER)) {
                modeManager.restore();
            }
        });
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void createSurface() {
        final IMidiAccess midiAccess = this.factory.createMidiAccess();
        final IMidiOutput output = midiAccess.createOutput();
        final IMidiInput input = midiAccess.createInput("Pads", "80????" /* Note off */,
                "90????" /* Note on */, "B040??" /* Sustain pedal */);
        final PushControlSurface surface = new PushControlSurface(this.host, this.colorManager, this.configuration, output, input);
        this.surfaces.add(surface);

        surface.addGraphicsDisplay(new Push2Display(this.host, this.valueChanger.getUpperBound(), this.configuration));
        surface.getModeManager().setDefaultID(Modes.TRACK);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void createModes() {
        final PushControlSurface surface = this.getSurface();
        final ModeManager modeManager = surface.getModeManager();

        modeManager.register(Modes.TRACK, new TrackMode(surface, this.model));
        modeManager.register(Modes.TRACK_DETAILS, new TrackDetailsMode(surface, this.model));
        modeManager.register(Modes.VOLUME, new VolumeMode(surface, this.model));
        modeManager.register(Modes.PAN, new PanMode(surface, this.model));
        modeManager.register(Modes.CROSSFADER, new CrossfadeMode(surface, this.model));

        for (int i = 0; i < 8; i++) {
            modeManager.register(Modes.get(Modes.SEND1, i), new SendMode(surface, this.model, i));
        }

        modeManager.register(Modes.MASTER, new MasterMode(surface, this.model, false));
        modeManager.register(Modes.MASTER_TEMP, new MasterMode(surface, this.model, true));

        modeManager.register(Modes.DEVICE_PARAMS, new DeviceParamsMode(surface, this.model));
        modeManager.register(Modes.DEVICE_CHAINS, new DeviceChainsMode(surface, this.model));
        modeManager.register(Modes.DEVICE_LAYER, new DeviceLayerMode(surface, this.model));
        modeManager.register(Modes.DEVICE_LAYER_VOLUME, new DeviceLayerVolumeMode(surface, this.model));
        modeManager.register(Modes.DEVICE_LAYER_PAN, new DeviceLayerPanMode(surface, this.model));

        for (int i = 0; i < 8; i++) {
            modeManager.register(Modes.get(Modes.DEVICE_LAYER_SEND1, i), new DeviceLayerSendMode(surface, this.model, i));
        }

        modeManager.register(Modes.DEVICE_LAYER_DETAILS, new DeviceLayerDetailsMode(surface, this.model));
        modeManager.register(Modes.BROWSER, new DeviceBrowserMode(surface, this.model));

        modeManager.register(Modes.CLIP, new ClipMode(surface, this.model));
        modeManager.register(Modes.NOTE, new NoteMode(surface, this.model));
        modeManager.register(Modes.FRAME, new FrameMode(surface, this.model));

        modeManager.register(Modes.GROOVE, new GrooveMode(surface, this.model));
        modeManager.register(Modes.REC_ARM, new QuantizeMode(surface, this.model));
        modeManager.register(Modes.ACCENT, new AccentMode(surface, this.model));

        modeManager.register(Modes.SCALES, new ScalesMode(surface, this.model));
        modeManager.register(Modes.SCALE_LAYOUT, new ScaleLayoutMode(surface, this.model));
        modeManager.register(Modes.FIXED, new FixedMode(surface, this.model));
        modeManager.register(Modes.RIBBON, new RibbonMode(surface, this.model));
        modeManager.register(Modes.VIEW_SELECT, new NoteViewSelectMode(surface, this.model));

        modeManager.register(Modes.AUTOMATION, new AutomationSelectionMode(surface, this.model));
        modeManager.register(Modes.TRANSPORT, new MetronomeMode(surface, this.model));
        modeManager.register(Modes.MARKERS, new MarkerMode(surface, this.model));
        modeManager.register(Modes.USER, new UserMode(surface, this.model));

        modeManager.register(Modes.SETUP, new SetupMode(surface, this.model));
        modeManager.register(Modes.INFO, new InfoMode(surface, this.model));

        modeManager.register(Modes.SESSION, new SessionMode(surface, this.model));
        modeManager.register(Modes.SESSION_VIEW_SELECT, new SessionViewSelectMode(surface, this.model));

        modeManager.register(Modes.REPEAT_NOTE, new NoteRepeatMode(surface, this.model));
        modeManager.register(Modes.ADD_TRACK, new AddTrackMode(surface, this.model));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void createObservers() {
        ////////////////////////////////////////////////////////////////////
        // Configuration observers

        super.createObservers();

        final PushControlSurface surface = this.getSurface();

        this.configuration.addSettingObserver(PushConfiguration.DISPLAY_BRIGHTNESS, surface::sendDisplayBrightness);
        this.configuration.addSettingObserver(PushConfiguration.LED_BRIGHTNESS, surface::sendLEDBrightness);
        this.configuration.addSettingObserver(PushConfiguration.PAD_SENSITIVITY, () -> {
            surface.sendPadVelocityCurve();
            surface.sendPadThreshold();
        });
        this.configuration.addSettingObserver(PushConfiguration.PAD_GAIN, () -> {
            surface.sendPadVelocityCurve();
            surface.sendPadThreshold();
        });
        this.configuration.addSettingObserver(PushConfiguration.PAD_DYNAMICS, () -> {
            surface.sendPadVelocityCurve();
            surface.sendPadThreshold();
        });

        this.configuration.addSettingObserver(PushConfiguration.RIBBON_MODE, this::updateRibbonMode);
        this.configuration.addSettingObserver(PushConfiguration.RIBBON_MODE_NOTE_REPEAT, this::updateRibbonMode);
        this.configuration.addSettingObserver(AbstractConfiguration.NOTEREPEAT_ACTIVE, this::updateRibbonMode);
        this.configuration.addSettingObserver(PushConfiguration.DEBUG_MODE, () -> {
            final ModeManager modeManager = surface.getModeManager();
            final Modes debugMode = this.configuration.getDebugMode();
            if (modeManager.get(debugMode) != null) {
                modeManager.setActive(debugMode);
            } else {
                this.host.error("Mode " + debugMode + " not registered.");
            }
        });

        this.configuration.addSettingObserver(PushConfiguration.DEBUG_WINDOW, this.getSurface().getGraphicsDisplay()::showDebugWindow);

        this.configuration.addSettingObserver(PushConfiguration.DISPLAY_SCENES_CLIPS, () -> {
            if (Views.isSessionView(this.getSurface().getViewManager().getActiveID())) {
                final ModeManager modeManager = this.getSurface().getModeManager();
                if (modeManager.isActive(Modes.SESSION)) {
                    modeManager.restore();
                } else {
                    modeManager.setActive(Modes.SESSION);
                }
            }
        });

        this.configuration.addSettingObserver(PushConfiguration.SESSION_VIEW, () -> {
            final ViewManager viewManager = this.getSurface().getViewManager();
            if (!Views.isSessionView(viewManager.getActiveID())) {
                return;
            }
            if (this.configuration.isScenesClipViewSelected()) {
                viewManager.setActive(Views.SCENE_PLAY);
            } else {
                viewManager.setActive(Views.SESSION);
            }
        });

        this.configuration.registerDeactivatedItemsHandler(this.model);

        this.configuration.addSettingObserver(PushConfiguration.COLOR_BACKGROUND, this::redraw);
        this.configuration.addSettingObserver(PushConfiguration.COLOR_BORDER, this::redraw);
        this.configuration.addSettingObserver(PushConfiguration.COLOR_TEXT, this::redraw);
        this.configuration.addSettingObserver(PushConfiguration.COLOR_FADER, this::redraw);
        this.configuration.addSettingObserver(PushConfiguration.COLOR_VU, this::redraw);
        this.configuration.addSettingObserver(PushConfiguration.COLOR_EDIT, this::redraw);
        this.configuration.addSettingObserver(PushConfiguration.COLOR_RECORD, this::redraw);
        this.configuration.addSettingObserver(PushConfiguration.COLOR_SOLO, this::redraw);
        this.configuration.addSettingObserver(PushConfiguration.COLOR_MUTE, this::redraw);
        this.configuration.addSettingObserver(PushConfiguration.COLOR_BACKGROUND_DARKER, this::redraw);
        this.configuration.addSettingObserver(PushConfiguration.COLOR_BACKGROUND_LIGHTER, this::redraw);

        this.createScaleObservers(this.configuration);
        this.createNoteRepeatObservers(this.configuration, surface);

        ////////////////////////////////////////////////////////////////////
        // Other observers

        surface.getViewManager().addChangeListener((previousViewId, activeViewId) -> this.onViewChange());

        this.activateBrowserObserver(Modes.BROWSER);
    }


    /**
     * Redraw the Push 2 display.
     */
    public void redraw() {
        final IMode mode = this.getSurface().getModeManager().getActive();
        if (mode != null) {
            mode.updateDisplay();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void createViews() {
        final PushControlSurface surface = this.getSurface();
        final ViewManager viewManager = surface.getViewManager();
        viewManager.register(Views.PLAY, new PlayView(surface, this.model));
        viewManager.register(Views.CHORDS, new ChordsView(surface, this.model));
        viewManager.register(Views.PIANO, new PianoView(surface, this.model));
        viewManager.register(Views.PRG_CHANGE, new PrgChangeView(surface, this.model));
        viewManager.register(Views.CLIP_LENGTH, new ClipLengthView<>(surface, this.model, true));
        viewManager.register(Views.COLOR, new ColorView<>(surface, this.model));

        viewManager.register(Views.SESSION, new SessionView(surface, this.model));
        viewManager.register(Views.SEQUENCER, new SequencerView(surface, this.model));
        viewManager.register(Views.POLY_SEQUENCER, new PolySequencerView(surface, this.model, true));
        viewManager.register(Views.DRUM, new DrumView(surface, this.model));
        viewManager.register(Views.DRUM4, new Drum4View(surface, this.model));
        viewManager.register(Views.DRUM8, new Drum8View(surface, this.model));
        viewManager.register(Views.RAINDROPS, new RaindropsView(surface, this.model));
        viewManager.register(Views.SCENE_PLAY, new ScenePlayView<>(surface, this.model));

        viewManager.register(Views.DRUM64, new Drum64View(surface, this.model));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerTriggerCommands() {
        final PushControlSurface surface = this.getSurface();
        final ViewManager viewManager = surface.getViewManager();
        final ModeManager modeManager = surface.getModeManager();

        final ITransport t = this.model.getTransport();

        this.addButton(ButtonID.PLAY, "Play", new PlayCommand<>(this.model, surface), PushControlSurface.PUSH_BUTTON_PLAY, t::isPlaying, PushColorManager.PUSH_BUTTON_STATE_PLAY_ON, PushColorManager.PUSH_BUTTON_STATE_PLAY_HI);

        this.addButton(ButtonID.RECORD, "Record", new RecordCommand<>(this.model, surface), PushControlSurface.PUSH_BUTTON_RECORD, () -> {

            if (this.isRecordShifted(surface)) {
                return t.isLauncherOverdub() ? 3 : 2;
            }
            return t.isRecording() ? 1 : 0;

        }, PushColorManager.PUSH_BUTTON_STATE_REC_ON, PushColorManager.PUSH_BUTTON_STATE_REC_HI, PushColorManager.PUSH_BUTTON_STATE_OVR_ON, PushColorManager.PUSH_BUTTON_STATE_OVR_HI);

        this.addButton(ButtonID.NEW, "New", new NewCommand<>(this.model, surface), PushControlSurface.PUSH_BUTTON_NEW);
        this.addButton(ButtonID.FIXED_LENGTH, "Fixed Length", new FixedLengthCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_FIXED_LENGTH, () -> modeManager.isActive(Modes.FIXED));
        this.addButton(ButtonID.DUPLICATE, "Duplicate", new DuplicateCommand<>(this.model, surface), PushControlSurface.PUSH_BUTTON_DUPLICATE);
        this.addButton(ButtonID.QUANTIZE, "Quantize", new PushQuantizeCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_QUANTIZE);
        this.addButton(ButtonID.DELETE, "Delete", new DeleteCommand<>(this.model, surface), PushControlSurface.PUSH_BUTTON_DELETE);
        this.addButton(ButtonID.DOUBLE, "Double Loop", new DoubleCommand<>(this.model, surface), PushControlSurface.PUSH_BUTTON_DOUBLE);
        this.addButton(ButtonID.UNDO, "Undo", new UndoCommand<>(this.model, surface), PushControlSurface.PUSH_BUTTON_UNDO, () -> {

            if (surface.isShiftPressed()) {
                if (!this.model.getApplication().canRedo()) {
                    return 0;
                }
            } else {
                if (!this.model.getApplication().canUndo()) {
                    return 0;
                }
            }
            return surface.getButton(ButtonID.UNDO).isPressed() ? 2 : 1;

        }, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON, ColorManager.BUTTON_STATE_HI);

        this.addButton(ButtonID.AUTOMATION, "Automate", new PushAutomationCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_AUTOMATION, () -> {

            if (this.isRecordShifted(surface)) {
                return t.isWritingClipLauncherAutomation() ? 3 : 2;
            }
            return t.isWritingArrangerAutomation() ? 1 : 0;

        }, PushColorManager.PUSH_BUTTON_STATE_REC_ON, PushColorManager.PUSH_BUTTON_STATE_REC_HI, PushColorManager.PUSH_BUTTON_STATE_OVR_ON, PushColorManager.PUSH_BUTTON_STATE_OVR_HI);

        this.addButton(ButtonID.TRACK, "Mix", new TrackCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_TRACK, () -> Modes.isMixMode(modeManager.getActiveID()));
        this.addButton(ButtonID.DEVICE, "Device", new DeviceCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_DEVICE, () -> Modes.isDeviceMode(modeManager.getActiveID()));
        this.addButton(ButtonID.BROWSE, "Browse", new BrowserCommand<>(this.model, surface), PushControlSurface.PUSH_BUTTON_BROWSE, () -> modeManager.isActive(Modes.BROWSER));
        this.addButton(ButtonID.CLIP, "Clip", new ClipCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_CLIP, () -> modeManager.isActive(Modes.CLIP));

        for (int i = 0; i < 8; i++) {
            final ButtonID row1ButtonID = ButtonID.get(ButtonID.ROW1_1, i);
            this.addButton(row1ButtonID, "Row 1: " + (i + 1), new ButtonRowModeCommand<>(0, i, this.model, surface), PushControlSurface.PUSH_BUTTON_ROW1_1 + i, () -> this.getModeColor(row1ButtonID));
            final ButtonID row2ButtonID = ButtonID.get(ButtonID.ROW2_1, i);
            this.addButton(row2ButtonID, "Row 2: " + (i + 1), new ButtonRowModeCommand<>(1, i, this.model, surface), PushControlSurface.PUSH_BUTTON_ROW2_1 + i, () -> this.getModeColor(row2ButtonID));
            final ButtonID sceneButtonID = ButtonID.get(ButtonID.SCENE1, i);
            this.addButton(sceneButtonID, "Scene " + (i + 1), new ViewButtonCommand<>(sceneButtonID, surface), PushControlSurface.PUSH_BUTTON_SCENE1 + 7 - i, () -> this.getButtonColorFromActiveView(sceneButtonID));
        }

        this.addButton(ButtonID.SHIFT, "Shift", new ShiftCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_SHIFT, () -> this.getModeColor(ButtonID.SELECT));
        this.addButton(ButtonID.SELECT, "Select", new SelectCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_SELECT);
        this.addButton(ButtonID.TAP_TEMPO, "Tap Tempo", new TapTempoCommand<>(this.model, surface), PushControlSurface.PUSH_BUTTON_TAP);
        this.addButton(ButtonID.METRONOME, "Metronome", new PushMetronomeCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_METRONOME, t::isMetronomeOn);
        this.addButton(ButtonID.MASTERTRACK, "Master track", new MastertrackCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_MASTER, () -> Modes.isMasterMode(modeManager.getActiveID()));
        this.addButton(ButtonID.PAGE_LEFT, "Page Left", new PageLeftCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_DEVICE_LEFT, () -> {

            if (viewManager.isActive(Views.SESSION)) {
                return this.model.getCurrentTrackBank().canScrollPageBackwards();
            }
            final IView activeView = viewManager.getActive();
            final INoteClip clip = activeView instanceof final AbstractSequencerView<?, ?> sequencerView && !(activeView instanceof ClipLengthView) ? sequencerView.getClip() : null;
            return clip != null && clip.doesExist() && clip.canScrollStepsBackwards();

        }, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);
        this.addButton(ButtonID.PAGE_RIGHT, "Page Right", new PageRightCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_DEVICE_RIGHT, () -> {

            if (viewManager.isActive(Views.SESSION)) {
                return this.model.getCurrentTrackBank().canScrollPageForwards();
            }
            final IView activeView = viewManager.getActive();
            final INoteClip clip = activeView instanceof final AbstractSequencerView<?, ?> sequencerView && !(activeView instanceof ClipLengthView) ? sequencerView.getClip() : null;
            return clip != null && clip.doesExist() && clip.canScrollStepsForwards();

        }, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);

        this.addButton(ButtonID.MUTE, "Mute", new MuteCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_MUTE, this::getMuteState, PushColorManager.PUSH_BUTTON_STATE_MUTE_ON, PushColorManager.PUSH_BUTTON_STATE_MUTE_HI);
        this.addButton(ButtonID.SOLO, "Solo", new SoloCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_SOLO, this::getSoloState, PushColorManager.PUSH_BUTTON_STATE_SOLO_ON, PushColorManager.PUSH_BUTTON_STATE_SOLO_HI);
        this.addButton(ButtonID.SCALES, "Scale", new ScalesCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_SCALES, () -> modeManager.isActive(Modes.SCALES));
        this.addButton(ButtonID.ACCENT, "Accent", new AccentCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_ACCENT, this.configuration::isAccentActive);
        this.addButton(ButtonID.ADD_EFFECT, "Add Device", new AddEffectCommand<>(this.model, surface, ButtonID.SHIFT, null), PushControlSurface.PUSH_BUTTON_ADD_EFFECT);
        this.addButton(ButtonID.ADD_TRACK, "Add Track", new ModeSelectCommand<>(this.model, surface, Modes.ADD_TRACK), PushControlSurface.PUSH_BUTTON_ADD_TRACK);
        this.addButton(ButtonID.NOTE, "Note", new SelectPlayViewCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_NOTE, () -> !Views.isSessionView(viewManager.getActiveID()));

        final PushCursorCommand cursorDownCommand = new PushCursorCommand(Direction.DOWN, this.model, surface);
        this.addButton(ButtonID.ARROW_DOWN, "Down", cursorDownCommand, PushControlSurface.PUSH_BUTTON_DOWN, cursorDownCommand::canScroll, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);
        final PushCursorCommand cursorUpCommand = new PushCursorCommand(Direction.UP, this.model, surface);
        this.addButton(ButtonID.ARROW_UP, "Up", cursorUpCommand, PushControlSurface.PUSH_BUTTON_UP, cursorUpCommand::canScroll, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);
        final PushCursorCommand cursorLeftCommand = new PushCursorCommand(Direction.LEFT, this.model, surface);
        this.addButton(ButtonID.ARROW_LEFT, "Left", cursorLeftCommand, PushControlSurface.PUSH_BUTTON_LEFT, cursorLeftCommand::canScroll, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);
        final PushCursorCommand cursorRightCommand = new PushCursorCommand(Direction.RIGHT, this.model, surface);
        this.addButton(ButtonID.ARROW_RIGHT, "Right", cursorRightCommand, PushControlSurface.PUSH_BUTTON_RIGHT, cursorRightCommand::canScroll, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);

        this.addButton(ButtonID.OCTAVE_DOWN, "Octave Down", new OctaveCommand(false, this.model, surface), PushControlSurface.PUSH_BUTTON_OCTAVE_DOWN, () -> {
            final IView activeView = viewManager.getActive();
            return activeView instanceof final TransposeView transposeView && transposeView.isOctaveDownButtonOn();
        }, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);
        this.addButton(ButtonID.OCTAVE_UP, "Octave Up", new OctaveCommand(true, this.model, surface), PushControlSurface.PUSH_BUTTON_OCTAVE_UP, () -> {
            final IView activeView = viewManager.getActive();
            return activeView instanceof final TransposeView transposeView && transposeView.isOctaveUpButtonOn();
        }, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);

        this.addButton(ButtonID.LAYOUT, "Layout", new LayoutCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_LAYOUT);
        this.addButton(ButtonID.SETUP, "Setup", new SetupCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_SETUP, () -> modeManager.isActive(Modes.SETUP));
        this.addButton(ButtonID.CONVERT, "Convert", new ConvertCommand<>(this.model, surface), PushControlSurface.PUSH_BUTTON_CONVERT, () -> {
            if (!this.model.canConvertClip()) {
                return 0;
            }
            return surface.getButton(ButtonID.CONVERT).isPressed() ? 2 : 1;
        }, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON, ColorManager.BUTTON_STATE_HI);
        this.addButton(ButtonID.USER, "User", new ModeSelectCommand<>(this.model, surface, Modes.USER), PushControlSurface.PUSH_BUTTON_USER_MODE, () -> modeManager.isActive(Modes.USER));

        this.addButton(ButtonID.STOP_CLIP, "Stop Clip", new StopAllClipsCommand<>(this.model, surface), PushControlSurface.PUSH_BUTTON_STOP_CLIP, () -> surface.isPressed(ButtonID.STOP_CLIP), PushColorManager.PUSH_BUTTON_STATE_STOP_ON, PushColorManager.PUSH_BUTTON_STATE_STOP_HI);
        this.addButton(ButtonID.SESSION, "Session", new SelectSessionViewCommand(this.model, surface), PushControlSurface.PUSH_BUTTON_SESSION, () -> Views.isSessionView(viewManager.getActiveID()));
        this.addButton(ButtonID.REPEAT, "Repeat", new FillModeNoteRepeatCommand<>(this.model, surface, true), PushControlSurface.PUSH_BUTTON_REPEAT, this.configuration::isNoteRepeatActive);
        this.addButton(ButtonID.FOOTSWITCH2, "Foot Controller", new FootswitchCommand<>(this.model, surface, 0), PushControlSurface.PUSH_FOOTSWITCH2);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerContinuousCommands() {
        final PushControlSurface surface = this.getSurface();
        final IMidiInput input = surface.getMidiInput();

        for (int i = 0; i < 8; i++) {
            final IHwRelativeKnob knob = this.addRelativeKnob(ContinuousID.get(ContinuousID.KNOB1, i), "Knob " + i, new KnobRowModeCommand<>(i, this.model, surface), PushControlSurface.PUSH_KNOB1 + i);
            knob.bindTouch(new KnobRowTouchModeCommand<>(i, this.model, surface), input, BindType.NOTE, 0, PushControlSurface.PUSH_KNOB1_TOUCH + i);
            knob.setIndexInGroup(i);
        }

        final IHwRelativeKnob knobMaster = this.addRelativeKnob(ContinuousID.MASTER_KNOB, "Master", null, PushControlSurface.PUSH_KNOB9);
        knobMaster.bindTouch(new MastertrackTouchCommand(this.model, surface), input, BindType.NOTE, 0, PushControlSurface.PUSH_KNOB9_TOUCH);
        new MasterVolumeMode<>(surface, this.model, ContinuousID.MASTER_KNOB).onActivate();

        final RasteredKnobCommand tempoCommand = new RasteredKnobCommand(this.model, surface);
        final IHwRelativeKnob knobTempo = this.addRelativeKnob(ContinuousID.TEMPO, "Tempo", tempoCommand, PushControlSurface.PUSH_SMALL_KNOB1);
        knobTempo.bindTouch(tempoCommand, input, BindType.NOTE, 0, PushControlSurface.PUSH_SMALL_KNOB1_TOUCH);

        final PlayPositionKnobCommand playPositionCommand = new PlayPositionKnobCommand(this.model, surface);
        final IHwRelativeKnob knobPlayPosition = this.addRelativeKnob(ContinuousID.PLAY_POSITION, "Play Position", playPositionCommand, PushControlSurface.PUSH_SMALL_KNOB2);
        knobPlayPosition.bindTouch(playPositionCommand, input, BindType.NOTE, 0, PushControlSurface.PUSH_SMALL_KNOB2_TOUCH);

        final ViewManager viewManager = surface.getViewManager();

        final Views[] views =
                {
                        Views.PLAY,
                        Views.PIANO,
                        Views.DRUM,
                        Views.DRUM64
                };
        for (final Views viewID : views) {
            final IView view = viewManager.get(viewID);
            view.registerAftertouchCommand(new AftertouchViewCommand<>(view, this.model, surface));
        }

        final IHwFader touchstrip = this.addFader(ContinuousID.TOUCHSTRIP, "Touchstrip", new TouchstripCommand(this.model, surface));
        touchstrip.bindTouch(new ConfigurePitchbendCommand(this.model, surface), input, BindType.NOTE, 0, PushControlSurface.PUSH_RIBBON_TOUCH);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void layoutControls() {
        final PushControlSurface surface = this.getSurface();

        surface.getButton(ButtonID.PAD1).setBounds(33.25, 141.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD2).setBounds(48.75, 141.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD3).setBounds(64.5, 141.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD4).setBounds(80.0, 141.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD5).setBounds(95.5, 141.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD6).setBounds(110.75, 141.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD7).setBounds(126.75, 141.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD8).setBounds(142.75, 141.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD9).setBounds(33.25, 130.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD10).setBounds(48.75, 130.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD11).setBounds(64.5, 130.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD12).setBounds(80.0, 130.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD13).setBounds(95.5, 130.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD14).setBounds(110.75, 130.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD15).setBounds(126.75, 130.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD16).setBounds(142.75, 130.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD17).setBounds(33.25, 118.25, 12.75, 10.0);
        surface.getButton(ButtonID.PAD18).setBounds(48.75, 118.25, 12.75, 10.0);
        surface.getButton(ButtonID.PAD19).setBounds(64.5, 118.25, 12.75, 10.0);
        surface.getButton(ButtonID.PAD20).setBounds(80.0, 118.25, 12.75, 10.0);
        surface.getButton(ButtonID.PAD21).setBounds(95.5, 118.25, 12.75, 10.0);
        surface.getButton(ButtonID.PAD22).setBounds(110.75, 118.25, 12.75, 10.0);
        surface.getButton(ButtonID.PAD23).setBounds(126.75, 118.25, 12.75, 10.0);
        surface.getButton(ButtonID.PAD24).setBounds(142.75, 118.25, 12.75, 10.0);
        surface.getButton(ButtonID.PAD25).setBounds(33.25, 105.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD26).setBounds(48.75, 105.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD27).setBounds(64.5, 105.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD28).setBounds(80.0, 105.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD29).setBounds(95.5, 105.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD30).setBounds(110.75, 105.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD31).setBounds(126.75, 105.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD32).setBounds(142.75, 105.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD33).setBounds(33.25, 93.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD34).setBounds(48.75, 93.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD35).setBounds(64.5, 93.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD36).setBounds(80.0, 93.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD37).setBounds(95.5, 93.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD38).setBounds(110.75, 93.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD39).setBounds(126.75, 93.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD40).setBounds(142.75, 93.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD41).setBounds(33.25, 82.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD42).setBounds(48.75, 82.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD43).setBounds(64.5, 82.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD44).setBounds(80.0, 82.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD45).setBounds(95.5, 82.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD46).setBounds(110.75, 82.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD47).setBounds(126.75, 82.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD48).setBounds(142.75, 82.0, 12.75, 10.0);
        surface.getButton(ButtonID.PAD49).setBounds(33.25, 70.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD50).setBounds(48.75, 70.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD51).setBounds(64.5, 70.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD52).setBounds(80.0, 70.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD53).setBounds(95.5, 70.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD54).setBounds(110.75, 70.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD55).setBounds(126.75, 70.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD56).setBounds(142.75, 70.75, 12.75, 10.0);
        surface.getButton(ButtonID.PAD57).setBounds(33.25, 59.25, 12.75, 10.0);
        surface.getButton(ButtonID.PAD58).setBounds(48.75, 59.25, 12.75, 10.0);
        surface.getButton(ButtonID.PAD59).setBounds(64.5, 59.25, 12.75, 10.0);
        surface.getButton(ButtonID.PAD60).setBounds(80.0, 59.25, 12.75, 10.0);
        surface.getButton(ButtonID.PAD61).setBounds(95.5, 59.25, 12.75, 10.0);
        surface.getButton(ButtonID.PAD62).setBounds(110.75, 59.25, 12.75, 10.0);
        surface.getButton(ButtonID.PAD63).setBounds(126.75, 59.25, 12.75, 10.0);
        surface.getButton(ButtonID.PAD64).setBounds(142.75, 59.25, 12.75, 10.0);

        surface.getGraphicsDisplay().getHardwareDisplay().setBounds(32.75, 28.0, 123.5, 21.0);

        surface.getButton(ButtonID.PLAY).setBounds(4.75, 142.75, 10.0, 8.5);
        surface.getButton(ButtonID.RECORD).setBounds(4.75, 132.25, 10.0, 8.5);
        surface.getButton(ButtonID.NEW).setBounds(4.75, 121.75, 10.0, 8.5);
        surface.getButton(ButtonID.FIXED_LENGTH).setBounds(4.75, 90.25, 10.0, 8.5);
        surface.getButton(ButtonID.DUPLICATE).setBounds(4.75, 111.25, 10.0, 8.5);
        surface.getButton(ButtonID.QUANTIZE).setBounds(4.75, 79.75, 10.0, 8.5);
        surface.getButton(ButtonID.DELETE).setBounds(4.5, 28.25, 10.0, 10.0);
        surface.getButton(ButtonID.DOUBLE).setBounds(4.75, 69.25, 10.0, 8.5);
        surface.getButton(ButtonID.UNDO).setBounds(4.5, 39.5, 10.0, 10.0);
        surface.getButton(ButtonID.AUTOMATION).setBounds(4.75, 100.75, 10.0, 8.5);
        surface.getButton(ButtonID.TRACK).setBounds(185.5, 30.0, 10.0, 8.75);
        surface.getButton(ButtonID.DEVICE).setBounds(173.5, 30.0, 10.0, 8.75);
        surface.getButton(ButtonID.BROWSE).setBounds(173.5, 40.75, 10.0, 8.75);
        surface.getButton(ButtonID.CLIP).setBounds(185.5, 40.75, 10.0, 8.75);

        surface.getButton(ButtonID.ROW1_1).setBounds(33.5, 51.25, 13.0, 5.5);
        surface.getButton(ButtonID.ROW2_1).setBounds(34.0, 20.25, 13.0, 5.5);
        surface.getButton(ButtonID.SCENE1).setBounds(159.75, 59.25, 10.0, 10.0);
        surface.getButton(ButtonID.ROW1_2).setBounds(49.0, 51.25, 13.0, 5.5);
        surface.getButton(ButtonID.ROW2_2).setBounds(49.5, 20.25, 13.0, 5.5);
        surface.getButton(ButtonID.SCENE2).setBounds(159.75, 70.75, 10.0, 10.0);
        surface.getButton(ButtonID.ROW1_3).setBounds(64.75, 51.25, 13.0, 5.5);
        surface.getButton(ButtonID.ROW2_3).setBounds(65.25, 20.25, 13.0, 5.5);
        surface.getButton(ButtonID.SCENE3).setBounds(159.75, 82.0, 10.0, 10.0);
        surface.getButton(ButtonID.ROW1_4).setBounds(80.25, 51.25, 13.0, 5.5);
        surface.getButton(ButtonID.ROW2_4).setBounds(80.75, 20.25, 13.0, 5.5);
        surface.getButton(ButtonID.SCENE4).setBounds(159.75, 93.75, 10.0, 10.0);
        surface.getButton(ButtonID.ROW1_5).setBounds(95.75, 51.25, 13.0, 5.5);
        surface.getButton(ButtonID.ROW2_5).setBounds(96.25, 20.25, 13.0, 5.5);
        surface.getButton(ButtonID.SCENE5).setBounds(159.75, 105.75, 10.0, 10.0);
        surface.getButton(ButtonID.ROW1_6).setBounds(111.25, 51.25, 13.0, 5.5);
        surface.getButton(ButtonID.ROW2_6).setBounds(111.75, 20.25, 13.0, 5.5);
        surface.getButton(ButtonID.SCENE6).setBounds(159.75, 118.25, 10.0, 10.0);
        surface.getButton(ButtonID.ROW1_7).setBounds(127.0, 51.25, 13.0, 5.5);
        surface.getButton(ButtonID.ROW2_7).setBounds(127.5, 20.25, 13.0, 5.5);
        surface.getButton(ButtonID.SCENE7).setBounds(159.75, 130.0, 10.0, 10.0);
        surface.getButton(ButtonID.ROW1_8).setBounds(142.5, 51.25, 13.0, 5.5);
        surface.getButton(ButtonID.ROW2_8).setBounds(143.0, 20.25, 13.0, 5.5);
        surface.getButton(ButtonID.SCENE8).setBounds(159.75, 141.75, 10.0, 10.0);

        surface.getButton(ButtonID.SHIFT).setBounds(173.5, 145.0, 10.0, 6.0);
        surface.getButton(ButtonID.SELECT).setBounds(185.5, 145.0, 10.0, 6.0);
        surface.getButton(ButtonID.TAP_TEMPO).setBounds(4.5, 20.25, 11.25, 5.5);
        surface.getButton(ButtonID.METRONOME).setBounds(17.5, 20.25, 11.25, 5.5);
        surface.getButton(ButtonID.MASTERTRACK).setBounds(159.75, 51.5, 10.0, 5.0);
        surface.getButton(ButtonID.PAGE_LEFT).setBounds(173.0, 131.0, 10.0, 6.25);
        surface.getButton(ButtonID.PAGE_RIGHT).setBounds(185.75, 131.0, 10.0, 6.25);
        surface.getButton(ButtonID.MUTE).setBounds(4.5, 51.0, 8.25, 5.5);
        surface.getButton(ButtonID.SOLO).setBounds(12.5, 51.0, 8.25, 5.5);
        surface.getButton(ButtonID.SCALES).setBounds(173.5, 105.75, 10.0, 6.25);
        surface.getButton(ButtonID.ACCENT).setBounds(185.5, 93.75, 10.0, 6.25);
        surface.getButton(ButtonID.ADD_EFFECT).setBounds(160.0, 30.0, 10.0, 8.75);
        surface.getButton(ButtonID.ADD_TRACK).setBounds(160.0, 40.75, 10.0, 8.75);
        surface.getButton(ButtonID.NOTE).setBounds(173.5, 113.75, 10.0, 6.25);
        surface.getButton(ButtonID.ARROW_DOWN).setBounds(181.5, 61.75, 6.0, 9.25);
        surface.getButton(ButtonID.ARROW_UP).setBounds(181.5, 51.75, 6.0, 9.25);
        surface.getButton(ButtonID.ARROW_LEFT).setBounds(173.5, 59.0, 7.25, 5.75);
        surface.getButton(ButtonID.ARROW_RIGHT).setBounds(187.75, 58.75, 7.25, 5.75);
        surface.getButton(ButtonID.OCTAVE_DOWN).setBounds(180.25, 137.25, 10.0, 6.25);
        surface.getButton(ButtonID.OCTAVE_UP).setBounds(179.75, 124.5, 10.0, 6.25);
        surface.getButton(ButtonID.LAYOUT).setBounds(185.5, 105.75, 10.0, 6.25);
        surface.getButton(ButtonID.SETUP).setBounds(173.5, 20.75, 10.0, 6.25);
        surface.getButton(ButtonID.STOP_CLIP).setBounds(21.0, 51.0, 8.25, 5.5);
        surface.getButton(ButtonID.SESSION).setBounds(185.5, 113.75, 10.0, 6.25);
        surface.getButton(ButtonID.REPEAT).setBounds(173.5, 93.75, 10.0, 6.25);
        surface.getButton(ButtonID.CONVERT).setBounds(4.75, 58.75, 10.0, 8.5);
        surface.getButton(ButtonID.USER).setBounds(185.5, 20.5, 10.0, 6.25);
        surface.getButton(ButtonID.FOOTSWITCH2).setBounds(160.0, 1.0, 12.0, 8.25);

        surface.getContinuous(ContinuousID.KNOB1).setBounds(34.75, 5.75, 10.0, 10.0);
        surface.getContinuous(ContinuousID.KNOB2).setBounds(50.25, 5.75, 10.0, 10.0);
        surface.getContinuous(ContinuousID.KNOB3).setBounds(65.75, 5.75, 10.0, 10.0);
        surface.getContinuous(ContinuousID.KNOB4).setBounds(81.25, 5.75, 10.0, 10.0);
        surface.getContinuous(ContinuousID.KNOB5).setBounds(96.75, 5.75, 10.0, 10.0);
        surface.getContinuous(ContinuousID.KNOB6).setBounds(112.25, 5.75, 10.0, 10.0);
        surface.getContinuous(ContinuousID.KNOB7).setBounds(127.75, 5.75, 10.0, 10.0);
        surface.getContinuous(ContinuousID.KNOB8).setBounds(143.25, 5.75, 10.0, 10.0);
        surface.getContinuous(ContinuousID.MASTER_KNOB).setBounds(180.0, 5.75, 10.0, 10.0);

        surface.getContinuous(ContinuousID.TEMPO).setBounds(4.0, 5.75, 10.0, 10.0);
        surface.getContinuous(ContinuousID.PLAY_POSITION).setBounds(17.75, 5.75, 10.0, 10.0);
        surface.getContinuous(ContinuousID.TOUCHSTRIP).setBounds(17.75, 58.5, 12.0, 93.0);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void startup() {
        final PushControlSurface surface = this.getSurface();

        final ViewManager viewManager = surface.getViewManager();
        if (this.configuration.shouldStartWithSessionView()) {
            viewManager.setActive(Views.SESSION);
        } else {
            this.recallLastView();
        }
        // This can happen if there is no track in the project
        // It is required to have a view otherwise the mode is not drawn
        if (viewManager.getActive() == null) {
            viewManager.setActive(Views.PLAY);
        }

        surface.sendPressureMode(true);
        surface.getMidiOutput().sendSysex(DeviceInquiry.createQuery());

        surface.updateColorPalette();
    }


    /**
     * Called when a new view is selected.
     */
    private void onViewChange() {
        final PushControlSurface surface = this.getSurface();

        // Update ribbon mode
        if (surface.getViewManager().isActive(Views.SESSION)) {
            surface.setRibbonMode(PushControlSurface.PUSH_RIBBON_PAN);
        } else {
            this.updateRibbonMode();
        }

        this.getSurface().getDisplay().cancelNotification();
    }


    private void updateRibbonMode() {
        final PushControlSurface surface = this.getSurface();
        surface.setRibbonValue(0);

        final int ribbonNoteRepeat = this.configuration.getRibbonNoteRepeat();
        if (this.configuration.isNoteRepeatActive() && ribbonNoteRepeat > PushConfiguration.NOTE_REPEAT_OFF) {
            surface.setRibbonMode(PushControlSurface.PUSH_RIBBON_DISCRETE);
            return;
        }

        final int ribbonMode = this.configuration.getRibbonMode();
        if (ribbonMode == PushConfiguration.RIBBON_MODE_CC || ribbonMode == PushConfiguration.RIBBON_MODE_FADER || ribbonMode == PushConfiguration.RIBBON_MODE_LAST_TOUCHED) {
            surface.setRibbonMode(PushControlSurface.PUSH_RIBBON_VOLUME);
        } else {
            surface.setRibbonMode(PushControlSurface.PUSH_RIBBON_PITCHBEND);
        }
    }


    private boolean getMuteState() {
        final PushControlSurface surface = this.getSurface();
        final ModeManager modeManager = surface.getModeManager();
        if (modeManager.isActive(Modes.DEVICE_LAYER)) {
            final ICursorDevice cd = this.model.getCursorDevice();
            final Optional<ILayer> layer = cd.getLayerBank().getSelectedItem();
            return layer.isPresent() && layer.get().isMute();
        }
        final ITrack selTrack = modeManager.isActive(Modes.MASTER) ? this.model.getMasterTrack() : this.model.getCursorTrack();
        return selTrack.isMute();
    }


    private boolean getSoloState() {
        final PushControlSurface surface = this.getSurface();
        final ModeManager modeManager = surface.getModeManager();
        if (modeManager.isActive(Modes.DEVICE_LAYER)) {
            final ICursorDevice cd = this.model.getCursorDevice();
            final Optional<ILayer> layer = cd.getLayerBank().getSelectedItem();
            return layer.isPresent() && layer.get().isSolo();
        }
        final ITrack selTrack = modeManager.isActive(Modes.MASTER) ? this.model.getMasterTrack() : this.model.getCursorTrack();
        return selTrack.isSolo();
    }

}
