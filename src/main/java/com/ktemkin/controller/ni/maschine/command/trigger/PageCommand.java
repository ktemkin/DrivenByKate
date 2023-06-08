// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.maschine.command.trigger;

import com.ktemkin.controller.ni.maschine.MaschineConfiguration;
import com.ktemkin.controller.ni.maschine.controller.MaschineControlSurface;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.command.trigger.Direction;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.ICursorDevice;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.IParameterBank;
import de.mossgrabers.framework.daw.data.bank.ISlotBank;
import de.mossgrabers.framework.featuregroup.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Move the track bank page left/right or switch through bank device bank.
 *
 * @author Jürgen Moßgraber
 */
public class PageCommand extends AbstractTriggerCommand<MaschineControlSurface, MaschineConfiguration> {
    private final Direction direction;


    /**
     * Constructor.
     *
     * @param direction The direction left or right
     * @param model     The model
     * @param surface   The surface
     */
    public PageCommand(final Direction direction, final IModel model, final MaschineControlSurface surface) {
        super(model, surface);

        this.direction = direction;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final ButtonEvent event, final int velocity) {
        if (event != ButtonEvent.DOWN)
            return;

        final ModeManager modeManager = this.surface.getModeManager();
        final Modes mode = modeManager.getActiveID();

        switch (mode) {
            case DEVICE_PARAMS:
                final ICursorDevice cursorDevice = this.model.getCursorDevice();
                if (this.direction == Direction.LEFT)
                    cursorDevice.selectPrevious();
                else
                    cursorDevice.selectNext();
                this.mvHelper.notifySelectedDevice();
                break;

            case USER:
                final IParameterBank userBank = this.model.getUserParameterBank();
                if (this.direction == Direction.LEFT)
                    userBank.selectPreviousPage();
                else
                    userBank.selectNextPage();
                this.mvHelper.notifySelectedUserPage();
                break;

            case BROWSER:
                if (this.direction == Direction.LEFT)
                    this.model.getBrowser().previousContentType();
                else
                    this.model.getBrowser().nextContentType();
                break;

            default:
                final ITrack cursorTrack = this.model.getCursorTrack();
                if (!cursorTrack.doesExist())
                    return;

                final ISlotBank slotBank = cursorTrack.getSlotBank();
                if (this.direction == Direction.LEFT) {
                    if (this.surface.isShiftPressed()) {
                        this.surface.setStopConsumed();
                        slotBank.selectPreviousPage();
                    } else
                        slotBank.selectPreviousItem();
                } else {
                    if (this.surface.isShiftPressed()) {
                        this.surface.setStopConsumed();
                        slotBank.selectNextPage();
                    } else
                        slotBank.selectNextItem();
                }
                break;
        }
    }
}
