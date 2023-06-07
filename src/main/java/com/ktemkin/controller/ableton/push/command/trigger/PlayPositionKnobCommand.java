// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ableton.push.command.trigger;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.command.continuous.PlayPositionCommand;
import de.mossgrabers.framework.command.core.TriggerCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Command to change the play position in the arranger.
 *
 * @author Jürgen Moßgraber
 */
public class PlayPositionKnobCommand extends PlayPositionCommand<CommonUIControlSurface, CommonUIConfiguration> implements TriggerCommand
{

    /**
     * Constructor.
     *
     * @param model   The model
     * @param surface The surface
     */
    public PlayPositionKnobCommand(final IModel model, final CommonUIControlSurface surface)
    {
        super(model, surface);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final int value)
    {
        if (this.surface.isSelectPressed()) {
            this.transport.changeLoopLength(this.model.getValueChanger().isIncrease(value), this.surface.isKnobSensitivitySlow());
            return;
        }

        super.execute(value);
        this.displayPosition();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final ButtonEvent event, final int velocity)
    {
        final boolean activate = event != ButtonEvent.UP;

        if (this.surface.isSelectPressed()) {
            if (activate) {
                this.mvHelper.delayDisplay(() -> "Loop Length: " + this.transport.getLoopLengthBeatText());
            }
            return;
        }

        this.transport.setTempoIndication(activate);
        if (activate) {
            this.displayPosition();
        }
    }


    private void displayPosition()
    {
        this.surface.getDisplay().notify(this.transport.getBeatText() + " - " + this.transport.getPositionText());
    }

}
