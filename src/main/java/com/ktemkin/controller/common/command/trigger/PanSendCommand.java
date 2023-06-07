// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.command.trigger;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.featuregroup.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Command to edit the Pan and Sends of tracks.
 *
 * @author Jürgen Moßgraber
 */
public class PanSendCommand extends AbstractTriggerCommand<CommonUIControlSurface<CommonUIConfiguration>, CommonUIConfiguration>
{

    /**
     * Constructor.
     *
     * @param model   The model
     * @param surface The surface
     */
    public PanSendCommand(final IModel model, final CommonUIControlSurface surface)
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
        final Modes       currentMode = modeManager.getActiveIDIgnoreTemporary();

        // Layer mode selection for Push 1
        Modes                       mode;
        final CommonUIConfiguration config = this.surface.getConfiguration();
        if (this.model.isEffectTrackBankActive()) {
            // No Sends on FX tracks
            mode = Modes.PAN;
        }
        else {
            if (currentMode.ordinal() < Modes.SEND1.ordinal() || currentMode.ordinal() > Modes.SEND8.ordinal()) {
                mode = Modes.SEND1;
            }
            else {
                mode = Modes.get(currentMode, 1);
                if (mode.ordinal() > Modes.SEND8.ordinal()) {
                    mode = Modes.PAN;
                }
            }

            // Check if Send channel exists
            final ITrackBank tb = this.model.getTrackBank();
            if (mode.ordinal() < Modes.SEND1.ordinal() || mode.ordinal() > Modes.SEND8.ordinal() || !tb.canEditSend(mode.ordinal() - Modes.SEND1.ordinal())) {
                mode = Modes.PAN;
            }
        }
        modeManager.setActive(mode);
    }

}
