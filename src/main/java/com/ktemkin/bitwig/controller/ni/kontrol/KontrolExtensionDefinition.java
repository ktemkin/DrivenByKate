// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.bitwig.controller.ni.kontrol;

import com.bitwig.extension.controller.api.ControllerHost;
import com.ktemkin.bitwig.framework.daw.HostImpl;
import com.ktemkin.controller.ni.kontrol.KontrolConfiguration;
import com.ktemkin.controller.ni.kontrol.KontrolControllerDefinition;
import com.ktemkin.controller.ni.kontrol.KontrolControllerSetup;
import com.ktemkin.controller.ni.kontrol.controller.KontrolControlSurface;
import com.ktemkin.controller.ni.kontrol.controller.KontrolProtocol;
import com.ktemkin.controller.ni.kontrol.controller.KontrolDeviceDescriptorV2;
import de.mossgrabers.bitwig.framework.BitwigSetupFactory;
import de.mossgrabers.bitwig.framework.configuration.SettingsUIImpl;
import de.mossgrabers.bitwig.framework.extension.AbstractControllerExtensionDefinition;
import de.mossgrabers.framework.controller.IControllerSetup;


/**
 * Extension definition class for devices supporting the Komplete Kontrol MIDI protocol version 2.
 *
 * @author Jürgen Moßgraber
 */
public class KontrolExtensionDefinition extends AbstractControllerExtensionDefinition<KontrolControlSurface, KontrolConfiguration> {
    /**
     * Constructor.
     */
    public KontrolExtensionDefinition() {
        super(new KontrolControllerDefinition(new KontrolDeviceDescriptorV2()));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected IControllerSetup<KontrolControlSurface, KontrolConfiguration> getControllerSetup(final ControllerHost host) {
        return new KontrolControllerSetup(new HostImpl(host), new BitwigSetupFactory(host), new SettingsUIImpl(host, host.getPreferences()), new SettingsUIImpl(host, host.getDocumentState()), KontrolProtocol.VERSION_2);
    }
}
