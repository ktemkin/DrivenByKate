// Written by Kate Temkin - ktemk.in
// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.kontrol.mkii.mode;

import com.ktemkin.controller.ni.kontrol.mkii.KontrolProtocolConfiguration;
import com.ktemkin.controller.ni.kontrol.mkii.controller.KontrolProtocolControlSurface;
import de.mossgrabers.framework.controller.display.AbstractGraphicDisplay;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.mode.track.TrackVolumeMode;
import de.mossgrabers.framework.utils.Pair;
import de.mossgrabers.framework.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Mode for editing a volume parameter of all tracks.
 *
 * @author Jürgen Moßgraber and Kate Temkin
 */
public class VolumeMode extends TrackVolumeMode<KontrolProtocolControlSurface, KontrolProtocolConfiguration> implements IKontrolTrackMode {
    protected final List<Pair<String, Boolean>> menu = new ArrayList<>();
    private boolean displayVU = false;

    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public VolumeMode(final KontrolProtocolControlSurface surface, final IModel model) {
        super(surface, model, false, DEFAULT_KNOB_IDS);

        this.initTouchedStates(9);

        for (int i = 0; i < 8; i++)
            this.menu.add(new Pair<>(" ", Boolean.FALSE));
    }

    @Override
    public void updateDisplay() {
        this.delegatePerDisplayType();
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

            if (this.displayVU && !this.isKnobTouched(i) && !(this.isKnobTouched(8) && t.isSelected())) {
                final int steps = (int) Math.round(this.model.getValueChanger().toNormalizedValue(t.getVu()) * 6);
                d.setCell(1, i, StringUtils.pad("", steps, '>'));
            } else
                d.setCell(1, i, t.getVolumeStr(6));
        }
    }


    @Override
    public void updateGraphicsDisplay(IGraphicDisplay display) {
        this.updateGraphicsChannelDisplay(display, AbstractGraphicDisplay.GRID_ELEMENT_CHANNEL_VOLUME, true, false);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched) {
        this.setTouchedKnob(index, isTouched);
        super.onKnobTouch(index == 8 ? -1 : index, isTouched);
    }


    /**
     * De-/activate to display VU meters.
     */
    public void toggleDisplayVU() {
        this.displayVU = !this.displayVU;
    }


    @Override
    public KontrolProtocolControlSurface getSurface() {
        return this.surface;
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
