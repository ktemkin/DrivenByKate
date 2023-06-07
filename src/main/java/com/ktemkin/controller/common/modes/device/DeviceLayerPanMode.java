// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.modes.device;

import com.ktemkin.controller.ableton.push.parameterprovider.PushPanLayerOrDrumPadParameterProvider;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.display.AbstractGraphicDisplay;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IChannel;
import de.mossgrabers.framework.daw.data.ILayer;
import de.mossgrabers.framework.mode.Modes;

import java.util.Optional;


/**
 * Mode for editing the panorama of all device layers.
 *
 * @author Jürgen Moßgraber
 */
public class DeviceLayerPanMode extends DeviceLayerMode
{

    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public DeviceLayerPanMode(final CommonUIControlSurface surface, final IModel model)
    {
        super(Modes.NAME_LAYER_PANORAMA, surface, model);

        this.setParameterProvider(new PushPanLayerOrDrumPadParameterProvider(this.cursorDevice));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched)
    {
        this.setTouchedKnob(index, isTouched);

        // Drum Pad Bank has size of 16, layers only 8
        final int      offset = this.getDrumPadIndex();
        final IChannel layer  = this.bank.getItem(offset + index);
        if (!layer.doesExist()) {
            return;
        }

        if (isTouched && this.surface.isDeletePressed()) {
            this.surface.setTriggerConsumed(ButtonID.DELETE);
            layer.resetPan();
        }

        layer.touchPan(isTouched);
        this.checkStopAutomationOnKnobRelease(isTouched);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDisplayElements(final IGraphicDisplay display, final Optional<ILayer> l)
    {
        this.updateChannelDisplay(display, AbstractGraphicDisplay.GRID_ELEMENT_CHANNEL_PAN, false, true);
    }

}