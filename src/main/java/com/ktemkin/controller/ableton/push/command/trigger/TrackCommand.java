// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ableton.push.command.trigger;

import com.ktemkin.controller.ableton.push.PushConfiguration;
import com.ktemkin.controller.ableton.push.controller.PushControlSurface;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.featuregroup.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;

import java.util.Optional;


/**
 * Command to edit track parameters.
 *
 * @author Jürgen Moßgraber
 */
public class TrackCommand extends AbstractTriggerCommand<PushControlSurface, PushConfiguration>
{

    /**
     * Constructor.
     *
     * @param model   The model
     * @param surface The surface
     */
    public TrackCommand(final IModel model, final PushControlSurface surface)
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

        final PushConfiguration config = this.surface.getConfiguration();

        if (this.surface.isShiftPressed()) {
            config.setVUMetersEnabled(!config.isEnableVUMeters());
            return;
        }

        final ModeManager modeManager = this.surface.getModeManager();
        final Modes       currentMode = modeManager.getActiveID();

        if (currentMode != null) {
            if (Modes.TRACK.equals(currentMode) || Modes.VOLUME.equals(currentMode) || Modes.CROSSFADER.equals(currentMode) || Modes.PAN.equals(currentMode)) {
                this.model.toggleCurrentTrackBank();
            }
            else if (currentMode.ordinal() >= Modes.SEND1.ordinal() && currentMode.ordinal() <= Modes.SEND8.ordinal()) {
                modeManager.setActive(Modes.TRACK);
                this.model.toggleCurrentTrackBank();
            }
            else {
                modeManager.setActive(config.getCurrentMixMode());
            }
        }
        else {
            modeManager.setActive(Modes.TRACK);
        }

        config.setDebugMode(modeManager.getActiveID());

        final ITrackBank       tb    = this.model.getCurrentTrackBank();
        final Optional<ITrack> track = tb.getSelectedItem();
        if (track.isEmpty()) {
            tb.getItem(0).select();
        }
    }

}
