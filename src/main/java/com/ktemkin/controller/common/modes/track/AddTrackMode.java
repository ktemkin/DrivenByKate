// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.modes.track;

import com.ktemkin.controller.common.CommonUIConfiguration;
import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import com.ktemkin.controller.common.modes.BaseMode;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IDeviceMetadata;
import de.mossgrabers.framework.daw.data.IItem;
import de.mossgrabers.framework.daw.resource.ChannelType;
import de.mossgrabers.framework.featuregroup.AbstractFeatureGroup;
import de.mossgrabers.framework.utils.ButtonEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Menu for adding tracks.
 *
 * @author Jürgen Moßgraber
 */
public class AddTrackMode extends BaseMode<IItem>
{

    private static final String STR_EMPTY = "Empty";


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public AddTrackMode(final CommonUIControlSurface surface, final IModel model)
    {
        super("Add Track", surface, model);
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

        final CommonUIConfiguration conf        = this.surface.getConfiguration();
        final List<IDeviceMetadata> devices     = new ArrayList<>();
        final ChannelType           channelType;
        String                      channelName = null;

        if (index < 4) {
            channelType = ChannelType.AUDIO;
            if (index > 0) {
                final Optional<IDeviceMetadata> audioFavorite = conf.getAudioFavorite(index - 1);
                if (audioFavorite.isPresent()) {
                    final IDeviceMetadata deviceMetadata = audioFavorite.get();
                    devices.add(deviceMetadata);
                    channelName = deviceMetadata.name();
                }
            }
        }
        else {
            channelType = ChannelType.EFFECT;
            if (index > 4) {
                final Optional<IDeviceMetadata> effectFavorite = conf.getEffectFavorite(index - 5);
                if (effectFavorite.isPresent()) {
                    final IDeviceMetadata deviceMetadata = effectFavorite.get();
                    devices.add(deviceMetadata);
                    channelName = deviceMetadata.name();
                }
            }
        }

        this.model.getTrackBank().addChannel(channelType, channelName, devices);

        this.surface.getModeManager().restore();
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

        String                      channelName = null;
        final List<IDeviceMetadata> devices     = new ArrayList<>();
        if (index > 0) {
            final CommonUIConfiguration     conf               = this.surface.getConfiguration();
            final Optional<IDeviceMetadata> instrumentFavorite = conf.getInstrumentFavorite(index - 1);
            if (instrumentFavorite.isPresent()) {
                final IDeviceMetadata deviceMetadata = instrumentFavorite.get();
                devices.add(deviceMetadata);
                channelName = deviceMetadata.name();
            }
        }

        this.model.getTrackBank().addChannel(ChannelType.INSTRUMENT, channelName, devices);
        this.surface.getModeManager().restore();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getButtonColor(final ButtonID buttonID)
    {
        var colorManager = this.getColorManager();

        int index = this.isButtonRow(0, buttonID);
        if (index >= 0) {
            if (index == 0) {
                return colorManager.getDeviceColor(ColorEx.GREEN);
            }
            if (index < 4) {
                return colorManager.getDeviceColor(ColorEx.DARK_GREEN);
            }
            if (index == 4) {
                return colorManager.getDeviceColor(ColorEx.BLUE);
            }
            return colorManager.getDeviceColor(ColorEx.DARK_BLUE);
        }

        index = this.isButtonRow(1, buttonID);
        if (index >= 0) {
            if (index == 0) {
                return colorManager.getDeviceColor(ColorEx.YELLOW);
            }
            return colorManager.getDeviceColor(ColorEx.DARK_YELLOW);
        }

        return this.colorManager.getColorIndex(AbstractFeatureGroup.BUTTON_COLOR_OFF);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        final CommonUIConfiguration conf = this.surface.getConfiguration();

        display.addOptionElement("Instrument", STR_EMPTY, false, ColorEx.YELLOW, "Audio", STR_EMPTY, false, ColorEx.GREEN, false, false);

        Optional<IDeviceMetadata> instrFav = conf.getInstrumentFavorite(0);
        Optional<IDeviceMetadata> audioFav = conf.getAudioFavorite(0);
        display.addOptionElement("", instrFav.isEmpty() ? "" : instrFav.get().name(), false, "", audioFav.isEmpty() ? "" : audioFav.get().name(), false, false);
        instrFav = conf.getInstrumentFavorite(1);
        audioFav = conf.getAudioFavorite(1);
        display.addOptionElement("", instrFav.isEmpty() ? "" : instrFav.get().name(), false, "", audioFav.isEmpty() ? "" : audioFav.get().name(), false, false);
        instrFav = conf.getInstrumentFavorite(2);
        audioFav = conf.getAudioFavorite(2);
        display.addOptionElement("", instrFav.isEmpty() ? "" : instrFav.get().name(), false, "", audioFav.isEmpty() ? "" : audioFav.get().name(), false, false);

        instrFav = conf.getInstrumentFavorite(3);
        display.addOptionElement("", instrFav.isEmpty() ? "" : instrFav.get().name(), false, null, "Effect", STR_EMPTY, false, ColorEx.BLUE, false, false);

        instrFav = conf.getInstrumentFavorite(4);
        Optional<IDeviceMetadata> effectFavorite = conf.getEffectFavorite(0);
        display.addOptionElement("", instrFav.isEmpty() ? "" : instrFav.get().name(), false, "", effectFavorite.isEmpty() ? "" : effectFavorite.get().name(), false, false);
        instrFav       = conf.getInstrumentFavorite(5);
        effectFavorite = conf.getEffectFavorite(1);
        display.addOptionElement("", instrFav.isEmpty() ? "" : instrFav.get().name(), false, "", effectFavorite.isEmpty() ? "" : effectFavorite.get().name(), false, false);
        instrFav       = conf.getInstrumentFavorite(6);
        effectFavorite = conf.getEffectFavorite(2);
        display.addOptionElement("", instrFav.isEmpty() ? "" : instrFav.get().name(), false, "", effectFavorite.isEmpty() ? "" : effectFavorite.get().name(), false, false);
    }

}
