// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ableton.push.command.trigger;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.featuregroup.IView;
import de.mossgrabers.framework.featuregroup.ViewManager;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.Views;
import de.mossgrabers.framework.view.sequencer.AbstractSequencerView;


/**
 * Command to dive out the layer / drum pads.
 *
 * @author Jürgen Moßgraber
 */
public class PageRightCommand extends AbstractTriggerCommand<CommonUIControlSurface<CommonUIConfiguration>, CommonUIConfiguration>
{

    /**
     * Constructor.
     *
     * @param model   The model
     * @param surface The surface
     */
    public PageRightCommand(final IModel model, final CommonUIControlSurface surface)
    {
        super(model, surface);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final ButtonEvent event, final int velocity)
    {
        final ViewManager viewManager = this.surface.getViewManager();
        if (viewManager.isActive(Views.SESSION)) {
            if (event == ButtonEvent.DOWN) {
                this.model.getCurrentTrackBank().selectNextPage();
            }
            return;
        }

        final IView activeView = viewManager.getActive();
        if (activeView instanceof final AbstractSequencerView<?, ?> sequencerView) {
            sequencerView.onRight(event);
        }
    }

}
