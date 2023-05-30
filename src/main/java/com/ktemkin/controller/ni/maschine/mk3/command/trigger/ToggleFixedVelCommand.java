// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.maschine.mk3.command.trigger;

import com.ktemkin.controller.ni.maschine.mk3.MaschineConfiguration;
import com.ktemkin.controller.ni.maschine.mk3.controller.MaschineControlSurface;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Command for toggling the fixed velocity.
 *
 * @author Jürgen Moßgraber
 */
public class ToggleFixedVelCommand extends AbstractTriggerCommand<MaschineControlSurface, MaschineConfiguration> {
    /**
     * Constructor.
     *
     * @param model   The model
     * @param surface The surface
     */
    public ToggleFixedVelCommand(final IModel model, final MaschineControlSurface surface) {
        super(model, surface);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final ButtonEvent event, final int velocity) {
        if (event != ButtonEvent.UP)
            return;

        final MaschineConfiguration configuration = this.surface.getConfiguration();
        final boolean enabled = !configuration.isAccentActive();
        configuration.setAccentEnabled(enabled);
        this.surface.getDisplay().notify("Fixed Velocity: " + (enabled ? "On" : "Off"));
    }
}
