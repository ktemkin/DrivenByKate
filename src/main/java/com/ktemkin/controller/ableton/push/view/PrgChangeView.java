// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ableton.push.view;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIColorManager;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.grid.IPadGrid;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.midi.MidiConstants;
import de.mossgrabers.framework.featuregroup.AbstractView;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * The Program Change view.
 *
 * @author Jürgen Moßgraber
 */
public class PrgChangeView extends AbstractView<CommonUIControlSurface<CommonUIConfiguration>, CommonUIConfiguration>
{

    private final int[] greens;

    private final int[] yellows;

    private int bankNumber = 0;

    private int programNumber = -1;

    private boolean isToggled = false;


    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model   The model
     */
    public PrgChangeView(final CommonUIControlSurface surface, final IModel model)
    {
        super("PrgChnge", surface, model);

        final var colorManager = (CommonUIColorManager)this.colorManager;

        final int greenHi     = colorManager.getDeviceColor(ColorEx.brighter(ColorEx.GREEN));
        final int green       = colorManager.getDeviceColor(ColorEx.GREEN);
        final int greenLo     = colorManager.getDeviceColor(ColorEx.DARK_GREEN);
        final int greenSpring = colorManager.getDeviceColor(ColorEx.brighter(ColorEx.DARK_GREEN));
        this.greens = new int[]
                {
                        greenHi,
                        green,
                        greenLo,
                        greenSpring,
                        greenHi,
                        green,
                        greenLo,
                        greenSpring
                };

        final int yellowHi   = colorManager.getDeviceColor(ColorEx.brighter(ColorEx.YELLOW));
        final int yellow     = colorManager.getDeviceColor(ColorEx.YELLOW);
        final int yellowLo   = colorManager.getDeviceColor(ColorEx.DARK_YELLOW);
        final int yellowLime = colorManager.getDeviceColor(ColorEx.brighter(ColorEx.GREEN));
        this.yellows = new int[]
                {
                        yellowHi,
                        yellow,
                        yellowLo,
                        yellowLime,
                        yellowHi,
                        yellow,
                        yellowLo,
                        yellowLime
                };
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onButton(final ButtonID buttonID, final ButtonEvent event, final int velocity)
    {
        if (!ButtonID.isSceneButton(buttonID) || event != ButtonEvent.DOWN) {return;}

        final int newBank = buttonID.ordinal() - ButtonID.SCENE1.ordinal();
        if (newBank == this.bankNumber) {this.isToggled = !this.isToggled;}
        else {
            this.bankNumber = newBank;
            this.isToggled  = false;
            this.surface.sendMidiEvent(MidiConstants.CMD_CC, 32, this.bankNumber);
            // Forces the bank change
            if (this.programNumber != -1) {
                this.surface.sendMidiEvent(MidiConstants.CMD_PROGRAM_CHANGE, this.programNumber, 0);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getButtonColor(final ButtonID buttonID)
    {
        final var colorManager = (CommonUIColorManager)this.colorManager;
        final int black = colorManager.getDeviceColor(ColorEx.BLACK);

        final int scene = buttonID.ordinal() - ButtonID.SCENE1.ordinal();
        if (scene < 0 || scene >= 8 || this.bankNumber != scene) {return black;}

        if (this.isToggled) {
            return colorManager.getDeviceColor(ColorEx.YELLOW);
        }
        return colorManager.getDeviceColor(ColorEx.YELLOW);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void drawGrid()
    {
        final var colorManager = (CommonUIColorManager)this.colorManager;

        final int[] colors = this.isToggled ? this.yellows : this.greens;
        final int   selPad;
        if (this.isToggled) {selPad = this.programNumber >= 64 ? this.programNumber - 64 : -1;}
        else {selPad = this.programNumber < 64 ? this.programNumber : -1;}
        final IPadGrid gridPad = this.surface.getPadGrid();
        final int      red     = colorManager.getDeviceColor(ColorEx.RED);
        for (int i = 36; i < 100; i++) {
            final int pad = i - 36;
            final int row = pad / 8;
            gridPad.light(i, selPad == pad ? red : colors[row], -1, false);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onGridNote(final int note, final int velocity)
    {
        if (velocity == 0) {return;}
        this.programNumber = note - 36 + (this.isToggled ? 64 : 0);
        this.surface.sendMidiEvent(MidiConstants.CMD_PROGRAM_CHANGE, this.programNumber, 0);
    }

}