// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.command.trigger;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.command.trigger.transport.MetronomeCommand;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.featuregroup.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Command to disable/enable the metronome. Also toggles metronome ticks when Shift is pressed. Long
 * pressing activates the metronome mode.
 *
 * @author Jürgen Moßgraber
 */
public class PushMetronomeCommand extends MetronomeCommand<CommonUIControlSurface<CommonUIConfiguration>, CommonUIConfiguration> {
    private final ModeManager modeManager;


    /**
     * Constructor.
     *
     * @param model   The model
     * @param surface The surface
     */
    public PushMetronomeCommand(final IModel model, final CommonUIControlSurface surface) {
        super(model, surface, false);

        this.modeManager = this.surface.getModeManager();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void executeNormal(final ButtonEvent event) {
        if (event == ButtonEvent.LONG) {
            this.surface.setTriggerConsumed(ButtonID.METRONOME);
            this.modeManager.setTemporary(Modes.TRANSPORT);
            return;
        }

        if (event == ButtonEvent.UP && this.modeManager.isActive(Modes.TRANSPORT)) {
            this.modeManager.restore();
            return;
        }

        super.executeNormal(event);
    }
}
