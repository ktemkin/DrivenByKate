// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.maschine.mk3.mode;

import com.ktemkin.controller.ni.maschine.mk3.MaschineConfiguration;
import com.ktemkin.controller.ni.maschine.mk3.controller.MaschineControlSurface;
import de.mossgrabers.framework.controller.display.AbstractGraphicDisplay;
import de.mossgrabers.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.mode.track.TrackPanMode;
import de.mossgrabers.framework.utils.Pair;
import de.mossgrabers.framework.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Mode for editing a pan parameter of all tracks.
 *
 * @author Jürgen Moßgraber
 */
public class MaschinePanMode extends TrackPanMode<MaschineControlSurface, MaschineConfiguration> implements IMaschineTrackMode {
    protected final List<Pair<String, Boolean>> menu = new ArrayList<>();


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public MaschinePanMode(final MaschineControlSurface surface, final IModel model) {
        super(surface, model, false, surface.getMaschine().hasMCUDisplay() ? DEFAULT_KNOB_IDS : null);

        this.initTouchedStates(9);
        for (int i = 0; i < 8; i++)
            this.menu.add(new Pair<>(" ", Boolean.FALSE));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDisplay() {
        this.delegatePerDisplayType();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched) {
        this.setTouchedKnob(index, isTouched);

        super.onKnobTouch(index == 8 ? -1 : index, isTouched);
    }


    @Override
    public MaschineControlSurface getSurface() {
        return this.surface;
    }


    @Override
    public void updateGraphicsDisplay(IGraphicDisplay display) {
        this.updateGraphicsChannelDisplay(display, AbstractGraphicDisplay.GRID_ELEMENT_CHANNEL_PAN, false, true);
    }


    @Override
    public void updateTextDisplay(ITextDisplay d) {
        final ITrackBank tb = this.model.getCurrentTrackBank();
        for (int i = 0; i < 8; i++) {
            final ITrack t = tb.getItem(i);
            String name = StringUtils.shortenAndFixASCII(t.getName(), 6);
            if (t.isSelected())
                name = ">" + name;
            d.setCell(0, i, name);
            d.setCell(1, i, t.getPanStr(6));
        }
    }


    @Override
    public IModel getModel() {
        return this.model;
    }


    @Override
    public List<Pair<String, Boolean>> getMenu() {
        return this.menu;
    }
}
