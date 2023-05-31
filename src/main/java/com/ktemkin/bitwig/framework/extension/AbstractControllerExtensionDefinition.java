// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.bitwig.framework.extension;

import de.mossgrabers.framework.configuration.Configuration;
import de.mossgrabers.framework.controller.IControlSurface;
import de.mossgrabers.framework.controller.IControllerDefinition;


/**
 * Some reoccurring functions for the extension definition.
 *
 * @param <C> The type of the configuration
 * @param <S> The type of the control surface
 * @author Kate Temkin
 */
public abstract class AbstractControllerExtensionDefinition<S extends IControlSurface<C>, C extends Configuration> extends de.mossgrabers.bitwig.framework.extension.AbstractControllerExtensionDefinition<S, C>
{

    /**
     * The version for this plugin, based on DBK instead of DBM.
     */
    public final String version;


    /**
     * Constructor.
     *
     * @param definition The definition
     */
    protected AbstractControllerExtensionDefinition(final IControllerDefinition definition)
    {
        super(definition);

        final ClassLoader l = this.getClass().getClassLoader();
        this.version = definition.getVersion(l.getDefinedPackage("com.ktemkin.framework"));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion()
    {
        return this.version;
    }

}
