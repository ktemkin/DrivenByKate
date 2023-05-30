// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.maschine;

/**
 * The supported Maschine models.
 *
 * @author Jürgen Moßgraber
 */
public enum Maschine {
    /**
     * Maschine JAM.
     */
    JAM("Maschine JAM", "1500", 0x1500, true, false, true, true, false, 440, 2),
    /**
     * Maschine Mikro Mk3.
     */
    MIKRO_MK3("Maschine Mikro Mk3", "1700", 0x1700, false, false, false, false, false, 440, 0),
    /**
     * Maschine Mk2.
     */
    MK2("Maschine Mk2", "0000", 0x0000, false, true, true, true, false, 714, 0),
    /**
     * Maschine Mk3.
     */
    MK3("Maschine Mk3", "1600", 0x1600, true, true, true, true, true, 800, 2),
    /**
     * Maschine+.
     */
    PLUS("Maschine+", "1820", 0x1820, true, true, true, true, true, 800, 2),
    /**
     * Maschine Studio.
     */
    STUDIO("Maschine Studio", "1300", 0x1300, false, true, true, true, false, 700, 4);


    private static final String MESSAGE_SHIFT_DOWN = "F0002109%s4D5000014D01F7";
    private static final String MESSAGE_SHIFT_UP = "F0002109%s4D5000014D00F7";
    private static final String MESSAGE_RETURN_FROM_HOST = "F0002109%s4D5000014601F7";

    private final String name;
    private final String maschineID;
    private final int niDeviceID;
    private final boolean hasShift;
    private final boolean hasMCUDisplay;
    private final boolean hasGroupButtons;
    private final boolean hasCursorKeys;
    private final boolean hasNIServiceIntegration;
    private final int height;
    private final int footswitches;

    private final String messageShiftDown;
    private final String messageShiftUp;
    private final String messageReturnFromHost;


    /**
     * Constructor.
     *
     * @param name                    The name of the Maschine
     * @param maschineID              The ID of the device
     * @param niDeviceID              The number used to indicate this device type by NI services. Matches the USB PID.
     * @param hasShift                Can the Shift button be used? Otherwise emulated with Stop button
     * @param hasMCUDisplay           Does it support a MCU protocol display?
     * @param hasGroupButtons         Does it have group buttons?
     * @param hasCursorKeys           Does the device have cursor keys?
     * @param hasNIServiceIntegration Can the device drive graphic displays via NIHostIntegrationAgent?
     * @param height                  The height of the simulator window
     * @param footswitches            The number of available footswitch on the Maschine
     */
    private Maschine(final String name, final String maschineID, final int niDeviceID, final boolean hasShift, final boolean hasMCUDisplay, final boolean hasGroupButtons, final boolean hasCursorKeys, final boolean hasNIServiceIntegration, final int height, final int footswitches) {
        this.name = name;
        this.maschineID = maschineID;
        this.niDeviceID = niDeviceID;
        this.hasShift = hasShift;
        this.hasMCUDisplay = hasMCUDisplay;
        this.hasGroupButtons = hasGroupButtons;
        this.hasCursorKeys = hasCursorKeys;
        this.hasNIServiceIntegration = hasNIServiceIntegration;
        this.height = height;
        this.footswitches = footswitches;

        this.messageShiftDown = String.format(MESSAGE_SHIFT_DOWN, this.maschineID);
        this.messageShiftUp = String.format(MESSAGE_SHIFT_UP, this.maschineID);
        this.messageReturnFromHost = String.format(MESSAGE_RETURN_FROM_HOST, this.maschineID);
    }


    /**
     * Get the name of the specific Maschine.
     *
     * @return The name
     */
    public String getName() {
        return this.name;
    }


    /**
     * Get the device identifier, as used by NI services.
     *
     * @return The device ID>
     */
    public int getDeviceId() {
        return this.niDeviceID;
    }


    /**
     * Can the Shift button be used? Otherwise emulated with Stop button.
     *
     * @return True if Shift is supported
     */
    public boolean hasShift() {
        return this.hasShift;
    }


    /**
     * Does it have a display that supports the MCU protocol?
     *
     * @return True if supported
     */
    public boolean hasMCUDisplay() {
        return this.hasMCUDisplay;
    }


    /**
     * Does it have a display we can address via the NIHostIntegrationAgent?
     *
     * @return True if supported
     */
    public boolean hasNIGraphicDisplay() {
        return this.hasNIServiceIntegration;
    }


    /**
     * Does it have group buttons?
     *
     * @return True if supported
     */
    public boolean hasGroupButtons() {
        return this.hasGroupButtons;
    }


    /**
     * Does the device have cursor keys?
     *
     * @return True if it has cursor keys
     */
    public boolean hasCursorKeys() {
        return this.hasCursorKeys;
    }


    /**
     * Get the width of the simulator window
     *
     * @return The height
     */
    public double getWidth() {
        return this == Maschine.STUDIO ? 1150 : 800;
    }


    /**
     * Get the height of the simulator window
     *
     * @return The height
     */
    public int getHeight() {
        return this.height;
    }


    /**
     * Get the number of foot switches on the Maschine.
     *
     * @return The number
     */
    public int getFootswitches() {
        return this.footswitches;
    }


    /**
     * Get the system exclusive message which is sent from the device on pressing the Shift button.
     *
     * @return The message
     */
    public String getMessageShiftDown() {
        return this.messageShiftDown;
    }


    /**
     * Get the system exclusive message which is sent from the device on releasing the Shift button.
     *
     * @return The message
     */
    public String getMessageShiftUp() {
        return this.messageShiftUp;
    }


    /**
     * Get the system exclusive message which is sent from the device when the MIDI mode is entered.
     *
     * @return The message
     */
    public String getMessageReturnFromHost() {
        return this.messageReturnFromHost;
    }
}
