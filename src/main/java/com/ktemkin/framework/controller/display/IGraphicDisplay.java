// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.framework.controller.display;

import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.graphics.IImage;


/**
 * Interface to a graphics display. Extensions over the DrivenByMoss base.
 *
 * @author Kate Temkin
 */
public interface IGraphicDisplay extends de.mossgrabers.framework.controller.display.IGraphicDisplay {

    /**
     * Adds a "device list" element, which displays a color-configurable text list, with an icon aside each element.
     *
     * @param items         The list of element text to display.
     * @param icons         The icons to display next to each element. Indices should correspond to the items array. Icons may be null to display no icon.
     * @param colors        The colors with which to style each element. Indices should correspond to the items array.
     * @param selectedIndex The index currently selected, from the list. Used only to determine where in the list should be displayed; does not affect item styling.
     * @param displaySize   The number of elements that should be displayed; used to fit elements to the screen.
     */
    void addDeviceListElement(final String[] items, final IImage[] icons, final ColorEx[] colors, final boolean[] isBold, final int selectedIndex, final int displaySize);
}
