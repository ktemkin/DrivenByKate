// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.bitwig.framework.graphics;

import com.bitwig.extension.api.graphics.GraphicsOutput;
import com.bitwig.extension.api.graphics.GraphicsOutput.AntialiasMode;
import de.mossgrabers.framework.controller.color.ColorEx;


/**
 * Extension to the DrivenByMoss graphics context.
 *
 * @author Kate Temkin
 */
public class GraphicsContextImpl extends de.mossgrabers.bitwig.framework.graphics.GraphicsContextImpl {
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
}
