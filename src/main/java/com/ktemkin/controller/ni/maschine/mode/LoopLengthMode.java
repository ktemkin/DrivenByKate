// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.maschine.mode;

import com.ktemkin.controller.ni.maschine.controller.MaschineControlSurface;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ITransport;


/**
 * The Arranger Loop Length mode.
 *
 * @author Jürgen Moßgraber
 */
public class LoopLengthMode extends BaseMode {
    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public LoopLengthMode(final MaschineControlSurface surface, final IModel model) {
        super("Loop Length", surface, model);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobValue(final int index, final int value) {
        final double speed = this.model.getValueChanger().calcKnobChange(value);
        this.model.getTransport().changeLoopLength(speed > 0, this.surface.isKnobSensitivitySlow());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTextDisplay(ITextDisplay d) {
        final ITransport transport = this.model.getTransport();
        d.setBlock(0, 0, "Arranger Loop");
        d.setBlock(0, 2, "Start:").setBlock(0, 3, "  " + transport.getLoopStartBeatText());
        d.setBlock(1, 2, "Length:").setBlock(1, 3, "> " + transport.getLoopLengthBeatText());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(IGraphicDisplay display) {
        final ITransport transport = this.model.getTransport();
        display.addOptionElement("Loop", "Volume", false, "Start:", transport.getLoopStartBeatText(), false, true);
        display.addOptionElement("Loop", "Pan", false, "Length:", transport.getLoopLengthBeatText(), false, true);
        display.addOptionElement("", "Crossfade", false, "", "", false, true);
        display.addOptionElement("", "Sends", false, "", "", false, true);
        display.addOptionElement("", "FX 1", false, "", "", false, true);
        display.addOptionElement("", "", false, "", "", false, true);
        display.addOptionElement("", "", false, "", "", false, true);
        display.addOptionElement("", "", false, "", "", false, true);
    }
}
