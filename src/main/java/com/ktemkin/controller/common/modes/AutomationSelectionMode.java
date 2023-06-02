// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.modes;

import com.ktemkin.controller.ableton.push.controller.Push1Display;
import com.ktemkin.controller.ableton.push.controller.PushControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.constants.AutomationMode;
import de.mossgrabers.framework.daw.data.IItem;
import de.mossgrabers.framework.featuregroup.AbstractFeatureGroup;
import de.mossgrabers.framework.featuregroup.AbstractMode;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Selection of the automation mode.
 *
 * @author Jürgen Moßgraber
 */
public class AutomationSelectionMode extends BaseMode<IItem>
{

    private final AutomationMode[] automationWriteModes;


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public AutomationSelectionMode(final PushControlSurface surface, final IModel model)
    {
        super("Automation", surface, model);

        this.automationWriteModes = this.model.getTransport().getAutomationWriteModes();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        final AutomationMode writeMode = this.model.getTransport().getAutomationWriteMode();
        for (int i = 0; i < 8; i++) {
            display.addOptionElement("", "", false, i == 0 ? "Automation Mode" : "", i < this.automationWriteModes.length ? this.automationWriteModes[i].getLabel() : "", i < this.automationWriteModes.length && this.automationWriteModes[i] == writeMode, false);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onFirstRow(final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.UP) {
            return;
        }
        if (index < this.automationWriteModes.length) {
            this.model.getTransport().setAutomationWriteMode(this.automationWriteModes[index]);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getButtonColor(final ButtonID buttonID)
    {
        final int index = this.isButtonRow(0, buttonID);
        if (index >= 0) {
            final AutomationMode writeMode = this.model.getTransport().getAutomationWriteMode();
            if (index < this.automationWriteModes.length) {
                return this.colorManager.getColorIndex(this.automationWriteModes[index] == writeMode ? AbstractMode.BUTTON_COLOR_HI : AbstractFeatureGroup.BUTTON_COLOR_ON);
            }
        }

        return super.getButtonColor(buttonID);
    }

}