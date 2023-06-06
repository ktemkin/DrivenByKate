// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ableton.push.command.trigger;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.command.trigger.Direction;
import de.mossgrabers.framework.command.trigger.mode.CursorCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.bank.ISceneBank;
import de.mossgrabers.framework.featuregroup.IMode;
import de.mossgrabers.framework.view.Views;


/**
 * Command for cursor arrow keys.
 *
 * @author Jürgen Moßgraber
 */
public class PushCursorCommand extends CursorCommand<CommonUIControlSurface, CommonUIConfiguration>
{

    private final ISceneBank sceneBank64;


    /**
     * Constructor.
     *
     * @param direction The direction of the pushed cursor arrow
     * @param model     The model
     * @param surface   The surface
     */
    public PushCursorCommand(final Direction direction, final IModel model, final CommonUIControlSurface surface)
    {
        super(direction, model, surface, false);

        this.sceneBank64 = model.getSceneBank(64);
    }


    /**
     * Scroll scenes up.
     */
    @Override
    protected void scrollUp()
    {
        final ISceneBank sceneBank = this.getSceneBank();
        if (this.surface.isShiftPressed() || this.isScenePlay()) {
            sceneBank.selectPreviousPage();
        }
        else {
            sceneBank.scrollBackwards();
        }
    }


    /**
     * Scroll scenes down.
     */
    @Override
    protected void scrollDown()
    {
        final ISceneBank sceneBank = this.getSceneBank();
        if (this.surface.isShiftPressed() || this.isScenePlay()) {
            sceneBank.selectNextPage();
        }
        else {
            sceneBank.scrollForwards();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void scrollLeft()
    {
        final IMode activeMode = this.surface.getModeManager().getActive();
        if (activeMode != null) {
            activeMode.selectPreviousItemPage();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void scrollRight()
    {
        final IMode activeMode = this.surface.getModeManager().getActive();
        if (activeMode != null) {
            activeMode.selectNextItemPage();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected ISceneBank getSceneBank()
    {
        if (this.isScenePlay()) {
            return this.sceneBank64;
        }
        return this.model.getCurrentTrackBank().getSceneBank();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateArrowStates()
    {
        final ISceneBank sceneBank = this.getSceneBank();
        this.canScrollUp   = sceneBank.canScrollBackwards();
        this.canScrollDown = sceneBank.canScrollForwards();

        final IMode   mode         = this.surface.getModeManager().getActive();
        final boolean shiftPressed = this.surface.isShiftPressed();
        this.canScrollLeft  = mode != null && (shiftPressed ? mode.hasPreviousItem() : mode.hasPreviousItemPage());
        this.canScrollRight = mode != null && (shiftPressed ? mode.hasNextItem() : mode.hasNextItemPage());
    }


    private boolean isScenePlay()
    {
        return this.surface.getViewManager().isActive(Views.SCENE_PLAY);
    }

}
