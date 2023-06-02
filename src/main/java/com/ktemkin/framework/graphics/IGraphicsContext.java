// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.framework.graphics;

import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.graphics.Align;

/**
 * Interface to drawing functions.
 *
 * @author Jürgen Moßgraber
 */
public interface IGraphicsContext extends de.mossgrabers.framework.graphics.IGraphicsContext {

    /**
     * Draw a stroked arc.
     * Angles are expressed in radians; 0 is due right, 1/2 pi is down; pi is due left, and 3/4 (or -1/2 pi) is up.
     *
     * @param x           The X coordinate of the circle's center.
     * @param y           The Y coordinate of the circle's center.
     * @param radius      The radius of the circle this arc should be taken from.
     * @param startAngle  The starting angle for the arc, in radians.
     * @param finishAngle The finishing angle for the arc, in radians.
     * @param flip        True to flip which section of the arc is filled.
     * @param lineWidth   The stroke width for the arc.
     * @param strokeColor The stroke color for the arc.
     */
    void drawArc(final double x, final double y, final double radius, final double startAngle, final double finishAngle, boolean flip, final double lineWidth, final ColorEx strokeColor);

    /**
     * Draws a set of text within a provided bounds.
     *
     * @param text            The text to draw.
     * @param x               The X coordinate of the -start- of the bounding box.
     * @param y               The Y coordinate of the -start- of the bounding box.
     * @param width           The width of the bounding box.
     * @param height          The height of the bounding box.
     * @param alignment       The text's alignment.
     * @param color           The text's color.
     * @param backgroundColor The backgroudn color, or NULL to not draw any.
     * @param fontSize        The font size to draw with.
     * @param bold            True iff the relevant text should be rendered with some semblance of bold.
     */
    void drawTextInBounds(final String text, final double x, final double y, final double width, final double height, final Align alignment, final ColorEx color, final ColorEx backgroundColor, final double fontSize, final boolean bold);
}

