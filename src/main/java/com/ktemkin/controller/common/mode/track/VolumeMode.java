// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.mode.track;

import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.controller.display.AbstractGraphicDisplay;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.parameterprovider.track.VolumeParameterProvider;

import java.util.Optional;


/**
 * Mode for editing a volume parameter of all tracks.
 *
 * @author Jürgen Moßgraber
 */
public class VolumeMode extends AbstractTrackMode
{

    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public VolumeMode(final CommonUIControlSurface surface, final IModel model)
    {
        super(Modes.NAME_VOLUME, surface, model);

        this.setParameterProvider(new VolumeParameterProvider(model));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        this.updateChannelDisplay(display, AbstractGraphicDisplay.GRID_ELEMENT_CHANNEL_VOLUME, true, false);
    }


    public void onKnobValue(int index, int value) {
        Optional<ITrack> track = this.getTrack(index);
        track.ifPresent(iTrack -> iTrack.changeVolume(value));
    }


    public int getKnobValue(int index) {
        Optional<ITrack> track = this.getTrack(index);
        return track.map(iTrack -> ((ITrack) iTrack).getVolume()).orElse(-1);
    }

}