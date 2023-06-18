// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.kontrol.controller;

import com.ktemkin.controller.common.controller.CommonUIColorManager;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.grid.IPadGrid;
import de.mossgrabers.framework.daw.DAWColor;
import de.mossgrabers.framework.featuregroup.AbstractFeatureGroup;
import de.mossgrabers.framework.featuregroup.AbstractMode;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.utils.Pair;
import de.mossgrabers.framework.view.AbstractPlayView;
import de.mossgrabers.framework.view.BrowserView;

import java.util.List;


/**
 * Different colors to use for the buttons of Komplete Kontrol MkII.
 *
 * @author Jürgen Moßgraber
 */
public class KontrolColorManager extends CommonUIColorManager {
    public static final int COLOR_BLACK = 0;
    public static final int COLOR_DARK_GREY = 76;
    public static final int COLOR_GREY = 77;
    public static final int COLOR_WHITE = 78;
    public static final int COLOR_ROSE = 7;
    public static final int COLOR_RED = 6;
    public static final int COLOR_RED_LO = 5;
    public static final int COLOR_AMBER = 14;
    public static final int COLOR_AMBER_LO = 13;
    public static final int COLOR_LIME = 34;
    public static final int COLOR_LIME_LO = 33;
    public static final int COLOR_GREEN = 30;
    public static final int COLOR_GREEN_LO = 29;
    public static final int COLOR_SPRING = 26;
    public static final int COLOR_SPRING_LO = 25;
    public static final int COLOR_TURQUOISE_LO = 27;
    public static final int COLOR_TURQUOISE = 31;
    public static final int COLOR_SKY = 38;
    public static final int COLOR_SKY_LO = 37;
    public static final int COLOR_BLUE = 42;
    public static final int COLOR_BLUE_LO = 45;
    public static final int COLOR_MAGENTA = 58;
    public static final int COLOR_MAGENTA_LO = 57;
    public static final int COLOR_PINK = 62;
    public static final int COLOR_PINK_LO = 61;
    public static final int COLOR_ORANGE = 10;
    public static final int COLOR_ORANGE_LO = 9;
    public static final int COLOR_PURPLE = 50;
    public static final int COLOR_PURPLE_LO = 53;
    public static final int COLOR_PEACH = 11;
    public static final int COLOR_YELLOW_LO = 21;
    public static final int COLOR_YELLOW = 22;

    /**
     * Constructor.
     */
    public KontrolColorManager() {
        this.registerColorIndex(IPadGrid.GRID_OFF, COLOR_BLACK);

        this.registerColorIndex(ColorManager.BUTTON_STATE_OFF, COLOR_BLACK);
        this.registerColorIndex(ColorManager.BUTTON_STATE_ON, COLOR_BLACK);
        this.registerColorIndex(ColorManager.BUTTON_STATE_HI, 127);

        this.registerColorIndex(Scales.SCALE_COLOR_OFF, COLOR_BLACK);
        this.registerColorIndex(Scales.SCALE_COLOR_OCTAVE, COLOR_BLUE);
        this.registerColorIndex(Scales.SCALE_COLOR_NOTE, COLOR_WHITE);
        this.registerColorIndex(Scales.SCALE_COLOR_OUT_OF_SCALE, COLOR_BLACK);

        this.registerColorIndex(AbstractFeatureGroup.BUTTON_COLOR_OFF, 0);
        this.registerColorIndex(AbstractFeatureGroup.BUTTON_COLOR_ON, 0);
        this.registerColorIndex(AbstractMode.BUTTON_COLOR_HI, 127);

        this.registerColorIndex(AbstractPlayView.COLOR_PLAY, COLOR_GREEN);
        this.registerColorIndex(AbstractPlayView.COLOR_RECORD, COLOR_RED);
        this.registerColorIndex(AbstractPlayView.COLOR_OFF, COLOR_BLACK);

        this.registerColorIndex(BrowserView.OFF, COLOR_BLACK);
        this.registerColorIndex(BrowserView.DISCARD, COLOR_RED);
        this.registerColorIndex(BrowserView.CONFIRM, COLOR_GREEN);
        this.registerColorIndex(BrowserView.PLAY, COLOR_AMBER_LO);
        this.registerColorIndex(BrowserView.COLUMN1, COLOR_WHITE);
        this.registerColorIndex(BrowserView.COLUMN2, COLOR_GREY);
        this.registerColorIndex(BrowserView.COLUMN3, COLOR_DARK_GREY);
        this.registerColorIndex(BrowserView.COLUMN4, COLOR_ROSE);
        this.registerColorIndex(BrowserView.COLUMN5, COLOR_SPRING);
        this.registerColorIndex(BrowserView.COLUMN6, COLOR_BLUE_LO);
        this.registerColorIndex(BrowserView.COLUMN7, COLOR_BLACK);
        this.registerColorIndex(BrowserView.COLUMN8, COLOR_YELLOW);

        this.registerColorIndex(DAWColor.COLOR_OFF, COLOR_BLACK);
        this.registerColorIndex(DAWColor.DAW_COLOR_GRAY_HALF, COLOR_DARK_GREY);
        this.registerColorIndex(DAWColor.DAW_COLOR_DARK_GRAY, COLOR_DARK_GREY);
        this.registerColorIndex(DAWColor.DAW_COLOR_GRAY, COLOR_GREY);
        this.registerColorIndex(DAWColor.DAW_COLOR_LIGHT_GRAY, COLOR_GREY);
        this.registerColorIndex(DAWColor.DAW_COLOR_SILVER, COLOR_GREY);
        this.registerColorIndex(DAWColor.DAW_COLOR_DARK_BROWN, COLOR_AMBER_LO);
        this.registerColorIndex(DAWColor.DAW_COLOR_BROWN, COLOR_AMBER_LO);
        this.registerColorIndex(DAWColor.DAW_COLOR_DARK_BLUE, COLOR_BLUE_LO);
        this.registerColorIndex(DAWColor.DAW_COLOR_PURPLE_BLUE, COLOR_PURPLE_LO);
        this.registerColorIndex(DAWColor.DAW_COLOR_PURPLE, COLOR_PURPLE);
        this.registerColorIndex(DAWColor.DAW_COLOR_PINK, COLOR_PINK);
        this.registerColorIndex(DAWColor.DAW_COLOR_RED, COLOR_RED);
        this.registerColorIndex(DAWColor.DAW_COLOR_ORANGE, COLOR_ORANGE);
        this.registerColorIndex(DAWColor.DAW_COLOR_LIGHT_ORANGE, COLOR_YELLOW);
        this.registerColorIndex(DAWColor.DAW_COLOR_MOSS_GREEN, COLOR_LIME_LO);
        this.registerColorIndex(DAWColor.DAW_COLOR_GREEN, COLOR_SPRING);
        this.registerColorIndex(DAWColor.DAW_COLOR_COLD_GREEN, COLOR_TURQUOISE);
        this.registerColorIndex(DAWColor.DAW_COLOR_BLUE, COLOR_BLUE);
        this.registerColorIndex(DAWColor.DAW_COLOR_LIGHT_PURPLE, COLOR_MAGENTA);
        this.registerColorIndex(DAWColor.DAW_COLOR_LIGHT_PINK, COLOR_PINK);
        this.registerColorIndex(DAWColor.DAW_COLOR_ROSE, COLOR_PEACH);
        this.registerColorIndex(DAWColor.DAW_COLOR_REDDISH_BROWN, COLOR_AMBER);
        this.registerColorIndex(DAWColor.DAW_COLOR_LIGHT_BROWN, COLOR_AMBER_LO);
        this.registerColorIndex(DAWColor.DAW_COLOR_LIGHT_GREEN, COLOR_SPRING);
        this.registerColorIndex(DAWColor.DAW_COLOR_BLUISH_GREEN, COLOR_LIME);
        this.registerColorIndex(DAWColor.DAW_COLOR_GREEN_BLUE, COLOR_TURQUOISE_LO);
        this.registerColorIndex(DAWColor.DAW_COLOR_LIGHT_BLUE, COLOR_SKY);

        this.registerColor(COLOR_BLACK, ColorEx.BLACK);
        this.registerColor(COLOR_DARK_GREY, ColorEx.DARK_GRAY);
        this.registerColor(COLOR_GREY, ColorEx.GRAY);
        this.registerColor(COLOR_WHITE, ColorEx.WHITE);
        this.registerColor(COLOR_ROSE, DAWColor.DAW_COLOR_ROSE.getColor());
        this.registerColor(COLOR_RED, DAWColor.DAW_COLOR_REDDISH_BROWN.getColor());
        this.registerColor(COLOR_RED_LO, ColorEx.fromRGB(39, 4, 1));
        this.registerColor(COLOR_AMBER, DAWColor.DAW_COLOR_REDDISH_BROWN.getColor());
        this.registerColor(COLOR_AMBER_LO, DAWColor.DAW_COLOR_DARK_BROWN.getColor());
        this.registerColor(COLOR_YELLOW_LO, ColorEx.darker(ColorEx.fromRGB(107, 105, 1)));
        this.registerColor(COLOR_YELLOW, ColorEx.fromRGB(107, 105, 1));
        this.registerColor(COLOR_LIME, ColorEx.fromRGB(29, 104, 1));
        this.registerColor(COLOR_LIME_LO, DAWColor.DAW_COLOR_MOSS_GREEN.getColor());
        this.registerColor(COLOR_GREEN, ColorEx.fromRGB(1, 104, 1));
        this.registerColor(COLOR_GREEN_LO, ColorEx.fromRGB(1, 36, 1));
        this.registerColor(COLOR_SPRING, DAWColor.DAW_COLOR_GREEN.getColor());
        this.registerColor(COLOR_SPRING_LO, ColorEx.fromRGB(1, 36, 1));
        this.registerColor(COLOR_TURQUOISE_LO, ColorEx.fromRGB(1, 248, 75));
        this.registerColor(COLOR_TURQUOISE, DAWColor.DAW_COLOR_COLD_GREEN.getColor());
        this.registerColor(COLOR_SKY, ColorEx.fromRGB(1, 82, 100));
        this.registerColor(COLOR_SKY_LO, ColorEx.fromRGB(1, 26, 37));
        this.registerColor(COLOR_BLUE, ColorEx.fromRGB(4, 23, 110));
        this.registerColor(COLOR_BLUE_LO, ColorEx.fromRGB(1, 8, 38));
        this.registerColor(COLOR_MAGENTA, ColorEx.fromRGB(110, 28, 109));
        this.registerColor(COLOR_MAGENTA_LO, ColorEx.fromRGB(39, 9, 38));
        this.registerColor(COLOR_PINK, ColorEx.fromRGB(110, 20, 40));
        this.registerColor(COLOR_PINK_LO, ColorEx.fromRGB(48, 9, 26));
        this.registerColor(COLOR_ORANGE, DAWColor.DAW_COLOR_ORANGE.getColor());
        this.registerColor(COLOR_ORANGE_LO, ColorEx.DARK_ORANGE);
        this.registerColor(COLOR_PURPLE, ColorEx.PURPLE);
        this.registerColor(COLOR_PURPLE_LO, ColorEx.PURPLE);
        this.registerColor(COLOR_PEACH, ColorEx.ROSE);
    }

    @Override
    protected List<Pair<Integer, ColorEx>> getColorMappings() {
        // FIXME(ktemkin): set these to more accurately match the maschine
        return List.of(
                new Pair<>(COLOR_BLACK, ColorEx.BLACK),
                new Pair<>(COLOR_DARK_GREY, ColorEx.DARK_GRAY),
                new Pair<>(COLOR_GREY, ColorEx.GRAY),
                new Pair<>(COLOR_WHITE, ColorEx.WHITE),
                new Pair<>(COLOR_ROSE, ColorEx.ROSE),
                new Pair<>(COLOR_RED, ColorEx.RED),
                new Pair<>(COLOR_RED_LO, ColorEx.darker(ColorEx.RED)),
                new Pair<>(COLOR_AMBER, ColorEx.DARK_YELLOW),
                new Pair<>(COLOR_AMBER_LO, ColorEx.darker(ColorEx.DARK_YELLOW)),
                new Pair<>(COLOR_LIME, ColorEx.brighter(ColorEx.GREEN)),
                new Pair<>(COLOR_LIME_LO, ColorEx.DARK_GREEN),
                new Pair<>(COLOR_GREEN, ColorEx.GREEN),
                new Pair<>(COLOR_GREEN_LO, ColorEx.DARK_GREEN),
                new Pair<>(COLOR_SPRING, ColorEx.brighter(ColorEx.GREEN)),
                new Pair<>(COLOR_SPRING_LO, ColorEx.darker(ColorEx.GREEN)),
                new Pair<>(COLOR_TURQUOISE_LO, ColorEx.brighter(ColorEx.DARK_BLUE)),
                new Pair<>(COLOR_TURQUOISE, ColorEx.brighter(ColorEx.BLUE)),
                new Pair<>(COLOR_SKY, ColorEx.BLUE),
                new Pair<>(COLOR_SKY_LO, ColorEx.DARK_BLUE),
                new Pair<>(COLOR_BLUE, ColorEx.BLUE),
                new Pair<>(COLOR_BLUE_LO, ColorEx.DARK_BLUE),
                new Pair<>(COLOR_MAGENTA, ColorEx.PURPLE),
                new Pair<>(COLOR_MAGENTA_LO, ColorEx.DARK_PURPLE),
                new Pair<>(COLOR_PINK, ColorEx.PINK),
                new Pair<>(COLOR_PINK_LO, ColorEx.darker(ColorEx.PINK)),
                new Pair<>(COLOR_ORANGE, ColorEx.ORANGE),
                new Pair<>(COLOR_ORANGE_LO, ColorEx.DARK_ORANGE),
                new Pair<>(COLOR_PURPLE, ColorEx.PURPLE),
                new Pair<>(COLOR_PURPLE_LO, ColorEx.DARK_PURPLE),
                new Pair<>(COLOR_PEACH, ColorEx.PINK),
                new Pair<>(COLOR_YELLOW_LO, ColorEx.YELLOW),
                new Pair<>(COLOR_YELLOW, ColorEx.DARK_YELLOW)
        );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ColorEx getColor(final int colorIndex, final ButtonID buttonID) {
        if (buttonID == null)
            return ColorEx.BLACK;

        switch (buttonID) {
            case PLAY:
                return colorIndex > 0 ? ColorEx.GREEN : ColorEx.DARK_GREEN;
            case RECORD:
            case REC_ARM:
            case ROW3_1:
            case ROW3_2:
            case ROW3_3:
            case ROW3_4:
            case ROW3_5:
            case ROW3_6:
            case ROW3_7:
            case ROW3_8:
                return colorIndex > 0 ? ColorEx.RED : ColorEx.DARK_RED;
            case SOLO:
            case ROW2_1:
            case ROW2_2:
            case ROW2_3:
            case ROW2_4:
            case ROW2_5:
            case ROW2_6:
            case ROW2_7:
            case ROW2_8:
                return colorIndex > 0 ? ColorEx.BLUE : ColorEx.DARK_BLUE;
            case MUTE:
            case ROW1_1:
            case ROW1_2:
            case ROW1_3:
            case ROW1_4:
            case ROW1_5:
            case ROW1_6:
            case ROW1_7:
            case ROW1_8:
                return colorIndex > 0 ? ColorEx.ORANGE : ColorEx.DARK_ORANGE;
            case ROW_SELECT_1:
            case ROW_SELECT_2:
            case ROW_SELECT_3:
            case ROW_SELECT_4:
            case ROW_SELECT_5:
            case ROW_SELECT_6:
            case ROW_SELECT_7:
            case ROW_SELECT_8:
                return colorIndex > 0 ? ColorEx.GRAY : ColorEx.BLACK;
            default:
                return colorIndex > 0 ? ColorEx.WHITE : ColorEx.DARK_GRAY;
        }
    }
}
