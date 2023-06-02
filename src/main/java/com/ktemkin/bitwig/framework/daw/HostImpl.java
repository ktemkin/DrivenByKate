// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.bitwig.framework.daw;

import com.bitwig.extension.api.graphics.BitmapFormat;
import com.bitwig.extension.controller.api.ControllerHost;
import com.ktemkin.bitwig.framework.graphics.BitmapImpl;
import com.ktemkin.bitwig.framework.hardware.HwSurfaceFactoryImpl;
import de.mossgrabers.framework.controller.hardware.IHwSurfaceFactory;
import de.mossgrabers.framework.graphics.IBitmap;


/**
 * Encapsulates the ControllerHost instance.
 *
 * @author Jürgen Moßgraber
 */
public class HostImpl extends de.mossgrabers.bitwig.framework.daw.HostImpl {
    /**
     * The underlying Bitwig ControllerHost.
     */
    private final ControllerHost host;


    /**
     * Constructor.
     *
     * @param host The host
     */
    public HostImpl(final ControllerHost host) {
        super(host);
        this.host = host;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public IHwSurfaceFactory createSurfaceFactory(double width, double height) {
        return new HwSurfaceFactoryImpl(this, width, height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBitmap createBitmap(final int width, final int height) {
        return new BitmapImpl(this.host.createBitmap(width, height, BitmapFormat.ARGB32));
    }


}
