// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.framework.daw.resource;

import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.graphics.IImage;

import java.util.HashMap;
import java.util.Map;


/**
 * Get and cache some resources like SVG images.
 */
public final class DBKResourceHandler {
    private static final Map<String, IImage> CACHE = new HashMap<>();
    private static IHost theHost;


    /**
     * Initialize the handler.
     *
     * @param host The controller host
     */
    public static void init(final IHost host) {
        theHost = host;

        addSVGImage("browser/folder_open.svg");
        addSVGImage("browser/folder_closed.svg");
        addSVGImage("browser/kind_device.svg");
    }

    /**
     * Get a SVG image as an Image object.
     *
     * @param imageName The name of the image
     * @return The buffered image
     */
    public static IImage getSVGImage(final String imageName) {
        return CACHE.get(imageName);
    }

    /**
     * Load and cache an image.
     *
     * @param imageName The name (absolute path) of the image
     */
    public static void addSVGImage(final String imageName) {
        CACHE.put(imageName, theHost.loadSVG(imageName, 1));
    }
}
