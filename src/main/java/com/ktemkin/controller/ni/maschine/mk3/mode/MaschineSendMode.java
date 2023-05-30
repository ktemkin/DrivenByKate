// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.maschine.mk3.mode;

import com.ktemkin.controller.ni.maschine.mk3.MaschineConfiguration;
import com.ktemkin.controller.ni.maschine.mk3.controller.MaschineControlSurface;
import de.mossgrabers.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.ISend;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ISendBank;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.graphics.canvas.utils.SendData;
import de.mossgrabers.framework.mode.track.TrackSendMode;
import de.mossgrabers.framework.utils.Pair;
import de.mossgrabers.framework.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Mode for editing a send volume parameter of all tracks.
 *
 * @author Jürgen Moßgraber
 */
public class MaschineSendMode extends TrackSendMode<MaschineControlSurface, MaschineConfiguration> implements IMaschineTrackMode {
    protected final List<Pair<String, Boolean>> menu = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param sendIndex The send index
     * @param surface   The control surface
     * @param model     The model
     */
    public MaschineSendMode(final int sendIndex, final MaschineControlSurface surface, final IModel model) {
        super(sendIndex, surface, model, false, surface.getMaschine().hasMCUDisplay() ? DEFAULT_KNOB_IDS : null);

        this.initTouchedStates(9);
        for (int i = 0; i < 8; i++)
            this.menu.add(new Pair<>(" ", Boolean.FALSE));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDisplay() {
        this.delegatePerDisplayType();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTextDisplay(ITextDisplay d) {
        final ITrackBank tb = this.model.getCurrentTrackBank();
        for (int i = 0; i < 8; i++) {
            final ITrack t = tb.getItem(i);
            final ISend send = t.getSendBank().getItem(this.sendIndex);
            String name = StringUtils.shortenAndFixASCII(t.getName(), 6);
            if (t.isSelected())
                name = ">" + name;
            d.setCell(0, i, name);
            d.setCell(1, i, send.getDisplayedValue(6));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(IGraphicDisplay display) {
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


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched) {
        this.setTouchedKnob(index, isTouched);

        super.onKnobTouch(index == 8 ? -1 : index, isTouched);
    }


    @Override
    public MaschineControlSurface getSurface() {
        return this.surface;
    }


    @Override
    public IModel getModel() {
        return this.model;
    }


    @Override
    public List<Pair<String, Boolean>> getMenu() {
        return this.menu;
    }
}
