// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.controller;

import com.ktemkin.controller.common.CommonUIConfiguration;
import de.mossgrabers.framework.controller.AbstractControlSurface;
import de.mossgrabers.framework.controller.IControlSurface;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.grid.PadGridImpl;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.IMidiOutput;


/**
 * Base control surface for CommonUI devices.
 * CommonUI devices contain at least one graphic display, a row of 8 buttons, and either a second row of
 * 8 buttons or a row of 8 touch knobs.
 *
 * @author Kate Temkin
 * @author Jürgen Moßgraber
 */
public class CommonUIControlSurface<C extends CommonUIConfiguration> extends AbstractControlSurface<C> implements IControlSurface<C>
{

    /**
     * Constructor.
     *
     * @param host          The host
     * @param colorManager  The color manager
     * @param configuration The configuration
     * @param output        The MIDI output
     * @param input         The MIDI input
     */
    public CommonUIControlSurface(final IHost host, final ColorManager colorManager, final CommonUIConfiguration configuration, final IMidiOutput output, final IMidiInput input, final double width, final double height)
    {
        super(host,
                (C)configuration,
                colorManager,
                output,
                input,
                new PadGridImpl(colorManager, output),
                width,
                height);
    }


    /**
     * Set the ribbon mode on the provided controller.
     * If your controller supports a ribbon, you should override this.
     *
     * @param mode The mode to set
     */
    public void setRibbonMode(final int mode)
    {
    }


    /**
     * Set the display value of the ribbon on the controller.
     * If your controller supports a ribbon, you should override this.
     *
     * @param value The value to set
     */
    public void setRibbonValue(final int value)
    {
    }


    /**
     * Send the display brightness.
     * Override this to implement a per-controller version.
     */
    public void sendDisplayBrightness()
    {
    }


    /**
     * Send the _global_ LED brightness, e.g. the brightness of your pad grid.
     * Override this to provide the variant for your controller.
     */
    public void sendLEDBrightness()
    {
    }


    public int getMajorVersion()
    {
        return -1;
    }


    public int getBoardRevision()
    {
        return -1;
    }


    public int getMinorVersion()
    {
        return -1;
    }


    public int getBuildNumber()
    {
        return -1;
    }


    public int getSerialNumber()
    {
        return -1;
    }

}