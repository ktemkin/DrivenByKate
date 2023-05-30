// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ableton.push.command.trigger;

import com.ktemkin.controller.ableton.push.PushConfiguration;
import com.ktemkin.controller.ableton.push.controller.PushControlSurface;
import com.ktemkin.controller.ableton.push.mode.track.ClipMode;
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
public class ClipCommand extends AbstractTriggerCommand<PushControlSurface, PushConfiguration> {
    /**
     * Constructor.
     *
     * @param model   The model
     * @param surface The surface
     */
    public ClipCommand(final IModel model, final PushControlSurface surface) {
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
        if (modeManager.isActive(Modes.CLIP)) {
            if (this.surface.getConfiguration().isPush2())
                ((ClipMode) modeManager.get(Modes.CLIP)).togglePianoRoll();
            else
                modeManager.restore();
        } else
            modeManager.setActive(Modes.CLIP);
    }
}
