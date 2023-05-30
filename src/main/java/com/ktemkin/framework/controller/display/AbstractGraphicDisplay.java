// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.framework.controller.display;

import com.ktemkin.framework.graphics.canvas.component.ParameterComponent;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.resource.ChannelType;
import de.mossgrabers.framework.graphics.IGraphicsConfiguration;
import de.mossgrabers.framework.graphics.IGraphicsDimensions;

/**
 * A display which uses graphics rather than fixed characters.
 *
 * @author Jürgen Moßgraber
 */
public abstract class AbstractGraphicDisplay extends de.mossgrabers.framework.controller.display.AbstractGraphicDisplay {

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


}
