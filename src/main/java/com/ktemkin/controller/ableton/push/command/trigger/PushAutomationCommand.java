// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ableton.push.command.trigger;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Command to enable automation and edit its settings.
 *
 * @author Jürgen Moßgraber
 */
public class PushAutomationCommand extends AbstractTriggerCommand<CommonUIControlSurface<CommonUIConfiguration>, CommonUIConfiguration> {
    private boolean quitAutomationMode;


    /**
     * Constructor.
     *
     * @param model   The model
     * @param surface The surface
     */
    public PushAutomationCommand(final IModel model, final CommonUIControlSurface surface) {
        super(model, surface);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final ButtonEvent event, final int velocity) {
        if (this.surface.isDeletePressed()) {
            this.surface.setTriggerConsumed(ButtonID.DELETE);
            if (event == ButtonEvent.DOWN)
                this.model.getTransport().resetAutomationOverrides();
            return;
        }

        switch (event) {
            case DOWN:
                this.quitAutomationMode = false;
                break;
            case LONG:
                this.quitAutomationMode = true;
                this.surface.getModeManager().setTemporary(Modes.AUTOMATION);
                break;
            case UP:
                if (this.quitAutomationMode)
                    this.surface.getModeManager().restore();
                else
                    this.doCommand();
                break;
        }
    }


    private void doCommand() {
        final boolean isShift = this.surface.isShiftPressed();
        final boolean flipRecord = this.surface.getConfiguration().isFlipRecord();
        if (isShift && !flipRecord || !isShift && flipRecord)
            this.model.getTransport().toggleWriteClipLauncherAutomation();
        else
            this.model.getTransport().toggleWriteArrangerAutomation();
    }
}
