// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.modes;

import com.ktemkin.controller.ableton.push.PushConfiguration;
import com.ktemkin.controller.ableton.push.controller.PushColorManager;
import com.ktemkin.controller.ableton.push.controller.PushControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IItem;
import de.mossgrabers.framework.featuregroup.AbstractFeatureGroup;
import de.mossgrabers.framework.featuregroup.AbstractMode;
import de.mossgrabers.framework.scale.Scale;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Selection of the scale and base note.
 *
 * @author Jürgen Moßgraber
 */
public class ScalesMode extends BaseMode<IItem>
{

    private final Scales scales;


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public ScalesMode(final PushControlSurface surface, final IModel model)
    {
        super("Scale", surface, model);

        this.scales = model.getScales();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobValue(final int index, final int value)
    {
        if (index != 0 || !this.increaseKnobMovement()) {
            return;
        }

        this.scales.changeScale(value);
        this.update();
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
        if (index == 0) {
            this.scales.nextScale();
        }
        else if (index > 0 && index < 7) {
            this.scales.setScaleOffsetByIndex(index - 1);
        }
        this.update();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getButtonColor(final ButtonID buttonID)
    {
        int index = this.isButtonRow(0, buttonID);
        if (index >= 0) {
            if (index == 0) {
                return this.colorManager.getColorIndex(PushColorManager.PUSH_ORANGE_LO);
            }
            if (index == 7) {
                return this.colorManager.getColorIndex(AbstractFeatureGroup.BUTTON_COLOR_OFF);
            }
            final int offset = this.scales.getScaleOffsetIndex();
            return this.colorManager.getColorIndex(offset == index - 1 ? AbstractMode.BUTTON_COLOR_HI : AbstractFeatureGroup.BUTTON_COLOR_ON);
        }

        index = this.isButtonRow(1, buttonID);
        if (index >= 0) {
            if (index == 0 || index == 7) {
                return PushColorManager.PUSH2_COLOR2_AMBER;
            }
            final int offset = this.scales.getScaleOffsetIndex();
            return this.colorManager.getColorIndex(offset == index - 1 + 6 ? AbstractMode.BUTTON_COLOR2_HI : AbstractMode.BUTTON_COLOR2_ON);
        }

        return super.getButtonColor(buttonID);
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
        if (index == 0) {
            this.scales.prevScale();
        }
        else if (index == 7) {
            this.scales.toggleChromatic();
        }
        else {
            this.scales.setScaleOffsetByIndex(index + 5);
        }
        this.update();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        final int selIndex = this.scales.getScale().ordinal();
        display.addListElement(6, Scale.getNames(), selIndex);

        final int    offset    = this.scales.getScaleOffsetIndex();
        final String rangeText = this.scales.getRangeText();
        for (int i = 0; i < 6; i++) {
            display.addOptionElement(i == 3 ? "Note range: " + rangeText : "", Scales.BASES.get(6 + i), offset == 6 + i, "", Scales.BASES.get(i), offset == i, false);
        }

        display.addOptionElement("", this.scales.isChromatic() ? "Chromatc" : "In Key", this.scales.isChromatic(), "", "", false, false);
    }


    private void update()
    {
        this.surface.getViewManager().getActive().updateNoteMapping();
        final PushConfiguration config = this.surface.getConfiguration();
        config.setScale(this.scales.getScale().getName());
        config.setScaleBase(Scales.BASES.get(this.scales.getScaleOffsetIndex()));
        config.setScaleInKey(!this.scales.isChromatic());
    }

}