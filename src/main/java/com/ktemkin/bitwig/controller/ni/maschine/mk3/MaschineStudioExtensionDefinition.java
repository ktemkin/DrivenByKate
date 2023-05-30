// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.bitwig.controller.ni.maschine.mk3;

import com.bitwig.extension.controller.api.ControllerHost;
import com.ktemkin.controller.ni.maschine.Maschine;
import com.ktemkin.controller.ni.maschine.mk3.MaschineConfiguration;
import com.ktemkin.controller.ni.maschine.mk3.MaschineControllerSetup;
import com.ktemkin.controller.ni.maschine.mk3.MaschineStudioControllerDefinition;
import com.ktemkin.controller.ni.maschine.mk3.controller.MaschineControlSurface;
import de.mossgrabers.bitwig.framework.BitwigSetupFactory;
import de.mossgrabers.bitwig.framework.configuration.SettingsUIImpl;
import de.mossgrabers.bitwig.framework.daw.HostImpl;
import de.mossgrabers.bitwig.framework.extension.AbstractControllerExtensionDefinition;
import de.mossgrabers.framework.controller.IControllerSetup;


/**
 * Definition class for the NI Maschine Studio controller.
 *
 * @author Jürgen Moßgraber
 */
public class MaschineStudioExtensionDefinition extends AbstractControllerExtensionDefinition<MaschineControlSurface, MaschineConfiguration> {
    /**
     * Constructor.
     */
    public MaschineStudioExtensionDefinition() {
        super(new MaschineStudioControllerDefinition());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected IControllerSetup<MaschineControlSurface, MaschineConfiguration> getControllerSetup(final ControllerHost host) {
        return new MaschineControllerSetup(new HostImpl(host), new BitwigSetupFactory(host), new SettingsUIImpl(host, host.getPreferences()), new SettingsUIImpl(host, host.getDocumentState()), Maschine.STUDIO);
    }
}
