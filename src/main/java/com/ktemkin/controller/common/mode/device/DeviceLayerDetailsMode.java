// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.mode.device;

import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import com.ktemkin.controller.common.mode.BaseMode;
import de.mossgrabers.framework.controller.ButtonID;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IChannel;
import de.mossgrabers.framework.daw.data.ICursorDevice;
import de.mossgrabers.framework.daw.data.ILayer;
import de.mossgrabers.framework.daw.data.bank.IBank;
import de.mossgrabers.framework.daw.data.bank.IDrumPadBank;
import de.mossgrabers.framework.featuregroup.ViewManager;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.ColorSelectMode;
import de.mossgrabers.framework.view.ColorView;
import de.mossgrabers.framework.view.Views;

import java.util.Optional;


/**
 * Mode for editing details of a layer.
 *
 * @author Jürgen Moßgraber
 */
public class DeviceLayerDetailsMode extends BaseMode<ILayer>
{

    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public DeviceLayerDetailsMode(final CommonUIControlSurface surface, final IModel model)
    {
        super("Layer details", surface, model, model.getCursorDevice().getLayerBank());

        final ICursorDevice cursorDevice = model.getCursorDevice();

        final ViewManager viewManager = surface.getViewManager();
        viewManager.addChangeListener((previousID, activeID) -> {

            final IBank<ILayer> bank;
            switch (activeID) {
                case DRUM:
                    bank = cursorDevice.getDrumPadBank();
                    break;
                case DRUM64:
                    bank = this.model.getDrumDevice(64).getDrumPadBank();
                    break;
                case COLOR:
                    // Do not switch banks if view is left for color selection
                    return;
                default:
                    bank = cursorDevice.getLayerBank();
                    break;
            }
            this.switchBanks(bank);

        });
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
        final Optional<ILayer> channelOpt = this.bank.getSelectedItem();
        if (channelOpt.isEmpty()) {
            return;
        }

        final IChannel channel = channelOpt.get();

        switch (index) {
            case 0:
                channel.toggleIsActivated();
                break;
            case 2:
                channel.toggleMute();
                break;
            case 3:
                channel.toggleSolo();
                break;
            case 7:
                final ViewManager viewManager = this.surface.getViewManager();
                ((ColorView<?, ?>) viewManager.get(Views.COLOR)).setMode(ColorSelectMode.MODE_LAYER);
                viewManager.setActive(Views.COLOR);
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
    public void onSecondRow(final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.UP) {
            return;
        }

        switch (index) {
            case 6:
                if (this.bank instanceof final IDrumPadBank drumPadBank) {
                    drumPadBank.clearMute();
                }
                break;
            case 7:
                if (this.bank instanceof final IDrumPadBank drumPadBank) {
                    drumPadBank.clearSolo();
                }
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
    public int getButtonColor(final ButtonID buttonID)
    {
        final Optional<ILayer> channelOpt = this.bank.getSelectedItem();
        if (channelOpt.isEmpty()) {
            return super.getButtonColor(buttonID);
        }

        int index = this.isButtonRow(0, buttonID);
        if (index >= 0) {
            final IChannel channel = channelOpt.get();
            return switch (index) {
                case 0 -> this.getColorManager().getDeviceColor(channel.isActivated() ? ColorEx.YELLOW : ColorEx.DARK_YELLOW);
                case 2 -> this.getColorManager().getDeviceColor(channel.isMute() ? ColorEx.ORANGE : ColorEx.DARK_ORANGE);
                case 3 -> this.getColorManager().getDeviceColor(channel.isSolo() ? ColorEx.ORANGE : ColorEx.DARK_ORANGE);
                 case 7 -> this.getColorManager().getDeviceColor(ColorEx.GREEN);
                default -> this.getColorManager().getDeviceColor(ColorEx.BLACK);
            };
        }

        index = this.isButtonRow(1, buttonID);
        if (index >= 0) {
            if (index >= 6) {
                return this.getColorManager().getDeviceColor(this.bank instanceof IDrumPadBank ? ColorEx.WHITE : ColorEx.BLACK);
            } else {
                return this.getColorManager().getDeviceColor(ColorEx.BLACK);
            }
        }

        return super.getButtonColor(buttonID);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        final Optional<ILayer> channelOpt = this.bank.getSelectedItem();
        if (channelOpt.isEmpty()) {
            display.setMessage(3, "Please select a layer...");
            return;
        }

        final IChannel channel = channelOpt.get();

        display.addOptionElement("Layer: " + channel.getName(), "", false, "", "Active", channel.isActivated(), false);
        display.addEmptyElement();
        display.addOptionElement("", "", false, "", "Mute", channel.isMute(), false);
        display.addOptionElement("", "", false, "", "Solo", channel.isSolo(), false);
        display.addEmptyElement();
        display.addEmptyElement();
        if (this.bank instanceof IDrumPadBank) {
            display.addOptionElement("", "Clear Mute", false, "", "", false, false);
            display.addOptionElement("", "Clear Solo", false, "", "Select Color", false, false);
        }
        else {
            display.addEmptyElement();
            display.addEmptyElement();
        }
    }

}