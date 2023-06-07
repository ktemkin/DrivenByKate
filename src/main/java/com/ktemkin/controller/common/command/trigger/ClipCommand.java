// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.command.trigger;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import com.ktemkin.controller.common.modes.track.ClipMode;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.featuregroup.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Command to edit Clip parameters.
 *
 * @author Jürgen Moßgraber
 */
public class ClipCommand extends AbstractTriggerCommand<CommonUIControlSurface<CommonUIConfiguration>, CommonUIConfiguration>
{

    /**
     * Constructor.
     *
     * @param model   The model
     * @param surface The surface
     */
    public ClipCommand(final IModel model, final CommonUIControlSurface surface)
    {
        super(model, surface);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final ButtonEvent event, final int velocity)
    {
        if (event != ButtonEvent.DOWN) {
            return;
        }

        final ModeManager modeManager = this.surface.getModeManager();
        if (modeManager.isActive(Modes.CLIP)) {
            ((ClipMode) modeManager.get(Modes.CLIP)).togglePianoRoll();
        }
        else {
            modeManager.setActive(Modes.CLIP);
        }
    }

}
