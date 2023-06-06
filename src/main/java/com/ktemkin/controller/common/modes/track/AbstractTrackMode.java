// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.modes.track;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIColorManager;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import com.ktemkin.controller.common.modes.BaseMode;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.constants.Capability;
import de.mossgrabers.framework.daw.data.ICursorTrack;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ISendBank;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.daw.resource.ChannelType;
import de.mossgrabers.framework.featuregroup.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.parameter.IParameter;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Abstract base mode for all track modes.
 *
 * @author Jürgen Moßgraber
 */
public abstract class AbstractTrackMode extends BaseMode<ITrack>
{

    protected final List<Pair<String, Boolean>> menu = new ArrayList<>();


    /**
     * Constructor.
     *
     * @param name    The name of the mode
     * @param surface The control surface
     * @param model   The model
     */
    protected AbstractTrackMode(final String name, final CommonUIControlSurface surface, final IModel model)
    {
        super(name, surface, model, model.getCurrentTrackBank());

        model.addTrackBankObserver(this::switchBanks);

        for (int i = 0; i < 8; i++) {
            this.menu.add(new Pair<>(" ", Boolean.FALSE));
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched)
    {
        this.setTouchedKnob(index, isTouched);

        final IParameter parameter = this.getParameterProvider().get(index);

        if (isTouched && this.surface.isDeletePressed()) {
            this.surface.setTriggerConsumed(ButtonID.DELETE);
            parameter.resetValue();
        }

        parameter.touchValue(isTouched);
        this.checkStopAutomationOnKnobRelease(isTouched);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onFirstRow(final int index, final ButtonEvent event)
    {
        if (event == ButtonEvent.DOWN) {
            return;
        }

        final ITrackBank tb    = this.model.getCurrentTrackBank();
        final ITrack     track = tb.getItem(index);

        if (event == ButtonEvent.UP) {
            if (this.surface.isPressed(ButtonID.DUPLICATE)) {
                this.surface.setTriggerConsumed(ButtonID.DUPLICATE);
                track.duplicate();
                return;
            }

            if (this.surface.isPressed(ButtonID.DELETE)) {
                this.surface.setTriggerConsumed(ButtonID.DELETE);
                track.remove();
                return;
            }

            if (this.surface.isPressed(ButtonID.STOP_CLIP)) {
                this.surface.setTriggerConsumed(ButtonID.STOP_CLIP);
                track.stop(true);
                return;
            }

            if (this.surface.isPressed(ButtonID.RECORD)) {
                this.surface.setTriggerConsumed(ButtonID.RECORD);
                track.toggleRecArm();
                return;
            }

            if (!track.isSelected()) {
                track.select();
                return;
            }

            // If it is a group display child channels of group, otherwise jump into device
            // mode
            if (track.isGroup()) {
                if (this.surface.isShiftPressed()) {
                    track.toggleGroupExpanded();
                }
                else {
                    track.enter();
                }
            }
            else {
                this.surface.getButton(ButtonID.DEVICE).trigger(ButtonEvent.DOWN);
            }
            return;
        }

        // LONG press, go out of group
        if (!this.model.isEffectTrackBankActive()) {
            this.model.getTrackBank().selectParent();
            this.surface.setTriggerConsumed(ButtonID.get(ButtonID.ROW1_1, index));
        }
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

        final ITrackBank tb    = this.model.getCurrentTrackBank();
        final ITrack     track = tb.getItem(index);
        if (this.surface.isPressed(ButtonID.STOP_CLIP)) {
            this.surface.setTriggerConsumed(ButtonID.STOP_CLIP);
            track.stop(false);
            return;
        }

        final CommonUIConfiguration config = this.surface.getConfiguration();
        if (config.isMuteLongPressed() || config.isSoloLongPressed() || config.isMuteSoloLocked()) {
            if (config.isMuteState()) {
                track.toggleMute();
            }
            else {
                track.toggleSolo();
            }
            return;
        }

        final ModeManager modeManager = this.surface.getModeManager();
        switch (index) {
            case 0 -> {
                if (modeManager.isActive(Modes.VOLUME)) {
                    modeManager.setActive(Modes.TRACK);
                }
                else {
                    modeManager.setActive(Modes.VOLUME);
                }
            }
            case 1 -> {
                if (modeManager.isActive(Modes.PAN)) {
                    modeManager.setActive(Modes.TRACK);
                }
                else {
                    modeManager.setActive(Modes.PAN);
                }
            }
            case 2 -> {
                if (modeManager.isActive(Modes.CROSSFADER)) {
                    modeManager.setActive(Modes.TRACK);
                }
                else {
                    modeManager.setActive(Modes.CROSSFADER);
                }
            }
            case 3 -> {
                final boolean isShift = this.surface.isShiftPressed();
                for (int i = 0; i < tb.getPageSize(); i++) {
                    final ISendBank sendBank = tb.getItem(i).getSendBank();
                    if (isShift) {
                        if (sendBank.canScrollPageBackwards()) {
                            sendBank.selectPreviousPage();
                        }
                        else {
                            sendBank.scrollTo(sendBank.getItemCount() / 4 * 4);
                        }
                    }
                    else {
                        if (sendBank.canScrollPageForwards()) {
                            sendBank.selectNextPage();
                        }
                        else {
                            sendBank.scrollTo(0);
                        }
                    }
                }
                this.bindControls();
            }
            case 7 -> {
                if (!this.model.isEffectTrackBankActive()) {
                    if (this.lastSendIsAccessible()) {
                        this.handleSendEffect(3);
                    }
                    else {
                        this.model.getTrackBank().selectParent();
                    }
                }
            }
            default -> this.handleSendEffect(index - 4);
        }

        config.setDebugMode(modeManager.getActiveID());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void selectPreviousItemPage()
    {
        final ICursorTrack cursorTrack = this.model.getCursorTrack();
        if (this.surface.isShiftPressed()) {
            cursorTrack.swapWithPrevious();
        }
        else {
            super.selectPreviousItemPage();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void selectNextItemPage()
    {
        final ICursorTrack cursorTrack = this.model.getCursorTrack();
        if (this.surface.isShiftPressed()) {
            cursorTrack.swapWithNext();
        }
        else {
            super.selectNextItemPage();
        }
    }


    /**
     * Handle the selection of a send effect.
     *
     * @param sendIndex The index of the send
     */
    protected void handleSendEffect(final int sendIndex)
    {
        final ITrackBank tb = this.model.getCurrentTrackBank();
        if (tb == null || !tb.canEditSend(sendIndex)) {
            return;
        }
        final Modes       si          = Modes.get(Modes.SEND1, sendIndex);
        final ModeManager modeManager = this.surface.getModeManager();
        modeManager.setActive(modeManager.isActive(si) ? Modes.TRACK : si);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getButtonColor(final ButtonID buttonID)
    {
        final CommonUIConfiguration config       = this.surface.getConfiguration();
        final ITrackBank            tb           = this.model.getCurrentTrackBank();
        final CommonUIColorManager  colorManager = this.getColorManager();

        int index = this.isButtonRow(0, buttonID);
        if (index >= 0) {
            final ITrack track = tb.getItem(index);
            if (!track.doesExist() || !track.isActivated()) {
                return colorManager.getDeviceColor(ColorEx.BLACK);
            }

            final ITrack  cursorTrack = this.model.getCursorTrack();
            final int     selIndex    = cursorTrack.doesExist() ? cursorTrack.getIndex() : -1;
            final boolean isSel       = track.getIndex() == selIndex;

            if (track.isRecArm()) {
                return colorManager.getDeviceColor(isSel ? ColorEx.RED : ColorEx.DARK_RED);
            }

            return colorManager.getDeviceColor(isSel ? ColorEx.ORANGE : ColorEx.DARK_ORANGE);
        }

        index = this.isButtonRow(1, buttonID);
        if (index >= 0) {
            final ITrack track = tb.getItem(index);

            if (this.surface.isPressed(ButtonID.STOP_CLIP)) {
                return colorManager.getDeviceColor(track.doesExist() && track.isPlaying() ? ColorEx.RED : ColorEx.BLACK);
            }

            if (config.isMuteLongPressed() || config.isSoloLongPressed() || config.isMuteSoloLocked()) {
                final boolean muteState = config.isMuteState();
                return this.getTrackStateColor(muteState, track);
            }

            final ModeManager modeManager = this.surface.getModeManager();
            switch (index) {
                case 0 -> {
                    return colorManager.getDeviceColor(modeManager.isActive(Modes.VOLUME) ? ColorEx.WHITE : ColorEx.BLACK);
                }
                case 1 -> {
                    return colorManager.getDeviceColor(modeManager.isActive(Modes.PAN) ? ColorEx.WHITE : ColorEx.BLACK);
                }
                case 2 -> {
                    return colorManager.getDeviceColor(modeManager.isActive(Modes.CROSSFADER) ? ColorEx.WHITE : ColorEx.BLACK);
                }
                case 4 -> {
                    return colorManager.getDeviceColor(modeManager.isActive(Modes.SEND1) ? ColorEx.WHITE : ColorEx.BLACK);
                }
                case 5 -> {
                    return colorManager.getDeviceColor(modeManager.isActive(Modes.SEND2) ? ColorEx.WHITE : ColorEx.BLACK);
                }
                case 6 -> {
                    return colorManager.getDeviceColor(modeManager.isActive(Modes.SEND3) ? ColorEx.WHITE : ColorEx.BLACK);
                }
                case 7 -> {
                    if (this.lastSendIsAccessible()) {
                        return colorManager.getDeviceColor(modeManager.isActive(Modes.SEND4) ? ColorEx.WHITE : ColorEx.BLACK);
                    }
                    return colorManager.getDeviceColor(tb.hasParent() ? ColorEx.WHITE : ColorEx.BLACK);
                }
                default -> {
                    return colorManager.getDeviceColor(ColorEx.BLACK);
                }
            }
        }

        return super.getButtonColor(buttonID);
    }


    protected int getTrackStateColor(final boolean muteState, final ITrack t)
    {
        final CommonUIColorManager colorManager = this.getColorManager();

        if (!t.doesExist()) {
            return colorManager.getDeviceColor(ColorEx.BLACK);
        }

        if (muteState) {
            if (t.isMute()) {
                return colorManager.getDeviceColor(ColorEx.DARK_YELLOW);
            }
        }
        else if (t.isSolo()) {
            return colorManager.getDeviceColor(ColorEx.YELLOW);
        }

        return colorManager.getDeviceColor(ColorEx.BLACK);
    }


    // Called from sub-classes
    protected void updateChannelDisplay(final IGraphicDisplay display, final int selectedMenu, final boolean isVolume, final boolean isPan)
    {
        this.updateMenuItems(selectedMenu);

        final IValueChanger         valueChanger = this.model.getValueChanger();
        final ITrackBank            tb           = this.model.getCurrentTrackBank();
        final CommonUIConfiguration config       = this.surface.getConfiguration();
        final ICursorTrack          cursorTrack  = this.model.getCursorTrack();
        for (int i = 0; i < 8; i++) {
            final ITrack                t              = tb.getItem(i);
            final Pair<String, Boolean> pair           = this.menu.get(i);
            final String                topMenu        = pair.getKey();
            final boolean               isTopMenuOn    = pair.getValue().booleanValue();
            final int                   crossfadeMode  = this.getCrossfadeModeAsNumber(t);
            final boolean               enableVUMeters = config.isEnableVUMeters();
            final int                   vuR            = valueChanger.toDisplayValue(enableVUMeters ? t.getVuRight() : 0);
            final int                   vuL            = valueChanger.toDisplayValue(enableVUMeters ? t.getVuLeft() : 0);
            display.addChannelElement(selectedMenu, topMenu, isTopMenuOn, t.doesExist() ? t.getName(12) : "", this.updateType(t), t.getColor(), t.isSelected(), valueChanger.toDisplayValue(t.getVolume()), valueChanger.toDisplayValue(t.getModulatedVolume()), isVolume && this.isKnobTouched(i) ? t.getVolumeStr(8) : "", valueChanger.toDisplayValue(t.getPan()), valueChanger.toDisplayValue(t.getModulatedPan()), isPan && this.isKnobTouched(i) ? t.getPanStr(8) : "", vuL, vuR, t.isMute(), t.isSolo(), t.isRecArm(), t.isActivated(), crossfadeMode, t.isSelected() && cursorTrack.isPinned());
        }
    }


    protected void updateMenuItems(final int selectedMenu)
    {
        if (this.surface.isPressed(ButtonID.STOP_CLIP)) {
            this.updateStopMenu();
            return;
        }
        final CommonUIConfiguration config = this.surface.getConfiguration();
        if (config.isMuteLongPressed() || config.isMuteSoloLocked() && config.isMuteState()) {
            this.updateMuteMenu();
        }
        else if (config.isSoloLongPressed() || config.isMuteSoloLocked() && config.isSoloState()) {
            this.updateSoloMenu();
        }
        else {
            this.updateTrackMenu(selectedMenu);
        }
    }


    protected void updateStopMenu()
    {
        final ITrackBank tb = this.model.getCurrentTrackBank();
        for (int i = 0; i < 8; i++) {
            final ITrack t = tb.getItem(i);
            this.menu.get(i).set(t.doesExist() ? "Stop Clip" : "", Boolean.valueOf(t.isPlaying()));
        }
    }


    protected void updateMuteMenu()
    {
        final ITrackBank tb = this.model.getCurrentTrackBank();
        for (int i = 0; i < 8; i++) {
            final ITrack t = tb.getItem(i);
            this.menu.get(i).set(t.doesExist() ? "Mute" : "", Boolean.valueOf(t.isMute()));
        }
    }


    protected void updateSoloMenu()
    {
        final ITrackBank tb = this.model.getCurrentTrackBank();
        for (int i = 0; i < 8; i++) {
            final ITrack t = tb.getItem(i);
            this.menu.get(i).set(t.doesExist() ? "Solo" : "", Boolean.valueOf(t.isSolo()));
        }
    }


    protected void updateTrackMenu(final int selectedMenu)
    {
        this.menu.get(0).set("Volume", Boolean.valueOf(selectedMenu - 1 == 0));
        this.menu.get(1).set("Pan", Boolean.valueOf(selectedMenu - 1 == 1));
        this.menu.get(2).set(this.model.getHost().supports(Capability.HAS_CROSSFADER) ? "Crossfader" : " ", Boolean.valueOf(selectedMenu - 1 == 2));

        if (this.model.isEffectTrackBankActive()) {
            // No sends for FX tracks
            for (int i = 3; i < 7; i++) {
                this.menu.get(i).set(" ", Boolean.FALSE);
            }
            return;
        }

        final ITrackBank       currentTrackBank = this.model.getCurrentTrackBank();
        final Optional<ITrack> selectedItem     = currentTrackBank.getSelectedItem();
        final ISendBank        sendBank         = (selectedItem.isPresent() ? selectedItem.get() : currentTrackBank.getItem(0)).getSendBank();
        final int              start            = Math.max(0, sendBank.getScrollPosition()) + 1;
        this.menu.get(3).set(String.format("Sends %d-%d", Integer.valueOf(start), Integer.valueOf(start + 3)), Boolean.FALSE);

        final ITrackBank tb = currentTrackBank;
        for (int i = 0; i < 4; i++) {
            final String  sendName = tb.getEditSendName(i);
            final boolean exists   = !sendName.isEmpty();
            this.menu.get(4 + i).set(exists ? sendName : " ", Boolean.valueOf(exists && 4 + i == selectedMenu - 1));
        }

        if (this.lastSendIsAccessible()) {
            return;
        }

        final boolean isUpAvailable = tb.hasParent();
        this.menu.get(7).set(isUpAvailable ? "Up" : " ", Boolean.valueOf(isUpAvailable));
    }


    /**
     * Check if the 4th/8th send is accessible. This is the case if the current tracks are not
     * inside a group (hence no need to go up), Shift is pressed or the 8th knob is touched.
     *
     * @return True if one of the above described conditions is met
     */
    private boolean lastSendIsAccessible()
    {
        return this.surface.isShiftPressed() || !this.model.getCurrentTrackBank().hasParent() || this.isKnobTouched(7);
    }


    protected int getCrossfadeModeAsNumber(final ITrack track)
    {
        if (this.model.getHost().supports(Capability.HAS_CROSSFADER)) {
            return (int) Math.round(this.model.getValueChanger().toNormalizedValue(track.getCrossfadeParameter().getValue()) * 2.0);
        }
        return -1;
    }


    /**
     * Update the group type, if it is an opened group.
     *
     * @param track The track for which to get the type
     * @return The type
     */
    protected ChannelType updateType(final ITrack track)
    {
        final ChannelType type = track.getType();
        return type == ChannelType.GROUP && track.isGroupExpanded() ? ChannelType.GROUP_OPEN : type;
    }

}