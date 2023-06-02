// Written by Kate Temkin -- ktemk.in
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.maschine.mk3.mode;

import com.ktemkin.controller.ni.maschine.mk3.controller.MaschineControlSurface;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;

/**
 * Mix-in class that adds display handling helpers for Maschine modes.
 *
 * @author Kate Temkin
 */
public interface IMaschineMode {

    /**
     * Fetches our device's control surface, for use in default methods.
     */
    MaschineControlSurface getSurface();

    /*
     * Helper that delegates display to the correct submethod.
     */
    default void delegatePerDisplayType() {
        if (this.getSurface().getGraphicsDisplay() != null) {
            IGraphicDisplay display = (IGraphicDisplay) this.getSurface().getGraphicsDisplay();
            this.updateGraphicsDisplay(display);
            display.send();
        } else {
            ITextDisplay display = this.getSurface().getTextDisplay();
            this.updateTextDisplay(display);
            display.allDone();
        }
    }


    /**
     * Code that renders the graphic display's screen content, if this Maschine has a Graphic display.
     *
     * @param display The display to be rendered to.
     */
    void updateGraphicsDisplay(IGraphicDisplay display);


    /**
     * Code that renders the text display's screen content, if this Maschine has a MCU display.
     *
     * @param display The display to be rendered to.
     */
    void updateTextDisplay(ITextDisplay display);

}
