// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.common.modes;

import com.ktemkin.controller.common.controller.CommonUIControlSurface;
import com.ktemkin.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.clip.INoteClip;
import de.mossgrabers.framework.daw.clip.IStepInfo;
import de.mossgrabers.framework.daw.clip.NoteOccurrenceType;
import de.mossgrabers.framework.daw.clip.NotePosition;
import de.mossgrabers.framework.daw.data.IItem;
import de.mossgrabers.framework.daw.data.empty.EmptyParameter;
import de.mossgrabers.framework.mode.INoteMode;
import de.mossgrabers.framework.mode.NoteEditor;
import de.mossgrabers.framework.parameter.NoteAttribute;
import de.mossgrabers.framework.parameter.NoteParameter;
import de.mossgrabers.framework.parameterprovider.IParameterProvider;
import de.mossgrabers.framework.parameterprovider.special.FixedParameterProvider;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.utils.StringUtils;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


/**
 * Editing of note parameters.
 *
 * @author Jürgen Moßgraber
 */
public class NoteMode extends BaseMode<IItem> implements INoteMode
{

    private static final String[] MENU =
            {
                    "Common",
                    "Expressions",
                    "Repeat",
                    " ",
                    " ",
                    " ",
                    " ",
                    "Recurr. Pattern"
            };

    private static final String[] RECURRENCE_PRESETS =
            {
                    "First",
                    "Not first",
                    " ",
                    "Last",
                    "Not last",
                    " ",
                    "Odd",
                    "Even",
            };

    private final IHost host;

    private final NoteEditor noteEditor = new NoteEditor();

    private final Map<Page, IParameterProvider> pageParamProviders = new EnumMap<>(Page.class);

    private Page page = Page.NOTE;


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model   The model
     */
    public NoteMode(final CommonUIControlSurface surface, final IModel model)
    {
        super("Note", surface, model);

        this.host = this.model.getHost();

        final IValueChanger valueChanger = model.getValueChanger();

        final NoteParameter durationParameter = new NoteParameter(NoteAttribute.DURATION, null, model, this, valueChanger);
        final NoteParameter muteParameter     = new NoteParameter(NoteAttribute.MUTE, null, model, this, valueChanger);

        this.pageParamProviders.put(Page.NOTE, new FixedParameterProvider(
                // Duration
                durationParameter,
                // Mute
                muteParameter,
                // Velocity
                new NoteParameter(NoteAttribute.VELOCITY, null, model, this, valueChanger),
                // Velocity Spread
                new NoteParameter(NoteAttribute.VELOCITY_SPREAD, null, model, this, valueChanger),
                // Release Velocity
                new NoteParameter(NoteAttribute.RELEASE_VELOCITY, null, model, this, valueChanger),
                // Chance
                new NoteParameter(NoteAttribute.CHANCE, null, model, this, valueChanger),
                // Occurrence
                new NoteParameter(NoteAttribute.OCCURRENCE, null, model, this, valueChanger),
                // Recurrence
                new NoteParameter(NoteAttribute.RECURRENCE_LENGTH, null, model, this, valueChanger)));

        this.pageParamProviders.put(Page.EXPRESSIONS, new FixedParameterProvider(
                // Duration
                durationParameter,
                // Mute
                muteParameter,
                // -
                EmptyParameter.INSTANCE,
                // Gain
                new NoteParameter(NoteAttribute.GAIN, null, model, this, valueChanger),
                // Panorama
                new NoteParameter(NoteAttribute.PANORAMA, null, model, this, valueChanger),
                // Transpose
                new NoteParameter(NoteAttribute.TRANSPOSE, null, model, this, valueChanger),
                // Timbre
                new NoteParameter(NoteAttribute.TIMBRE, null, model, this, valueChanger),
                // Pressure
                new NoteParameter(NoteAttribute.PRESSURE, null, model, this, valueChanger)));

        this.pageParamProviders.put(Page.REPEAT, new FixedParameterProvider(
                // Duration
                durationParameter,
                // Mute
                muteParameter,
                // -
                EmptyParameter.INSTANCE,
                // Repeat
                new NoteParameter(NoteAttribute.REPEAT, null, model, this, valueChanger),
                // Repeat Curve
                new NoteParameter(NoteAttribute.REPEAT_CURVE, null, model, this, valueChanger),
                // Repeat Velocity Curve
                new NoteParameter(NoteAttribute.REPEAT_VELOCITY_CURVE, null, model, this, valueChanger),
                // Repeat Velocity End
                new NoteParameter(NoteAttribute.REPEAT_VELOCITY_END, null, model, this, valueChanger),
                // -
                EmptyParameter.INSTANCE));

        this.pageParamProviders.put(Page.RECCURRENCE_PATTERN, new FixedParameterProvider(
                // -
                EmptyParameter.INSTANCE,
                // -
                EmptyParameter.INSTANCE,
                // -
                EmptyParameter.INSTANCE,
                // -
                EmptyParameter.INSTANCE,
                // -
                EmptyParameter.INSTANCE,
                // -
                EmptyParameter.INSTANCE,
                // -
                EmptyParameter.INSTANCE,
                // Recurrence Length
                new NoteParameter(NoteAttribute.RECURRENCE_LENGTH, null, model, this, valueChanger)));

        this.rebind();
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

        final INoteClip          clip  = this.noteEditor.getClip();
        final List<NotePosition> notes = this.noteEditor.getNotes();
        for (final NotePosition notePosition : notes) {
            final IStepInfo stepInfo = clip.getStep(notePosition);

            switch (this.page) {
                case NOTE:
                    switch (index) {
                        case 5 -> {
                            if (this.host.supports(NoteAttribute.CHANCE)) {
                                clip.updateStepIsChanceEnabled(notePosition, !stepInfo.isChanceEnabled());
                            }
                        }
                        case 6 -> {
                            if (this.host.supports(NoteAttribute.OCCURRENCE)) {
                                clip.updateStepIsOccurrenceEnabled(notePosition, !stepInfo.isOccurrenceEnabled());
                            }
                        }
                        case 7 -> {
                            if (this.host.supports(NoteAttribute.RECURRENCE_LENGTH)) {
                                clip.updateStepIsRecurrenceEnabled(notePosition, !stepInfo.isRecurrenceEnabled());
                            }
                        }
                        default -> {
                            return;
                        }
                    }
                    break;

                case EXPRESSIONS:
                    break;

                case REPEAT:
                    if (index == 3 && this.host.supports(NoteAttribute.REPEAT)) {
                        clip.updateStepIsRepeatEnabled(notePosition, !stepInfo.isRepeatEnabled());
                    }
                    break;

                case RECCURRENCE_PATTERN:
                    if (this.host.supports(NoteAttribute.RECURRENCE_LENGTH)) {
                        if (this.surface.isShiftPressed()) {
                            switch (index) {
                                // First
                                case 0 -> clip.updateStepRecurrenceMask(notePosition, 1);

                                // Not first
                                case 1 -> clip.updateStepRecurrenceMask(notePosition, 254);

                                // Last
                                case 3 -> {
                                    final int lastRecurrence = 1 << stepInfo.getRecurrenceLength() - 1;
                                    clip.updateStepRecurrenceMask(notePosition, lastRecurrence);
                                }
                                // Not last
                                case 4 -> {
                                    final int notLastRecurrence = (1 << stepInfo.getRecurrenceLength() - 1) - 1;
                                    clip.updateStepRecurrenceMask(notePosition, notLastRecurrence);
                                }
                                // Even
                                case 6 -> clip.updateStepRecurrenceMask(notePosition, 85);

                                // Odd
                                case 7 -> clip.updateStepRecurrenceMask(notePosition, 170);

                                // Not used
                                default -> {
                                }
                            }
                        }
                        else {
                            clip.updateStepRecurrenceMaskToggleBit(notePosition, index);
                        }
                    }
                    break;
            }
        }
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

        switch (index) {
            case 0 -> this.page = Page.NOTE;
            case 1 -> {
                if (this.host.supports(NoteAttribute.TIMBRE)) {
                    this.page = Page.EXPRESSIONS;
                }
            }
            case 2 -> {
                if (this.host.supports(NoteAttribute.REPEAT)) {
                    this.page = Page.REPEAT;
                }
            }
            case 7 -> {
                if (this.host.supports(NoteAttribute.RECURRENCE_LENGTH)) {
                    this.page = Page.RECCURRENCE_PATTERN;
                }
            }
            default -> {
            }
            // Not used:
        }

        this.rebind();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onKnobTouch(final int index, final boolean isTouched)
    {
        final List<NotePosition> notes = this.noteEditor.getNotes();
        if (notes.isEmpty()) {
            return;
        }

        if (isTouched && this.surface.isDeletePressed()) {
            this.surface.setTriggerConsumed(ButtonID.DELETE);
            this.defaultParameterProvider.get(index).resetValue();
            return;
        }

        final INoteClip clip = this.noteEditor.getClip();
        if (isTouched) {
            clip.startEdit(notes);
        }
        else {
            clip.stopEdit();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGraphicsDisplay(final IGraphicDisplay display)
    {
        final List<NotePosition> notes = this.noteEditor.getNotes();
        if (notes.isEmpty()) {
            for (int i = 0; i < 8; i++) {
                display.addOptionElement(i == 2 ? "Please select a note to edit..." : "", "", false, "", "", false, true);
            }
            return;
        }

        final NotePosition  notePosition = notes.get(0);
        final IStepInfo     stepInfo     = this.noteEditor.getClip().getStep(notePosition);
        final IValueChanger valueChanger = this.model.getValueChanger();

        if (this.page != Page.RECCURRENCE_PATTERN) {
            final int     size      = notes.size();
            final boolean isOneNote = size == 1;

            final String stepBottomMenu = isOneNote ? "Step: " + (notePosition.getStep() + 1) : "Notes: " + size;
            display.addParameterElementWithPlainMenu(MENU[0], this.page == Page.NOTE, stepBottomMenu, null, false, "Length", -1, this.formatLength(stepInfo.getDuration()), this.isKnobTouched(0), -1);
            final boolean hasExpressions = this.host.supports(NoteAttribute.TIMBRE);

            final String  topMenu     = hasExpressions ? MENU[1] : " ";
            final boolean isTopMenuOn = hasExpressions && this.page == Page.EXPRESSIONS;
            final String  bottomMenu  = isOneNote ? Scales.formatNoteAndOctave(notePosition.getNote(), -3) : "*";
            if (this.host.supports(NoteAttribute.MUTE)) {
                final int value = stepInfo.isMuted() ? valueChanger.getUpperBound() : 0;
                display.addParameterElementWithPlainMenu(topMenu, isTopMenuOn, bottomMenu, null, false, "Is Muted?", value, stepInfo.isMuted() ? "Yes" : "No", this.isKnobTouched(1), value);
            }
            else {
                display.addParameterElementWithPlainMenu(topMenu, isTopMenuOn, bottomMenu, null, false, null, -1, null, false, -1);
            }
        }

        switch (this.page) {
            case NOTE -> {
                final double noteVelocity   = stepInfo.getVelocity();
                final int    parameterValue = valueChanger.fromNormalizedValue(noteVelocity);
                display.addParameterElementWithPlainMenu(this.host.supports(NoteAttribute.REPEAT) ? MENU[2] : " ", false, null, null, false, "Velocity", parameterValue, StringUtils.formatPercentage(noteVelocity), this.isKnobTouched(2), parameterValue);
                if (this.host.supports(NoteAttribute.VELOCITY_SPREAD)) {
                    final double noteVelocitySpread   = stepInfo.getVelocitySpread();
                    final int    parameterSpreadValue = valueChanger.fromNormalizedValue(noteVelocitySpread);
                    display.addParameterElementWithPlainMenu(MENU[3], false, null, null, false, "Vel-Spread", parameterSpreadValue, StringUtils.formatPercentage(noteVelocitySpread), this.isKnobTouched(3), parameterSpreadValue);
                }
                else {
                    display.addEmptyElement(true);
                }
                if (this.host.supports(NoteAttribute.RELEASE_VELOCITY)) {
                    final double noteReleaseVelocity   = stepInfo.getReleaseVelocity();
                    final int    parameterReleaseValue = valueChanger.fromNormalizedValue(noteReleaseVelocity);
                    display.addParameterElementWithPlainMenu(MENU[4], false, null, null, false, "R-Velocity", parameterReleaseValue, StringUtils.formatPercentage(noteReleaseVelocity), this.isKnobTouched(4), parameterReleaseValue);
                }
                else {
                    display.addEmptyElement(true);
                }
                if (this.host.supports(NoteAttribute.CHANCE)) {
                    final double chance      = stepInfo.getChance();
                    final int    chanceValue = valueChanger.fromNormalizedValue(chance);
                    display.addParameterElementWithPlainMenu(MENU[5], false, stepInfo.isChanceEnabled() ? "On" : "Off", null, false, "Chance", chanceValue, StringUtils.formatPercentage(chance), this.isKnobTouched(5), chanceValue);
                }
                else {
                    display.addEmptyElement(true);
                }
                if (this.host.supports(NoteAttribute.OCCURRENCE)) {
                    final NoteOccurrenceType occurrence = stepInfo.getOccurrence();
                    display.addParameterElementWithPlainMenu(MENU[6], false, stepInfo.isOccurrenceEnabled() ? "On" : "Off", null, false, "Occurrence", -1, StringUtils.optimizeName(occurrence.getName(), 9), this.isKnobTouched(6), -1);
                }
                else {
                    display.addEmptyElement(true);
                }
                if (this.host.supports(NoteAttribute.RECURRENCE_LENGTH)) {
                    final int    recurrence    = stepInfo.getRecurrenceLength();
                    final String recurrenceStr = recurrence < 2 ? "Off" : Integer.toString(recurrence);
                    final int    recurrenceVal = (recurrence - 1) * (this.model.getValueChanger().getUpperBound() - 1) / 7;
                    display.addParameterElementWithPlainMenu(MENU[7], false, stepInfo.isRecurrenceEnabled() ? "On" : "Off", null, false, "Recurrence", recurrenceVal, recurrenceStr, this.isKnobTouched(7), recurrenceVal);
                }
                else {
                    display.addEmptyElement(true);
                }
            }
            case EXPRESSIONS -> {
                display.addParameterElementWithPlainMenu(MENU[2], false, null, null, false, null, -1, null, false, -1);
                final double noteGain           = stepInfo.getGain();
                final int    parameterGainValue = Math.min(1023, valueChanger.fromNormalizedValue(noteGain));
                display.addParameterElementWithPlainMenu(MENU[3], false, null, null, false, "Gain", parameterGainValue, StringUtils.formatPercentage(noteGain), this.isKnobTouched(3), parameterGainValue);
                final double notePan           = stepInfo.getPan();
                final int    parameterPanValue = valueChanger.fromNormalizedValue((notePan + 1.0) / 2.0);
                display.addParameterElementWithPlainMenu(MENU[4], false, null, null, false, "Pan", parameterPanValue, StringUtils.formatPercentage(notePan), this.isKnobTouched(4), parameterPanValue);
                final double noteTranspose           = stepInfo.getTranspose();
                final int    parameterTransposeValue = valueChanger.fromNormalizedValue((noteTranspose + 24.0) / 48.0);
                display.addParameterElementWithPlainMenu(MENU[5], false, null, null, false, "Pitch", parameterTransposeValue, String.format("%.1f", noteTranspose), this.isKnobTouched(5), parameterTransposeValue);
                final double noteTimbre           = stepInfo.getTimbre();
                final int    parameterTimbreValue = valueChanger.fromNormalizedValue((noteTimbre + 1.0) / 2.0);
                display.addParameterElementWithPlainMenu(MENU[6], false, null, null, false, "Timbre", parameterTimbreValue, StringUtils.formatPercentage(noteTimbre), this.isKnobTouched(6), parameterTimbreValue);
                final double notePressure           = stepInfo.getPressure();
                final int    parameterPressureValue = valueChanger.fromNormalizedValue(notePressure);
                display.addParameterElementWithPlainMenu(MENU[7], this.page == Page.RECCURRENCE_PATTERN, null, null, false, "Pressure", parameterPressureValue, StringUtils.formatPercentage(notePressure), this.isKnobTouched(7), parameterPressureValue);
            }
            case REPEAT -> {
                display.addParameterElementWithPlainMenu(MENU[2], true, null, null, false, null, -1, null, false, -1);
                final int    repeatCount      = stepInfo.getRepeatCount();
                final String repeatCountValue = stepInfo.getFormattedRepeatCount();
                final int    rc               = (repeatCount + 127) * (this.model.getValueChanger().getUpperBound() - 1) / 254;
                display.addParameterElementWithPlainMenu(MENU[3], false, stepInfo.isRepeatEnabled() ? "On" : "Off", null, false, "Count", rc, repeatCountValue, this.isKnobTouched(3), rc);
                final double repeatCurve      = stepInfo.getRepeatCurve();
                final int    repeatCurveValue = valueChanger.fromNormalizedValue((repeatCurve + 1.0) / 2.0);
                display.addParameterElementWithPlainMenu(MENU[4], false, null, null, false, "Curve", repeatCurveValue, StringUtils.formatPercentage(repeatCurve), this.isKnobTouched(4), repeatCurveValue);
                final double repeatVelocityCurve      = stepInfo.getRepeatVelocityCurve();
                final int    repeatVelocityCurveValue = valueChanger.fromNormalizedValue((repeatVelocityCurve + 1.0) / 2.0);
                display.addParameterElementWithPlainMenu(MENU[5], false, null, null, false, "Vel. Curve", repeatVelocityCurveValue, StringUtils.formatPercentage(repeatVelocityCurve), this.isKnobTouched(5), repeatVelocityCurveValue);
                final double repeatVelocityEnd      = stepInfo.getRepeatVelocityEnd();
                final int    repeatVelocityEndValue = valueChanger.fromNormalizedValue((repeatVelocityEnd + 1.0) / 2.0);
                display.addParameterElementWithPlainMenu(MENU[6], false, null, null, false, "Vel. End", repeatVelocityEndValue, StringUtils.formatPercentage(repeatVelocityEnd), this.isKnobTouched(6), repeatVelocityEndValue);
                display.addParameterElementWithPlainMenu(MENU[7], false, null, null, false, null, -1, null, false, -1);
            }
            case RECCURRENCE_PATTERN -> {
                final int recurrenceLength = stepInfo.getRecurrenceLength();
                final int mask             = stepInfo.getRecurrenceMask();
                for (int i = 0; i < 8; i++) {
                    ColorEx color = ColorEx.BLACK;
                    String  label = "-";

                    if (this.surface.isShiftPressed()) {
                        color = RECURRENCE_PRESETS[i].isBlank() ? ColorEx.BLACK : ColorEx.GRAY;
                        label = RECURRENCE_PRESETS[i];
                    }
                    else {
                        final boolean isOn = (mask & 1 << i) > 0;
                        if (i < recurrenceLength) {
                            color = isOn ? ColorEx.ORANGE : null;
                            label = isOn ? "On" : "Off";
                        }
                    }

                    if (i == 7) {
                        final int    recurrence    = stepInfo.getRecurrenceLength();
                        final String recurrenceStr = recurrence < 2 ? "Off" : Integer.toString(recurrence);
                        final int    recurrenceVal = (recurrence - 1) * (this.model.getValueChanger().getUpperBound() - 1) / 7;
                        display.addParameterElementWithPlainMenu(MENU[i], true, label, color, false, "Recurrence", recurrenceVal, recurrenceStr, this.isKnobTouched(7), recurrenceVal);
                    }
                    else {
                        display.addParameterElementWithPlainMenu(MENU[i], false, label, color, false, null, -1, null, false, -1);
                    }
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getButtonColor(final ButtonID buttonID)
    {
        var colorManager = this.getColorManager();

        final List<NotePosition> notes = this.noteEditor.getNotes();
        if (notes.isEmpty()) {
            return colorManager.getDeviceColor(ColorEx.BLACK);
        }

        final INoteClip clip = this.noteEditor.getClip();
        for (final NotePosition notePosition : notes) {
            final IStepInfo stepInfo = clip.getStep(notePosition);

            int index = this.isButtonRow(0, buttonID);
            if (index >= 0) {
                switch (this.page) {
                    case NOTE:
                        if (index == 5 && this.host.supports(NoteAttribute.CHANCE)) {
                            return colorManager.getDeviceColor(stepInfo.isChanceEnabled() ? ColorEx.ORANGE : ColorEx.DARK_ORANGE);
                        }
                        if (index == 6 && this.host.supports(NoteAttribute.OCCURRENCE)) {
                            return colorManager.getDeviceColor(stepInfo.isOccurrenceEnabled() ? ColorEx.ORANGE : ColorEx.DARK_ORANGE);
                        }
                        if (index == 7 && this.host.supports(NoteAttribute.RECURRENCE_LENGTH)) {
                            return colorManager.getDeviceColor(stepInfo.isRecurrenceEnabled() ? ColorEx.ORANGE : ColorEx.DARK_ORANGE);
                        }
                        break;

                    case EXPRESSIONS:
                        break;

                    case REPEAT:
                        if (index == 3) {
                            return colorManager.getDeviceColor(stepInfo.isRepeatEnabled() ? ColorEx.ORANGE : ColorEx.DARK_ORANGE);
                        }
                        break;

                    case RECCURRENCE_PATTERN:
                        if (this.surface.isShiftPressed()) {
                            return colorManager.getDeviceColor(RECURRENCE_PRESETS[index].isBlank() ? ColorEx.BLACK : ColorEx.DARK_GREEN);
                        }

                        final int recurrenceLength = stepInfo.getRecurrenceLength();
                        final int mask = stepInfo.getRecurrenceMask();
                        final boolean isOn = (mask & 1 << index) > 0;
                        ColorEx color = ColorEx.BLACK;
                        if (index < recurrenceLength) {
                            color = isOn ? ColorEx.ORANGE : ColorEx.DARK_ORANGE;
                        }
                        return colorManager.getDeviceColor(color);
                }

                return colorManager.getDeviceColor(ColorEx.BLACK);
            }

            index = this.isButtonRow(1, buttonID);
            if (index >= 0) {
                switch (this.page) {
                    case NOTE -> {
                        if (index == 0) {
                            return colorManager.getDeviceColor(ColorEx.GREEN);
                        }
                    }
                    case EXPRESSIONS -> {
                        if (index == 1) {
                            return colorManager.getDeviceColor(ColorEx.GREEN);
                        }
                    }
                    case REPEAT -> {
                        if (index == 2) {
                            return colorManager.getDeviceColor(ColorEx.GREEN);
                        }
                    }
                    case RECCURRENCE_PATTERN -> {
                        if (index == 7) {
                            return colorManager.getDeviceColor(ColorEx.GREEN);
                        }
                    }
                }

                if (index == 0 || index == 1 && this.host.supports(NoteAttribute.TIMBRE)) {
                    return colorManager.getDeviceColor(ColorEx.DARK_GRAY);
                }
                if (index == 2 && this.host.supports(NoteAttribute.REPEAT) || index == 7 && this.host.supports(NoteAttribute.RECURRENCE_LENGTH)) {
                    return colorManager.getDeviceColor(ColorEx.DARK_GRAY);
                }

                return colorManager.getDeviceColor(ColorEx.BLACK);
            }
        }

        return colorManager.getDeviceColor(ColorEx.BLACK);
    }


    /**
     * Format the duration of the current note.
     *
     * @param duration The note duration
     * @return The formatted value
     */
    private String formatLength(final double duration)
    {
        return StringUtils.formatMeasures(this.model.getTransport().getQuartersPerMeasure(), duration, 0, true);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public INoteClip getClip()
    {
        return this.noteEditor.getClip();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void clearNotes()
    {
        this.noteEditor.clearNotes();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setNote(final INoteClip clip, final NotePosition notePosition)
    {
        this.noteEditor.setNote(clip, notePosition);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addNote(final INoteClip clip, final NotePosition notePosition)
    {
        this.noteEditor.addNote(clip, notePosition);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<NotePosition> getNotes()
    {
        return this.noteEditor.getNotes();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<NotePosition> getNotePosition(final int parameterIndex)
    {
        return this.noteEditor.getNotePosition(parameterIndex);
    }


    private void rebind()
    {
        this.setParameterProvider(this.pageParamProviders.get(this.page));
        this.bindControls();
    }


    private enum Page
    {
        NOTE,
        RECCURRENCE_PATTERN,
        EXPRESSIONS,
        REPEAT
    }

}