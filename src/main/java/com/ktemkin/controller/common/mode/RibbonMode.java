// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.mode;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IItem;
import de.mossgrabers.framework.featuregroup.AbstractFeatureGroup;
import de.mossgrabers.framework.featuregroup.AbstractMode;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Editing of accent parameters.
 *
 * @author Jürgen Moßgraber
 */
public class RibbonMode extends BaseMode<IItem>
{

    private static final int[] MIDI_CCS =
            {
                    1,
                    11,
                    7,
                    64
            };

    private static final String[] TOP_HEADERS =
            {
                    "CC",
                    "Quick Select",
                    "",
                    "",
                    "",
                    "Note Repeat",
                    "",
                    ""
            };

    private static final String[] BOTTOM_HEADERS =
            {
                    "Function",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
            };

    private static final String[] TOP_OPTIONS =
            {
                    "",
                    "Modulation",
                    "Expression",
                    "Volume",
                    "Sustain",
                    "Off",
                    "Period",
                    "Length"
            };

    private static final String[] BOTTOM_OPTIONS =
            {
                    "Pitchbend",
                    "CC",
                    "CC/Pitch",
                    "Pitch/CC",
                    "Fader",
                    "Last Touched",
                    "",
                    ""
            };

    private static final int[] FUNCTION_IDS =
            {
                    CommonUIConfiguration.RIBBON_MODE_PITCH,
                    CommonUIConfiguration.RIBBON_MODE_CC,
                    CommonUIConfiguration.RIBBON_MODE_CC_PB,
                    CommonUIConfiguration.RIBBON_MODE_PB_CC,
                    CommonUIConfiguration.RIBBON_MODE_FADER,
                    CommonUIConfiguration.RIBBON_MODE_LAST_TOUCHED
            };

    private final CommonUIConfiguration configuration;


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public RibbonMode(final CommonUIControlSurface surface, final IModel model)
    {
        super("Ribbon", surface, model);

        this.configuration = this.surface.getConfiguration();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobValue(final int index, final int value)
    {
        if (index == 0) {
            this.configuration.setRibbonModeCC(this.model.getValueChanger().changeValue(value, this.configuration.getRibbonModeCCVal(), -100, 128));
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched)
    {
        this.setTouchedKnob(index, isTouched);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onFirstRow(final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.UP) {
            return;
        }
        if (index < RibbonMode.FUNCTION_IDS.length) {
            this.configuration.setRibbonMode(index);
        }
        else {
            this.surface.getModeManager().restore();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onSecondRow(final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.UP || index == 0) {
            return;
        }
        if (index < 5) {
            this.surface.getConfiguration().setRibbonModeCC(RibbonMode.MIDI_CCS[index - 1]);
        }
        else {
            this.configuration.setRibbonNoteRepeat(index - 5);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getButtonColorID(final ButtonID buttonID)
    {
        int index = this.isButtonRow(0, buttonID);
        if (index >= 0) {
            if (index < RibbonMode.FUNCTION_IDS.length) {
                return this.configuration.getRibbonMode() == CommonUIConfiguration.RIBBON_MODE_PITCH + index ? AbstractMode.BUTTON_COLOR_HI : AbstractFeatureGroup.BUTTON_COLOR_ON;
            }
            return AbstractFeatureGroup.BUTTON_COLOR_OFF;
        }

        index = this.isButtonRow(1, buttonID);
        if (index >= 0) {
            if (index == 0) {
                return AbstractFeatureGroup.BUTTON_COLOR_OFF;
            }
            if (index < 5) {
                return AbstractFeatureGroup.BUTTON_COLOR_ON;
            }
            return this.configuration.getRibbonNoteRepeat() == index - 5 ? AbstractMode.BUTTON_COLOR_HI : AbstractFeatureGroup.BUTTON_COLOR_ON;
        }

        return AbstractFeatureGroup.BUTTON_COLOR_OFF;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        final int ribbonMode       = this.configuration.getRibbonMode();
        final int ribbonNoteRepeat = this.configuration.getRibbonNoteRepeat();

        for (int i = 0; i < 8; i++) {
            boolean isMenuTopSelected = true;
            String  menuTopName       = TOP_OPTIONS[i];
            if (i == 0) {
                menuTopName = Integer.toString(this.configuration.getRibbonModeCCVal());
            }
            else {
                isMenuTopSelected = i > 4 && ribbonNoteRepeat == i - 5;
            }
            display.addOptionElement(TOP_HEADERS[i], menuTopName, isMenuTopSelected, BOTTOM_HEADERS[i], BOTTOM_OPTIONS[i], i < RibbonMode.FUNCTION_IDS.length && ribbonMode == RibbonMode.FUNCTION_IDS[i], false);
        }
    }

}
