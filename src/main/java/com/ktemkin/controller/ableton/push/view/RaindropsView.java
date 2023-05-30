// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ableton.push.view;

import com.ktemkin.controller.ableton.push.PushConfiguration;
import com.ktemkin.controller.ableton.push.controller.PushControlSurface;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.view.Views;
import de.mossgrabers.framework.view.sequencer.AbstractRaindropsView;


/**
 * The Raindrops Sequencer view.
 *
 * @author Jürgen Moßgraber
 */
public class RaindropsView extends AbstractRaindropsView<PushControlSurface, PushConfiguration> {
    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model   The model
     */
    public RaindropsView(final PushControlSurface surface, final IModel model) {
        super(Views.NAME_RAINDROPS, surface, model, true);
    }
}