// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.bitwig.framework.hardware;

import com.bitwig.extension.controller.api.HardwareSurface;
import com.ktemkin.bitwig.framework.graphics.BitmapImpl;
import de.mossgrabers.bitwig.framework.daw.HostImpl;
import de.mossgrabers.bitwig.framework.hardware.HwGraphicsDisplayImpl;
import de.mossgrabers.framework.controller.OutputID;
import de.mossgrabers.framework.controller.hardware.IHwGraphicsDisplay;
import de.mossgrabers.framework.graphics.IBitmap;


/**
 * Factory for creating hardware elements proxies of a hardware controller device.
 *
 * @author Jürgen Moßgraber
 */
public class HwSurfaceFactoryImpl extends de.mossgrabers.bitwig.framework.hardware.HwSurfaceFactoryImpl {
    private final HardwareSurface hardwareSurface;


    /**
     * Constructor.
     *
     * @param host   The host
     * @param width  The width of the controller device
     * @param height The height of the controller device
     */
    public HwSurfaceFactoryImpl(final HostImpl host, final double width, final double height) {
        super(host, width, height);
        this.hardwareSurface = host.getControllerHost().createHardwareSurface();
    }

    private static String createID(final int surfaceID, final String name) {
        return surfaceID + 1 + "_" + name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IHwGraphicsDisplay createGraphicsDisplay(final int surfaceID, final OutputID outputID, final IBitmap bitmap) {
        final String id = createID(surfaceID, outputID.name());
        return new HwGraphicsDisplayImpl(this.hardwareSurface.createHardwarePixelDisplay(id, ((BitmapImpl) bitmap).bitmap()));
    }

}
