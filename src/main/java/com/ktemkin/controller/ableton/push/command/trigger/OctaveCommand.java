// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ableton.push.command.trigger;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.featuregroup.IView;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.TransposeView;


/**
 * Command for the octave up/down keys.
 *
 * @author Jürgen Moßgraber
 */
public class OctaveCommand extends AbstractTriggerCommand<CommonUIControlSurface<CommonUIConfiguration>, CommonUIConfiguration>
{

    private final boolean isUp;


    /**
     * Constructor.
     *
     * @param isUp    True if octave up otherwise down
     * @param model   The model
     * @param surface The surface
     */
    public OctaveCommand(final boolean isUp, final IModel model, final CommonUIControlSurface surface)
    {
        super(model, surface);

        this.isUp = isUp;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final ButtonEvent event, final int velocity)
    {
        final IView activeView = this.surface.getViewManager().getActive();
        if (activeView instanceof final TransposeView transposeView) {
            if (this.isUp) {
                transposeView.onOctaveUp(event);
            }
            else {
                transposeView.onOctaveDown(event);
            }
        }
    }

}
