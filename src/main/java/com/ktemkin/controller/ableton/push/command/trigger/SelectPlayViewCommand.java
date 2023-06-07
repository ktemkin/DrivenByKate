// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ableton.push.command.trigger;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.featuregroup.ModeManager;
import de.mossgrabers.framework.featuregroup.ViewManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.Views;


/**
 * Command to display a selection for the play modes.
 *
 * @author Jürgen Moßgraber
 */
public class SelectPlayViewCommand extends AbstractTriggerCommand<CommonUIControlSurface<CommonUIConfiguration>, CommonUIConfiguration> {
    /**
     * Constructor.
     *
     * @param model   The model
     * @param surface The surface
     */
    public SelectPlayViewCommand(final IModel model, final CommonUIControlSurface surface) {
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
        final ViewManager viewManager = this.surface.getViewManager();
        if (Views.isSessionView(viewManager.getActiveID())) {
            this.surface.recallPreferredView(this.model.getCursorTrack());

            if (modeManager.isActive(Modes.SESSION) || modeManager.isTemporary())
                modeManager.restore();

            return;
        }

        if (modeManager.isActive(Modes.VIEW_SELECT))
            modeManager.restore();
        else
            modeManager.setTemporary(Modes.VIEW_SELECT);
    }
}
