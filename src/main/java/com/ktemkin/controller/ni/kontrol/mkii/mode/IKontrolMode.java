// Written by Kate Temkin -- ktemk.in
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.kontrol.mkii.mode;

import com.ktemkin.controller.ni.kontrol.mkii.controller.KontrolProtocolControlSurface;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.daw.IModel;

/**
 * Mix-in class that adds display handling helpers for Kontrol modes.
 *
 * @author Kate Temkin
 */
public interface IKontrolMode {

    /**
     * Fetches our device's control surface, for use in default methods.
     */
    KontrolProtocolControlSurface getSurface();

    /**
     * Helper that returns `this.model` for implementing classes.
     */
    IModel getModel();

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
     * Code that renders the graphic display's screen content, if this Kontrol has a Graphic display.
     *
     * @param display The display to be rendered to.
     */
    void updateGraphicsDisplay(IGraphicDisplay display);


    /**
     * Code that renders the text display's screen content, if we've fallen back to MCU.
     *
     * @param display The display to be rendered to.
     */
    void updateTextDisplay(ITextDisplay display);

}
