// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.framework.controller.display;

import com.ktemkin.framework.daw.resource.DBKResourceHandler;
import com.ktemkin.framework.graphics.canvas.component.DeviceListComponent;
import com.ktemkin.framework.graphics.canvas.component.ParameterComponent;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.resource.ChannelType;
import de.mossgrabers.framework.graphics.IGraphicsConfiguration;
import de.mossgrabers.framework.graphics.IGraphicsDimensions;
import de.mossgrabers.framework.graphics.IImage;

import java.util.ArrayList;
import java.util.List;

/**
 * A display which uses graphics rather than fixed characters.
 *
 * @author Jürgen Moßgraber
 */
public abstract class AbstractGraphicDisplay extends de.mossgrabers.framework.controller.display.AbstractGraphicDisplay implements IGraphicDisplay {

    private boolean isSplitDisplay;

    private double fontScalingFactor = 1.0;

    /**
     * Constructor.
     *
     * @param host          The host
     * @param configuration The configuration
     * @param dimensions    The pre-calculated dimensions
     * @param windowTitle   The window title
     */
    protected AbstractGraphicDisplay(final IHost host, final IGraphicsConfiguration configuration, final IGraphicsDimensions dimensions, final String windowTitle) {
        this(host, configuration, dimensions, windowTitle, false, 1.0);
        DBKResourceHandler.init(host);
    }


    /**
     * Constructor.
     *
     * @param host          The host
     * @param configuration The configuration
     * @param dimensions    The pre-calculated dimensions
     * @param windowTitle   The window title
     */
    protected AbstractGraphicDisplay(final IHost host, final IGraphicsConfiguration configuration, final IGraphicsDimensions dimensions, final String windowTitle, boolean isSplitDisplay, double fontScalingFactor) {
        super(host, configuration, dimensions, windowTitle);
        this.isSplitDisplay = isSplitDisplay;
        this.fontScalingFactor = fontScalingFactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addParameterElement(final String topMenu, final boolean isTopMenuOn, final String bottomMenu, final ChannelType type, final ColorEx bottomMenuColor, final boolean isBottomMenuOn, final String parameterName, final int parameterValue, final String parameterValueStr, final boolean parameterIsActive, final int parameterModulatedValue) {
        this.addElement(new ParameterComponent(topMenu, isTopMenuOn, bottomMenu, type, bottomMenuColor, isBottomMenuOn, parameterName, parameterValue, parameterModulatedValue, parameterValueStr, parameterIsActive));
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("SimplifiableConditionalExpression")
    @Override
    public void addDeviceListElement(final String[] items, final IImage[] icons, final ColorEx[] colors, final boolean[] isBold, final int selectedIndex, final int displaySize) {
        final List<String> itemText = new ArrayList<>();
        final List<IImage> itemIcons = new ArrayList<>();
        final List<ColorEx> itemColors = new ArrayList<>();
        final List<Boolean> itemBold = new ArrayList<>();

        // FIXME(ktemkin): make this not awkwardly cling to the top -- move it down by one
        final int startIndex = Math.max(0, Math.min(selectedIndex, items.length - displaySize));

        for (int i = 0; i < displaySize; ++i) {
            final int position = startIndex + i;

            itemText.add((position < items.length) ? items[position] : null);
            itemIcons.add((position < items.length) ? icons[position] : null);
            itemColors.add((position < items.length) ? colors[position] : null);
            itemBold.add((position < items.length) ? isBold[position] : false);
        }

        this.addElement(new DeviceListComponent(itemText, itemIcons, itemColors, itemBold, startIndex, items.length));
    }

}
