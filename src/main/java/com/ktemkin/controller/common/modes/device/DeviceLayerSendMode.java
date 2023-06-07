// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.modes.device;

import com.ktemkin.controller.ableton.push.parameterprovider.PushSendLayerOrDrumPadParameterProvider;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IChannel;
import de.mossgrabers.framework.daw.data.ILayer;
import de.mossgrabers.framework.daw.data.ISend;
import de.mossgrabers.framework.daw.data.bank.ISendBank;
import de.mossgrabers.framework.daw.resource.ChannelType;
import de.mossgrabers.framework.graphics.canvas.utils.SendData;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.Pair;

import java.util.Optional;


/**
 * Mode for editing all sends of a device layer.
 *
 * @author Jürgen Moßgraber
 */
public class DeviceLayerSendMode extends DeviceLayerMode
{

    private final int sendIndex;


    /**
     * Constructor.
     *
     * @param surface   The control surface
     * @param model     The model
     * @param sendIndex The index of the Send
     */
    public DeviceLayerSendMode(final CommonUIControlSurface surface, final IModel model, final int sendIndex)
    {
        super(Modes.NAME_LAYER_SENDS, surface, model);

        this.sendIndex = sendIndex;

        this.setParameterProvider(new PushSendLayerOrDrumPadParameterProvider(this.cursorDevice, sendIndex));
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
            layer.getSendBank().getItem(this.sendIndex).resetValue();
        }

        layer.getSendBank().getItem(this.sendIndex).touchValue(isTouched);
        this.checkStopAutomationOnKnobRelease(isTouched);

        // Toggle send enablement
        if (isTouched && this.surface.isShiftPressed() && this.surface.isSelectPressed() && this.getParameterProvider().get(index) instanceof final ISend send) {
            send.toggleEnabled();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDisplayElements(final IGraphicDisplay display, final Optional<ILayer> l)
    {
        this.updateMenuItems(5 + this.sendIndex % 4);

        // Drum Pad Bank has size of 16, layers only 8
        final int offset = this.getDrumPadIndex();
        for (int i = 0; i < 8; i++) {
            final IChannel layer = this.bank.getItem(offset + i);

            final Pair<String, Boolean> pair        = this.menu.get(i);
            final String                topMenu     = pair.getKey();
            final boolean               isTopMenuOn = pair.getValue();

            // Channel info
            final SendData[] sendData = new SendData[4];
            final ISendBank  sendBank = layer.getSendBank();
            for (int j = 0; j < 4; j++) {
                final ISend   send   = sendBank.getItem(j);
                final boolean exists = send.doesExist();
                sendData[j] = new SendData(send.isEnabled(), send.getName(), exists && this.sendIndex == j && this.isKnobTouched(i) ? send.getDisplayedValue() : "", exists ? send.getValue() : 0, exists ? send.getModulatedValue() : 0, this.sendIndex == j);
            }

            display.addSendsElement(topMenu, isTopMenuOn, layer.doesExist() ? layer.getName() : "", ChannelType.LAYER, this.bank.getItem(offset + i).getColor(), layer.isSelected(), sendData, false, layer.isActivated(), layer.isActivated());
        }
    }

}