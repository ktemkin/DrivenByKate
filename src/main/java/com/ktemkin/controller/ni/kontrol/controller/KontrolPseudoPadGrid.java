// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.kontrol.controller;

import com.ktemkin.controller.common.controller.grid.CommonUIPadGrid;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.daw.midi.IMidiOutput;
import de.mossgrabers.framework.scale.Scales;


/**
 * Implementation of a "pseudo pad grid" used to handle lighting up the keystrip on a Kontrol.
 *
 * @author Kate Temkin
 */
public class KontrolPseudoPadGrid extends CommonUIPadGrid {

    /**
     * The scales used to locate key indices.
     */
    protected final Scales scales;


    /**
     * The surface associated with this pad.
     */
    protected KontrolControlSurface surface;


    /**
     * Constructor. A 4x4 grid.
     *
     * @param colorManager The color manager for accessing specific colors to use
     * @param output       The MIDI output which can address the pad states
     */
    public KontrolPseudoPadGrid(final ColorManager colorManager, final IMidiOutput output, final Scales scales) {
        super(colorManager, output, 1, 88, 0);
        this.scales = scales;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLight(int note, int color, int blinkColor, boolean fast) {
        var keyIndex = note - this.scales.getStartNote();

        // ... and set its color.
        this.surface.setKeyColor(keyIndex, color);
    }


    /**
     * Sets the surface associated with this pad grid.
     *
     * @param surface The surface to use.
     */
    public void setSurface(KontrolControlSurface surface) {
        this.surface = surface;
    }
}