// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ableton.push.controller;

import com.ktemkin.controller.common.controller.CommonUIColorManager;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.color.ColorIndexException;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.grid.IPadGrid;
import de.mossgrabers.framework.daw.DAWColor;
import de.mossgrabers.framework.featuregroup.AbstractFeatureGroup;
import de.mossgrabers.framework.featuregroup.AbstractMode;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.utils.Pair;
import de.mossgrabers.framework.view.AbstractPlayView;
import de.mossgrabers.framework.view.AbstractSessionView;
import de.mossgrabers.framework.view.ScenePlayView;
import de.mossgrabers.framework.view.sequencer.AbstractDrumView;
import de.mossgrabers.framework.view.sequencer.AbstractSequencerView;
import de.mossgrabers.framework.view.sequencer.ClipLengthView;

import java.util.List;


/**
 * Different colors to use for the pads and buttons of Push 1 and Push 2.
 *
 * @author Jürgen Moßgraber
 */
public class PushColorManager extends CommonUIColorManager {

    /**
     * ID for color when button signals a recording state.
     */
    public static final String PUSH_BUTTON_STATE_REC_ON = "PUSH_BUTTON_STATE_REC_ON";

    /**
     * ID for color when button signals an activated recording state.
     */
    public static final String PUSH_BUTTON_STATE_REC_HI = "PUSH_BUTTON_STATE_REC_HI";

    /**
     * ID for color when button signals an overwrite state.
     */
    public static final String PUSH_BUTTON_STATE_OVR_ON = "PUSH_BUTTON_STATE_OVR_ON";

    /**
     * ID for color when button signals an activated overwrite state.
     */
    public static final String PUSH_BUTTON_STATE_OVR_HI = "PUSH_BUTTON_STATE_OVR_HI";

    /**
     * ID for color when button signals a play state.
     */
    public static final String PUSH_BUTTON_STATE_PLAY_ON = "PUSH_BUTTON_STATE_PLAY_ON";

    /**
     * ID for color when button signals an activated play state.
     */
    public static final String PUSH_BUTTON_STATE_PLAY_HI = "PUSH_BUTTON_STATE_PLAY_HI";

    /**
     * ID for color when button signals a mute state.
     */
    public static final String PUSH_BUTTON_STATE_MUTE_ON = "PUSH_BUTTON_STATE_MUTE_ON";

    /**
     * ID for color when button signals an activated mute state.
     */
    public static final String PUSH_BUTTON_STATE_MUTE_HI = "PUSH_BUTTON_STATE_MUTE_HI";

    /**
     * ID for color when button signals a solo state.
     */
    public static final String PUSH_BUTTON_STATE_SOLO_ON = "PUSH_BUTTON_STATE_SOLO_ON";

    /**
     * ID for color when button signals an activated solo state.
     */
    public static final String PUSH_BUTTON_STATE_SOLO_HI = "PUSH_BUTTON_STATE_SOLO_HI";

    /**
     * ID for color when button signals a stop clip state.
     */
    public static final String PUSH_BUTTON_STATE_STOP_ON = "PUSH_BUTTON_STATE_STOP_ON";

    /**
     * ID for color when button signals an activated stop clip state.
     */
    public static final String PUSH_BUTTON_STATE_STOP_HI = "PUSH_BUTTON_STATE_STOP_HI";

    /**
     * ID for the color to use for note repeat resolution.
     */
    public static final String NOTE_REPEAT_PERIOD_OFF = "NOTE_REPEAT_PERIOD_OFF";

    /**
     * ID for the color to use for note repeat resolution selected.
     */
    public static final String NOTE_REPEAT_PERIOD_HI = "NOTE_REPEAT_PERIOD_HI";

    /**
     * ID for the color to use for note repeat length.
     */
    public static final String NOTE_REPEAT_LENGTH_OFF = "NOTE_REPEAT_LENGTH_OFF";

    /**
     * ID for the color to use for note repeat length selected.
     */
    public static final String NOTE_REPEAT_LENGTH_HI = "NOTE_REPEAT_LENGTH_HI";

    // @formatter:off
    // Second row & Pad button colors
    public static final int PUSH2_COLOR2_BLACK = 0;
    // @formatter:on
    public static final int PUSH2_COLOR2_GREY_LO = 1;

    public static final int PUSH2_COLOR2_GREY_MD = 103;

    public static final int PUSH2_COLOR2_GREY_LT = 2;

    public static final int PUSH2_COLOR2_WHITE = 3;

    public static final int PUSH2_COLOR2_ROSE = 4;

    public static final int PUSH2_COLOR2_RED_HI = 5;

    public static final int PUSH2_COLOR2_RED = 6;

    public static final int PUSH2_COLOR2_RED_LO = 7;

    public static final int PUSH2_COLOR2_RED_AMBER = 8;

    public static final int PUSH2_COLOR2_AMBER_HI = 9;

    public static final int PUSH2_COLOR2_AMBER = 10;

    public static final int PUSH2_COLOR2_AMBER_LO = 11;

    public static final int PUSH2_COLOR2_AMBER_YELLOW = 12;

    public static final int PUSH2_COLOR2_YELLOW_HI = 13;

    public static final int PUSH2_COLOR2_YELLOW = 14;

    public static final int PUSH2_COLOR2_YELLOW_LO = 15;

    public static final int PUSH2_COLOR2_YELLOW_LIME = 16;

    public static final int PUSH2_COLOR2_LIME_HI = 17;

    public static final int PUSH2_COLOR2_LIME = 18;

    public static final int PUSH2_COLOR2_LIME_LO = 19;

    public static final int PUSH2_COLOR2_LIME_GREEN = 20;

    public static final int PUSH2_COLOR2_GREEN_HI = 21;

    public static final int PUSH2_COLOR2_GREEN = 22;

    public static final int PUSH2_COLOR2_GREEN_LO = 23;

    public static final int PUSH2_COLOR2_GREEN_SPRING = 24;

    public static final int PUSH2_COLOR2_SPRING_HI = 25;

    public static final int PUSH2_COLOR2_SPRING = 26;

    public static final int PUSH2_COLOR2_SPRING_LO = 27;

    public static final int PUSH2_COLOR2_SPRING_TURQUOISE = 28;

    public static final int PUSH2_COLOR2_TURQUOISE_LO = 29;

    public static final int PUSH2_COLOR2_TURQUOISE = 30;

    public static final int PUSH2_COLOR2_TURQUOISE_HI = 31;

    public static final int PUSH2_COLOR2_TURQUOISE_CYAN = 32;

    public static final int PUSH2_COLOR2_CYAN_HI = 33;

    public static final int PUSH2_COLOR2_CYAN = 34;

    public static final int PUSH2_COLOR2_CYAN_LO = 35;

    public static final int PUSH2_COLOR2_CYAN_SKY = 36;

    public static final int PUSH2_COLOR2_SKY_HI = 37;

    public static final int PUSH2_COLOR2_SKY = 38;

    public static final int PUSH2_COLOR2_SKY_LO = 39;

    public static final int PUSH2_COLOR2_SKY_OCEAN = 40;

    public static final int PUSH2_COLOR2_OCEAN_HI = 41;

    public static final int PUSH2_COLOR2_OCEAN = 42;

    public static final int PUSH2_COLOR2_OCEAN_LO = 43;

    public static final int PUSH2_COLOR2_OCEAN_BLUE = 44;

    public static final int PUSH2_COLOR2_BLUE_HI = 45;

    public static final int PUSH2_COLOR2_BLUE = 46;

    public static final int PUSH2_COLOR2_BLUE_LO = 47;

    public static final int PUSH2_COLOR2_BLUE_ORCHID = 48;

    public static final int PUSH2_COLOR2_ORCHID_HI = 49;

    public static final int PUSH2_COLOR2_ORCHID = 50;

    public static final int PUSH2_COLOR2_ORCHID_LO = 51;

    public static final int PUSH2_COLOR2_ORCHID_MAGENTA = 52;

    public static final int PUSH2_COLOR2_MAGENTA_HI = 53;

    public static final int PUSH2_COLOR2_MAGENTA = 54;

    public static final int PUSH2_COLOR2_MAGENTA_LO = 55;

    public static final int PUSH2_COLOR2_MAGENTA_PINK = 56;

    public static final int PUSH2_COLOR2_PINK_HI = 57;

    public static final int PUSH2_COLOR2_PINK = 58;

    public static final int PUSH2_COLOR2_PINK_LO = 59;

    public static final int PUSH2_COLOR2_SILVER = 118;

    public static final int PUSH2_COLOR2_ORANGE = 65;

    public static final int PUSH2_COLOR2_ORANGE_LIGHT = 3;

    public static final int PUSH2_COLOR2_LIGHT_BROWN = 69;

    // First row colors
    public static final int PUSH2_COLOR_BLACK = 0;

    public static final int PUSH2_COLOR_RED_LO = PUSH2_COLOR2_RED_LO;

    public static final int PUSH2_COLOR_RED_LO_SBLINK = 2;

    public static final int PUSH2_COLOR_RED_LO_FBLINK = 3;

    public static final int PUSH2_COLOR_RED_HI = PUSH2_COLOR2_RED_HI;

    public static final int PUSH2_COLOR_RED_HI_SBLINK = 5;

    public static final int PUSH2_COLOR_RED_HI_FBLINK = 6;

    public static final int PUSH2_COLOR_ORANGE_LO = PUSH2_COLOR2_AMBER_LO;

    public static final int PUSH2_COLOR_ORANGE_LO_SBLINK = 8;

    public static final int PUSH2_COLOR_ORANGE_LO_FBLINK = 9;

    public static final int PUSH2_COLOR_ORANGE_HI = PUSH2_COLOR2_AMBER_HI;

    public static final int PUSH2_COLOR_ORANGE_HI_SBLINK = 11;

    public static final int PUSH2_COLOR_ORANGE_HI_FBLINK = 12;

    public static final int PUSH2_COLOR_YELLOW_LO = PUSH2_COLOR2_YELLOW_LO;

    public static final int PUSH2_COLOR_YELLOW_LO_SBLINK = 14;

    public static final int PUSH2_COLOR_YELLOW_LO_FBLINK = 15;

    public static final int PUSH2_COLOR_YELLOW_MD = PUSH2_COLOR2_YELLOW_HI;

    public static final int PUSH2_COLOR_YELLOW_MD_SBLINK = 17;

    public static final int PUSH2_COLOR_YELLOW_MD_FBLINK = 18;

    public static final int PUSH2_COLOR_GREEN_LO = PUSH2_COLOR2_GREEN_LO;

    public static final int PUSH2_COLOR_GREEN_LO_SBLINK = 20;

    public static final int PUSH2_COLOR_GREEN_LO_FBLINK = 21;

    public static final int PUSH2_COLOR_GREEN_HI = PUSH2_COLOR2_GREEN_HI;

    public static final int PUSH2_COLOR_GREEN_HI_SBLINK = 23;

    public static final int PUSH2_COLOR_GREEN_HI_FBLINK = 24;

    // Scene button colors
    public static final int PUSH2_COLOR_SCENE_RED = PUSH2_COLOR2_RED;

    public static final int PUSH2_COLOR_SCENE_RED_BLINK = 2;

    public static final int PUSH2_COLOR_SCENE_RED_BLINK_FAST = 3;

    public static final int PUSH2_COLOR_SCENE_RED_HI = PUSH2_COLOR2_RED_HI;

    public static final int PUSH2_COLOR_SCENE_RED_HI_BLINK = 5;

    public static final int PUSH2_COLOR_SCENE_RED_HI_BLINK_FAST = 6;

    public static final int PUSH2_COLOR_SCENE_ORANGE = PUSH2_COLOR2_AMBER;

    public static final int PUSH2_COLOR_SCENE_ORANGE_BLINK = 8;

    public static final int PUSH2_COLOR_SCENE_ORANGE_BLINK_FAST = 9;

    public static final int PUSH2_COLOR_SCENE_ORANGE_HI = PUSH2_COLOR2_AMBER_HI;

    public static final int PUSH2_COLOR_SCENE_ORANGE_HI_BLINK = 11;

    public static final int PUSH2_COLOR_SCENE_ORANGE_HI_BLINK_FAST = 12;

    public static final int PUSH2_COLOR_SCENE_YELLOW = PUSH2_COLOR2_YELLOW;

    public static final int PUSH2_COLOR_SCENE_YELLOW_BLINK = 14;

    public static final int PUSH2_COLOR_SCENE_YELLOW_BLINK_FAST = 15;

    public static final int PUSH2_COLOR_SCENE_YELLOW_HI = PUSH2_COLOR2_YELLOW_HI;

    public static final int PUSH2_COLOR_SCENE_YELLOW_HI_BLINK = 17;

    public static final int PUSH2_COLOR_SCENE_YELLOW_HI_BLINK_FAST = 18;

    public static final int PUSH2_COLOR_SCENE_GREEN = PUSH2_COLOR2_GREEN;

    public static final int PUSH2_COLOR_SCENE_GREEN_BLINK = 20;

    public static final int PUSH2_COLOR_SCENE_GREEN_BLINK_FAST = 21;

    public static final int PUSH2_COLOR_SCENE_GREEN_HI = PUSH2_COLOR2_GREEN_HI;

    public static final int PUSH2_COLOR_SCENE_GREEN_HI_BLINK = 23;

    public static final int PUSH2_COLOR_SCENE_GREEN_HI_BLINK_FAST = 24;

    public static final int PUSH2_COLOR_SCENE_WHITE = 60;


    public static final String PUSH_RED = "PUSH_RED";

    public static final String PUSH_RED_LO = "PUSH_RED_LO";

    public static final String PUSH_RED_HI = "PUSH_RED_HI";

    public static final String PUSH_ORANGE_LO = "PUSH_ORANGE_LO";

    public static final String PUSH_ORANGE_HI = "PUSH_ORANGE_HI";

    public static final String PUSH_YELLOW_LO = "PUSH_YELLOW_LO";

    public static final String PUSH_YELLOW_MD = "PUSH_YELLOW_MD";

    public static final String PUSH_GREEN_LO = "PUSH_GREEN_LO";

    public static final String PUSH_GREEN_HI = "PUSH_GREEN_HI";


    public static final String PUSH_BLACK = "PUSH_BLACK";

    public static final String PUSH_BLACK_2 = "PUSH_BLACK_2";

    public static final String PUSH_WHITE_2 = "PUSH_WHITE_2";

    public static final String PUSH_GREY_LO_2 = "PUSH_GREY_LO_2";

    public static final String PUSH_GREEN_2 = "PUSH_GREEN_2";

    /**
     * The default color palette (like fixed on Push 1)
     */
    protected static final int[][] DEFAULT_PALETTE =
            {
                    {0x00, 0x00, 0x00},
                    {0x1E, 0x1E, 0x1E},
                    {0x7F, 0x7F, 0x7F},
                    {0xFF, 0xFF, 0xFF},
                    {0xFF, 0x4C, 0x4C},
                    {0xFF, 0x00, 0x00},
                    {0x59, 0x00, 0x00},
                    {0x19, 0x00, 0x00},
                    {0xFF, 0xBD, 0x6C},
                    {0xFF, 0x54, 0x00},
                    {0x59, 0x1D, 0x00},
                    {0x27, 0x1B, 0x00},
                    {0xFF, 0xFF, 0x4C},
                    {0xFF, 0xFF, 0x00},
                    {0x59, 0x59, 0x00},
                    {0x19, 0x19, 0x00},
                    {0x88, 0xFF, 0x4C},
                    {0x54, 0xFF, 0x00},
                    {0x1D, 0x59, 0x00},
                    {0x14, 0x2B, 0x00},
                    {0x4C, 0xFF, 0x4C},
                    {0x00, 0xFF, 0x00},
                    {0x00, 0x59, 0x00},
                    {0x00, 0x19, 0x00},
                    {0x4C, 0xFF, 0x5E},
                    {0x00, 0xFF, 0x19},
                    {0x00, 0x59, 0x0D},
                    {0x00, 0x19, 0x02},
                    {0x4C, 0xFF, 0x88},
                    {0x00, 0xFF, 0x55},
                    {0x00, 0x59, 0x1D},
                    {0x00, 0x1F, 0x12},
                    {0x4C, 0xFF, 0xB7},
                    {0x00, 0xFF, 0x99},
                    {0x00, 0x59, 0x35},
                    {0x00, 0x19, 0x12},
                    {0x4C, 0xC3, 0xFF},
                    {0x00, 0xA9, 0xFF},
                    {0x00, 0x41, 0x52},
                    {0x00, 0x10, 0x19},
                    {0x4C, 0x88, 0xFF},
                    {0x00, 0x55, 0xFF},
                    {0x00, 0x1D, 0x59},
                    {0x00, 0x08, 0x19},
                    {0x4C, 0x4C, 0xFF},
                    {0x00, 0x00, 0xFF},
                    {0x00, 0x00, 0x59},
                    {0x00, 0x00, 0x19},
                    {0x87, 0x4C, 0xFF},
                    {0x54, 0x00, 0xFF},
                    {0x19, 0x00, 0x64},
                    {0x0F, 0x00, 0x30},
                    {0xFF, 0x4C, 0xFF},
                    {0xFF, 0x00, 0xFF},
                    {0x59, 0x00, 0x59},
                    {0x19, 0x00, 0x19},
                    {0xFF, 0x4C, 0x87},
                    {0xFF, 0x00, 0x54},
                    {0x59, 0x00, 0x1D},
                    {0x22, 0x00, 0x13},
                    {0xFF, 0x15, 0x00},
                    {0x99, 0x35, 0x00},
                    {0x79, 0x51, 0x00},
                    {0x43, 0x64, 0x00},
                    {0x03, 0x39, 0x00},
                    {0x00, 0x57, 0x35},
                    {0x00, 0x54, 0x7F},
                    {0x00, 0x00, 0xFF},
                    {0x00, 0x45, 0x4F},
                    {0x25, 0x00, 0xCC},
                    {0x7F, 0x7F, 0x7F},
                    {0x20, 0x20, 0x20},
                    {0xFF, 0x00, 0x00},
                    {0xBD, 0xFF, 0x2D},
                    {0xAF, 0xED, 0x06},
                    {0x64, 0xFF, 0x09},
                    {0x10, 0x8B, 0x00},
                    {0x00, 0xFF, 0x87},
                    {0x00, 0xA9, 0xFF},
                    {0x00, 0x2A, 0xFF},
                    {0x3F, 0x00, 0xFF},
                    {0x7A, 0x00, 0xFF},
                    {0xB2, 0x1A, 0x7D},
                    {0x40, 0x21, 0x00},
                    {0xFF, 0x4A, 0x00},
                    {0x88, 0xE1, 0x06},
                    {0x72, 0xFF, 0x15},
                    {0x00, 0xFF, 0x00},
                    {0x3B, 0xFF, 0x26},
                    {0x59, 0xFF, 0x71},
                    {0x38, 0xFF, 0xCC},
                    {0x5B, 0x8A, 0xFF},
                    {0x31, 0x51, 0xC6},
                    {0x87, 0x7F, 0xE9},
                    {0xD3, 0x1D, 0xFF},
                    {0xFF, 0x00, 0x5D},
                    {0xFF, 0x7F, 0x00},
                    {0xB9, 0xB0, 0x00},
                    {0x90, 0xFF, 0x00},
                    {0x83, 0x5D, 0x07},
                    {0x39, 0x2B, 0x00},
                    {0x14, 0x4C, 0x10},
                    {0x0D, 0x50, 0x38},
                    {0x15, 0x15, 0x2A},
                    {0x16, 0x20, 0x5A},
                    {0x69, 0x3C, 0x1C},
                    {0xA8, 0x00, 0x0A},
                    {0xDE, 0x51, 0x3D},
                    {0xD8, 0x6A, 0x1C},
                    {0xFF, 0xE1, 0x26},
                    {0x9E, 0xE1, 0x2F},
                    {0x67, 0xB5, 0x0F},
                    {0x1E, 0x1E, 0x30},
                    {0xDC, 0xFF, 0x6B},
                    {0x80, 0xFF, 0xBD},
                    {0x9A, 0x99, 0xFF},
                    {0x8E, 0x66, 0xFF},
                    {0x40, 0x40, 0x40},
                    {0x75, 0x75, 0x75},
                    {0xE0, 0xFF, 0xFF},
                    {0xA0, 0x00, 0x00},
                    {0x35, 0x00, 0x00},
                    {0x1A, 0xD0, 0x00},
                    {0x07, 0x42, 0x00},
                    {0xB9, 0xB0, 0x00},
                    {0x3F, 0x31, 0x00},
                    {0xB3, 0x5F, 0x00},
                    {0x4B, 0x15, 0x02}
            };


    /**
     * Array that stores a set of display colors.
     * Each color should match its corresponding index in ``firstRowdeviceColors``.
     */
    protected final ColorEx[] firstRowDisplayColors;


    /**
     * Array that stores a set of device colors.
     * Each color should match its corresponding index in ``firstRowdisplayColors``.
     */
    protected final int[] firstRowDeviceColors;


    /**
     * Private due to utility class.
     */
    public PushColorManager() {
        this.registerColorIndex(PUSH_BLACK, PUSH2_COLOR_BLACK);
        this.registerColorIndex(PUSH_RED, PUSH2_COLOR_RED_HI);
        this.registerColorIndex(PUSH_RED_LO, PUSH2_COLOR_RED_LO);
        this.registerColorIndex(PUSH_RED_HI, PUSH2_COLOR_RED_HI);
        this.registerColorIndex(PUSH_ORANGE_LO, PUSH2_COLOR_ORANGE_LO);
        this.registerColorIndex(PUSH_ORANGE_HI, PUSH2_COLOR_ORANGE_HI);
        this.registerColorIndex(PUSH_YELLOW_LO, PUSH2_COLOR_YELLOW_LO);
        this.registerColorIndex(PUSH_YELLOW_MD, PUSH2_COLOR_YELLOW_MD);
        this.registerColorIndex(PUSH_GREEN_LO, PUSH2_COLOR_GREEN_LO);
        this.registerColorIndex(PUSH_GREEN_HI, PUSH2_COLOR_GREEN_HI);

        this.registerColorIndex(PUSH_BLACK_2, PUSH2_COLOR2_BLACK);
        this.registerColorIndex(PUSH_WHITE_2, PUSH2_COLOR2_WHITE);
        this.registerColorIndex(PUSH_GREY_LO_2, PUSH2_COLOR2_GREY_LO);
        this.registerColorIndex(PUSH_GREEN_2, PUSH2_COLOR2_GREEN);

        this.registerColorIndex(Scales.SCALE_COLOR_OFF, PUSH2_COLOR2_BLACK);
        this.registerColorIndex(Scales.SCALE_COLOR_OCTAVE, PUSH2_COLOR2_OCEAN_HI);
        this.registerColorIndex(Scales.SCALE_COLOR_NOTE, PUSH2_COLOR2_WHITE);
        this.registerColorIndex(Scales.SCALE_COLOR_OUT_OF_SCALE, PUSH2_COLOR_BLACK);

        this.registerColorIndex(AbstractFeatureGroup.BUTTON_COLOR_OFF, PUSH2_COLOR_BLACK);
        this.registerColorIndex(AbstractFeatureGroup.BUTTON_COLOR_ON, PUSH2_COLOR2_GREY_LO);
        this.registerColorIndex(AbstractMode.BUTTON_COLOR_HI, PUSH2_COLOR2_WHITE);
        this.registerColorIndex(AbstractMode.BUTTON_COLOR2_ON, PUSH2_COLOR2_GREY_LO);
        this.registerColorIndex(AbstractMode.BUTTON_COLOR2_HI, PUSH2_COLOR2_WHITE);

        this.registerColorIndex(AbstractSequencerView.COLOR_NO_CONTENT, PUSH2_COLOR2_BLACK);
        this.registerColorIndex(AbstractSequencerView.COLOR_NO_CONTENT_4, PUSH2_COLOR2_BLACK);
        this.registerColorIndex(AbstractSequencerView.COLOR_CONTENT, PUSH2_COLOR2_BLUE_HI);
        this.registerColorIndex(AbstractSequencerView.COLOR_CONTENT_CONT, PUSH2_COLOR2_BLUE_LO);
        this.registerColorIndex(AbstractSequencerView.COLOR_STEP_HILITE_NO_CONTENT, PUSH2_COLOR2_GREEN_LO);
        this.registerColorIndex(AbstractSequencerView.COLOR_STEP_HILITE_CONTENT, PUSH2_COLOR2_GREEN_HI);
        this.registerColorIndex(AbstractSequencerView.COLOR_STEP_MUTED, PUSH2_COLOR2_GREY_MD);
        this.registerColorIndex(AbstractSequencerView.COLOR_STEP_MUTED_CONT, PUSH2_COLOR2_GREY_LO);
        this.registerColorIndex(AbstractSequencerView.COLOR_STEP_SELECTED, PUSH2_COLOR2_YELLOW_HI);
        this.registerColorIndex(AbstractSequencerView.COLOR_PAGE, PUSH2_COLOR2_WHITE);
        this.registerColorIndex(AbstractSequencerView.COLOR_ACTIVE_PAGE, PUSH2_COLOR2_GREEN);
        this.registerColorIndex(AbstractSequencerView.COLOR_SELECTED_PAGE, PUSH2_COLOR2_OCEAN_HI);
        this.registerColorIndex(AbstractSequencerView.COLOR_RESOLUTION, PUSH2_COLOR_SCENE_ORANGE);
        this.registerColorIndex(AbstractSequencerView.COLOR_RESOLUTION_SELECTED, PUSH2_COLOR_SCENE_ORANGE_HI);
        this.registerColorIndex(AbstractSequencerView.COLOR_RESOLUTION_OFF, PUSH2_COLOR2_BLACK);
        this.registerColorIndex(AbstractSequencerView.COLOR_TRANSPOSE, PUSH2_COLOR_SCENE_WHITE);
        this.registerColorIndex(AbstractSequencerView.COLOR_TRANSPOSE_SELECTED, PUSH2_COLOR_SCENE_YELLOW_HI);

        this.registerColorIndex(AbstractDrumView.COLOR_PAD_OFF, PUSH2_COLOR_BLACK);
        this.registerColorIndex(AbstractDrumView.COLOR_PAD_RECORD, PUSH2_COLOR2_RED_HI);
        this.registerColorIndex(AbstractDrumView.COLOR_PAD_PLAY, PUSH2_COLOR2_GREEN_HI);
        this.registerColorIndex(AbstractDrumView.COLOR_PAD_SELECTED, PUSH2_COLOR2_BLUE_HI);
        this.registerColorIndex(AbstractDrumView.COLOR_PAD_MUTED, PUSH2_COLOR2_AMBER_LO);
        this.registerColorIndex(AbstractDrumView.COLOR_PAD_HAS_CONTENT, PUSH2_COLOR2_YELLOW_HI);
        this.registerColorIndex(AbstractDrumView.COLOR_PAD_NO_CONTENT, PUSH2_COLOR2_YELLOW_LO);

        this.registerColorIndex(AbstractPlayView.COLOR_PLAY, PUSH2_COLOR2_GREEN_HI);
        this.registerColorIndex(AbstractPlayView.COLOR_RECORD, PUSH2_COLOR2_RED_HI);
        this.registerColorIndex(AbstractPlayView.COLOR_OFF, PUSH2_COLOR2_BLACK);

        this.registerColorIndex(ClipLengthView.COLOR_OUTSIDE, PUSH2_COLOR_BLACK);
        this.registerColorIndex(ClipLengthView.COLOR_PART, PUSH2_COLOR2_OCEAN_HI);

        this.registerColorIndex(AbstractSessionView.COLOR_SCENE, PUSH2_COLOR_SCENE_GREEN);
        this.registerColorIndex(AbstractSessionView.COLOR_SELECTED_SCENE, PUSH2_COLOR_SCENE_GREEN_HI);
        this.registerColorIndex(AbstractSessionView.COLOR_SCENE_OFF, PUSH2_COLOR2_BLACK);

        this.registerColorIndex(ScenePlayView.COLOR_SELECTED_PLAY_SCENE, PUSH2_COLOR2_WHITE);

        this.registerColorIndex(IPadGrid.GRID_OFF, PUSH2_COLOR2_BLACK);

        this.registerColorIndex(NOTE_REPEAT_PERIOD_OFF, PUSH2_COLOR_SCENE_YELLOW);
        this.registerColorIndex(NOTE_REPEAT_PERIOD_HI, PUSH2_COLOR_SCENE_YELLOW_HI);
        this.registerColorIndex(NOTE_REPEAT_LENGTH_OFF, PUSH2_COLOR_SCENE_RED);
        this.registerColorIndex(NOTE_REPEAT_LENGTH_HI, PUSH2_COLOR_SCENE_RED_HI);

        // Push 2 DAW colors are set in the color palette from indices 70 to 96
        this.registerColorIndex(DAWColor.COLOR_OFF, PUSH2_COLOR2_BLACK);
        this.registerColorIndex(DAWColor.DAW_COLOR_GRAY_HALF, 70);
        this.registerColorIndex(DAWColor.DAW_COLOR_DARK_GRAY, 71);
        this.registerColorIndex(DAWColor.DAW_COLOR_GRAY, 72);
        this.registerColorIndex(DAWColor.DAW_COLOR_LIGHT_GRAY, 73);
        this.registerColorIndex(DAWColor.DAW_COLOR_SILVER, 74);
        this.registerColorIndex(DAWColor.DAW_COLOR_DARK_BROWN, 75);
        this.registerColorIndex(DAWColor.DAW_COLOR_BROWN, 76);
        this.registerColorIndex(DAWColor.DAW_COLOR_DARK_BLUE, 77);
        this.registerColorIndex(DAWColor.DAW_COLOR_PURPLE_BLUE, 78);
        this.registerColorIndex(DAWColor.DAW_COLOR_PURPLE, 79);
        this.registerColorIndex(DAWColor.DAW_COLOR_PINK, 80);
        this.registerColorIndex(DAWColor.DAW_COLOR_RED, 81);
        this.registerColorIndex(DAWColor.DAW_COLOR_ORANGE, 82);
        this.registerColorIndex(DAWColor.DAW_COLOR_LIGHT_ORANGE, 83);
        this.registerColorIndex(DAWColor.DAW_COLOR_MOSS_GREEN, 84);
        this.registerColorIndex(DAWColor.DAW_COLOR_GREEN, 85);
        this.registerColorIndex(DAWColor.DAW_COLOR_COLD_GREEN, 86);
        this.registerColorIndex(DAWColor.DAW_COLOR_BLUE, 87);
        this.registerColorIndex(DAWColor.DAW_COLOR_LIGHT_PURPLE, 88);
        this.registerColorIndex(DAWColor.DAW_COLOR_LIGHT_PINK, 89);
        this.registerColorIndex(DAWColor.DAW_COLOR_ROSE, 90);
        this.registerColorIndex(DAWColor.DAW_COLOR_REDDISH_BROWN, 91);
        this.registerColorIndex(DAWColor.DAW_COLOR_LIGHT_BROWN, 92);
        this.registerColorIndex(DAWColor.DAW_COLOR_LIGHT_GREEN, 93);
        this.registerColorIndex(DAWColor.DAW_COLOR_BLUISH_GREEN, 94);
        this.registerColorIndex(DAWColor.DAW_COLOR_GREEN_BLUE, 95);
        this.registerColorIndex(DAWColor.DAW_COLOR_LIGHT_BLUE, 96);

        this.registerColorIndex(ColorManager.BUTTON_STATE_OFF, 0);
        this.registerColorIndex(ColorManager.BUTTON_STATE_ON, 8);
        this.registerColorIndex(ColorManager.BUTTON_STATE_HI, 127);
        this.registerColorIndex(PUSH_BUTTON_STATE_REC_ON, PUSH2_COLOR2_GREY_LO);
        this.registerColorIndex(PUSH_BUTTON_STATE_REC_HI, PUSH2_COLOR2_RED_HI);
        this.registerColorIndex(PUSH_BUTTON_STATE_OVR_ON, PUSH2_COLOR2_GREY_LO);
        this.registerColorIndex(PUSH_BUTTON_STATE_OVR_HI, PUSH2_COLOR2_AMBER);
        this.registerColorIndex(PUSH_BUTTON_STATE_PLAY_ON, PUSH2_COLOR2_GREY_LO);
        this.registerColorIndex(PUSH_BUTTON_STATE_PLAY_HI, PUSH2_COLOR2_GREEN_HI);
        this.registerColorIndex(PUSH_BUTTON_STATE_MUTE_ON, PUSH2_COLOR2_GREY_LO);
        this.registerColorIndex(PUSH_BUTTON_STATE_MUTE_HI, PUSH2_COLOR2_AMBER_LO);
        this.registerColorIndex(PUSH_BUTTON_STATE_SOLO_ON, PUSH2_COLOR2_GREY_LO);
        this.registerColorIndex(PUSH_BUTTON_STATE_SOLO_HI, PUSH2_COLOR2_YELLOW);
        this.registerColorIndex(PUSH_BUTTON_STATE_STOP_ON, PUSH2_COLOR2_RED_LO);
        this.registerColorIndex(PUSH_BUTTON_STATE_STOP_HI, PUSH2_COLOR2_RED_HI);

        for (int i = 0; i < 128; i++) {
            this.registerColor(i, getPaletteColor(i));
        }

        //
        // Populate our first-row color mappings for lookup.
        //
        var colorMappings = this.getFirstRowColorMappings();

        this.firstRowDeviceColors = new int[colorMappings.size()];
        this.firstRowDisplayColors = new ColorEx[colorMappings.size()];

        // Break our mappings into two arrays, which is what our color utilities like.
        var index = 0;
        for (var pair : colorMappings) {
            this.firstRowDeviceColors[index] = pair.getKey();
            this.firstRowDisplayColors[index] = pair.getValue();
            index += 1;
        }
    }

    /**
     * Get a color entry of the default Push color palette.
     *
     * @param index 0-127
     * @return The palette color as RGB (0-255)
     */
    public static int[] getPaletteColorRGB(final int index) {
        if (index >= 70 && index <= 96) {
            return DAWColor.getColorEntry(index - 69).toIntRGB255();
        }
        return DEFAULT_PALETTE[index];
    }

    /**
     * Get a color of the default Push color palette.
     *
     * @param index 0-127
     * @return The palette color
     */
    public static ColorEx getPaletteColor(final int index) {
        if (index >= 70 && index <= 96) {
            return DAWColor.getColorEntry(index - 69);
        }
        return ColorEx.fromRGB(DEFAULT_PALETTE[index][0], DEFAULT_PALETTE[index][1], DEFAULT_PALETTE[index][2]);
    }

    @Override
    protected List<Pair<Integer, ColorEx>> getColorMappings() {
        // FIXME(ktemkin): set these to more accurately match the push
        return List.of(
                new Pair<>(PUSH2_COLOR2_BLACK, ColorEx.BLACK),
                new Pair<>(PUSH2_COLOR2_GREY_LO, lo(ColorEx.DARK_GRAY)),
                new Pair<>(PUSH2_COLOR2_GREY_MD, ColorEx.GRAY),
                new Pair<>(PUSH2_COLOR2_GREY_LT, ColorEx.LIGHT_GRAY),
                new Pair<>(PUSH2_COLOR2_WHITE, ColorEx.WHITE),
                new Pair<>(PUSH2_COLOR2_ROSE, hi(ColorEx.PINK)),
                new Pair<>(PUSH2_COLOR2_RED_HI, hi(ColorEx.RED)),
                new Pair<>(PUSH2_COLOR2_RED, ColorEx.RED_WINE),
                new Pair<>(PUSH2_COLOR2_RED_LO, ColorEx.DARK_RED),
                new Pair<>(PUSH2_COLOR2_RED_AMBER, lo(ColorEx.DARK_RED)),
                new Pair<>(PUSH2_COLOR2_AMBER_HI, hi(hi(ColorEx.YELLOW))),
                new Pair<>(PUSH2_COLOR2_AMBER, hi(ColorEx.YELLOW)),
                new Pair<>(PUSH2_COLOR2_AMBER_LO, ColorEx.DARK_YELLOW),
                new Pair<>(PUSH2_COLOR2_AMBER_YELLOW, ColorEx.YELLOW),
                new Pair<>(PUSH2_COLOR2_YELLOW_HI, hi(ColorEx.YELLOW)),
                new Pair<>(PUSH2_COLOR2_YELLOW, ColorEx.YELLOW),
                new Pair<>(PUSH2_COLOR2_YELLOW_LO, ColorEx.DARK_YELLOW),
                new Pair<>(PUSH2_COLOR2_YELLOW_LIME, ColorEx.GREEN),
                new Pair<>(PUSH2_COLOR2_LIME_HI, hi(hi(ColorEx.GREEN))),
                new Pair<>(PUSH2_COLOR2_LIME, hi(ColorEx.GREEN)),
                new Pair<>(PUSH2_COLOR2_LIME_LO, ColorEx.DARK_GREEN),
                new Pair<>(PUSH2_COLOR2_LIME_GREEN, ColorEx.GREEN),
                new Pair<>(PUSH2_COLOR2_GREEN_HI, ColorEx.GREEN),
                new Pair<>(PUSH2_COLOR2_GREEN, ColorEx.GREEN),
                new Pair<>(PUSH2_COLOR2_GREEN_LO, ColorEx.DARK_GREEN),
                new Pair<>(PUSH2_COLOR2_GREEN_SPRING, ColorEx.GREEN),
                new Pair<>(PUSH2_COLOR2_SPRING_HI, ColorEx.OLIVE),
                new Pair<>(PUSH2_COLOR2_SPRING, ColorEx.OLIVE),
                new Pair<>(PUSH2_COLOR2_SPRING_LO, ColorEx.DARK_GREEN),
                new Pair<>(PUSH2_COLOR2_SPRING_TURQUOISE, ColorEx.DARK_BLUE),
                new Pair<>(PUSH2_COLOR2_TURQUOISE_LO, ColorEx.DARK_BLUE),
                new Pair<>(PUSH2_COLOR2_TURQUOISE, ColorEx.BLUE),
                new Pair<>(PUSH2_COLOR2_TURQUOISE_HI, ColorEx.CYAN),
                new Pair<>(PUSH2_COLOR2_TURQUOISE_CYAN, ColorEx.CYAN),
                new Pair<>(PUSH2_COLOR2_CYAN_HI, hi(ColorEx.CYAN)),
                new Pair<>(PUSH2_COLOR2_CYAN, ColorEx.CYAN),
                new Pair<>(PUSH2_COLOR2_CYAN_LO, lo(ColorEx.CYAN)),
                new Pair<>(PUSH2_COLOR2_CYAN_SKY, hi(hi(ColorEx.CYAN))),
                new Pair<>(PUSH2_COLOR2_SKY_HI, hi(ColorEx.SKY_BLUE)),
                new Pair<>(PUSH2_COLOR2_SKY, ColorEx.SKY_BLUE),
                new Pair<>(PUSH2_COLOR2_SKY_LO, lo(ColorEx.SKY_BLUE)),
                new Pair<>(PUSH2_COLOR2_SKY_OCEAN, lo(lo(ColorEx.SKY_BLUE))),
                new Pair<>(PUSH2_COLOR2_OCEAN_HI, hi(ColorEx.MINT)),
                new Pair<>(PUSH2_COLOR2_OCEAN, ColorEx.MINT),
                new Pair<>(PUSH2_COLOR2_OCEAN_LO, lo(ColorEx.MINT)),
                new Pair<>(PUSH2_COLOR2_OCEAN_BLUE, ColorEx.BLUE),
                new Pair<>(PUSH2_COLOR2_BLUE_HI, hi(ColorEx.BLUE)),
                new Pair<>(PUSH2_COLOR2_BLUE, ColorEx.BLUE),
                new Pair<>(PUSH2_COLOR2_BLUE_LO, lo(ColorEx.BLUE)),
                new Pair<>(PUSH2_COLOR2_ORCHID_HI, hi(ColorEx.DARK_PURPLE)),
                new Pair<>(PUSH2_COLOR2_ORCHID, ColorEx.DARK_PURPLE),
                new Pair<>(PUSH2_COLOR2_ORCHID_LO, lo(ColorEx.DARK_PURPLE)),
                new Pair<>(PUSH2_COLOR2_MAGENTA_HI, hi(ColorEx.PURPLE)),
                new Pair<>(PUSH2_COLOR2_MAGENTA, ColorEx.PURPLE),
                new Pair<>(PUSH2_COLOR2_MAGENTA_LO, lo(ColorEx.PURPLE)),
                new Pair<>(PUSH2_COLOR2_PINK_HI, hi(ColorEx.PINK)),
                new Pair<>(PUSH2_COLOR2_PINK, ColorEx.PINK),
                new Pair<>(PUSH2_COLOR2_PINK_LO, lo(ColorEx.PINK)),
                new Pair<>(PUSH2_COLOR2_SILVER, hi(ColorEx.GRAY)),
                new Pair<>(PUSH2_COLOR2_ORANGE, ColorEx.ORANGE),
                new Pair<>(PUSH2_COLOR2_ORANGE_LIGHT, hi(ColorEx.ORANGE)),
                new Pair<>(PUSH2_COLOR2_LIGHT_BROWN, ColorEx.DARK_ORANGE)
        );
    }


    // First row colors.
    public static final int PUSH2_ROW1_RED_LO = PUSH2_COLOR2_RED_LO;
    public static final int PUSH2_ROW1_RED_HI = PUSH2_COLOR2_RED_HI;
    public static final int PUSH2_ROW1_ORANGE_LO = PUSH2_COLOR2_AMBER_LO;
    public static final int PUSH2_ROW1_ORANGE_HI = PUSH2_COLOR2_AMBER_HI;
    public static final int PUSH2_ROW1_YELLOW_LO = PUSH2_COLOR2_YELLOW_LO;
    public static final int PUSH2_ROW1_YELLOW_HI = PUSH2_COLOR2_YELLOW_HI;
    public static final int PUSH2_ROW1_GREEN_LO = PUSH2_COLOR2_GREEN_LO;
    public static final int PUSH2_ROW1_GREEN_HI = PUSH2_COLOR2_GREEN_HI;


    /**
     * Variant of getColorMappings that corresponds to the first row of buttons.
     */
    protected List<Pair<Integer, ColorEx>> getFirstRowColorMappings() {
        // FIXME(ktemkin): set these to more accurately match the push
        return List.of(
                new Pair<>(PUSH2_ROW1_RED_LO, lo(ColorEx.RED)),
                new Pair<>(PUSH2_ROW1_RED_HI, hi(ColorEx.RED)),
                new Pair<>(PUSH2_ROW1_ORANGE_LO, lo(ColorEx.ORANGE)),
                new Pair<>(PUSH2_ROW1_ORANGE_HI, hi(ColorEx.ORANGE)),
                new Pair<>(PUSH2_ROW1_YELLOW_LO, lo(ColorEx.YELLOW)),
                new Pair<>(PUSH2_ROW1_YELLOW_HI, hi(ColorEx.YELLOW)),
                new Pair<>(PUSH2_ROW1_GREEN_LO, lo(ColorEx.GREEN)),
                new Pair<>(PUSH2_ROW1_GREEN_HI, hi(ColorEx.GREEN))
        );
    }


    /**
     * Converts a Java color into a per-device color.
     * <p>
     * If your device has different color types for e.g. individual ranges of buttons, override this.
     * The default implementation ignores controlType.
     *
     * @param color       The display color to be converted.
     * @param controlType The type of control we're requesting a color for.
     * @return A device-specific integer that means this color.
     */
    public int getDeviceColor(ColorEx color, ControlType controlType) {

        switch (controlType) {

            // Handle the first row using its own table.
            case BUTTON_ROW1:
                return this.colorLookupCache.computeIfAbsent(color, (c) -> this.firstRowDeviceColors[ColorEx.getClosestColorIndex(c, this.firstRowDisplayColors)]);

            // Otherwise, compute the color lookup using the default table, memoizing as we go.
            default:
                return super.getDeviceColor(color, controlType);
        }
    }


    /**
     * Shortcut for ColorEx.brighter.
     */
    private ColorEx hi(ColorEx c) {
        return ColorEx.brighter(c);
    }

    /**
     * Shortcut for ColorEx.darker.
     */
    private ColorEx lo(ColorEx c) {
        return ColorEx.darker(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColor(final int colorIndex, final ButtonID buttonID) {
        if (colorIndex < 0) {
            return ColorEx.BLACK;
        }

        switch (buttonID) {
            case NEW, DUPLICATE, FIXED_LENGTH, QUANTIZE, DOUBLE, DELETE, UNDO, METRONOME, TAP_TEMPO, VOLUME, PAN_SEND, TRACK, CLIP, DEVICE, BROWSE, PAGE_LEFT, PAGE_RIGHT, SCALES, USER, REPEAT, ACCENT, OCTAVE_DOWN, OCTAVE_UP, ADD_EFFECT, ADD_TRACK, NOTE, SESSION, SELECT, SHIFT, ARROW_LEFT, ARROW_RIGHT, ARROW_DOWN, ARROW_UP, MASTERTRACK, SETUP, LAYOUT -> {
                int color = PUSH2_COLOR2_WHITE;
                if (colorIndex == 0) {
                    color = PUSH2_COLOR2_BLACK;
                } else if (colorIndex == 8) {
                    color = PUSH2_COLOR2_GREY_LO;
                }
                return this.colorByIndex.get(color);
            }
            default -> {
            }
            // Fall through
        }

        final ColorEx color = this.colorByIndex.get(colorIndex);
        if (color == null) {
            throw new ColorIndexException("Color for index " + colorIndex + " is not registered!");
        }
        return color;
    }

}