// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.mode;

import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import com.ktemkin.controller.common.mode.track.AbstractTrackMode;
import de.mossgrabers.framework.controller.ButtonID;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IScene;
import de.mossgrabers.framework.daw.data.ISlot;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ISceneBank;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.featuregroup.AbstractFeatureGroup;
import de.mossgrabers.framework.featuregroup.AbstractMode;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.utils.Pair;
import de.mossgrabers.framework.view.Views;

import java.util.ArrayList;
import java.util.List;


/**
 * Mode for displaying clips or scenes.
 *
 * @author Jürgen Moßgraber
 */
public class SessionMode extends AbstractTrackMode
{

    private final ISceneBank sceneBank;

    private RowDisplayMode rowDisplayMode;


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public SessionMode(final CommonUIControlSurface surface, final IModel model)
    {
        super("Session", surface, model);

        this.sceneBank = model.getSceneBank(64);

        this.rowDisplayMode = RowDisplayMode.ALL;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched)
    {
        this.setTouchedKnob(index, isTouched);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobValue(final int index, final int value)
    {
        // Intentionally empty
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

        if (index == 0) {
            if (this.rowDisplayMode == RowDisplayMode.ALL || this.rowDisplayMode == RowDisplayMode.LOWER) {
                this.rowDisplayMode = RowDisplayMode.UPPER;
            }
            else {
                this.rowDisplayMode = RowDisplayMode.ALL;
            }
        }
        else if (index == 1) {
            if (this.rowDisplayMode == RowDisplayMode.ALL || this.rowDisplayMode == RowDisplayMode.UPPER) {
                this.rowDisplayMode = RowDisplayMode.LOWER;
            }
            else {
                this.rowDisplayMode = RowDisplayMode.ALL;
            }
        }
        else if (index == 7) {
            super.onSecondRow(index, event);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getButtonColor(final ButtonID buttonID)
    {
        final int index = this.isButtonRow(1, buttonID);
        if (index >= 0) {
            if (index == 0) {
                return this.colorManager.getColorIndex(this.rowDisplayMode == RowDisplayMode.UPPER ? AbstractMode.BUTTON_COLOR_HI : AbstractFeatureGroup.BUTTON_COLOR_ON);
            }
            if (index == 1) {
                return this.colorManager.getColorIndex(this.rowDisplayMode == RowDisplayMode.LOWER ? AbstractMode.BUTTON_COLOR_HI : AbstractFeatureGroup.BUTTON_COLOR_ON);
            }
            if (index < 5) {
                return this.colorManager.getColorIndex(AbstractFeatureGroup.BUTTON_COLOR_OFF);
            }

            final ITrackBank tb = this.model.getCurrentTrackBank();
            return this.getColorManager().getDeviceColor(tb.hasParent() ? ColorEx.WHITE : ColorEx.BLACK);
        }

        return super.getButtonColor(buttonID);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        if (this.surface.getViewManager().isActive(Views.SESSION)) {
            this.updateDisplay2Clips(display);
        }
        else {
            this.updateDisplay2Scenes(display);
        }
    }


    private void updateDisplay2Scenes(final IGraphicDisplay display)
    {
        final int maxCols = 8;
        final int maxRows = this.rowDisplayMode == RowDisplayMode.ALL ? 8 : 4;

        for (int col = 0; col < maxCols; col++) {
            final List<IScene> scenes = new ArrayList<>(maxRows);
            for (int row = 0; row < maxRows; row++) {
                int sceneIndex = (maxRows - 1 - row) * 8 + col;
                if (this.rowDisplayMode == RowDisplayMode.LOWER) {
                    sceneIndex += 32;
                }
                scenes.add(this.sceneBank.getItem(sceneIndex));
            }
            display.addSceneListElement(scenes);
        }
    }


    private void updateDisplay2Clips(final IGraphicDisplay display)
    {
        final ITrackBank tb        = this.model.getCurrentTrackBank();
        final int        numTracks = tb.getPageSize();
        final int        numScenes = tb.getSceneBank().getPageSize();

        final boolean flipSession = this.surface.getConfiguration().isFlipSession();
        final int     maxCols     = flipSession ? numScenes : numTracks;
        int           maxRows     = flipSession ? numTracks : numScenes;
        if (this.rowDisplayMode != RowDisplayMode.ALL) {
            maxRows = maxRows / 2;
        }

        for (int col = 0; col < maxCols; col++) {
            final List<Pair<ITrack, ISlot>> slots = new ArrayList<>(maxRows);

            for (int row = 0; row < maxRows; row++) {
                int x = flipSession ? row : col;
                int y = flipSession ? col : row;

                if (this.rowDisplayMode == RowDisplayMode.LOWER) {
                    if (flipSession) {
                        x += maxRows;
                    }
                    else {
                        y += maxRows;
                    }
                }
                final ITrack track = tb.getItem(x);
                slots.add(new Pair<>(track, track.getSlotBank().getItem(y)));
            }
            display.addSlotListElement(slots);
        }
    }


    private enum RowDisplayMode
    {
        ALL,
        UPPER,
        LOWER
    }

}