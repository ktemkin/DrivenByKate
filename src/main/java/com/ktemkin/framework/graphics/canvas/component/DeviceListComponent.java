// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.framework.graphics.canvas.component;

import com.ktemkin.bitwig.framework.graphics.GraphicsContextImpl;
import com.ktemkin.framework.graphics.IGraphicsContext;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.graphics.*;
import de.mossgrabers.framework.graphics.canvas.component.IComponent;

import java.util.ArrayList;
import java.util.List;


/**
 * A component which displays a list of "devices", which each have an icon, some text, and a color.
 * Useful for creating device browser columns.
 *
 * @author Kate Temkin
 */
public class DeviceListComponent implements IComponent {

    /**
     * The actual text associated with a given item.
     */
    private final List<String> itemText = new ArrayList<>(6);

    /**
     * The icon associated with the given item.
     */
    private final List<IImage> itemIcon = new ArrayList<>(6);

    /**
     * The color with which to render each given item.
     */
    private final List<ColorEx> itemColor = new ArrayList<>(6);

    /**
     * Whether to render each given item with some emphasis.
     */
    private final List<Boolean> itemBold = new ArrayList<>(6);


    /**
     * The total number of items in the collection we're drawing from.
     */
    private final int totalItems;


    /**
     * The first displayed item in the collection we're drawing from.
     */
    private final int firstDisplayedItem;


    /**
     * Creates a new display
     *
     * @param text                 The text to display, for each item. Note that only _displayed_ items should be included, here.
     * @param icons                The icon to display, for each icon. Indices should match their associated text.
     * @param colors               The color to display in, for each icon. Indices should match their associated text.
     * @param positionInCollection The current "position" in a larger collection. For example, if we're displaying the 3rd-8th of a 100 item collection, our position would be 3.
     * @param collectionSize       The size of the collection we're drawing from.
     */
    public DeviceListComponent(final List<String> text, final List<IImage> icons, final List<ColorEx> colors, final List<Boolean> isBold, int positionInCollection, int collectionSize) {
        this.itemText.addAll(text);
        this.itemIcon.addAll(icons);
        this.itemColor.addAll(colors);
        this.itemBold.addAll(isBold);
        this.totalItems = collectionSize;
        this.firstDisplayedItem = positionInCollection;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(final IGraphicsInfo info) {
        final IGraphicsContext gc = (GraphicsContextImpl) info.getContext();
        final IGraphicsDimensions dimensions = info.getDimensions();
        final IGraphicsConfiguration configuration = info.getConfiguration();
        final double left = info.getBounds().left();
        final double width = info.getBounds().width();
        final double height = info.getBounds().height();

        final double separatorSize = dimensions.getSeparatorSize();
        final double inset = dimensions.getInset();

        final int size = this.itemText.size();

        final double iconLeft = left + separatorSize;


        final double itemWidth = width - separatorSize;
        final double itemHeight = height / size;

        //
        // Draw each of our text items...
        //
        for (int i = 0; i < size; i++) {
            final String text = this.itemText.get(i);
            final IImage icon = this.itemIcon.get(i);
            final ColorEx color = this.itemColor.get(i);
            final boolean isBold = this.itemBold.get(i);

            final double itemTop = i * itemHeight;
            double itemLeft = iconLeft;


            // If we have an icon, draw one.
            if (icon != null) {
                gc.drawImage(icon, iconLeft, itemTop);
                itemLeft = iconLeft + icon.getWidth();
            }

            // If we have text, draw it.
            if (text != null) {
                gc.drawTextInBounds(text, itemLeft + inset, itemTop, itemWidth - 2 * inset, itemHeight, Align.LEFT, color, null, itemHeight * 0.6, isBold);
            }
        }

        // If we don't know our total, then we can't render a scrollbar.
        // If we don't have any items, there's no point in rendering one.
        if (this.totalItems == 0) {
            return;
        }

        //
        // ... and draw our scrollbar.
        //
        final double scrollbarLeft = left + width - inset;
        final double scrollbarAreaTop = separatorSize;
        final double scrollbarAreaHeight = height - (separatorSize * 2);

        // Our final scrollbar drawing should be proportional to the percent of the display we're
        double scrollbarHeight = Math.min(1, ((double) size / this.totalItems)) * scrollbarAreaHeight;
        double scrollbarTop = ((double) this.firstDisplayedItem / this.totalItems) * scrollbarAreaHeight + scrollbarAreaTop;

        // If our scrollbar is shorter than our item height, we'll pad it out by a certain amount to make it look nicer.
        if (scrollbarHeight < itemHeight) {
            final double paddingHeight = itemHeight - scrollbarHeight;
            final double paddingTop = paddingHeight / 2;

            scrollbarHeight += paddingHeight;
            scrollbarTop = Math.max(0, scrollbarTop - paddingTop);

            // If this top would push us past the end, move it back.
            if ((scrollbarTop + scrollbarHeight) > (height - separatorSize)) {
                scrollbarTop = height - separatorSize - scrollbarHeight;
            }
        }

        gc.drawLine(scrollbarLeft, scrollbarTop, scrollbarLeft, scrollbarTop + scrollbarHeight, ColorEx.DARK_GRAY);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = 1;
        return prime * result + (this.itemText.hashCode() ^ this.itemIcon.hashCode() ^ this.itemColor.hashCode());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null || this.getClass() != obj.getClass())
            return false;
        final DeviceListComponent other = (DeviceListComponent) obj;
        return (this.itemText.equals(other.itemText) && this.itemIcon.equals(other.itemIcon) && this.itemColor.equals(other.itemColor));
    }
}
