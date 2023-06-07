// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.modes;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IItem;
import de.mossgrabers.framework.daw.resource.ChannelType;
import de.mossgrabers.framework.featuregroup.AbstractFeatureGroup;
import de.mossgrabers.framework.featuregroup.AbstractMode;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Configuration settings for CommonUI devices.
 *
 * @author Jürgen Moßgraber
 */
public class SetupMode extends BaseMode<IItem>
{

    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public SetupMode(final CommonUIControlSurface surface, final IModel model)
    {
        super("Setup", surface, model);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobValue(final int index, final int value)
    {
        final CommonUIConfiguration config = this.surface.getConfiguration();
        switch (index) {
            case 2:
                config.changeDisplayBrightness(value);
                break;
            case 3:
                config.changeLEDBrightness(value);
                break;
            case 5:
                config.changePadSensitivity(value);
                break;
            case 6:
                config.changePadGain(value);
                break;
            case 7:
                config.changePadDynamics(value);
                break;
            default:
                // Not used
                break;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched)
    {
        this.setTouchedKnob(index, isTouched);

        if (!isTouched || !this.surface.isDeletePressed()) {
            return;
        }

        this.surface.setTriggerConsumed(ButtonID.DELETE);

        final CommonUIConfiguration config = this.surface.getConfiguration();
        switch (index) {
            case 2:
                config.setDisplayBrightness(100);
                break;
            case 3:
                config.setLEDBrightness(100);
                break;
            case 5:
                config.setPadSensitivity(5);
                break;
            case 6:
                config.setPadGain(5);
                break;
            case 7:
                config.setPadDynamics(5);
                break;
            default:
                // Not used
                break;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getButtonColorID(final ButtonID buttonID)
    {
        final int index = this.isButtonRow(1, buttonID);
        if (index >= 0) {
            if (index == 0) {
                return AbstractMode.BUTTON_COLOR_HI;
            }
            if (index == 1) {
                return AbstractFeatureGroup.BUTTON_COLOR_ON;
            }
        }

        return AbstractFeatureGroup.BUTTON_COLOR_OFF;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onSecondRow(final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.UP) {
            return;
        }
        if (index == 1) {
            this.surface.getModeManager().setTemporary(Modes.INFO);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        final CommonUIConfiguration config            = this.surface.getConfiguration();
        final int                   displayBrightness = config.getDisplayBrightness();
        final int                   ledBrightness     = config.getLedBrightness();
        final int                   padSensitivity    = config.getPadSensitivity();
        final int                   padGain           = config.getPadGain();
        final int                   padDynamics       = config.getPadDynamics();

        display.addOptionElement("", "Setup", true, "", "", false, true);
        display.addOptionElement("Brightness", "Info", false, "", "", false, true);
        display.addParameterElement(" ", false, "", (ChannelType) null, null, false, "Display", displayBrightness * 1023 / 100, displayBrightness + "%", this.isKnobTouched(2), -1);
        display.addParameterElement(" ", false, "", (ChannelType) null, null, false, "LEDs", ledBrightness * 1023 / 100, ledBrightness + "%", this.isKnobTouched(3), -1);
        display.addOptionElement("        Pads", " ", false, "", "", false, true);
        display.addParameterElement(" ", false, "", (ChannelType) null, null, false, "Sensitivity", padSensitivity * 1023 / 10, Integer.toString(padSensitivity), this.isKnobTouched(5), -1);
        display.addParameterElement(" ", false, "", (ChannelType) null, null, false, "Gain", padGain * 1023 / 10, Integer.toString(padGain), this.isKnobTouched(6), -1);
        display.addParameterElement(" ", false, "", (ChannelType) null, null, false, "Dynamics", padDynamics * 1023 / 10, Integer.toString(padDynamics), this.isKnobTouched(7), -1);
    }

}