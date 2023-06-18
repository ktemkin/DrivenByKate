package com.ktemkin.controller.common.controller;

import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.utils.Pair;

import java.util.HashMap;
import java.util.List;

/**
 * Generic color manager for CommonUI devices.
 */
abstract public class CommonUIColorManager extends ColorManager {

    /**
     * Array that stores a set of display colors.
     * Each color should match its corresponding index in ``deviceColors``.
     */
    protected final ColorEx[] displayColors;


    /**
     * Array that stores a set of device colors.
     * Each color should match its corresponding index in ``displayColors``.
     */
    protected final int[] deviceColors;


    /**
     * Container that memoizes past ColorEx -> DeviceColor lookups.
     */
    protected final HashMap<ColorEx, Integer> colorLookupCache = new HashMap<>();


    protected CommonUIColorManager() {
        super();

        //
        // Populate our color mappings for lookup.
        //
        var colorMappings = this.getColorMappings();

        this.deviceColors = new int[colorMappings.size()];
        this.displayColors = new ColorEx[colorMappings.size()];

        // Break our mappings into two arrays, which is what our color utilities like.
        var index = 0;
        for (var pair : colorMappings) {
            this.deviceColors[index] = pair.getKey();
            this.displayColors[index] = pair.getValue();
            index += 1;
        }

    }


    /**
     * Fetches a set of color mappings for the device, which map Display colors
     * to control colors.
     *
     * @return A Pair with each known Device color, and the closest available display color.
     */
    abstract protected List<Pair<Integer, ColorEx>> getColorMappings();


    /**
     * Converts a Java color into a per-device color.
     * <p>
     * If your device has different color types for e.g. individual ranges of buttons, override this.
     * The default implementation ignores controlType.
     *
     * @param color       The display color to be converted.
     * @param controlType The type of control we're requesting a color for.
     * @return A device-specific integer that means this color.
     */
    public int getDeviceColor(ColorEx color, ControlType controlType) {
        // Compute the color lookup, memoizing as we go.
        return this.colorLookupCache.computeIfAbsent(color, (c) -> this.deviceColors[ColorEx.getClosestColorIndex(c, this.displayColors)]);
    }


    /**
     * Converts a Java color into a per-device color.
     * <p>
     * If your device has different color types for e.g. individual ranges of buttons, override this.
     * The default implementation ignores controlType.
     *
     * @param color The display color to be converted.
     * @return A device-specific integer that means this color.
     */
    public int getDeviceColor(ColorEx color) {
        return this.getDeviceColor(color, ControlType.BUTTON);
    }


    /**
     * Specifies which type of control we're asking for.
     */
    public enum ControlType {
        /**
         * Indicates that the color is being requested for a button.
         */
        BUTTON,

        /**
         * Indicates that the color is being requested for a button on a "special" ROW1.
         * Usually the row _below_ the device's screen.
         */
        BUTTON_ROW1,

        /**
         * Indicates that the color is being requested for a button on a "special" ROW2.
         * Usually the row above the device's screen.
         */
        BUTTON_ROW2,

    }

}
