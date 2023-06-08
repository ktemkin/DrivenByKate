// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.maschine.view;

import com.ktemkin.controller.common.view.IExecuteFunction;
import com.ktemkin.controller.ni.maschine.MaschineConfiguration;
import com.ktemkin.controller.ni.maschine.controller.MaschineControlSurface;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.featuregroup.AbstractView;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Base class for Maschine views.
 *
 * @author Jürgen Moßgraber
 */
public abstract class BaseView extends AbstractView<MaschineControlSurface, MaschineConfiguration> implements IExecuteFunction
{
    /**
     * Constructor.
     *
     * @param name    The name of the view
     * @param surface The controller
     * @param model   The model
     */
    protected BaseView(final String name, final MaschineControlSurface surface, final IModel model) {
        super(name, surface, model);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onGridNote(final int note, final int velocity) {
        this.executeFunction(note - 36, velocity > 0 ? ButtonEvent.DOWN : ButtonEvent.UP);
    }
}