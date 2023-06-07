// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ableton.push.command.trigger;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Command to trigger the Accent.
 *
 * @author Jürgen Moßgraber
 */
public class AccentCommand extends AbstractTriggerCommand<CommonUIControlSurface<CommonUIConfiguration>, CommonUIConfiguration>
{

    private boolean quitAccentMode;


    /**
     * Constructor.
     *
     * @param model   The model
     * @param surface The surface
     */
    public AccentCommand(final IModel model, final CommonUIControlSurface surface)
    {
        super(model, surface);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final ButtonEvent event, final int velocity)
    {
        switch (event) {
            case DOWN -> this.quitAccentMode = false;
            case LONG -> {
                this.quitAccentMode = true;
                this.surface.getModeManager().setTemporary(Modes.ACCENT);
            }
            case UP -> {
                if (this.quitAccentMode) {
                    this.surface.getModeManager().restore();
                }
                else {
                    final CommonUIConfiguration config = this.surface.getConfiguration();
                    config.setAccentEnabled(!config.isAccentActive());
                }
            }
        }
    }

}
