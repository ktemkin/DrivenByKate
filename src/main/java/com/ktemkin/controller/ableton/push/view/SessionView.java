// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ableton.push.view;

import com.ktemkin.controller.ableton.push.command.trigger.SelectSessionViewCommand;
import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIColorManager;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.grid.LightInfo;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IScene;
import de.mossgrabers.framework.daw.data.ISlot;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ISceneBank;
import de.mossgrabers.framework.featuregroup.AbstractFeatureGroup;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.AbstractSessionView;
import de.mossgrabers.framework.view.TransposeView;


/**
 * The Session view.
 *
 * @author Jürgen Moßgraber
 */
public class SessionView extends AbstractSessionView<CommonUIControlSurface<CommonUIConfiguration>, CommonUIConfiguration> implements TransposeView
{

    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model   The model
     */
    public SessionView(final CommonUIControlSurface surface, final IModel model)
    {
        super("Session", surface, model, 8, 8, true);

        var colorManager = (CommonUIColorManager)model.getColorManager();

        final int       redLo             = colorManager.getDeviceColor(ColorEx.DARK_RED);
        final int       redHi             = colorManager.getDeviceColor(ColorEx.RED);
        final int       black             = colorManager.getDeviceColor(ColorEx.BLACK);
        final int       white             = colorManager.getDeviceColor(ColorEx.WHITE);
        final int       green             = colorManager.getDeviceColor(ColorEx.GREEN);
        final int       amber             = colorManager.getDeviceColor(ColorEx.DARK_YELLOW);

        final LightInfo isRecording       = new LightInfo(redHi, redHi, false);
        final LightInfo isRecordingQueued = new LightInfo(redHi, black, true);
        final LightInfo isPlaying         = new LightInfo(green, green, false);
        final LightInfo isPlayingQueued   = new LightInfo(green, green, true);
        final LightInfo hasContent        = new LightInfo(amber, white, false);
        final LightInfo noContent         = new LightInfo(black, -1, false);
        final LightInfo recArmed          = new LightInfo(redLo, -1, false);
        this.setColors(isRecording, isRecordingQueued, isPlaying, isPlayingQueued, hasContent, noContent, recArmed);

        this.birdColorHasContent = new LightInfo(amber, -1, false);
        this.birdColorSelected   = isPlaying;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onGridNote(final int note, final int velocity)
    {
        if (velocity == 0) {
            ((SelectSessionViewCommand) this.surface.getButton(ButtonID.SESSION).getCommand()).setTemporary();
        }

        // Birds-eye-view navigation
        if (this.isBirdsEyeActive()) {
            if (velocity == 0) {return;}

            final int index = note - 36;
            final int x     = index % this.columns;
            final int y     = this.rows - 1 - index / this.columns;

            this.onGridNoteBirdsEyeView(x, y, 0);
            return;
        }

        super.onGridNote(note, velocity);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleButtonCombinations(final ITrack track, final ISlot slot)
    {
        if (this.isButtonCombination(ButtonID.SELECT)) {
            if (slot.doesExist()) {slot.select();}
            return true;
        }

        return super.handleButtonCombinations(track, slot);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBirdsEyeActive()
    {
        return this.surface.isShiftPressed() && this.surface.isSelectPressed();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getButtonColorID(final ButtonID buttonID)
    {
        final int scene = buttonID.ordinal() - ButtonID.SCENE1.ordinal();
        if (scene < 0 || scene >= 8) {return AbstractFeatureGroup.BUTTON_COLOR_OFF;}

        final ISceneBank sceneBank = this.model.getSceneBank();
        final IScene     s         = sceneBank.getItem(scene);
        if (s.doesExist()) {
            return s.isSelected() ? AbstractSessionView.COLOR_SELECTED_SCENE : AbstractSessionView.COLOR_SCENE;
        }
        return AbstractSessionView.COLOR_SCENE_OFF;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onOctaveDown(final ButtonEvent event)
    {
        if (event == ButtonEvent.DOWN) {this.model.getCurrentTrackBank().getSceneBank().selectNextPage();}
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onOctaveUp(final ButtonEvent event)
    {
        if (event == ButtonEvent.DOWN) {this.model.getCurrentTrackBank().getSceneBank().selectPreviousPage();}
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOctaveUpButtonOn()
    {
        return this.model.getCurrentTrackBank().getSceneBank().canScrollPageForwards();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOctaveDownButtonOn()
    {
        return this.model.getCurrentTrackBank().getSceneBank().canScrollPageBackwards();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onButton(final ButtonID buttonID, final ButtonEvent event, final int velocity)
    {
        super.onButton(buttonID, event, velocity);

        if (ButtonID.isSceneButton(buttonID) && event == ButtonEvent.UP) {
            ((SelectSessionViewCommand) this.surface.getButton(ButtonID.SESSION).getCommand()).setTemporary();
        }
    }

}