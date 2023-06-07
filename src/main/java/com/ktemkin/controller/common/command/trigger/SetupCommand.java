// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.command.trigger;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.featuregroup.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Command to Setup the hardware of Push 2.
 *
 * @author Jürgen Moßgraber
 */
public class SetupCommand extends AbstractTriggerCommand<CommonUIControlSurface<CommonUIConfiguration>, CommonUIConfiguration> {


    /**
     * Constructor.
     *
     * @param model   The model
     * @param surface The surface
     */
    public SetupCommand(final IModel model, final CommonUIControlSurface surface) {
        super(model, surface);

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final ButtonEvent event, final int velocity) {
        if (event != ButtonEvent.DOWN)
            return;
        final ModeManager modeManager = this.surface.getModeManager();

        final Modes mode = this.getMode();

        if (modeManager.isActive(mode))
            modeManager.restore();
        else
            modeManager.setTemporary(mode);
    }


    private Modes getMode() {
        return Modes.SETUP;
    }
}
