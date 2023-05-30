// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.maschine.mk3.mode;

import com.ktemkin.controller.ni.maschine.Maschine;
import com.ktemkin.controller.ni.maschine.mk3.MaschineConfiguration;
import com.ktemkin.controller.ni.maschine.mk3.controller.MaschineControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.DAWColor;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.constants.Capability;
import de.mossgrabers.framework.daw.data.ICursorDevice;
import de.mossgrabers.framework.daw.data.IDevice;
import de.mossgrabers.framework.daw.data.bank.IDeviceBank;
import de.mossgrabers.framework.daw.data.bank.IParameterBank;
import de.mossgrabers.framework.daw.data.bank.IParameterPageBank;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.mode.device.SelectedDeviceMode;
import de.mossgrabers.framework.parameter.IParameter;
import de.mossgrabers.framework.parameterprovider.device.BankParameterProvider;
import de.mossgrabers.framework.utils.StringUtils;


/**
 * Mode for editing the parameters of the selected device.
 *
 * @author Jürgen Moßgraber
 */
public class MaschineParametersMode extends SelectedDeviceMode<MaschineControlSurface, MaschineConfiguration> implements IMaschineMode {
    private static final String[] MENU =
            {
                    "On",
                    "Parameters",
                    "Expanded",
                    "Chains",
                    "Banks",
                    "Pin Device",
                    "Window",
                    "Up"
            };
    protected final String[] hostMenu = new String[MENU.length];
    protected boolean showDevices;


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public MaschineParametersMode(final MaschineControlSurface surface, final IModel model) {
        super(surface, model, surface.getMaschine().hasMCUDisplay() ? DEFAULT_KNOB_IDS : null, () -> false);

        if (surface.getMaschine().hasMCUDisplay())
            this.setParameterProvider(new BankParameterProvider(this.bank));

        this.initTouchedStates(9);

        System.arraycopy(MENU, 0, this.hostMenu, 0, MENU.length);
        final IHost host = this.model.getHost();
        if (!host.supports(Capability.HAS_PARAMETER_PAGE_SECTION))
            this.hostMenu[1] = "";
        if (!host.supports(Capability.HAS_PINNING))
            this.hostMenu[5] = "";
        if (!host.supports(Capability.HAS_SLOT_CHAINS))
            this.hostMenu[3] = "";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDisplay() {
        this.delegatePerDisplayType();
    }


    protected boolean getTopMenuEnablement(final ICursorDevice cd, final boolean hasPinning, final int index) {
        switch (index) {
            case 0:
                return cd.isEnabled();
            case 1:
                return cd.isParameterPageSectionVisible();
            case 2:
                return cd.isExpanded();
            case 3:
                return this.surface.getModeManager().isActive(Modes.DEVICE_CHAINS);
            case 4:
                return !this.surface.getModeManager().isActive(Modes.DEVICE_CHAINS) && !this.showDevices;
            case 5:
                return hasPinning && cd.isPinned();
            case 6:
                return cd.isWindowOpen();
            case 7:
                return true;
            default:
                // Not used
                return false;
        }
    }


    @Override
    public void updateGraphicsDisplay(IGraphicDisplay display) {
        final ICursorDevice cd = this.model.getCursorDevice();
        if (!cd.doesExist()) {
            return;
        }

        final String channelColor = this.model.getCurrentTrackBank().getSelectedChannelColorEntry();
        final ColorEx bottomMenuColor = DAWColor.getColorEntry(channelColor);
        final ColorEx colorBackground = this.surface.getConfiguration().getColorBackground();

        final IDeviceBank deviceBank = cd.getDeviceBank();
        final IParameterBank parameterBank = cd.getParameterBank();
        final IParameterPageBank parameterPageBank = parameterBank.getPageBank();
        final int selectedPage = parameterPageBank.getSelectedItemIndex();
        final boolean hasPinning = this.model.getHost().supports(Capability.HAS_PINNING);
        final IValueChanger valueChanger = this.model.getValueChanger();

        for (int i = 0; i < parameterBank.getPageSize(); i++) {
            final boolean isTopMenuOn = this.getTopMenuEnablement(cd, hasPinning, i);

            String bottomMenu;
            final String bottomMenuIcon;
            boolean isBottomMenuOn;
            ColorEx color = bottomMenuColor;
            if (this.showDevices) {
                final IDevice device = deviceBank.getItem(i);
                bottomMenuIcon = device.getName();
                bottomMenu = device.doesExist() ? device.getName(12) : "";
                isBottomMenuOn = i == cd.getIndex();
                if (!device.isEnabled())
                    color = colorBackground;
            } else {
                bottomMenuIcon = cd.getName();
                bottomMenu = parameterPageBank.getItem(i);

                if (bottomMenu.length() > 12)
                    bottomMenu = bottomMenu.substring(0, 12);
                isBottomMenuOn = i == selectedPage;
            }

            final IParameter param = parameterBank.getItem(i);
            final boolean exists = param.doesExist();
            final String parameterName = exists ? param.getName(9) : "";
            final int parameterValue = valueChanger.toDisplayValue(exists ? param.getValue() : 0);
            final String parameterValueStr = exists ? param.getDisplayedValue(8) : "";
            final boolean parameterIsActive = this.isKnobTouched(i);
            final int parameterModulatedValue = valueChanger.toDisplayValue(exists ? param.getModulatedValue() : -1);

            display.addParameterElement(this.hostMenu[i], isTopMenuOn, bottomMenu, bottomMenuIcon, color, isBottomMenuOn, parameterName, parameterValue, parameterValueStr, parameterIsActive, parameterModulatedValue);
        }
    }


    @Override
    public void updateTextDisplay(ITextDisplay d) {
        final ICursorDevice cd = this.model.getCursorDevice();
        if (!cd.doesExist()) {
            d.notify("Please select a device...");
            return;
        }

        // Row 1 & 2
        final IParameterBank parameterBank = cd.getParameterBank();
        for (int i = 0; i < 8; i++) {
            final IParameter param = parameterBank.getItem(i);
            String name = param.doesExist() ? StringUtils.shortenAndFixASCII(param.getName(), 6) : "";
            if (i == this.getSelectedParameter())
                name = ">" + name;
            d.setCell(0, i, name).setCell(1, i, StringUtils.shortenAndFixASCII(param.getDisplayedValue(8), 8));
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched) {
        this.setTouchedKnob(index, isTouched);

        super.onKnobTouch(index == 8 ? -1 : index, isTouched);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void selectPreviousItem() {
        if (this.surface.getMaschine() == Maschine.STUDIO && this.surface.isPressed(ButtonID.TRACK)) {
            this.model.getCursorDevice().selectPrevious();
            this.mvHelper.notifySelectedDevice();
            return;
        }

        final int selectedParameter = this.getSelectedParameter();
        if (selectedParameter == 0) {
            super.selectPreviousItem();
            this.selectParameter(7);
        } else
            this.selectParameter(selectedParameter - 1);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void selectNextItem() {
        if (this.surface.getMaschine() == Maschine.STUDIO && this.surface.isPressed(ButtonID.TRACK)) {
            this.model.getCursorDevice().selectNext();
            this.mvHelper.notifySelectedDevice();
            return;
        }

        final int selectedParameter = this.getSelectedParameter();
        if (selectedParameter == 7) {
            super.selectNextItem();
            this.selectParameter(0);
        } else
            this.selectParameter(selectedParameter + 1);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void selectPreviousItemPage() {
        super.selectPreviousItem();
        this.mvHelper.notifySelectedParameterPage();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void selectNextItemPage() {
        super.selectNextItem();
        this.mvHelper.notifySelectedParameterPage();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPreviousItem() {
        return this.getSelectedParameter() > 0 || super.hasPreviousItem();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNextItem() {
        return this.getSelectedParameter() < 7 || super.hasNextItem();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPreviousItemPage() {
        return super.hasPreviousItem();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNextItemPage() {
        return super.hasNextItem();
    }


    @Override
    public MaschineControlSurface getSurface() {
        return this.surface;
    }


}
