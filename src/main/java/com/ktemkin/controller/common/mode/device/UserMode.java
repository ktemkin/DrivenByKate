// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.mode.device;

import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import com.ktemkin.controller.common.mode.BaseMode;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IItem;
import de.mossgrabers.framework.featuregroup.AbstractFeatureGroup;
import de.mossgrabers.framework.featuregroup.AbstractMode;
import de.mossgrabers.framework.parameter.IParameter;
import de.mossgrabers.framework.parameterprovider.device.BankParameterProvider;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Mode for editing user control parameters.
 *
 * @author Jürgen Moßgraber
 */
public class UserMode extends BaseMode<IParameter> {
    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public UserMode(final CommonUIControlSurface surface, final IModel model) {
        super("User Controls", surface, model, model.getUserParameterBank());

        this.setParameterProvider(new BankParameterProvider(this.bank));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched) {
        this.setTouchedKnob(index, isTouched);

        final IParameter param = this.bank.getItem(index);
        if (isTouched && this.surface.isDeletePressed()) {
            this.surface.setTriggerConsumed(ButtonID.DELETE);
            param.resetValue();
        }
        param.touchValue(isTouched);
        this.checkStopAutomationOnKnobRelease(isTouched);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onFirstRow(final int index, final ButtonEvent event) {
        if (event == ButtonEvent.UP)
            this.selectItemPage(index);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getButtonColor(final ButtonID buttonID) {
        int index = this.isButtonRow(0, buttonID);
        if (index >= 0) {
            final int selectedColor = this.getColorManager().getDeviceColor(ColorEx.ORANGE);
            final int existsColor = this.getColorManager().getDeviceColor(ColorEx.DARK_YELLOW);

            final int selectedPage = this.bank.getScrollPosition() / this.bank.getPageSize();
            return index == selectedPage ? selectedColor : existsColor;
        }

        index = this.isButtonRow(1, buttonID);
        if (index >= 0) {
            final IItem param = this.bank.getItem(index);
            if (!param.doesExist())
                return super.getButtonColor(buttonID);

            final int max = this.model.getValueChanger().getUpperBound() - 1;
            return this.colorManager.getColorIndex(((IParameter) param).getValue() > max / 2 ? AbstractMode.BUTTON_COLOR_HI : AbstractFeatureGroup.BUTTON_COLOR_ON);
        }

        return super.getButtonColor(buttonID);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onSecondRow(final int index, final ButtonEvent event) {
        if (event != ButtonEvent.UP)
            return;

        final IParameter param = this.bank.getItem(index);
        if (!param.doesExist())
            return;

        // Toggle between the min and max value
        final int max = this.model.getValueChanger().getUpperBound() - 1;
        param.setValueImmediatly(param.getValue() < max / 2 ? max : 0);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display) {
        final IValueChanger valueChanger = this.model.getValueChanger();
        final String[] userPageNames = this.surface.getConfiguration().getUserPageNames();
        final int pageSize = this.bank.getPageSize();
        final int selectedPage = this.bank.getScrollPosition() / pageSize;
        for (int i = 0; i < pageSize; i++) {
            final boolean isBottomMenuOn = i == selectedPage;

            final IParameter param = this.bank.getItem(i);
            final boolean exists = param.doesExist();
            final String parameterName = exists ? param.getName(9) : "";
            final int parameterValue = valueChanger.toDisplayValue(exists ? param.getValue() : 0);
            final String parameterValueStr = exists ? param.getDisplayedValue(8) : "";
            final boolean parameterIsActive = this.isKnobTouched(i);
            final int parameterModulatedValue = valueChanger.toDisplayValue(exists ? param.getModulatedValue() : -1);

            display.addParameterElement("", false, userPageNames[i], "USER", isBottomMenuOn ? ColorEx.WHITE : ColorEx.GRAY, isBottomMenuOn, parameterName, parameterValue, parameterValueStr, parameterIsActive, parameterModulatedValue);
        }
    }
}