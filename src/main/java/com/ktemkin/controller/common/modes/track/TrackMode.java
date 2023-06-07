// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.modes.track;

import com.ktemkin.controller.ableton.push.parameterprovider.PushTrackParameterProvider;
import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.controller.color.ColorEx;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.ICursorTrack;
import de.mossgrabers.framework.daw.data.ISend;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.graphics.canvas.utils.SendData;
import de.mossgrabers.framework.utils.Pair;

import java.util.Optional;


/**
 * Mode for editing a track parameters.
 *
 * @author Jürgen Moßgraber
 */
public class TrackMode extends AbstractTrackMode
{

    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public TrackMode(final CommonUIControlSurface surface, final IModel model)
    {
        super("Track", surface, model);

        this.setParameterProvider(new PushTrackParameterProvider(model, (CommonUIConfiguration) surface.getConfiguration()));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched)
    {
        super.onKnobTouch(index, isTouched);

        if (isTouched && this.surface.isShiftPressed() && this.surface.isSelectPressed() && this.getParameterProvider().get(index) instanceof final ISend send) {
            send.toggleEnabled();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        final ITrackBank       tb            = this.model.getCurrentTrackBank();
        final Optional<ITrack> selectedTrack = tb.getSelectedItem();

        // Get the index at which to draw the Sends element
        final int selectedIndex = selectedTrack.isEmpty() ? -1 : selectedTrack.get().getIndex();
        int       sendsIndex    = selectedTrack.isEmpty() ? -1 : selectedTrack.get().getIndex() + 1;
        if (sendsIndex == 8) {
            sendsIndex = 6;
        }

        this.updateMenuItems(0);

        final ICursorTrack cursorTrack = this.model.getCursorTrack();

        final CommonUIConfiguration config = this.surface.getConfiguration();
        for (int i = 0; i < 8; i++) {
            final ITrack t = tb.getItem(i);

            // The menu item
            final Pair<String, Boolean> pair            = this.menu.get(i);
            final String                topMenu         = pair.getKey();
            final boolean               topMenuSelected = pair.getValue().booleanValue();

            // Channel info
            final String  bottomMenu      = t.doesExist() ? t.getName() : "";
            final ColorEx bottomMenuColor = t.getColor();
            final boolean isBottomMenuOn  = t.isSelected();

            final IValueChanger valueChanger = this.model.getValueChanger();
            if (t.isSelected()) {
                final int     crossfadeMode  = this.getCrossfadeModeAsNumber(t);
                final boolean enableVUMeters = config.isEnableVUMeters();
                final int     vuR            = valueChanger.toDisplayValue(enableVUMeters ? t.getVuRight() : 0);
                final int     vuL            = valueChanger.toDisplayValue(enableVUMeters ? t.getVuLeft() : 0);
                display.addChannelElement(topMenu, topMenuSelected, bottomMenu, this.updateType(t), bottomMenuColor, isBottomMenuOn, valueChanger.toDisplayValue(t.getVolume()), valueChanger.toDisplayValue(t.getModulatedVolume()), this.isKnobTouched(0) ? t.getVolumeStr(8) : "", valueChanger.toDisplayValue(t.getPan()), valueChanger.toDisplayValue(t.getModulatedPan()), this.isKnobTouched(1) ? t.getPanStr(8) : "", vuL, vuR, t.isMute(), t.isSolo(), t.isRecArm(), t.isActivated(), crossfadeMode, cursorTrack.isPinned());
            }
            else if (sendsIndex == i) {
                final ITrack     selTrack = tb.getItem(selectedIndex);
                final SendData[] sendData = new SendData[4];
                for (int j = 0; j < 4; j++) {
                    if (selTrack != null) {
                        final ISend send = selTrack.getSendBank().getItem(j);
                        if (send != null) {
                            final boolean exists = send.doesExist();
                            sendData[j] = new SendData(send.isEnabled(), send.getName(), exists && this.isKnobTouched(4 + j) ? send.getDisplayedValue(8) : "", valueChanger.toDisplayValue(exists ? send.getValue() : 0), valueChanger.toDisplayValue(exists ? send.getModulatedValue() : 0), true);
                            continue;
                        }
                    }
                    sendData[j] = new SendData(false, "", "", 0, 0, true);
                }
                display.addSendsElement(topMenu, topMenuSelected, bottomMenu, this.updateType(t), bottomMenuColor, isBottomMenuOn, sendData, true, selTrack == null || selTrack.isActivated(), t.isActivated());
            }
            else {
                display.addChannelSelectorElement(topMenu, topMenuSelected, bottomMenu, this.updateType(t), bottomMenuColor, isBottomMenuOn, t.isActivated());
            }
        }
    }

}