// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.command.trigger;

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
 * Command to select the session view.
 *
 * @author Jürgen Moßgraber
 */
public class SelectSessionViewCommand extends AbstractTriggerCommand<CommonUIControlSurface<CommonUIConfiguration>, CommonUIConfiguration> {
    private boolean isTemporary;


    /**
     * Constructor.
     *
     * @param model   The model
     * @param surface The surface
     */
    public SelectSessionViewCommand(final IModel model, final CommonUIControlSurface surface) {
        super(model, surface);
    }


    /**
     * Activate temporary display of session view.
     */
    public void setTemporary() {
        this.isTemporary = true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final ButtonEvent event, final int velocity) {
        if (event == ButtonEvent.DOWN) {
            this.isTemporary = false;

            final ViewManager viewManager = this.surface.getViewManager();
            final ModeManager modeManager = this.surface.getModeManager();
            if (Views.isSessionView(viewManager.getActiveID())) {
                if (modeManager.isActive(Modes.SESSION_VIEW_SELECT))
                    modeManager.restore();
                else
                    modeManager.setTemporary(Modes.SESSION_VIEW_SELECT);
                return;
            }

            // Switch to the preferred session view and display scene/clip mode if enabled
            final var configuration = this.surface.getConfiguration();
            viewManager.setActive(configuration.isScenesClipViewSelected() ? Views.SCENE_PLAY : Views.SESSION);
            if (configuration.shouldDisplayScenesOrClips())
                modeManager.setActive(Modes.SESSION);
            return;
        }

        if (event == ButtonEvent.UP && this.isTemporary)
            this.surface.getViewManager().restore();
    }
}
