// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.bitwig.controller.ableton.push;

import com.bitwig.extension.controller.api.ControllerHost;
import com.ktemkin.controller.ableton.push.PushConfiguration;
import com.ktemkin.controller.ableton.push.PushControllerDefinition;
import com.ktemkin.controller.ableton.push.PushControllerSetup;
import com.ktemkin.controller.ableton.push.controller.PushControlSurface;
import de.mossgrabers.bitwig.framework.BitwigSetupFactory;
import de.mossgrabers.bitwig.framework.configuration.SettingsUIImpl;
import de.mossgrabers.bitwig.framework.daw.HostImpl;
import de.mossgrabers.bitwig.framework.extension.AbstractControllerExtensionDefinition;
import de.mossgrabers.framework.controller.IControllerSetup;


/**
 * Definition class for the Push 2 controller extension.
 *
 * @author Jürgen Moßgraber
 */
public class Push2ControllerExtensionDefinition extends AbstractControllerExtensionDefinition<PushControlSurface, PushConfiguration> {
    /**
     * Constructor.
     */
    public Push2ControllerExtensionDefinition() {
        super(new PushControllerDefinition(true));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected IControllerSetup<PushControlSurface, PushConfiguration> getControllerSetup(final ControllerHost host) {
        return new PushControllerSetup(new HostImpl(host), new BitwigSetupFactory(host), new SettingsUIImpl(host, host.getPreferences()), new SettingsUIImpl(host, host.getDocumentState()), true);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUsingBetaAPI() {
        return true;
    }
}
