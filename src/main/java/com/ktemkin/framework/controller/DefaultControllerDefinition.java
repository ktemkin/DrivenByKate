// Written by Kate Temkin - ktemkin.com
// (c) 2017-2023
// Licensed under LGPLv3 -- http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.framework.controller;

import java.util.UUID;

public abstract class DefaultControllerDefinition extends de.mossgrabers.framework.controller.DefaultControllerDefinition
{

    /**
     * {@inheritDoc}
     */
    protected DefaultControllerDefinition(UUID uuid, String hardwareModel, String hardwareVendor, int numMidiInPorts, int numMidiOutPorts)
    {
        super(uuid, hardwareModel, hardwareVendor, numMidiInPorts, numMidiOutPorts);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthor()
    {
        // Don't blame Moss for the things I've broken.
        //
        // He deserves credit for most of this, but I really don't want to add to his support burden,
        // so I'm not listing him here.
        return "Kate Temkin";
    }


}