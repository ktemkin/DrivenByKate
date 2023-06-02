// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.bitwig.framework.graphics;

import com.bitwig.extension.api.graphics.GraphicsOutput;
import com.bitwig.extension.api.graphics.GraphicsOutput.AntialiasMode;
import com.ktemkin.framework.graphics.IGraphicsContext;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.graphics.Align;
import de.mossgrabers.framework.utils.StringUtils;


/**
 * Extension to the DrivenByMoss graphics context.
 *
 * @author Kate Temkin
 */
public class GraphicsContextImpl extends de.mossgrabers.bitwig.framework.graphics.GraphicsContextImpl implements IGraphicsContext {
    private final GraphicsOutput gc;


    /**
     * Constructor.
     *
     * @param antialiasMode The antialias mode to apply
     * @param gc            The Bitwig graphics context
     */
    public GraphicsContextImpl(final AntialiasMode antialiasMode, final GraphicsOutput gc) {
        super(antialiasMode, gc);

        gc.setAntialias(antialiasMode);
        this.gc = gc;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void drawArc(final double x, final double y, final double radius, final double startAngle, final double finishAngle, boolean flip, final double lineWidth, final ColorEx strokeColor) {
        this.setColor(strokeColor);
        this.gc.newPath();
        this.gc.setLineWidth(lineWidth);
        if (flip) {
            this.gc.arcNegative(x, y, radius, startAngle, finishAngle);
        } else {
            this.gc.arc(x, y, radius, startAngle, finishAngle);
        }
        this.gc.stroke();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void drawTextInBounds(final String text, final double x, final double y, final double width, final double height, final Align alignment, final ColorEx color, final ColorEx backgroundColor, final double fontSize, final boolean bold) {
        if (text == null || text.length() == 0)
            return;

        final String txt = StringUtils.fixFontCharacters(text);

        this.gc.save();
        this.gc.setFontSize(fontSize);

        // We need to calculate the text height from a character which has no ascent, since showText
        // always draws the text on the baseline of the font!
        final double h = this.gc.getTextExtents("T").getHeight();
        final double w = this.gc.getTextExtents(txt).getWidth();
        final double posX = alignment == Align.CENTER ? x + (width - w) / 2.0 : x;
        final double posY = y + (height + h) / 2;

        this.gc.rectangle(x, y, width, height);
        this.gc.clip();

        if (backgroundColor != null) {
            final double inset = 12.0;
            this.fillRoundedRectangle(posX - inset, posY - h - inset, w + 2 * inset, h + 2 * inset, inset, backgroundColor);
        }

        this.setColor(color);
        this.gc.moveTo(posX, posY);
        this.gc.showText(txt);

        // This is a _terrible_ hack, but Bitwig seems to not let us do bold.
        if (bold) {
            this.gc.moveTo(posX + 1, posY);
            this.gc.showText(txt);
        }

        this.gc.resetClip();
        this.gc.restore();
    }
}
