// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.modes.track;

import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.ISend;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ISendBank;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.graphics.canvas.utils.SendData;
import de.mossgrabers.framework.parameterprovider.track.SendParameterProvider;
import de.mossgrabers.framework.utils.Pair;


/**
 * Mode for editing a Send volumes.
 *
 * @author Jürgen Moßgraber
 */
public class SendMode extends AbstractTrackMode {
    private final int sendIndex;


    /**
     * Constructor.
     *
     * @param surface   The control surface
     * @param model     The model
     * @param sendIndex The index of the send
     */
    public SendMode(final CommonUIControlSurface surface, final IModel model, final int sendIndex) {
        super("Send", surface, model);

        this.sendIndex = sendIndex;

        this.setParameterProvider(new SendParameterProvider(model, this.sendIndex, 0));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched) {
        super.onKnobTouch(index, isTouched);

        if (isTouched && this.surface.isShiftPressed() && this.surface.isSelectPressed() && this.getParameterProvider().get(index) instanceof final ISend send)
            send.toggleEnabled();
    }



    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("null")
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display) {
        this.updateTrackMenu(5 + this.sendIndex % 4);

        final ITrackBank tb = this.model.getCurrentTrackBank();
        final IValueChanger valueChanger = this.model.getValueChanger();

        for (int i = 0; i < 8; i++) {
            final ITrack t = tb.getItem(i);
            final ISendBank sendBank = t.getSendBank();
            final SendData[] sendData = new SendData[4];
            for (int j = 0; j < 4; j++) {
                final ISend send = sendBank.getItem(j);
                final boolean exists = send != null && send.doesExist();
                sendData[j] = new SendData(send.isEnabled(), exists ? send.getName() : "", exists && this.sendIndex == j && this.isKnobTouched(i) ? send.getDisplayedValue(8) : "", valueChanger.toDisplayValue(exists ? send.getValue() : -1), valueChanger.toDisplayValue(exists ? send.getModulatedValue() : -1), this.sendIndex == j);
            }
            final Pair<String, Boolean> pair = this.menu.get(i);
            display.addSendsElement(pair.getKey(), pair.getValue().booleanValue(), t.doesExist() ? t.getName() : "", this.updateType(t), t.getColor(), t.isSelected(), sendData, false, t.isActivated(), t.isActivated());
        }
    }
}