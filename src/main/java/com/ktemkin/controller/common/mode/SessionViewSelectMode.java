// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.mode;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IItem;
import de.mossgrabers.framework.featuregroup.AbstractFeatureGroup;
import de.mossgrabers.framework.featuregroup.AbstractMode;
import de.mossgrabers.framework.featuregroup.ViewManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.Views;


/**
 * Mode to select a view.
 *
 * @author Jürgen Moßgraber
 */
public class SessionViewSelectMode extends BaseMode<IItem>
{

    /**
     * The views to choose from.
     */
    private static final Views[] VIEWS =
            {
                    Views.SESSION,
                    Views.SESSION,
                    Views.SCENE_PLAY,
                    null,
                    null
            };

    /**
     * The views to choose from.
     */
    private static final String[] VIEW_NAMES =
            {
                    "Session",
                    "Flipped",
                    "Scenes",
                    "",
                    ""
            };


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public SessionViewSelectMode(final CommonUIControlSurface surface, final IModel model)
    {
        super("Session View", surface, model);
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

        final CommonUIConfiguration configuration = this.surface.getConfiguration();

        switch (index) {
            case 0, 1 -> {
                configuration.setFlipSession(index == 1);
                this.activateView(VIEWS[index]);
            }
            case 2 -> {
                configuration.setSceneView();
                this.surface.getModeManager().restore();
            }
            case 6 -> this.surface.getModeManager().setActive(Modes.MARKERS);
            case 7 -> {
                this.surface.getModeManager().restore();
                configuration.toggleScenesClipMode();
            }
            default -> {
            }
            // Not used
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        final ViewManager viewManager = this.surface.getViewManager();
        for (int i = 0; i < VIEWS.length; i++) {
            final boolean isMenuBottomSelected = VIEWS[i] != null && this.isSelected(viewManager, i);
            display.addOptionElement("", "", false, i == 0 ? "Session view" : "", VIEW_NAMES[i], isMenuBottomSelected, false);
        }
        final boolean isOn = this.surface.getModeManager().isActive(Modes.SESSION);
        display.addOptionElement("", "", false, "", "", false, false);
        display.addOptionElement("", "", false, "Session mode", "Markers", false, false);
        display.addOptionElement("", "", false, "", "Clips", isOn, false);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getButtonColorID(final ButtonID buttonID)
    {
        final int index = this.isButtonRow(0, buttonID);
        if (index >= 0) {
            if (index < VIEWS.length) {
                if (VIEWS[index] == null) {
                    return AbstractFeatureGroup.BUTTON_COLOR_OFF;
                }
                final ViewManager viewManager = this.surface.getViewManager();
                return this.isSelected(viewManager, index) ? AbstractMode.BUTTON_COLOR_HI : AbstractFeatureGroup.BUTTON_COLOR_ON;
            }

            if (index == 6) {
                return AbstractFeatureGroup.BUTTON_COLOR_ON;
            }
            if (index == 7) {
                final boolean isOn = this.surface.getModeManager().isActive(Modes.SESSION);
                return isOn ? AbstractMode.BUTTON_COLOR_HI : AbstractFeatureGroup.BUTTON_COLOR_ON;
            }
            return AbstractFeatureGroup.BUTTON_COLOR_OFF;
        }

        return AbstractFeatureGroup.BUTTON_COLOR_OFF;
    }


    private void activateView(final Views viewID)
    {
        if (viewID == null) {
            return;
        }
        this.surface.getViewManager().setActive(viewID);
        this.surface.getModeManager().restore();
    }


    private boolean isSelected(final ViewManager viewManager, final int index)
    {
        final boolean activeView = viewManager.isActive(VIEWS[index]);
        switch (index) {
            case 0:
                return activeView && !this.surface.getConfiguration().isFlipSession();

            case 1:
                return activeView && this.surface.getConfiguration().isFlipSession();

            default:
                return activeView;
        }
    }

}