// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.modes.track;

import com.ktemkin.controller.ableton.push.controller.PushControlSurface;
import de.mossgrabers.framework.controller.display.AbstractGraphicDisplay;
import de.mossgrabers.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.parameterprovider.track.PanParameterProvider;


/**
 * Mode for editing the panorama of all tracks.
 *
 * @author Jürgen Moßgraber
 */
public class PanMode extends AbstractTrackMode
{

    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public PanMode(final PushControlSurface surface, final IModel model)
    {
        super("Panorama", surface, model);

        this.setParameterProvider(new PanParameterProvider(model));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        this.updateChannelDisplay(display, AbstractGraphicDisplay.GRID_ELEMENT_CHANNEL_PAN, false, true);
    }

}