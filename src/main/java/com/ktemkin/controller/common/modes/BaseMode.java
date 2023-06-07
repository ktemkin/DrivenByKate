// Written by Kate Temkin - ktemkin.com
// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.modes;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIColorManager;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ITransport;
import de.mossgrabers.framework.daw.data.IItem;
import de.mossgrabers.framework.daw.data.bank.IBank;
import de.mossgrabers.framework.featuregroup.AbstractParameterMode;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Base class for all modes used by "Common UI" devices.
 * These can be used for pretty much any device that has a screen with either two rows of buttons,
 * or a row of buttons and a row of touch-knobs.
 *
 * @param <B> The type of the item bank
 * @author Kate Temkin
 * @author Jürgen Moßgraber
 */
public abstract class BaseMode<B extends IItem> extends AbstractParameterMode<CommonUIControlSurface<CommonUIConfiguration>, CommonUIConfiguration, B>
{

    protected static final int SCROLL_RATE = 8;

    private int movementCounter = 0;


    /**
     * Constructor.
     *
     * @param name    The name of the mode
     * @param surface The control surface
     * @param model   The model
     */
    protected BaseMode(final String name, final CommonUIControlSurface surface, final IModel model)
    {
        this(name, surface, model, null);
    }


    /**
     * Constructor.
     *
     * @param name    The name of the mode
     * @param surface The control surface
     * @param model   The model
     * @param bank    The parameter bank to control with this mode, might be null
     */
    protected BaseMode(final String name, final CommonUIControlSurface surface, final IModel model, final IBank<B> bank)
    {
        super(name, surface, model, true, bank, DEFAULT_KNOB_IDS);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDisplay()
    {
        final IGraphicDisplay display = (IGraphicDisplay) this.surface.getGraphicsDisplay();
        this.updateGraphicsDisplay(display);
        display.send();
    }


    /**
     * @return the mode's color manager as a CommonUI type
     */
    public CommonUIColorManager getColorManager()
    {
        return (CommonUIColorManager) this.colorManager;
    }


    /**
     * Update the graphical display of our device.
     *
     * @param display The display
     */
    public abstract void updateGraphicsDisplay(final IGraphicDisplay display);


    /**
     * {@inheritDoc}
     */
    @Override
    public void onButton(final int row, final int index, final ButtonEvent event)
    {
        if (row == 0) {
            this.onFirstRow(index, event);
        }
        else {
            this.onSecondRow(index, event);
        }
    }


    /**
     * Down press on a first row button.
     *
     * @param index The index of the button
     * @param event The button event
     */
    public void onFirstRow(final int index, final ButtonEvent event)
    {
        if (event == ButtonEvent.UP) {
            this.model.getCurrentTrackBank().getItem(index).select();
        }
    }


    /**
     * Down press on a second row button.
     *
     * @param index The index of the button
     * @param event The button event
     */
    public void onSecondRow(final int index, final ButtonEvent event)
    {
        // Intentionally empty
    }


    /**
     * Check if the automation needs to be stopped because a knob is no longer touched.
     *
     * @param isTouched The touch state
     */
    protected void checkStopAutomationOnKnobRelease(final boolean isTouched)
    {
        if (!this.surface.getConfiguration().isStopAutomationOnKnobRelease() || isTouched) {
            return;
        }
        final ITransport transport = this.model.getTransport();
        if (transport.isWritingArrangerAutomation()) {
            transport.toggleWriteArrangerAutomation();
        }
        if (transport.isWritingClipLauncherAutomation()) {
            transport.toggleWriteClipLauncherAutomation();
        }
    }


    /**
     * Slows down knob movement. Increases the counter till the scroll rate.
     *
     * @return True if the knob movement should be executed otherwise false
     */
    protected boolean increaseKnobMovement()
    {
        this.movementCounter++;
        if (this.movementCounter < SCROLL_RATE) {
            return false;
        }
        this.movementCounter = 0;
        return true;
    }

}