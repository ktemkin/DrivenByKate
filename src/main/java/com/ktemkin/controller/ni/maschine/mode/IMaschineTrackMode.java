// Written by Kate Temkin -- ktemk.in
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.maschine.mode;

import com.ktemkin.controller.ni.maschine.MaschineConfiguration;
import de.mossgrabers.framework.controller.ButtonID;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.constants.Capability;
import de.mossgrabers.framework.daw.data.ICursorTrack;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ISendBank;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.daw.resource.ChannelType;
import de.mossgrabers.framework.utils.Pair;

import java.util.List;
import java.util.Optional;

/**
 * Mix-in class that adds display handling helpers for per-track Maschine modes.
 *
 * @author Kate Temkin
 */
public interface IMaschineTrackMode extends IMaschineMode {

    /**
     * Helper that returns `this.model` for implementing classes.
     */
    IModel getModel();


    /**
     * Helper that returns `this.menu` for implementing classes.
     */
    List<Pair<String, Boolean>> getMenu();

    /**
     * Returns true if any knob is touched.
     */
    boolean isKnobTouched(final int index);


    default int getCrossfadeModeAsNumber(final ITrack track) {
        if (this.getModel().getHost().supports(Capability.HAS_CROSSFADER))
            return (int) Math.round(this.getModel().getValueChanger().toNormalizedValue(track.getCrossfadeParameter().getValue()) * 2.0);
        return -1;
    }


    /**
     * Update the group type, if it is an opened group.
     *
     * @param track The track for which to get the type
     * @return The type
     */
    default ChannelType updateType(final ITrack track) {
        final ChannelType type = track.getType();
        return type == ChannelType.GROUP && track.isGroupExpanded() ? ChannelType.GROUP_OPEN : type;
    }


    default void updateMenuItems(final int selectedMenu) {
        if (this.getSurface().isPressed(ButtonID.STOP_CLIP)) {
            this.updateStopMenu();
            return;
        }
		/*
        final MaschineConfiguration config = this.getSurface().getConfiguration ();
        if (config.isMuteLongPressed () || config.isMuteSoloLocked () && config.isMuteState ())
            this.updateMuteMenu ();
        else if (config.isSoloLongPressed () || config.isMuteSoloLocked () && config.isSoloState ())
            this.updateSoloMenu ();
        else
		*/
        this.updateTrackMenu(selectedMenu);
    }


    default void updateStopMenu() {
        final ITrackBank tb = this.getModel().getCurrentTrackBank();
        for (int i = 0; i < 8; i++) {
            final ITrack t = tb.getItem(i);
            this.getMenu().get(i).set(t.doesExist() ? "Stop Clip" : "", Boolean.valueOf(t.isPlaying()));
        }
    }


    default void updateMuteMenu() {
        final ITrackBank tb = this.getModel().getCurrentTrackBank();
        for (int i = 0; i < 8; i++) {
            final ITrack t = tb.getItem(i);
            this.getMenu().get(i).set(t.doesExist() ? "Mute" : "", Boolean.valueOf(t.isMute()));
        }
    }


    default void updateSoloMenu() {
        final ITrackBank tb = this.getModel().getCurrentTrackBank();
        for (int i = 0; i < 8; i++) {
            final ITrack t = tb.getItem(i);
            this.getMenu().get(i).set(t.doesExist() ? "Solo" : "", Boolean.valueOf(t.isSolo()));
        }
    }


    default void updateTrackMenu(final int selectedMenu) {
        this.getMenu().get(0).set("Volume", Boolean.valueOf(selectedMenu - 1 == 0));
        this.getMenu().get(1).set("Pan", Boolean.valueOf(selectedMenu - 1 == 1));
        this.getMenu().get(2).set(this.getModel().getHost().supports(Capability.HAS_CROSSFADER) ? "Crossfader" : " ", Boolean.valueOf(selectedMenu - 1 == 2));

        if (this.getModel().isEffectTrackBankActive()) {
            // No sends for FX tracks
            for (int i = 3; i < 7; i++)
                this.getMenu().get(i).set(" ", Boolean.FALSE);
            return;
        }

        final ITrackBank currentTrackBank = this.getModel().getCurrentTrackBank();
        final Optional<ITrack> selectedItem = currentTrackBank.getSelectedItem();
        final ISendBank sendBank = (selectedItem.isPresent() ? selectedItem.get() : currentTrackBank.getItem(0)).getSendBank();
        final int start = Math.max(0, sendBank.getScrollPosition()) + 1;
        this.getMenu().get(3).set(String.format("Sends %d-%d", Integer.valueOf(start), Integer.valueOf(start + 3)), Boolean.FALSE);

        final ITrackBank tb = currentTrackBank;
        for (int i = 0; i < 4; i++) {
            final String sendName = tb.getEditSendName(i);
            final boolean exists = !sendName.isEmpty();
            this.getMenu().get(4 + i).set(exists ? sendName : " ", Boolean.valueOf(exists && 4 + i == selectedMenu - 1));
        }

        if (this.lastSendIsAccessible())
            return;

        final boolean isUpAvailable = tb.hasParent();
        this.getMenu().get(7).set(isUpAvailable ? "Up" : " ", Boolean.valueOf(isUpAvailable));
    }


    /**
     * Check if the 4th/8th send is accessible. This is the case if the current tracks are not
     * inside a group (hence no need to go up), Shift is pressed or the 8th knob is touched.
     *
     * @return True if one of the above described conditions is met
     */
    default boolean lastSendIsAccessible() {
        return this.getSurface().isShiftPressed() || !this.getModel().getCurrentTrackBank().hasParent() || this.isKnobTouched(7);
    }


    default void updateGraphicsChannelDisplay(IGraphicDisplay display, final int selectedMenu, final boolean isVolume, final boolean isPan) {
        this.updateMenuItems(selectedMenu);

        final IValueChanger valueChanger = this.getModel().getValueChanger();
        final ITrackBank tb = this.getModel().getCurrentTrackBank();
        final MaschineConfiguration config = this.getSurface().getConfiguration();
        final ICursorTrack cursorTrack = this.getModel().getCursorTrack();
        for (int i = 0; i < 8; i++) {
            final ITrack t = tb.getItem(i);
            final Pair<String, Boolean> pair = this.getMenu().get(i);
            final String topMenu = pair.getKey();
            final boolean isTopMenuOn = pair.getValue().booleanValue();
            final int crossfadeMode = this.getCrossfadeModeAsNumber(t);
            final boolean enableVUMeters = config.isEnableVUMeters();
            final int vuR = valueChanger.toDisplayValue(enableVUMeters ? t.getVuRight() : 0);
            final int vuL = valueChanger.toDisplayValue(enableVUMeters ? t.getVuLeft() : 0);
            display.addChannelElement(selectedMenu, topMenu, isTopMenuOn, t.doesExist() ? t.getName(12) : "", this.updateType(t), t.getColor(), t.isSelected(), valueChanger.toDisplayValue(t.getVolume()), valueChanger.toDisplayValue(t.getModulatedVolume()), isVolume && this.isKnobTouched(i) ? t.getVolumeStr(8) : "", valueChanger.toDisplayValue(t.getPan()), valueChanger.toDisplayValue(t.getModulatedPan()), isPan && this.isKnobTouched(i) ? t.getPanStr(8) : "", vuL, vuR, t.isMute(), t.isSolo(), t.isRecArm(), t.isActivated(), crossfadeMode, t.isSelected() && cursorTrack.isPinned());
        }
    }


}
