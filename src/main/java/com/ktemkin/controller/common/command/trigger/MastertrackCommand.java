// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.command.trigger;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.featuregroup.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Command to display the master mode.
 *
 * @author Jürgen Moßgraber
 */
public class MastertrackCommand extends AbstractTriggerCommand<CommonUIControlSurface<CommonUIConfiguration>, CommonUIConfiguration> {
    private boolean quitMasterMode = false;
    private int selectedTrackBeforeMasterMode = -1;


    /**
     * Constructor.
     *
     * @param model   The model
     * @param surface The surface
     */
    public MastertrackCommand(final IModel model, final CommonUIControlSurface surface) {
        super(model, surface);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final ButtonEvent event, final int velocity) {
        // Avoid accidentally leaving the browser
        final ModeManager modeManager = this.surface.getModeManager();
        if (modeManager.isActive(Modes.BROWSER))
            return;

        switch (event) {
            case DOWN:
                this.quitMasterMode = false;
                break;

            case UP:
                this.handleButtonUp(modeManager);
                break;

            case LONG:
                this.quitMasterMode = true;
                modeManager.setTemporary(Modes.FRAME);
                break;
        }
    }


    private void handleButtonUp(final ModeManager modeManager) {
        if (this.quitMasterMode) {
            modeManager.restore();
            return;
        }

        if (Modes.MASTER.equals(modeManager.getActiveID())) {
            if (this.selectedTrackBeforeMasterMode >= 0)
                this.model.getCurrentTrackBank().getItem(this.selectedTrackBeforeMasterMode).select();
            return;
        }

        modeManager.setActive(Modes.MASTER);
        this.model.getMasterTrack().select();
        final ITrack cursorTrack = this.model.getCursorTrack();
        this.selectedTrackBeforeMasterMode = cursorTrack.doesExist() ? cursorTrack.getIndex() : -1;
    }
}
