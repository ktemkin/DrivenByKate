// Written by Kate Temkin - ktemk.in
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.core;

import de.mossgrabers.framework.utils.ButtonEvent;

/**
 * Interface for classes (like control surfaces) that can handle events from the NIHostIntegrationAgent.
 */
public interface INIEventHandler {

    /**
     * Called when a new button event is received.
     *
     * @param ButtonID    The button corresponding to the event.
     * @param ButtonEvent The event that has occurred.
     */
    void handleButtonEvent(int rawButtonId, ButtonEvent event);

    /**
     * Called when a new "knob rotated" event has occurred.
     * Note that knob _touches_ are handled by `handleButtonEvent`.
     *
     * @param index        The index of the knob that's been turned.
     * @param encoderValue The new encoder value for the knob.
     */
    void handleKnobEvent(int rawContinuousId, int newValue);

    /**
     * Called when a new "main encoder rotated" event has occurred.
     * Note that directional input is handled over MIDI.
     *
     * @param encoderValue The new encoder value for the knob.
     */
    void handleMainEncoderEvent(int newValue);


    /**
     * Called when the device's octave changed.
     *
     * @param newBaseNote The new base; appears to be one octave above the first note on the keyboard.
     */
    default void handleOctaveChanged(int newBaseNote) {

    }


    /**
     * Called when a pad touch event has occurred.
     *
     * @param padNumber   The number of the pad, indexed from the _top_.
     * @param newPressure The new pressure; or 0 if the pad's been released.
     */
    default void handlePadEvent(int padNumber, int newPressure) {
    }

}
