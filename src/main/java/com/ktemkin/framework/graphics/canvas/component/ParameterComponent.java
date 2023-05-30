// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.framework.graphics.canvas.component;

import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.daw.resource.ChannelType;
import de.mossgrabers.framework.graphics.*;
import de.mossgrabers.framework.graphics.canvas.component.LabelComponent.LabelLayout;


/**
 * An element in the grid which contains a fader and text for a value.
 * <p>
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author Jürgen Moßgraber
 */
public class ParameterComponent extends de.mossgrabers.framework.graphics.canvas.component.ParameterComponent {

    private final String paramName;
    private final int paramValue;
    private final int modulatedParamValue;
    private final String paramValueText;
    private final boolean isTouched;

    /**
     * Constructor. A generic parameter.
     *
     * @param menuName            The text for the menu
     * @param isMenuSelected      True if the menu is selected
     * @param name                The of the grid element (track name, parameter name, etc.)
     * @param color               The color to use for the header, may be null
     * @param isSelected          True if the grid element is selected
     * @param paramName           The name of the parameter
     * @param paramValue          The value of the fader
     * @param modulatedParamValue The modulated value of the fader, -1 if not modulated
     * @param paramValueText      The textual form of the faders value
     * @param isTouched           True if touched
     */
    public ParameterComponent(final String menuName, final boolean isMenuSelected, final String name, final ColorEx color, final boolean isSelected, final String paramName, final int paramValue, final int modulatedParamValue, final String paramValueText, final boolean isTouched) {
        this(menuName, isMenuSelected, name, (String) null, color, isSelected, paramName, paramValue, modulatedParamValue, paramValueText, isTouched);
    }


    /**
     * Constructor. A parameter with a device footer.
     *
     * @param menuName            The text for the menu
     * @param isMenuSelected      True if the menu is selected
     * @param name                The of the grid element (track name, parameter name, etc.)
     * @param deviceName          The name of the device
     * @param color               The color to use for the header, may be null
     * @param isSelected          True if the grid element is selected
     * @param paramName           The name of the parameter
     * @param paramValue          The value of the fader
     * @param modulatedParamValue The modulated value of the fader, -1 if not modulated
     * @param paramValueText      The textual form of the faders value
     * @param isTouched           True if touched
     */
    public ParameterComponent(final String menuName, final boolean isMenuSelected, final String name, final String deviceName, final ColorEx color, final boolean isSelected, final String paramName, final int paramValue, final int modulatedParamValue, final String paramValueText, final boolean isTouched) {
        this(menuName, isMenuSelected, name, deviceName, color, isSelected, paramName, paramValue, modulatedParamValue, paramValueText, isTouched, LabelLayout.SEPARATE_COLOR);
    }


    /**
     * Constructor. A parameter with a device footer.
     *
     * @param menuName            The text for the menu
     * @param isMenuSelected      True if the menu is selected
     * @param name                The of the grid element (track name, parameter name, etc.)
     * @param deviceName          The name of the device
     * @param color               The color to use for the header, may be null
     * @param isSelected          True if the grid element is selected
     * @param paramName           The name of the parameter
     * @param paramValue          The value of the fader
     * @param modulatedParamValue The modulated value of the fader, -1 if not modulated
     * @param paramValueText      The textual form of the faders value
     * @param isTouched           True if touched
     * @param lowerLayout         The layout for the lower label
     */
    public ParameterComponent(final String menuName, final boolean isMenuSelected, final String name, final String deviceName, final ColorEx color, final boolean isSelected, final String paramName, final int paramValue, final int modulatedParamValue, final String paramValueText, final boolean isTouched, final LabelLayout lowerLayout) {
        super(menuName, isMenuSelected, name, deviceName, color, isSelected, paramName, paramValue, modulatedParamValue, paramValueText, isTouched, lowerLayout);

        this.paramName = paramName;
        this.paramValue = paramValue;
        this.modulatedParamValue = modulatedParamValue;
        this.paramValueText = paramValueText;
        this.isTouched = isTouched;
    }


    /**
     * Constructor. A parameter with a channel footer.
     *
     * @param menuName            The text for the menu
     * @param isMenuSelected      True if the menu is selected
     * @param name                The of the grid element (track name, parameter name, etc.)
     * @param type                The type of the channel
     * @param color               The color to use for the header, may be null
     * @param isSelected          True if the grid element is selected
     * @param paramName           The name of the parameter
     * @param paramValue          The value of the fader
     * @param modulatedParamValue The modulated value of the fader, -1 if not modulated
     * @param paramValueText      The textual form of the faders value
     * @param isTouched           True if touched
     */
    public ParameterComponent(final String menuName, final boolean isMenuSelected, final String name, final ChannelType type, final ColorEx color, final boolean isSelected, final String paramName, final int paramValue, final int modulatedParamValue, final String paramValueText, final boolean isTouched) {
        super(menuName, isMenuSelected, name, color, isSelected, paramName, paramValue, modulatedParamValue, paramValueText, isTouched);

        this.paramName = paramName;
        this.paramValue = paramValue;
        this.modulatedParamValue = modulatedParamValue;
        this.paramValueText = paramValueText;
        this.isTouched = isTouched;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(final IGraphicsInfo info) {
        super.draw(info);

        final IGraphicsContext gc = info.getContext();
        final IGraphicsDimensions dimensions = info.getDimensions();
        final IGraphicsConfiguration configuration = info.getConfiguration();
        final double left = info.getBounds().left();
        final double width = info.getBounds().width();
        final double height = info.getBounds().height();

        final double separatorSize = dimensions.getSeparatorSize();
        final double menuHeight = dimensions.getMenuHeight();
        final double unit = dimensions.getUnit();
        final double controlsTop = dimensions.getControlsTop();
        final double inset = dimensions.getInset();

        final boolean isValueMissing = this.paramValue == -1;
        final boolean isModulated = this.modulatedParamValue != -1;

        final int trackRowHeight = (int) (1.6 * unit);
        final double trackRowTop = height - trackRowHeight - unit - separatorSize;

        // Component is off if the name is empty
        if (this.paramName == null || this.paramName.length() == 0)
            return;

        final double elementWidth = width - inset * 0.5;
        final double elementHeight = (trackRowTop - controlsTop - inset) / 4;

        // Draw the name and value texts
        final ColorEx textColor = configuration.getColorText();
        final double fontSize = elementHeight * 2 / 3;
        final double halfInset = inset * 0.5;
        gc.drawTextInBounds(this.paramValueText, left + inset - 1, controlsTop - halfInset, elementWidth, elementHeight, Align.CENTER, textColor, fontSize * info.getFontScalingFactor() * 1.4);
        gc.drawTextInBounds(this.paramName, left + inset - 1, controlsTop + halfInset + elementHeight * 3, elementWidth, elementHeight, Align.CENTER, textColor, fontSize * info.getFontScalingFactor());

        // Value knob
        if (isValueMissing)
            return;
        final double elementInnerWidth = elementWidth - 2;
        final double maxValue = dimensions.getParameterUpperBound();
        final double value = isModulated ? this.modulatedParamValue : this.paramValue;
        final double innerTop = controlsTop + elementHeight + 1;

        final double arcWidthUnfilled = dimensions.getInset() * 0.6;
        final double arcWidthFilled = dimensions.getInset() * 0.8;

        final double radius = Math.min(elementWidth, elementHeight);
        final double centerX = left + (elementWidth / 2) + inset;
        final double centerY = innerTop + (elementHeight / 2) + (inset * 1.5);

        // Our start and end angles for the whole fader.
        final double unfilledStartAngleRadians = 70 * (Math.PI / 180);
        final double endAngleRadians = 110 * (Math.PI / 180);

        // The start and end angles for the filled section.
        final double filledArcDegrees = (value / maxValue) * 320;
        final double filledAngleDegrees = filledArcDegrees - 180 - 70;
        final double filledStartAngleRadians = filledAngleDegrees * (Math.PI / 180);

        // Draw the knob-style fader.
        gc.drawArc(centerX, centerY, radius, unfilledStartAngleRadians, endAngleRadians, true, arcWidthUnfilled, configuration.getColorFader());
        gc.drawArc(centerX, centerY, radius, filledStartAngleRadians, endAngleRadians, true, arcWidthFilled, isTouched ? ColorEx.brighter(configuration.getColorEdit()) : configuration.getColorEdit());
    }
}
