// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.maschine.mode;

import com.ktemkin.controller.ni.maschine.controller.MaschineControlSurface;
import com.ktemkin.controller.ni.maschine.view.DrumView;
import de.mossgrabers.framework.controller.ButtonID;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.featuregroup.ViewManager;
import de.mossgrabers.framework.mode.INoteMode;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.Views;


/**
 * The Drum Configuration mode.
 *
 * @author Jürgen Moßgraber
 */
public class DrumConfigurationMode extends BaseMode {
    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public DrumConfigurationMode(final MaschineControlSurface surface, final IModel model) {
        super("Drum Configuration", surface, model);

        this.selectedParam = 6;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobValue(final int index, final int value) {
        if (!this.model.canSelectedTrackHoldNotes())
            return;

        final int idx = index < 0 ? this.selectedParam : index;

        final Scales scales = this.model.getScales();
        final boolean inc = this.model.getValueChanger().isIncrease(value);

        final ViewManager viewManager = this.surface.getViewManager();
        switch (idx) {
            case 6:
            case 7:
                ((DrumView) viewManager.get(Views.DRUM)).changeOctave(ButtonEvent.DOWN, inc, scales.getDrumDefaultOffset(), true, false);
                viewManager.get(Views.DRUM).updateNoteMapping();
                ((INoteMode) this.surface.getModeManager().get(Modes.NOTE)).clearNotes();
                break;

            default:
                // Not used
                break;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched) {
        if (isTouched && this.surface.isDeletePressed()) {
            this.surface.setTriggerConsumed(ButtonID.DELETE);

            final int idx = index < 0 ? this.selectedParam : index;
            switch (idx) {
                case 6:
                case 7:
                    ((DrumView) this.surface.getViewManager().get(Views.DRUM)).resetOctave();
                    this.surface.getViewManager().get(Views.DRUM).updateNoteMapping();
                    ((INoteMode) this.surface.getModeManager().get(Modes.NOTE)).clearNotes();
                    break;

                default:
                    // Not used
                    break;
            }
        }
    }

    @Override
    public void updateGraphicsDisplay(IGraphicDisplay display) {
        final Scales scales = this.model.getScales();
        final int octave = scales.getDrumOffset();

        display.addOptionElement("", "Volume", false, "", "", false, true);
        display.addOptionElement("", "Pan", false, "", "", false, true);
        display.addOptionElement("", "Crossfade", false, "Drum Offset", (octave > 0 ? "+" : "") + Integer.toString(octave), false, true);
        display.addOptionElement("", "Sends", false, "", "", false, true);

        display.addOptionElement("", "FX 1", false, "", "", false, true);
        display.addOptionElement("", " ", false, "", "", false, true);
        display.addOptionElement("", " ", false, "", "", false, true);
        display.addOptionElement("", " ", false, "", "", false, true);
    }


    @Override
    public void updateTextDisplay(ITextDisplay d) {
        final Scales scales = this.model.getScales();
        final int octave = scales.getDrumOffset();

        d.setBlock(0, 3, this.mark("Drum Offset", 6)).setBlock(1, 3, (octave > 0 ? "+" : "") + Integer.toString(octave));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void selectPreviousItem() {
        // Not used since it is only 1 parameter
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void selectNextItem() {
        // Not used since it is only 1 parameter
    }

}
