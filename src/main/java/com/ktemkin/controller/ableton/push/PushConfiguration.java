// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ableton.push;

import com.ktemkin.controller.common.CommonUIConfiguration;
import de.mossgrabers.framework.configuration.IIntegerSetting;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.constants.Capability;
import de.mossgrabers.framework.daw.midi.ArpeggiatorMode;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.view.Views;

import java.util.List;


/**
 * The configuration settings for Push.
 *
 * @author Jürgen Moßgraber
 */
public class PushConfiguration extends CommonUIConfiguration {

    private int padSensitivity = 5;

    private int padGain = 5;

    private int padDynamics = 5;


    private IIntegerSetting padSensitivitySetting;

    private IIntegerSetting padGainSetting;

    private IIntegerSetting padDynamicsSetting;


    /**
     * Constructor.
     *
     * @param host             The DAW host
     * @param valueChanger     The value changer
     * @param arpeggiatorModes The available arpeggiator modes
     */
    public PushConfiguration(final IHost host, final IValueChanger valueChanger, final List<ArpeggiatorMode> arpeggiatorModes) {
        super(host, valueChanger, arpeggiatorModes);

        this.preferredAudioView = Views.CLIP_LENGTH;

        this.dontNotifyAll.add(DEBUG_WINDOW);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final ISettingsUI globalSettings, final ISettingsUI documentSettings) {
        super.init(globalSettings, documentSettings);

        ///////////////////////////
        // Push 2 Hardware

        this.activatePush2HardwareSettings(globalSettings);
        this.activatePush2DisplayColorsSettings(globalSettings);
    }


    /**
     * Get the ribbon mode.
     *
     * @return The functionality for the ribbon
     */
    public int getRibbonMode() {
        return this.ribbonMode;
    }


    /**
     * Set the ribbon mode.
     *
     * @param mode The functionality for the ribbon
     */
    public void setRibbonMode(final int mode) {
        this.ribbonModeSetting.set(RIBBON_MODE_VALUES[mode]);
    }


    /**
     * Set the MIDI CC to use for the CC functionality of the ribbon.
     *
     * @param value The MIDI CC value
     */
    public void setRibbonModeCC(final int value) {
        this.ribbonModeCCSetting.set(value);
    }


    /**
     * Get the MIDI CC to use for the CC functionality of the ribbon.
     *
     * @return The MIDI CC value
     */
    public int getRibbonModeCCVal() {
        return this.ribbonModeCCVal;
    }


    /**
     * Get the ribbon mode note repeat.
     *
     * @return The functionality for the ribbon in note repeat mode
     */
    public int getRibbonNoteRepeat() {
        return this.ribbonModeNoteRepeat;
    }


    /**
     * Set the ribbon mode note repeat.
     *
     * @param mode The functionality for the ribbon in note repeat mode
     */
    public void setRibbonNoteRepeat(final int mode) {
        this.ribbonModeNoteRepeatSetting.set(RIBBON_NOTE_REPEAT_VALUES[mode]);
    }


    /**
     * Change the display brightness.
     *
     * @param control The control value
     */
    public void changeDisplayBrightness(final int control) {
        this.displayBrightnessSetting.set(this.valueChanger.changeValue(control, this.displayBrightness, -100, 101));
    }


    /**
     * Change the LED brightness.
     *
     * @param control The control value
     */
    public void changeLEDBrightness(final int control) {
        this.ledBrightnessSetting.set(this.valueChanger.changeValue(control, this.ledBrightness, -100, 101));
    }


    /**
     * Change the pad sensitivity.
     *
     * @param control The control value
     */
    public void changePadSensitivity(final int control) {
        this.padSensitivitySetting.set(this.valueChanger.changeValue(control, this.padSensitivity, -100, 11));
    }


    /**
     * Change the pad gain.
     *
     * @param control The control value
     */
    public void changePadGain(final int control) {
        this.padGainSetting.set(this.valueChanger.changeValue(control, this.padGain, -100, 11));
    }


    /**
     * Change the pad dynamics.
     *
     * @param control The control value
     */
    public void changePadDynamics(final int control) {
        this.padDynamicsSetting.set(this.valueChanger.changeValue(control, this.padDynamics, -100, 11));
    }


    /**
     * Get the display brightness.
     *
     * @return The display brightness.
     */
    public int getDisplayBrightness() {
        return this.displayBrightness;
    }


    /**
     * Set the display brightness.
     *
     * @param displayBrightness The display brightness.
     */
    public void setDisplayBrightness(final int displayBrightness) {
        this.displayBrightnessSetting.set(displayBrightness);
    }


    /**
     * Get the LED brightness.
     *
     * @return The LED brightness
     */
    public int getLedBrightness() {
        return this.ledBrightness;
    }


    /**
     * Set the LED brightness.
     *
     * @param ledBrightness The LED brightness
     */
    public void setLEDBrightness(final int ledBrightness) {
        this.ledBrightnessSetting.set(ledBrightness);
    }


    /**
     * Stop automation recording on knob release?
     *
     * @return True if should be stopped
     */
    public boolean isStopAutomationOnKnobRelease() {
        return this.stopAutomationOnKnobRelease;
    }


    /**
     * Is mute long pressed?
     *
     * @return True if mute is long pressed
     */
    public boolean isMuteLongPressed() {
        return this.isMuteLongPressed;
    }


    /**
     * Set if mute is long pressed.
     *
     * @param isMuteLongPressed True if mute is long pressed
     */
    public void setIsMuteLongPressed(final boolean isMuteLongPressed) {
        this.isMuteLongPressed = isMuteLongPressed;
    }


    /**
     * Is solo long pressed?
     *
     * @return True if solo is long pressed
     */
    public boolean isSoloLongPressed() {
        return this.isSoloLongPressed;
    }


    /**
     * Set if solo is long pressed.
     *
     * @param isSoloLongPressed True if solo is long pressed
     */
    public void setIsSoloLongPressed(final boolean isSoloLongPressed) {
        this.isSoloLongPressed = isSoloLongPressed;
    }


    /**
     * Is mute and solo locked (all mode buttons are used for solo or mute).
     *
     * @return True if locked
     */
    public boolean isMuteSoloLocked() {
        return this.isMuteSoloLocked;
    }


    /**
     * Set if mute and solo is locked (all mode buttons are used for solo or mute).
     *
     * @param isMuteSoloLocked True if locked
     */
    public void setMuteSoloLocked(final boolean isMuteSoloLocked) {
        this.isMuteSoloLocked = isMuteSoloLocked;
    }


    /**
     * Get the pad sensitivity.
     *
     * @return The pad sensitivity
     */
    public int getPadSensitivity() {
        return this.padSensitivity;
    }


    /**
     * Set the pad sensitivity.
     *
     * @param padSensitivity The pad sensitivity
     */
    public void setPadSensitivity(final int padSensitivity) {
        this.padSensitivitySetting.set(padSensitivity);
    }


    /**
     * Get the pad gain.
     *
     * @return The pad gain
     */
    public int getPadGain() {
        return this.padGain;
    }


    /**
     * Set the pad gain.
     *
     * @param padGain The pad gain
     */
    public void setPadGain(final int padGain) {
        this.padGainSetting.set(padGain);
    }


    /**
     * Get the pad dynamics.
     *
     * @return The pad dynamics.
     */
    public int getPadDynamics() {
        return this.padDynamics;
    }


    /**
     * Set the pad dynamics.
     *
     * @param padDynamics The pad dynamics.
     */
    public void setPadDynamics(final int padDynamics) {
        this.padDynamicsSetting.set(padDynamics);
    }


    /**
     * Use the 2nd row buttons for mute?
     *
     * @return True if used for mute
     */
    public boolean isMuteState() {
        return this.trackState == TrackState.MUTE;
    }


    /**
     * Use the 2nd row buttons for solo?
     *
     * @return True if used for solo
     */
    public boolean isSoloState() {
        return this.trackState == TrackState.SOLO;
    }


    /**
     * Set the track state.
     *
     * @param state The new track state
     */
    public void setTrackState(final TrackState state) {
        this.trackState = state;
    }


    /**
     * Get the current mode which is selected for mixing.
     *
     * @return The ID of the current mode which is selected for mixing.
     */
    public Modes getCurrentMixMode() {
        return Modes.isTrackMode(this.debugMode) ? this.debugMode : null;
    }


    /**
     * Set the current mode which is selected for layer mixing.
     *
     * @param layerMode The ID of a layer mode
     */
    public void setLayerMixMode(final Modes layerMode) {
        this.layerMode = layerMode;
    }


    /**
     * Get the current mode which is selected for layer mixing.
     *
     * @return The ID of the current mode which is selected for layer mixing.
     */
    public Modes getCurrentLayerMixMode() {
        if (this.layerMode != null) {
            return this.layerMode;
        }

        final Modes currentMixMode = this.getCurrentMixMode();
        if (currentMixMode == null) {
            this.layerMode = Modes.DEVICE_LAYER;
        } else {
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
    public ColorEx getColorBackground() {
        return this.colorBackground;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorBackgroundDarker() {
        return this.colorBackgroundDarker;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorBackgroundLighter() {
        return this.colorBackgroundLighter;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorBorder() {
        return this.colorBorder;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorText() {
        return this.colorText;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorEdit() {
        return this.colorEdit;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorFader() {
        return this.colorFader;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorVu() {
        return this.colorVU;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorRecord() {
        return this.colorRecord;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorSolo() {
        return this.colorSolo;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColorMute() {
        return this.colorMute;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAntialiasEnabled() {
        return true;
    }


    /**
     * Get the selected display mode for debugging.
     *
     * @return The ID of a mode
     */
    public Modes getDebugMode() {
        return this.debugMode;
    }


    /**
     * Set the selected display mode for debugging.
     *
     * @param debugMode The ID of a mode
     */
    public void setDebugMode(final Modes debugMode) {
        this.debugModeSetting.set(debugMode.toString());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setFlipSession(final boolean enabled) {
        this.sessionViewSetting.set(enabled ? SESSION_VIEW_OPTIONS[1] : SESSION_VIEW_OPTIONS[0]);
    }


    /**
     * Set the scene view.
     */
    public void setSceneView() {
        this.sessionViewSetting.set(SESSION_VIEW_OPTIONS[2]);
    }


    /**
     * Returns true if the session view should also switch to the scene/clip mode.
     *
     * @return True if the session view should also switch to the scene/clip mode.
     */
    public boolean shouldDisplayScenesOrClips() {
        return this.displayScenesClips;
    }


    /**
     * Returns true if the scene/clip view is enabled (otherwise the normal session view).
     *
     * @return True if the scene/clip view is enabled
     */
    public boolean isScenesClipViewSelected() {
        return this.isScenesClipView;
    }


    /**
     * Toggles the mode display for scenes/clips in session view.
     */
    public void toggleScenesClipMode() {
        this.displayScenesClipsSetting.set(this.displayScenesClips ? ON_OFF_OPTIONS[0] : ON_OFF_OPTIONS[1]);
    }


    /**
     * Activate the Push 2 hardware settings.
     *
     * @param settingsUI The settings
     */
    private void activatePush2HardwareSettings(final ISettingsUI settingsUI) {
        this.displayBrightnessSetting = settingsUI.getRangeSetting("Display Brightness", CATEGORY_HARDWARE_SETUP, 0, 100, 1, "%", 100);
        this.displayBrightnessSetting.addValueObserver(value -> {
            this.displayBrightness = value.intValue();
            this.notifyObservers(DISPLAY_BRIGHTNESS);
        });

        this.ledBrightnessSetting = settingsUI.getRangeSetting("LED Brightness", CATEGORY_HARDWARE_SETUP, 0, 100, 1, "%", 100);
        this.ledBrightnessSetting.addValueObserver(value -> {
            this.ledBrightness = value.intValue();
            this.notifyObservers(LED_BRIGHTNESS);
        });
    }


    /**
     * Activate the Push 2 pad settings.
     *
     * @param settingsUI The settings
     */
    private void activatePush2PadSettings(final ISettingsUI settingsUI) {
        this.padSensitivitySetting = settingsUI.getRangeSetting("Sensitivity", CATEGORY_PADS, 0, 10, 1, "", 5);
        this.padSensitivitySetting.addValueObserver(value -> {
            this.padSensitivity = value.intValue();
            this.notifyObservers(PAD_SENSITIVITY);
        });

        this.padGainSetting = settingsUI.getRangeSetting("Gain", CATEGORY_PADS, 0, 10, 1, "", 5);
        this.padGainSetting.addValueObserver(value -> {
            this.padGain = value.intValue();
            this.notifyObservers(PAD_GAIN);
        });

        this.padDynamicsSetting = settingsUI.getRangeSetting("Dynamics", CATEGORY_PADS, 0, 10, 1, "", 5);
        this.padDynamicsSetting.addValueObserver(value -> {
            this.padDynamics = value.intValue();
            this.notifyObservers(PAD_DYNAMICS);
        });
    }


    /**
     * Activate the color settings for the Push 2 display.
     *
     * @param settingsUI The settings
     */
    private void activatePush2DisplayColorsSettings(final ISettingsUI settingsUI) {
        settingsUI.getSignalSetting("Reset colors to default", CATEGORY_COLORS, "Reset").addSignalObserver(value -> {
            this.colorBackgroundSetting.set(DEFAULT_COLOR_BACKGROUND);
            this.colorBackgroundDarkerSetting.set(DEFAULT_COLOR_BACKGROUND_DARKER);
            this.colorBackgroundLighterSetting.set(DEFAULT_COLOR_BACKGROUND_LIGHTER);
            this.colorBorderSetting.set(DEFAULT_COLOR_BORDER);
            this.colorTextSetting.set(DEFAULT_COLOR_TEXT);
            this.colorFaderSetting.set(DEFAULT_COLOR_FADER);
            this.colorVUSetting.set(DEFAULT_COLOR_VU);
            this.colorEditSetting.set(DEFAULT_COLOR_EDIT);
            this.colorRecordSetting.set(DEFAULT_COLOR_RECORD);
            this.colorSoloSetting.set(DEFAULT_COLOR_SOLO);
            this.colorMuteSetting.set(DEFAULT_COLOR_MUTE);
        });

        this.colorBackgroundSetting = settingsUI.getColorSetting("Background", CATEGORY_COLORS, DEFAULT_COLOR_BACKGROUND);
        this.colorBackgroundSetting.addValueObserver(color -> {
            this.colorBackground = color;
            this.notifyObservers(COLOR_BACKGROUND);
        });

        this.colorBackgroundDarkerSetting = settingsUI.getColorSetting("Background Darker", CATEGORY_COLORS, DEFAULT_COLOR_BACKGROUND_DARKER);
        this.colorBackgroundDarkerSetting.addValueObserver(color -> {
            this.colorBackgroundDarker = color;
            this.notifyObservers(COLOR_BACKGROUND_DARKER);
        });

        this.colorBackgroundLighterSetting = settingsUI.getColorSetting("Background Selected", CATEGORY_COLORS, DEFAULT_COLOR_BACKGROUND_LIGHTER);
        this.colorBackgroundLighterSetting.addValueObserver(color -> {
            this.colorBackgroundLighter = color;
            this.notifyObservers(COLOR_BACKGROUND_LIGHTER);
        });

        this.colorBorderSetting = settingsUI.getColorSetting("Border", CATEGORY_COLORS, DEFAULT_COLOR_BORDER);
        this.colorBorderSetting.addValueObserver(color -> {
            this.colorBorder = color;
            this.notifyObservers(COLOR_BORDER);
        });

        this.colorTextSetting = settingsUI.getColorSetting("Text", CATEGORY_COLORS, DEFAULT_COLOR_TEXT);
        this.colorTextSetting.addValueObserver(color -> {
            this.colorText = color;
            this.notifyObservers(COLOR_TEXT);
        });

        this.colorFaderSetting = settingsUI.getColorSetting("Fader", CATEGORY_COLORS, DEFAULT_COLOR_FADER);
        this.colorFaderSetting.addValueObserver(color -> {
            this.colorFader = color;
            this.notifyObservers(COLOR_FADER);
        });

        this.colorVUSetting = settingsUI.getColorSetting("VU", CATEGORY_COLORS, DEFAULT_COLOR_VU);
        this.colorVUSetting.addValueObserver(color -> {
            this.colorVU = color;
            this.notifyObservers(COLOR_VU);
        });

        this.colorEditSetting = settingsUI.getColorSetting("Edit", CATEGORY_COLORS, DEFAULT_COLOR_EDIT);
        this.colorEditSetting.addValueObserver(color -> {
            this.colorEdit = color;
            this.notifyObservers(COLOR_EDIT);
        });

        this.colorRecordSetting = settingsUI.getColorSetting("Record", CATEGORY_COLORS, DEFAULT_COLOR_RECORD);
        this.colorRecordSetting.addValueObserver(color -> {
            this.colorRecord = color;
            this.notifyObservers(COLOR_RECORD);
        });

        this.colorSoloSetting = settingsUI.getColorSetting("Solo", CATEGORY_COLORS, DEFAULT_COLOR_SOLO);
        this.colorSoloSetting.addValueObserver(color -> {
            this.colorSolo = color;
            this.notifyObservers(COLOR_SOLO);
        });

        this.colorMuteSetting = settingsUI.getColorSetting("Mute", CATEGORY_COLORS, DEFAULT_COLOR_MUTE);
        this.colorMuteSetting.addValueObserver(color -> {
            this.colorMute = color;
            this.notifyObservers(COLOR_MUTE);
        });
    }

}
