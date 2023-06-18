// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.kontrol.view;

import com.ktemkin.controller.ni.kontrol.KontrolConfiguration;
import com.ktemkin.controller.ni.kontrol.controller.KontrolControlSurface;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.view.ControlOnlyView;


/**
 * The view for controlling the DAW.
 *
 * @author Jürgen Moßgraber
 */
public class ControlView extends ControlOnlyView<KontrolControlSurface, KontrolConfiguration> {
    private static final int[] IDENTITY_MAP = Scales.getIdentityMatrix();


    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model   The model
     */
    public ControlView(final KontrolControlSurface surface, final IModel model) {
        super(surface, model);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNoteMapping() {
        this.delayedUpdateNoteMapping(IDENTITY_MAP);
    }
}