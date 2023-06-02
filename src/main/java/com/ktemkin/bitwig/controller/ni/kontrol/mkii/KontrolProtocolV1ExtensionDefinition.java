// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.bitwig.controller.ni.kontrol.mkii;

import com.bitwig.extension.controller.api.ControllerHost;
import com.ktemkin.controller.ni.kontrol.mkii.KontrolProtocolConfiguration;
import com.ktemkin.controller.ni.kontrol.mkii.KontrolProtocolControllerDefinition;
import com.ktemkin.controller.ni.kontrol.mkii.KontrolProtocolControllerSetup;
import com.ktemkin.controller.ni.kontrol.mkii.controller.KontrolProtocol;
import com.ktemkin.controller.ni.kontrol.mkii.controller.KontrolProtocolControlSurface;
import com.ktemkin.controller.ni.kontrol.mkii.controller.KontrolProtocolDeviceDescriptorV1;
import de.mossgrabers.bitwig.framework.BitwigSetupFactory;
import de.mossgrabers.bitwig.framework.configuration.SettingsUIImpl;
import com.ktemkin.bitwig.framework.daw.HostImpl;
import de.mossgrabers.bitwig.framework.extension.AbstractControllerExtensionDefinition;
import de.mossgrabers.framework.controller.IControllerSetup;


/**
 * Extension definition class for devices supporting the Komplete Kontrol MIDI protocol version 1.
 *
 * @author Jürgen Moßgraber
 */
public class KontrolProtocolV1ExtensionDefinition extends AbstractControllerExtensionDefinition<KontrolProtocolControlSurface, KontrolProtocolConfiguration> {
    /**
     * Constructor.
     */
    public KontrolProtocolV1ExtensionDefinition() {
        super(new KontrolProtocolControllerDefinition(new KontrolProtocolDeviceDescriptorV1()));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected IControllerSetup<KontrolProtocolControlSurface, KontrolProtocolConfiguration> getControllerSetup(final ControllerHost host) {
        return new KontrolProtocolControllerSetup(new HostImpl(host), new BitwigSetupFactory(host), new SettingsUIImpl(host, host.getPreferences()), new SettingsUIImpl(host, host.getDocumentState()), KontrolProtocol.VERSION_1);
    }
}
