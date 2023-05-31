// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common;

import de.mossgrabers.framework.configuration.*;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.constants.Capability;
import de.mossgrabers.framework.daw.midi.ArpeggiatorMode;
import de.mossgrabers.framework.graphics.IGraphicsConfiguration;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.view.Views;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;


/**
 * The configuration settings for CommonUI devices. See `CommonUIControlSurface` for more information.
 *
 * @author Kate Temkin
 * @author Jürgen Moßgraber
 */
public class CommonUIConfiguration extends AbstractConfiguration implements IGraphicsConfiguration
{

    /**
     * Setting for the ribbon mode.
     */
    public static final Integer RIBBON_MODE = 50;

    /**
     * Setting for the ribbon mode MIDI CC.
     */
    public static final Integer RIBBON_MODE_CC_VAL = 51;

    /**
     * Setting for the ribbon mode note repeat.
     */
    public static final Integer RIBBON_MODE_NOTE_REPEAT = 52;

    /**
     * Setting for the velocity curve.
     */
    public static final Integer VELOCITY_CURVE = 53;

    /**
     * Setting for the pad threshold.
     */
    public static final Integer PAD_THRESHOLD = 54;

    /**
     * Setting for the display brightness.
     */
    public static final Integer DISPLAY_BRIGHTNESS = 55;

    /**
     * Setting for the pad LED brightness.
     */
    public static final Integer LED_BRIGHTNESS = 56;

    /**
     * Setting for the pad sensitivity.
     */
    public static final Integer PAD_SENSITIVITY = 57;

    /**
     * Setting for the pad gain.
     */
    public static final Integer PAD_GAIN = 58;

    /**
     * Setting for the pad dynamics.
     */
    public static final Integer PAD_DYNAMICS = 59;

    /**
     * Setting for stopping automation recording on knob release.
     */
    public static final Integer STOP_AUTOMATION_ON_KNOB_RELEASE = 60;

    /**
     * Mode debug.
     */
    public static final Integer DEBUG_MODE = 61;

    /**
     * Push 2 display debug window.
     */
    public static final Integer DEBUG_WINDOW = 62;

    /**
     * Background color of an element.
     */
    public static final Integer COLOR_BACKGROUND = 70;

    /**
     * Border color of an element.
     */
    public static final Integer COLOR_BORDER = 71;

    /**
     * Text color of an element.
     */
    public static final Integer COLOR_TEXT = 72;

    /**
     * Fader color of an element.
     */
    public static final Integer COLOR_FADER = 73;

    /**
     * VU color of an element.
     */
    public static final Integer COLOR_VU = 74;

    /**
     * Edit color of an element.
     */
    public static final Integer COLOR_EDIT = 75;

    /**
     * Record color of an element.
     */
    public static final Integer COLOR_RECORD = 76;

    /**
     * Solo color of an element.
     */
    public static final Integer COLOR_SOLO = 77;

    /**
     * Mute color of an element.
     */
    public static final Integer COLOR_MUTE = 78;

    /**
     * Background color darker of an element.
     */
    public static final Integer COLOR_BACKGROUND_DARKER = 79;

    /**
     * Background color lighter of an element.
     */
    public static final Integer COLOR_BACKGROUND_LIGHTER = 80;

    /**
     * Session view options.
     */
    public static final Integer SESSION_VIEW = 81;

    /**
     * Display scenes or clips.
     */
    public static final Integer DISPLAY_SCENES_CLIPS = 82;

    /**
     * Use ribbon for pitch bend.
     */
    public static final int RIBBON_MODE_PITCH = 0;

    /**
     * Use ribbon for MIDI CC.
     */
    public static final int RIBBON_MODE_CC = 1;

    /**
     * Use ribbon for MIDI CC and pitch bend.
     */
    public static final int RIBBON_MODE_CC_PB = 2;

    /**
     * Use ribbon for pitch bend and MIDI CC.
     */
    public static final int RIBBON_MODE_PB_CC = 3;

    /**
     * Use ribbon as volume fader.
     */
    public static final int RIBBON_MODE_FADER = 4;

    /**
     * Use ribbon to change the last touched parameter.
     */
    public static final int RIBBON_MODE_LAST_TOUCHED = 5;

    /**
     * Use ribbon not for note repeat settings.
     */
    public static final int NOTE_REPEAT_OFF = 0;

    /**
     * Use ribbon for changing the note repeat period.
     */
    public static final int NOTE_REPEAT_PERIOD = 1;

    /**
     * Use ribbon for changing the note repeat length.
     */
    public static final int NOTE_REPEAT_LENGTH = 2;

    protected static final String CATEGORY_RIBBON = "Ribbon";

    protected static final String CATEGORY_COLORS = "Display Colors";

    protected static final String[] RIBBON_MODE_VALUES =
            {
                    "Pitch",
                    "CC",
                    "CC/Pitch",
                    "Pitch/CC",
                    "Fader",
                    "Last Touched"
            };

    protected static final String[] RIBBON_NOTE_REPEAT_VALUES =
            {
                    "Off",
                    "Period",
                    "Length"
            };

    protected static final String[] SESSION_VIEW_OPTIONS =
            {
                    "Session",
                    "Flipped",
                    "Scenes"
            };

    protected static final Views[] PREFERRED_NOTE_VIEWS =
            {
                    Views.PLAY,
                    Views.CHORDS,
                    Views.PIANO,
                    Views.DRUM64,
                    Views.DRUM,
                    Views.DRUM4,
                    Views.DRUM8,
                    Views.SEQUENCER,
                    Views.RAINDROPS,
                    Views.POLY_SEQUENCER
            };

    /**
     * Debug modes.
     */
    protected static final Set<Modes> DEBUG_MODES = EnumSet.noneOf(Modes.class);

    static {
        DEBUG_MODES.add(Modes.TRACK);
        DEBUG_MODES.add(Modes.TRACK_DETAILS);
        DEBUG_MODES.add(Modes.VOLUME);
        DEBUG_MODES.add(Modes.CROSSFADER);
        DEBUG_MODES.add(Modes.PAN);
        DEBUG_MODES.add(Modes.SEND1);
        DEBUG_MODES.add(Modes.SEND2);
        DEBUG_MODES.add(Modes.SEND3);
        DEBUG_MODES.add(Modes.SEND4);
        DEBUG_MODES.add(Modes.SEND5);
        DEBUG_MODES.add(Modes.SEND6);
        DEBUG_MODES.add(Modes.SEND7);
        DEBUG_MODES.add(Modes.SEND8);
        DEBUG_MODES.add(Modes.MASTER);
        DEBUG_MODES.add(Modes.MASTER_TEMP);
        DEBUG_MODES.add(Modes.DEVICE_PARAMS);
        DEBUG_MODES.add(Modes.DEVICE_CHAINS);
        DEBUG_MODES.add(Modes.DEVICE_LAYER);
        DEBUG_MODES.add(Modes.DEVICE_LAYER_VOLUME);
        DEBUG_MODES.add(Modes.DEVICE_LAYER_PAN);
        DEBUG_MODES.add(Modes.DEVICE_LAYER_SEND1);
        DEBUG_MODES.add(Modes.DEVICE_LAYER_SEND2);
        DEBUG_MODES.add(Modes.DEVICE_LAYER_SEND3);
        DEBUG_MODES.add(Modes.DEVICE_LAYER_SEND4);
        DEBUG_MODES.add(Modes.DEVICE_LAYER_SEND5);
        DEBUG_MODES.add(Modes.DEVICE_LAYER_SEND6);
        DEBUG_MODES.add(Modes.DEVICE_LAYER_SEND7);
        DEBUG_MODES.add(Modes.DEVICE_LAYER_SEND8);
        DEBUG_MODES.add(Modes.DEVICE_LAYER_DETAILS);
        DEBUG_MODES.add(Modes.BROWSER);
        DEBUG_MODES.add(Modes.CLIP);
        DEBUG_MODES.add(Modes.NOTE);
        DEBUG_MODES.add(Modes.FRAME);
        DEBUG_MODES.add(Modes.GROOVE);
        DEBUG_MODES.add(Modes.REC_ARM);
        DEBUG_MODES.add(Modes.ACCENT);
        DEBUG_MODES.add(Modes.SCALES);
        DEBUG_MODES.add(Modes.SCALE_LAYOUT);
        DEBUG_MODES.add(Modes.FIXED);
        DEBUG_MODES.add(Modes.RIBBON);
        DEBUG_MODES.add(Modes.VIEW_SELECT);
        DEBUG_MODES.add(Modes.AUTOMATION);
        DEBUG_MODES.add(Modes.TRANSPORT);
        DEBUG_MODES.add(Modes.MARKERS);
        DEBUG_MODES.add(Modes.USER);
        DEBUG_MODES.add(Modes.SETUP);
        DEBUG_MODES.add(Modes.INFO);
        DEBUG_MODES.add(Modes.CONFIGURATION);
        DEBUG_MODES.add(Modes.SESSION);
        DEBUG_MODES.add(Modes.SESSION_VIEW_SELECT);
        DEBUG_MODES.add(Modes.REPEAT_NOTE);
    }

    protected ColorEx colorBackground = DEFAULT_COLOR_BACKGROUND;

    protected ColorEx colorBorder = DEFAULT_COLOR_BORDER;

    protected ColorEx colorText = DEFAULT_COLOR_TEXT;

    protected ColorEx colorFader = DEFAULT_COLOR_FADER;

    protected ColorEx colorVU = DEFAULT_COLOR_VU;

    protected ColorEx colorEdit = DEFAULT_COLOR_EDIT;

    protected ColorEx colorRecord = DEFAULT_COLOR_RECORD;

    protected ColorEx colorSolo = DEFAULT_COLOR_SOLO;

    protected ColorEx colorMute = DEFAULT_COLOR_MUTE;

    protected ColorEx colorBackgroundDarker = DEFAULT_COLOR_BACKGROUND_DARKER;

    protected ColorEx colorBackgroundLighter = DEFAULT_COLOR_BACKGROUND_LIGHTER;

    protected IIntegerSetting displayBrightnessSetting;

    protected IIntegerSetting ledBrightnessSetting;

    protected IEnumSetting ribbonModeSetting;

    protected IIntegerSetting ribbonModeCCSetting;

    protected IEnumSetting ribbonModeNoteRepeatSetting;

    protected IEnumSetting debugModeSetting;

    protected IColorSetting colorBackgroundSetting;

    protected IColorSetting colorBackgroundDarkerSetting;

    protected IColorSetting colorBackgroundLighterSetting;

    protected IColorSetting colorBorderSetting;

    protected IColorSetting colorTextSetting;

    protected IColorSetting colorFaderSetting;

    protected IColorSetting colorVUSetting;

    protected IColorSetting colorEditSetting;

    protected IColorSetting colorRecordSetting;

    protected IColorSetting colorSoloSetting;

    protected IColorSetting colorMuteSetting;

    protected IEnumSetting sessionViewSetting;

    protected IEnumSetting displayScenesClipsSetting;

    protected boolean isSoloLongPressed = false;

    protected boolean isMuteLongPressed = false;

    protected boolean isMuteSoloLocked = false;

    protected boolean displayScenesClips;

    protected boolean isScenesClipView;

    /**
     * What does the ribbon send?
     **/
    protected int ribbonMode = RIBBON_MODE_PITCH;

    protected int ribbonModeCCVal = 1;

    protected int ribbonModeNoteRepeat = NOTE_REPEAT_PERIOD;

    protected boolean stopAutomationOnKnobRelease = false;

    protected TrackState trackState = TrackState.MUTE;

    protected Modes debugMode = Modes.TRACK;

    protected Modes layerMode = null;


    protected int displayBrightness = 255;

    protected int ledBrightness = 127;


    /**
     * Constructor.
     *
     * @param host             The DAW host
     * @param valueChanger     The value changer
     * @param arpeggiatorModes The available arpeggiator modes
     */
    public CommonUIConfiguration(final IHost host, final IValueChanger valueChanger, final List<ArpeggiatorMode> arpeggiatorModes)
    {
        super(host, valueChanger, arpeggiatorModes);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final ISettingsUI globalSettings, final ISettingsUI documentSettings)
    {
        ///////////////////////////
        // Scale

        this.activateScaleSetting(documentSettings);
        this.activateScaleBaseSetting(documentSettings);
        this.activateScaleInScaleSetting(documentSettings);
        this.activateScaleLayoutSetting(documentSettings);

        ///////////////////////////
        // Note Repeat

        this.activateNoteRepeatSetting(documentSettings);

        ///////////////////////////
        // Session

        this.activateSessionView(globalSettings);
        this.activateSelectClipOnLaunchSetting(globalSettings);
        this.activateDrawRecordStripeSetting(globalSettings);
        this.activateActionForRecArmedPad(globalSettings);

        ///////////////////////////
        // Transport

        this.activateBehaviourOnPauseSetting(globalSettings);
        this.activateFlipRecordSetting(globalSettings);

        ///////////////////////////
        // Play and Sequence

        this.activateAccentActiveSetting(globalSettings);
        this.activateAccentValueSetting(globalSettings);
        this.activateQuantizeAmountSetting(globalSettings);
        this.activatePreferredNoteViewSetting(globalSettings, PREFERRED_NOTE_VIEWS);
        this.activateStartWithSessionViewSetting(globalSettings);
        this.activateMidiEditChannelSetting(documentSettings);

        ///////////////////////////
        // Drum Sequencer

        if (this.host.supports(Capability.HAS_DRUM_DEVICE)) {
            this.activateAutoSelectDrumSetting(globalSettings);
            this.activateTurnOffEmptyDrumPadsSetting(globalSettings);
        }

        ///////////////////////////
        // Workflow

        this.activateExcludeDeactivatedItemsSetting(globalSettings);
        this.activateEnableVUMetersSetting(globalSettings);
        this.activateFootswitchSetting(globalSettings, 0, "Footswitch 2");
        this.activateStopAutomationOnKnobReleaseSetting(globalSettings);
        this.activateNewClipLengthSetting(globalSettings);
        this.activateKnobSpeedSetting(globalSettings);
        this.activateUserPageNamesSetting(documentSettings);

        ///////////////////////////
        // Add Track - Device Favorites

        this.activateDeviceFavorites(globalSettings, 7, 3, 3);

        ///////////////////////////
        // Ribbon

        this.activateRibbonSettings(globalSettings);

        ///////////////////////////
        // Browser

        this.activateBrowserSettings(globalSettings);

        ///////////////////////////
        // Debugging

        this.activateDebugSettings(globalSettings);
    }


    /**
     * Get the ribbon mode.
     *
     * @return The functionality for the ribbon
     */
    public int getRibbonMode()
    {
        return this.ribbonMode;
    }


    /**
     * Set the ribbon mode.
     *
     * @param mode The functionality for the ribbon
     */
    public void setRibbonMode(final int mode)
    {
        this.ribbonModeSetting.set(RIBBON_MODE_VALUES[mode]);
    }


    /**
     * Set the MIDI CC to use for the CC functionality of the ribbon.
     *
     * @param value The MIDI CC value
     */
    public void setRibbonModeCC(final int value)
    {
        this.ribbonModeCCSetting.set(value);
    }


    /**
     * Get the MIDI CC to use for the CC functionality of the ribbon.
     *
     * @return The MIDI CC value
     */
    public int getRibbonModeCCVal()
    {
        return this.ribbonModeCCVal;
    }


    /**
     * Get the ribbon mode note repeat.
     *
     * @return The functionality for the ribbon in note repeat mode
     */
    public int getRibbonNoteRepeat()
    {
        return this.ribbonModeNoteRepeat;
    }


    /**
     * Set the ribbon mode note repeat.
     *
     * @param mode The functionality for the ribbon in note repeat mode
     */
    public void setRibbonNoteRepeat(final int mode)
    {
        this.ribbonModeNoteRepeatSetting.set(RIBBON_NOTE_REPEAT_VALUES[mode]);
    }


    /**
     * Change the display brightness.
     *
     * @param control The control value
     */
    public void changeDisplayBrightness(final int control)
    {
        this.displayBrightnessSetting.set(this.valueChanger.changeValue(control, this.displayBrightness, -100, 101));
    }


    /**
     * Change the LED brightness.
     *
     * @param control The control value
     */
    public void changeLEDBrightness(final int control)
    {
        this.ledBrightnessSetting.set(this.valueChanger.changeValue(control, this.ledBrightness, -100, 101));
    }


    /**
     * Get the display brightness.
     *
     * @return The display brightness.
     */
    public int getDisplayBrightness()
    {
        return this.displayBrightness;
    }


    /**
     * Set the display brightness.
     *
     * @param displayBrightness The display brightness.
     */
    public void setDisplayBrightness(final int displayBrightness)
    {
        this.displayBrightnessSetting.set(displayBrightness);
    }


    /**
     * Get the LED brightness.
     *
     * @return The LED brightness
     */
    public int getLedBrightness()
    {
        return this.ledBrightness;
    }


    /**
     * Set the LED brightness.
     *
     * @param ledBrightness The LED brightness
     */
    public void setLEDBrightness(final int ledBrightness)
    {
        this.ledBrightnessSetting.set(ledBrightness);
    }


    /**
     * Stop automation recording on knob release?
     *
     * @return True if should be stopped
     */
    public boolean isStopAutomationOnKnobRelease()
    {
        return this.stopAutomationOnKnobRelease;
    }


    /**
     * Is mute long pressed?
     *
     * @return True if mute is long pressed
     */
    public boolean isMuteLongPressed()
    {
        return this.isMuteLongPressed;
    }


    /**
     * Set if mute is long pressed.
     *
     * @param isMuteLongPressed True if mute is long pressed
     */
    public void setIsMuteLongPressed(final boolean isMuteLongPressed)
    {
        this.isMuteLongPressed = isMuteLongPressed;
    }


    /**
     * Is solo long pressed?
     *
     * @return True if solo is long pressed
     */
    public boolean isSoloLongPressed()
    {
        return this.isSoloLongPressed;
    }


    /**
     * Set if solo is long pressed.
     *
     * @param isSoloLongPressed True if solo is long pressed
     */
    public void setIsSoloLongPressed(final boolean isSoloLongPressed)
    {
        this.isSoloLongPressed = isSoloLongPressed;
    }


    /**
     * Is mute and solo locked (all mode buttons are used for solo or mute).
     *
     * @return True if locked
     */
    public boolean isMuteSoloLocked()
    {
        return this.isMuteSoloLocked;
    }


    /**
     * Set if mute and solo is locked (all mode buttons are used for solo or mute).
     *
     * @param isMuteSoloLocked True if locked
     */
    public void setMuteSoloLocked(final boolean isMuteSoloLocked)
    {
        this.isMuteSoloLocked = isMuteSoloLocked;
    }


    /**
     * Use the 2nd row buttons for mute?
     *
     * @return True if used for mute
     */
    public boolean isMuteState()
    {
        return this.trackState == TrackState.MUTE;
    }


    /**
     * Use the 2nd row buttons for solo?
     *
     * @return True if used for solo
     */
    public boolean isSoloState()
    {
        return this.trackState == TrackState.SOLO;
    }


    /**
     * Set the track state.
     *
     * @param state The new track state
     */
    public void setTrackState(final TrackState state)
    {
        this.trackState = state;
    }


    /**
     * Get the current mode which is selected for mixing.
     *
     * @return The ID of the current mode which is selected for mixing.
     */
    public Modes getCurrentMixMode()
    {
        return Modes.isTrackMode(this.debugMode) ? this.debugMode : null;
    }


    /**
     * Set the current mode which is selected for layer mixing.
     *
     * @param layerMode The ID of a layer mode
     */
    public void setLayerMixMode(final Modes layerMode)
    {
        this.layerMode = layerMode;
    }


    /**
     * Get the current mode which is selected for layer mixing.
     *
     * @return The ID of the current mode which is selected for layer mixing.
     */
    public Modes getCurrentLayerMixMode()
    {
        if (this.layerMode != null) {return this.layerMode;}

        final Modes currentMixMode = this.getCurrentMixMode();
        if (currentMixMode == null) {this.layerMode = Modes.DEVICE_LAYER;}
        else {
            switch (currentMixMode) {
                case VOLUME:
                    this.layerMode = Modes.DEVICE_LAYER_VOLUME;
                    break;

                case PAN:
                    this.layerMode = Modes.DEVICE_LAYER_PAN;
                    break;

                case SEND1:
                    this.layerMode = Modes.DEVICE_LAYER_SEND1;
                    break;
                case SEND2:
                    this.layerMode = Modes.DEVICE_LAYER_SEND2;
                    break;
                case SEND3:
                    this.layerMode = Modes.DEVICE_LAYER_SEND3;
                    break;
                case SEND4:
                    this.layerMode = Modes.DEVICE_LAYER_SEND4;
                    break;
                case SEND5:
                    this.layerMode = Modes.DEVICE_LAYER_SEND5;
                    break;
                case SEND6:
                    this.layerMode = Modes.DEVICE_LAYER_SEND6;
                    break;
                case SEND7:
                    this.layerMode = Modes.DEVICE_LAYER_SEND7;
                    break;
                case SEND8:
                    this.layerMode = Modes.DEVICE_LAYER_SEND8;
                    break;

                case TRACK:
                default:
                    this.layerMode = Modes.DEVICE_LAYER;
                    break;
            }
        }
        return this.layerMode;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorBackground()
    {
        return this.colorBackground;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorBackgroundDarker()
    {
        return this.colorBackgroundDarker;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorBackgroundLighter()
    {
        return this.colorBackgroundLighter;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorBorder()
    {
        return this.colorBorder;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorText()
    {
        return this.colorText;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorEdit()
    {
        return this.colorEdit;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorFader()
    {
        return this.colorFader;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorVu()
    {
        return this.colorVU;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorRecord()
    {
        return this.colorRecord;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorSolo()
    {
        return this.colorSolo;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorMute()
    {
        return this.colorMute;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAntialiasEnabled()
    {
        return true;
    }


    /**
     * Get the selected display mode for debugging.
     *
     * @return The ID of a mode
     */
    public Modes getDebugMode()
    {
        return this.debugMode;
    }


    /**
     * Set the selected display mode for debugging.
     *
     * @param debugMode The ID of a mode
     */
    public void setDebugMode(final Modes debugMode)
    {
        this.debugModeSetting.set(debugMode.toString());
    }


    /**
     * Activate the session view settings.
     *
     * @param settingsUI The settings
     */
    protected void activateSessionView(final ISettingsUI settingsUI)
    {
        this.sessionViewSetting = settingsUI.getEnumSetting("Session view", CATEGORY_SESSION, SESSION_VIEW_OPTIONS, SESSION_VIEW_OPTIONS[0]);
        this.sessionViewSetting.addValueObserver(value -> {
            this.flipSession      = SESSION_VIEW_OPTIONS[1].equals(value);
            this.isScenesClipView = SESSION_VIEW_OPTIONS[2].equals(value);
            this.notifyObservers(AbstractConfiguration.FLIP_SESSION);
            this.notifyObservers(CommonUIConfiguration.SESSION_VIEW);
        });

        this.displayScenesClipsSetting = settingsUI.getEnumSetting("Display scenes/clips", CATEGORY_SESSION, ON_OFF_OPTIONS, ON_OFF_OPTIONS[0]);
        this.displayScenesClipsSetting.addValueObserver(value -> {
            this.displayScenesClips = "On".equals(value);
            this.notifyObservers(CommonUIConfiguration.DISPLAY_SCENES_CLIPS);
        });
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setFlipSession(final boolean enabled)
    {
        this.sessionViewSetting.set(enabled ? SESSION_VIEW_OPTIONS[1] : SESSION_VIEW_OPTIONS[0]);
    }


    /**
     * Set the scene view.
     */
    public void setSceneView()
    {
        this.sessionViewSetting.set(SESSION_VIEW_OPTIONS[2]);
    }


    /**
     * Returns true if the session view should also switch to the scene/clip mode.
     *
     * @return True if the session view should also switch to the scene/clip mode.
     */
    public boolean shouldDisplayScenesOrClips()
    {
        return this.displayScenesClips;
    }


    /**
     * Returns true if the scene/clip view is enabled (otherwise the normal session view).
     *
     * @return True if the scene/clip view is enabled
     */
    public boolean isScenesClipViewSelected()
    {
        return this.isScenesClipView;
    }


    /**
     * Toggles the mode display for scenes/clips in session view.
     */
    public void toggleScenesClipMode()
    {
        this.displayScenesClipsSetting.set(this.displayScenesClips ? ON_OFF_OPTIONS[0] : ON_OFF_OPTIONS[1]);
    }


    /**
     * Activate the ribbon settings.
     *
     * @param settingsUI The settings
     */
    protected void activateRibbonSettings(final ISettingsUI settingsUI)
    {
        this.ribbonModeSetting = settingsUI.getEnumSetting("Mode", CATEGORY_RIBBON, RIBBON_MODE_VALUES, RIBBON_MODE_VALUES[0]);
        this.ribbonModeSetting.addValueObserver(value -> {
            this.ribbonMode = lookupIndex(RIBBON_MODE_VALUES, value);
            this.notifyObservers(RIBBON_MODE);
        });

        this.ribbonModeCCSetting = settingsUI.getRangeSetting("CC", CATEGORY_RIBBON, 0, 127, 1, "", 1);
        this.ribbonModeCCSetting.addValueObserver(value -> {
            this.ribbonModeCCVal = value.intValue();
            this.notifyObservers(RIBBON_MODE_CC_VAL);
        });

        this.ribbonModeNoteRepeatSetting = settingsUI.getEnumSetting("Function if Note Repeat is active", CATEGORY_RIBBON, RIBBON_NOTE_REPEAT_VALUES, RIBBON_NOTE_REPEAT_VALUES[1]);
        this.ribbonModeNoteRepeatSetting.addValueObserver(value -> {
            this.ribbonModeNoteRepeat = lookupIndex(RIBBON_NOTE_REPEAT_VALUES, value);
            this.notifyObservers(RIBBON_MODE_NOTE_REPEAT);
        });
    }


    /**
     * Activate the stop automation on knob release setting.
     *
     * @param settingsUI The settings
     */
    protected void activateStopAutomationOnKnobReleaseSetting(final ISettingsUI settingsUI)
    {
        settingsUI.getEnumSetting("Stop automation recording on knob release", CATEGORY_WORKFLOW, ON_OFF_OPTIONS, ON_OFF_OPTIONS[0]).addValueObserver(value -> {
            this.stopAutomationOnKnobRelease = "On".equals(value);
            this.notifyObservers(STOP_AUTOMATION_ON_KNOB_RELEASE);
        });
    }


    /**
     * Activate the debug settings.
     *
     * @param settingsUI The settings
     */
    protected void activateDebugSettings(final ISettingsUI settingsUI)
    {
        final String[] modes = new String[DEBUG_MODES.size()];
        int            i     = 0;
        for (final Modes mode : DEBUG_MODES) {
            modes[i] = mode.toString();
            i++;
        }

        this.debugModeSetting = settingsUI.getEnumSetting("Display Mode", CATEGORY_DEBUG, modes, Modes.TRACK.toString());
        this.debugModeSetting.addValueObserver(value -> {
            try {
                this.debugMode = Modes.valueOf(value);
            } catch (final IllegalArgumentException ex) {
                this.debugMode = Modes.TRACK;
            }
            this.notifyObservers(DEBUG_MODE);
        });

        settingsUI.getSignalSetting(" ", CATEGORY_DEBUG, "Display window").addSignalObserver(value -> this.notifyObservers(DEBUG_WINDOW));
    }


    /**
     * Settings for different Mute and Solo behavior.
     */
    public enum TrackState
    {
        /**
         * Use Mute, Solo for muting/soloing the current track.
         */
        NONE,
        /**
         * Use all mode buttons for muting.
         */
        MUTE,
        /**
         * Use all mode buttons for soloing.
         */
        SOLO
    }

}
