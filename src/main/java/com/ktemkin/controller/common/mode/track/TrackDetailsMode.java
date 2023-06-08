// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.mode.track;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import com.ktemkin.controller.common.mode.BaseMode;
import de.mossgrabers.framework.controller.ButtonID;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.constants.Capability;
import de.mossgrabers.framework.daw.data.ICursorTrack;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.daw.resource.ChannelType;
import de.mossgrabers.framework.featuregroup.AbstractFeatureGroup;
import de.mossgrabers.framework.featuregroup.ViewManager;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.ColorSelectMode;
import de.mossgrabers.framework.view.ColorView;
import de.mossgrabers.framework.view.Views;

import java.util.Optional;


/**
 * Mode for editing details of a track.
 *
 * @author Jürgen Moßgraber
 */
public class TrackDetailsMode extends BaseMode<ITrack>
{

    private final boolean hasPinning;


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public TrackDetailsMode(final CommonUIControlSurface surface, final IModel model)
    {
        super("Track details", surface, model, model.getCurrentTrackBank());

        this.hasPinning = this.model.getHost().supports(Capability.HAS_PINNING);

        model.addTrackBankObserver(this::switchBanks);
    }


    /**
     * Get a label for the track.
     *
     * @param track The track
     * @return The label
     */
    private static String getTrackTitle(final ITrack track)
    {
        return ChannelType.getLabel(track.getType()) + " Track: ";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobValue(final int index, final int value)
    {
        if (index < 6) {
            return;
        }

        final CommonUIConfiguration configuration = this.surface.getConfiguration();
        final int                   speed         = this.model.getValueChanger().isIncrease(value) ? 1 : -1;
        configuration.setMidiEditChannel(configuration.getMidiEditChannel() + speed);
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
        if (this.model.getMasterTrack().isSelected()) {
            this.onFirstRowMasterTrack(index);
        }
        else {
            this.onFirstRowTrack(index);
        }
    }


    private void onFirstRowMasterTrack(final int index)
    {
        switch (index) {
            case 0:
                this.model.getMasterTrack().toggleIsActivated();
                break;
            case 1:
                this.model.getMasterTrack().toggleRecArm();
                break;
            case 2:
                this.model.getMasterTrack().toggleMute();
                break;
            case 3:
                this.model.getMasterTrack().toggleSolo();
                break;
            case 4:
                this.model.getMasterTrack().toggleMonitor();
                break;
            case 5:
                this.model.getMasterTrack().toggleAutoMonitor();
                break;
            case 6:
                // Not used
                break;
            case 7:
                final ViewManager viewManager = this.surface.getViewManager();
                ((ColorView<?, ?>) viewManager.get(Views.COLOR)).setMode(ColorSelectMode.MODE_TRACK);
                viewManager.setActive(Views.COLOR);
                break;
            default:
                // Not used
                break;
        }
    }


    private void onFirstRowTrack(final int index)
    {
        final ITrackBank       tb    = this.model.getCurrentTrackBank();
        final Optional<ITrack> track = tb.getSelectedItem();
        if (track.isEmpty()) {
            return;
        }

        final ITrack t = track.get();

        switch (index) {
            case 0:
                t.toggleIsActivated();
                break;
            case 1:
                t.toggleRecArm();
                break;
            case 2:
                t.toggleMute();
                break;
            case 3:
                t.toggleSolo();
                break;
            case 4:
                t.toggleMonitor();
                break;
            case 5:
                t.toggleAutoMonitor();
                break;
            case 6:
                this.model.getCursorTrack().togglePinned();
                break;
            case 7:
                final ViewManager viewManager = this.surface.getViewManager();
                ((ColorView<?, ?>) viewManager.get(Views.COLOR)).setMode(ColorSelectMode.MODE_TRACK);
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
    public int getButtonColor(final ButtonID buttonID)
    {
        var colorManager = this.getColorManager();

        int index = this.isButtonRow(0, buttonID);
        if (index >= 0) {
            final ICursorTrack cursorTrack = this.model.getCursorTrack();
            if (!cursorTrack.doesExist()) {
                return super.getButtonColor(buttonID);
            }

            switch (index) {
                case 0:
                    return colorManager.getDeviceColor(cursorTrack.isActivated() ? ColorEx.YELLOW : ColorEx.DARK_YELLOW);
                case 1:
                    return colorManager.getDeviceColor(cursorTrack.isRecArm() ? ColorEx.RED : ColorEx.DARK_RED);
                case 2:
                    return colorManager.getDeviceColor(cursorTrack.isMute() ? ColorEx.ORANGE : ColorEx.DARK_ORANGE);
                case 3:
                    return colorManager.getDeviceColor(cursorTrack.isSolo() ? ColorEx.ORANGE : ColorEx.DARK_ORANGE);
                case 4:
                    return colorManager.getDeviceColor(cursorTrack.isMonitor() ? ColorEx.GREEN : ColorEx.DARK_GREEN);
                case 5:
                    return colorManager.getDeviceColor(cursorTrack.isAutoMonitor() ? ColorEx.GREEN : ColorEx.DARK_GREEN);
                case 6:
                    if (!this.hasPinning) {
                        return colorManager.getDeviceColor(ColorEx.BLACK);
                    }
                    return colorManager.getDeviceColor(cursorTrack.isPinned() ? ColorEx.GREEN : ColorEx.DARK_GREEN);
                default:
                case 7:
                    return colorManager.getDeviceColor(ColorEx.GREEN);
            }
        }

        index = this.isButtonRow(1, buttonID);
        if (index >= 0) {
            return this.colorManager.getColorIndex(index < 6 ? AbstractFeatureGroup.BUTTON_COLOR_OFF : AbstractFeatureGroup.BUTTON_COLOR_ON);
        }

        return this.colorManager.getColorIndex(AbstractFeatureGroup.BUTTON_COLOR_OFF);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onSecondRow(final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.DOWN) {
            return;
        }

        final CommonUIConfiguration configuration = this.surface.getConfiguration();
        switch (index) {
            case 6:
                configuration.setMidiEditChannel(configuration.getMidiEditChannel() - 1);
                break;

            case 7:
                configuration.setMidiEditChannel(configuration.getMidiEditChannel() + 1);
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
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        final ITrack cursorTrack = this.model.getCursorTrack();
        if (!cursorTrack.doesExist()) {
            display.setMessage(3, "Please select a track...");
            return;
        }

        display.addOptionElement(getTrackTitle(cursorTrack) + cursorTrack.getName(), "", false, "", "Active", cursorTrack.isActivated(), false);
        display.addOptionElement("", "", false, "", "Rec Arm", cursorTrack.isRecArm(), false);
        display.addOptionElement("", "", false, "", "Mute", cursorTrack.isMute(), false);
        display.addOptionElement("", "", false, "", "Solo", cursorTrack.isSolo(), false);
        display.addOptionElement("", "", false, "", "Monitor", cursorTrack.isMonitor(), false);
        display.addOptionElement("Midi Insert/Edit Channel:", "", false, "", "Auto Monitor", cursorTrack.isAutoMonitor(), false);
        if (this.hasPinning) {
            display.addOptionElement("", "", false, "", "Pin Track", this.model.getCursorTrack().isPinned(), false);
        }
        else {
            display.addEmptyElement();
        }
        display.addOptionElement("        " + (this.surface.getConfiguration().getMidiEditChannel() + 1), "", false, "", "Select Color", false, false);
    }

}