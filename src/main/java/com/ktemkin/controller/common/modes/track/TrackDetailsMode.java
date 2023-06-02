// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.modes.track;

import com.ktemkin.controller.ableton.push.PushConfiguration;
import com.ktemkin.controller.ableton.push.controller.PushColorManager;
import com.ktemkin.controller.ableton.push.controller.PushControlSurface;
import com.ktemkin.controller.common.modes.BaseMode;
import de.mossgrabers.framework.controller.ButtonID;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
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
    public TrackDetailsMode(final PushControlSurface surface, final IModel model)
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

        final PushConfiguration configuration = this.surface.getConfiguration();
        final int               speed         = this.model.getValueChanger().isIncrease(value) ? 1 : -1;
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
        int index = this.isButtonRow(0, buttonID);
        if (index >= 0) {
            final ICursorTrack cursorTrack = this.model.getCursorTrack();
            if (!cursorTrack.doesExist()) {
                return super.getButtonColor(buttonID);
            }

            switch (index) {
                case 0:
                    return this.colorManager.getColorIndex(cursorTrack.isActivated() ? PushColorManager.PUSH_YELLOW_MD : PushColorManager.PUSH_YELLOW_LO);
                case 1:
                    return this.colorManager.getColorIndex(cursorTrack.isRecArm() ? PushColorManager.PUSH_RED_HI : PushColorManager.PUSH_RED_LO);
                case 2:
                    return this.colorManager.getColorIndex(cursorTrack.isMute() ? PushColorManager.PUSH_ORANGE_HI : PushColorManager.PUSH_ORANGE_LO);
                case 3:
                    return this.colorManager.getColorIndex(cursorTrack.isSolo() ? PushColorManager.PUSH_ORANGE_HI : PushColorManager.PUSH_ORANGE_LO);
                case 4:
                    return this.colorManager.getColorIndex(cursorTrack.isMonitor() ? PushColorManager.PUSH_GREEN_HI : PushColorManager.PUSH_GREEN_LO);
                case 5:
                    return this.colorManager.getColorIndex(cursorTrack.isAutoMonitor() ? PushColorManager.PUSH_GREEN_HI : PushColorManager.PUSH_GREEN_LO);
                case 6:
                    if (!this.hasPinning) {
                        return PushColorManager.PUSH2_COLOR_BLACK;
                    }
                    return this.colorManager.getColorIndex(cursorTrack.isPinned() ? PushColorManager.PUSH_GREEN_HI : PushColorManager.PUSH_GREEN_LO);
                default:
                case 7:
                    return PushColorManager.PUSH2_COLOR_GREEN_HI;
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

        final PushConfiguration configuration = this.surface.getConfiguration();
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