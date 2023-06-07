// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.modes.track;

import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import com.ktemkin.controller.common.modes.BaseMode;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.color.ColorManager;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.IProject;
import de.mossgrabers.framework.daw.constants.Capability;
import de.mossgrabers.framework.daw.data.ICursorTrack;
import de.mossgrabers.framework.daw.data.IMasterTrack;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.empty.EmptyParameter;
import de.mossgrabers.framework.daw.resource.ChannelType;
import de.mossgrabers.framework.featuregroup.AbstractFeatureGroup;
import de.mossgrabers.framework.parameterprovider.special.FixedParameterProvider;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Mode for editing the parameters of the master track.
 *
 * @author Jürgen Moßgraber
 */
public class MasterMode extends BaseMode<ITrack>
{

    private static final String TAG_VOLUME = "Volume";


    /**
     * Constructor.
     *
     * @param surface     The control surface
     * @param model       The model
     * @param isTemporary If true treat this mode only as temporary
     */
    public MasterMode(final CommonUIControlSurface surface, final IModel model, final boolean isTemporary)
    {
        super("Master", surface, model);

        final IMasterTrack masterTrack = this.model.getMasterTrack();
        final IProject     project     = this.model.getProject();
        this.setParameterProvider(new FixedParameterProvider(masterTrack.getVolumeParameter(), masterTrack.getPanParameter(), project.getCueVolumeParameter(), project.getCueMixParameter(), EmptyParameter.INSTANCE, EmptyParameter.INSTANCE, EmptyParameter.INSTANCE, EmptyParameter.INSTANCE));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivate()
    {
        super.onActivate();

        this.setActive(true);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeactivate()
    {
        super.onDeactivate();

        this.setActive(false);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched)
    {
        this.setTouchedKnob(index, isTouched);

        if (isTouched && this.surface.isDeletePressed()) {
            this.surface.setTriggerConsumed(ButtonID.DELETE);

            switch (index) {
                case 0 -> this.model.getMasterTrack().resetVolume();
                case 1 -> this.model.getMasterTrack().resetPan();
                case 2 -> this.model.getProject().resetCueVolume();
                case 3 -> this.model.getProject().resetCueMix();
                default -> {
                }
                // Not used
            }
        }

        switch (index) {
            case 0 -> this.model.getMasterTrack().touchVolume(isTouched);
            case 1 -> this.model.getMasterTrack().touchPan(isTouched);
            case 2 -> this.model.getProject().touchCueVolume(isTouched);
            case 3 -> this.model.getProject().touchCueMix(isTouched);
            default -> {
            }
            // Not used
        }

        this.checkStopAutomationOnKnobRelease(isTouched);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        final IMasterTrack master  = this.model.getMasterTrack();
        final IProject     project = this.model.getProject();

        final IValueChanger valueChanger   = this.model.getValueChanger();
        final boolean       enableVUMeters = this.surface.getConfiguration().isEnableVUMeters();
        final int           vuR            = valueChanger.toDisplayValue(enableVUMeters ? master.getVuRight() : 0);
        final int           vuL            = valueChanger.toDisplayValue(enableVUMeters ? master.getVuLeft() : 0);

        final ICursorTrack cursorTrack = this.model.getCursorTrack();

        display.addChannelElement(TAG_VOLUME, false, master.getName(), ChannelType.MASTER, master.getColor(), master.isSelected(), valueChanger.toDisplayValue(master.getVolume()), valueChanger.toDisplayValue(master.getModulatedVolume()), this.isKnobTouched(0) ? master.getVolumeStr(8) : "", valueChanger.toDisplayValue(master.getPan()), valueChanger.toDisplayValue(master.getModulatedPan()), this.isKnobTouched(1) ? master.getPanStr(8) : "", vuL, vuR, master.isMute(), master.isSolo(), master.isRecArm(), master.isActivated(), 0, master.isSelected() && cursorTrack.isPinned());
        display.addChannelSelectorElement("Pan", false, "", null, ColorEx.BLACK, false, master.isActivated());

        if (this.model.getHost().supports(Capability.CUE_VOLUME)) {
            display.addChannelElement("Cue Volume", false, "Cue", ChannelType.MASTER, ColorEx.GRAY, false, valueChanger.toDisplayValue(project.getCueVolume()), -1, this.isKnobTouched(2) ? project.getCueVolumeStr(8) : "", valueChanger.toDisplayValue(project.getCueMix()), -1, this.isKnobTouched(3) ? project.getCueMixStr(8) : "", 0, 0, false, false, false, true, 0, false);
            display.addChannelSelectorElement("Cue Mix", false, "", null, ColorEx.BLACK, false, true);
        }
        else {
            display.addOptionElement("", "", false, "", "", false, false);
            display.addOptionElement("", "", false, "", "", false, false);
        }

        display.addOptionElement("", "", false, "Audio Engine", this.model.getApplication().isEngineActive() ? "Active" : "Off", false, false);
        display.addOptionElement("", "", false, "", "", false, false);
        display.addOptionElement("Project:", "", false, this.model.getProject().getName(), "Previous", false, false);
        display.addOptionElement("", "", false, "", "Next", false, false);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onFirstRow(final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.UP) {return;}

        if (this.surface.isPressed(ButtonID.RECORD)) {
            this.surface.setTriggerConsumed(ButtonID.RECORD);
            this.model.getMasterTrack().toggleRecArm();
            return;
        }

        switch (index) {
            case 0 -> this.surface.getButton(ButtonID.DEVICE).trigger(ButtonEvent.DOWN);
            case 4 -> this.model.getApplication().toggleEngineActive();
            case 6 -> this.model.getProject().previous();
            case 7 -> this.model.getProject().next();
            default -> {
            }
            // Not used
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
            final ColorManager colorManager = this.model.getColorManager();

            if (index == 0) {return this.getTrackButtonColor();}
            if (index < 4 || index == 5) {return colorManager.getColorIndex(AbstractFeatureGroup.BUTTON_COLOR_OFF);}
            if (index > 5) {return colorManager.getColorIndex(AbstractFeatureGroup.BUTTON_COLOR_ON);}

            final int red = this.getColorManager().getDeviceColor(ColorEx.RED);
            return this.model.getApplication().isEngineActive() ? colorManager.getColorIndex(AbstractFeatureGroup.BUTTON_COLOR_ON) : red;
        }

        index = this.isButtonRow(1, buttonID);
        if (index >= 0) {
            return this.getColorManager().getDeviceColor(ColorEx.BLACK);
        }

        return super.getButtonColor(buttonID);

    }


    private int getTrackButtonColor()
    {
        final IMasterTrack track = this.model.getMasterTrack();
        if (!track.isActivated()) {
            return this.getColorManager().getDeviceColor(ColorEx.BLACK);
        }
        if (track.isRecArm()) {
            return this.getColorManager().getDeviceColor(ColorEx.RED);
        }
        return this.getColorManager().getDeviceColor(ColorEx.ORANGE);
    }


    private void setActive(final boolean enable)
    {
        final IMasterTrack mt = this.model.getMasterTrack();
        mt.setVolumeIndication(enable);
        mt.setPanIndication(enable);
    }

}