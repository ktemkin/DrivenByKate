// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.maschine;

import com.ktemkin.controller.common.CommonUIConfiguration;
import de.mossgrabers.framework.configuration.*;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.midi.ArpeggiatorMode;
import de.mossgrabers.framework.scale.ScaleLayout;

import java.util.List;


/**
 * The configuration settings for Maschine.
 *
 * @author Jürgen Moßgraber
 */
public class MaschineConfiguration extends CommonUIConfiguration {
    //
    // Constants.
    //

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

    //
    // State.
    //
    /**
     * Category for configuring display colors.
     */
    private static final String CATEGORY_COLORS = "Display Colors";
    private final Maschine maschine;
    private final boolean hasDisplay;

    private String serialForDisplay = null;
    private ColorEx colorBackground = DEFAULT_COLOR_BACKGROUND;
    private ColorEx colorBorder = DEFAULT_COLOR_BORDER;
    private ColorEx colorText = DEFAULT_COLOR_TEXT;
    private ColorEx colorFader = DEFAULT_COLOR_FADER;
    private ColorEx colorVU = DEFAULT_COLOR_VU;
    private ColorEx colorEdit = DEFAULT_COLOR_EDIT;
    private ColorEx colorRecord = DEFAULT_COLOR_RECORD;
    private ColorEx colorSolo = DEFAULT_COLOR_SOLO;
    private ColorEx colorMute = DEFAULT_COLOR_MUTE;
    private ColorEx colorBackgroundDarker = DEFAULT_COLOR_BACKGROUND_DARKER;
    private ColorEx colorBackgroundLighter = DEFAULT_COLOR_BACKGROUND_LIGHTER;
    private IColorSetting colorBackgroundSetting;
    private IColorSetting colorBackgroundDarkerSetting;
    private IColorSetting colorBackgroundLighterSetting;
    private IColorSetting colorBorderSetting;
    private IColorSetting colorTextSetting;
    private IColorSetting colorFaderSetting;
    private IColorSetting colorVUSetting;
    private IColorSetting colorEditSetting;
    private IColorSetting colorRecordSetting;
    private IColorSetting colorSoloSetting;
    private IColorSetting colorMuteSetting;
    private IStringSetting serialForDisplaySetting;


    /**
     * Constructor.
     *
     * @param host             The DAW host
     * @param valueChanger     The value changer
     * @param arpeggiatorModes The available arpeggiator modes
     * @param maschine         The type of Maschine
     */
    public MaschineConfiguration(final IHost host, final IValueChanger valueChanger, final List<ArpeggiatorMode> arpeggiatorModes, final Maschine maschine) {
        super(host, valueChanger, arpeggiatorModes);

        this.maschine = maschine;

        // FIXME(ktemkin): choose this based on our model
        this.hasDisplay = true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final ISettingsUI globalSettings, final ISettingsUI documentSettings) {
        super.init(globalSettings, documentSettings);

        ///////////////////////////
        // Workflow

        final int footswitches = this.maschine.getFootswitches();
        if (footswitches >= 2) {
            this.activateFootswitchSetting(globalSettings, 0, "Footswitch (Tip)");
            this.activateFootswitchSetting(globalSettings, 1, "Footswitch (Ring)");

            if (footswitches == 4) {
                this.activateFootswitchSetting(globalSettings, 2, "Footswitch 2 (Tip)");
                this.activateFootswitchSetting(globalSettings, 3, "Footswitch 2 (Ring)");
            }
        }

        ///////////////////////////
        // Pads

        this.activateConvertAftertouchSetting(globalSettings);

        ///////////////////////////
        // Display colors
        this.activateDisplayColorSettings(globalSettings);

        ///////////////////////////
        // Display.
        this.serialForDisplaySetting = globalSettings.getStringSetting("Serial Number (multi-display)", "Misc", 8, "");
        this.serialForDisplaySetting.addValueObserver(serial -> this.serialForDisplay = serial);

    }

    /** {@inheritDoc} */
    @Override
    protected void activateScaleLayoutSetting(ISettingsUI settingsUI) {
        // Default to having a scale layout of SEQUENTIAL UP, since we only have four pads.
        this.activateScaleLayoutSetting(settingsUI, ScaleLayout.SEQUENT_UP.getName());
    }


    /**
     * Activate the color settings for the NI-style display.
     *
     * @param settingsUI The settings
     */
    private void activateDisplayColorSettings(final ISettingsUI settingsUI) {
        if (!this.hasDisplay) {
            return;
        }

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

    //
    // Graphics configuration methods.
    //

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
     * Returns the serial number the user wants to use to display relevant data.
     */
    public String getSerialForDisplay() {
        return this.serialForDisplay;
    }

}
