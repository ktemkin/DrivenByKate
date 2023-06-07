// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.modes.track;

import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.controller.display.AbstractGraphicDisplay;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.parameterprovider.track.CrossfadeParameterProvider;

import java.util.HashMap;
import java.util.Map;


/**
 * Mode for editing the cross-fade setting of all tracks.
 *
 * @author Jürgen Moßgraber
 */
public class CrossfadeMode extends AbstractTrackMode
{

    private static final Map<String, String> CROSSFADE_TEXT = new HashMap<>(3);

    static {
        CROSSFADE_TEXT.put("A", "A");
        CROSSFADE_TEXT.put("B", "       B");
        CROSSFADE_TEXT.put("AB", "   <> ");
    }


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public CrossfadeMode(final CommonUIControlSurface surface, final IModel model)
    {
        super(Modes.NAME_CROSSFADE, surface, model);

        this.setParameterProvider(new CrossfadeParameterProvider(model));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        this.updateChannelDisplay(display, AbstractGraphicDisplay.GRID_ELEMENT_CHANNEL_CROSSFADER, false, false);
    }

}