// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.maschine.view;

import com.ktemkin.controller.ni.maschine.core.MaschineColorManager;
import com.ktemkin.controller.ni.maschine.controller.MaschineControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.grid.IPadGrid;
import de.mossgrabers.framework.daw.DAWColor;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.featuregroup.AbstractFeatureGroup;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * The Solo view.
 *
 * @author Jürgen Moßgraber
 */
public class SoloView extends BaseView {
    /**
     * Constructor.
     *
     * @param surface The controller
     * @param model   The model
     */
    public SoloView(final MaschineControlSurface surface, final IModel model) {
        super("Solo", surface, model);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void executeFunction(final int padIndex, final ButtonEvent buttonEvent) {
        if (buttonEvent != ButtonEvent.DOWN)
            return;

        final ITrack track = this.model.getCurrentTrackBank().getItem(padIndex);

        if (this.isButtonCombination(ButtonID.DUPLICATE)) {
            track.duplicate();
            return;
        }

        if (this.isButtonCombination(ButtonID.DELETE)) {
            track.remove();
            return;
        }

        track.toggleSolo();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void drawGrid() {
        final IPadGrid padGrid = this.surface.getPadGrid();

        final ITrackBank tb = this.model.getCurrentTrackBank();
        for (int i = 0; i < 16; i++) {
            final ITrack item = tb.getItem(i);
            final int x = i % 4;
            final int y = 3 - i / 4;
            if (item.doesExist()) {
                final int colorIndex = this.colorManager.getColorIndex(DAWColor.getColorID(item.getColor()));
                if (item.isSolo())
                    padGrid.lightEx(x, y, colorIndex, MaschineColorManager.COLOR_WHITE, false);
                else
                    padGrid.lightEx(x, y, colorIndex);
            } else
                padGrid.lightEx(x, y, AbstractFeatureGroup.BUTTON_COLOR_OFF);
        }
    }
}