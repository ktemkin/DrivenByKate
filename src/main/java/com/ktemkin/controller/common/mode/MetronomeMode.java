// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.mode;

import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ITransport;
import de.mossgrabers.framework.daw.data.IItem;
import de.mossgrabers.framework.featuregroup.AbstractFeatureGroup;
import de.mossgrabers.framework.featuregroup.AbstractMode;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Editing of transport parameters.
 *
 * @author Jürgen Moßgraber
 */
public class MetronomeMode extends BaseMode<IItem>
{

    private static final int[] PREROLL_MEASURE =
            {
                    0,
                    1,
                    2,
                    4
            };

    private static final String[] PREROLL_NAMES =
            {
                    "None",
                    "1 Bar",
                    "2 Bars",
                    "4 Bars"
            };

    private final ITransport transport;


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public MetronomeMode(final CommonUIControlSurface surface, final IModel model)
    {
        super("Transport", surface, model);

        this.transport = this.model.getTransport();
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

        if (index < PREROLL_MEASURE.length) {
            this.transport.setPrerollMeasures(PREROLL_MEASURE[index]);
        }
        else if (index == 7) {
            this.transport.togglePrerollMetronome();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getButtonColorID(final ButtonID buttonID)
    {
        final int index = this.isButtonRow(0, buttonID);
        if (index >= 0) {
            final int prerollMeasures = this.transport.getPrerollMeasures();
            if (index < PREROLL_MEASURE.length) {
                return PREROLL_MEASURE[index] == prerollMeasures ? AbstractMode.BUTTON_COLOR_HI : AbstractFeatureGroup.BUTTON_COLOR_ON;
            }
            if (index == 7) {
                return this.transport.isPrerollMetronomeEnabled() ? AbstractMode.BUTTON_COLOR2_HI : AbstractFeatureGroup.BUTTON_COLOR_ON;
            }
        }

        return AbstractFeatureGroup.BUTTON_COLOR_OFF;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        final int prerollMeasures = this.transport.getPrerollMeasures();
        for (int i = 0; i < PREROLL_MEASURE.length; i++) {
            display.addOptionElement("", "", false, i == 0 ? "Pre-roll" : "", PREROLL_NAMES[i], PREROLL_MEASURE[i] == prerollMeasures, false);
        }

        display.addEmptyElement();
        display.addOptionElement("", "", false, "Play Metronome during Pre-Roll?", "", false, false);
        display.addEmptyElement();
        display.addOptionElement("", "", false, "", this.transport.isPrerollMetronomeEnabled() ? "Yes" : "No", this.transport.isPrerollMetronomeEnabled(), false);
    }

}