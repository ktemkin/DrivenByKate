// Written by Kate Temkin - ktemk.in
// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.core;

import de.mossgrabers.framework.controller.display.AbstractGraphicDisplay;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.graphics.DefaultGraphicsDimensions;
import de.mossgrabers.framework.graphics.IBitmap;
import de.mossgrabers.framework.graphics.IGraphicsConfiguration;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Standard NI Graphics display; as used on newer NI hardware.
 *
 * @author Jürgen Moßgraber and Kate Temkin
 */
public class NIGraphicDisplay extends AbstractGraphicDisplay {
    /**
     * The header that precedes data sent to the left display.
     */
    private static final byte[] DISPLAY_HEADER_LEFT =
            {
                    (byte) 0x44, // Command ('Dsd')
                    (byte) 0x73,
                    (byte) 0x64,
                    (byte) 0x03,

                    (byte) 0x00, // Display ID (screen number)
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,

                    (byte) 0x00, // Padding?
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,

                    (byte) 0x10, // Width and height of the target screen
                    (byte) 0x01,
                    (byte) 0xe0,
                    (byte) 0x01,

                    (byte) 0x1c, // Size of the data to follow. (DISPLAY_PACKET_SIZE).
                    (byte) 0xfc,
                    (byte) 0x03,
                    (byte) 0x00,

                    // From here, the commands -mostly- match our USB protocol.

                    (byte) 0x84, // Start display commands.
                    (byte) 0x00,
                    (byte) 0x00, // Screen number.
                    (byte) 0x60, // ???

                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,

                    (byte) 0x00, // X position, MSB
                    (byte) 0x00, // X position, LSB
                    (byte) 0x00, // Y position, MSB
                    (byte) 0x00, // Y position, LSB

                    (byte) 0x01, // Width (480 for a full update), MSB
                    (byte) 0xe0, // Width, LSB
                    (byte) 0x01, // Height (272 for a full update), MSB
                    (byte) 0x10, // Height, LSB

                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0xFF, // Half the image size in _pixels_, MSB
                    (byte) 0x00, // Half the image size in _pixels_, LSB
            };
    /**
     * The header that precedes data sent to the right display.
     */
    private static final byte[] DISPLAY_HEADER_RIGHT =
            {
                    (byte) 0x44, // Command ('Dsd')
                    (byte) 0x73,
                    (byte) 0x64,
                    (byte) 0x03,

                    (byte) 0x01, // Display ID (screen number)
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,

                    (byte) 0x00, // Padding?
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,

                    (byte) 0x10, // Width and height of the target screen
                    (byte) 0x01,
                    (byte) 0xe0,
                    (byte) 0x01,

                    (byte) 0x1c, // Size of the data to follow. (DISPLAY_PACKET_SIZE).
                    (byte) 0xfc,
                    (byte) 0x03,
                    (byte) 0x00,

                    (byte) 0x84, // Start display commands.
                    (byte) 0x00,
                    (byte) 0x01, // Screen number.
                    (byte) 0x60, // ???

                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,

                    (byte) 0x00, // X position, MSB
                    (byte) 0x00, // X position, LSB
                    (byte) 0x00, // Y position, MSB
                    (byte) 0x00, // Y position, LSB

                    (byte) 0x01, // Width (480 for a full update), MSB
                    (byte) 0xe0, // Width, LSB
                    (byte) 0x01, // Height (272 for a full update), MSB
                    (byte) 0x10, // Height, LSB

                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0xFF, // Half the image size in _pixels_, MSB
                    (byte) 0x00, // Half the image size in _pixels_, LSB
            };
    private static final byte[] DISPLAY_FOOTER =
            {
                    (byte) 0x03, // ???
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,

                    (byte) 0x40, // ???
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
            };
    /**
     * The size of the display content. 2Bpp on a 480 * 272 display.
     */
    private static final int DISPLAY_DATA_SZ = 480 * 272 * 2;
    private static final int DISPLAY_PACKET_SIZE = DISPLAY_HEADER_LEFT.length + DISPLAY_DATA_SZ + DISPLAY_FOOTER.length;
    private static final int TIMEOUT = 1000;

    //
    // Display constants.
    //
    //protected final static int NI_MSG_DISPLAY   = 0x03566775;
    /**
     * Executor used to update screen data in the background.
     */
    private final ScheduledExecutorService executor;
    /**
     * Our connection to the Native Instruments host integration service.
     */
    private final AbstractNIHostInterop niConnection;
    /**
     * Memory that stores packets to be issued to the left display.
     */
    private final byte[] byteStoreLeft = new byte[DISPLAY_PACKET_SIZE];
    /**
     * Memory that stores packets to be issued to the left display.
     */
    private final byte[] byteStoreRight = new byte[DISPLAY_PACKET_SIZE];
    /**
     * Lock that ensures we don't render into the screen while it's being sent.
     */
    private final Object screenBufferUpdateLock = new Object();
    /**
     * True iff the display is currently running.
     */
    private boolean isShutdown = false;


    //
    // Implementation
    //

    /**
     * Constructor. A virtual LCD display of 960x272 pixels spread across two actual 480-pixel-wide displays.
     *
     * @param host              The host
     * @param maxParameterValue The maximum parameter value (upper bound)
     * @param configuration     The controller's configuration
     * @param int               deviceId The device type; used to create a new NIHIA interop.
     * @param int               deviceId The device's serial; used to create a new NIHIA interop.
     */
    public NIGraphicDisplay(final IHost host, final int maxParameterValue, final IGraphicsConfiguration configuration, int deviceId, String deviceSerial) throws IOException {
        super(host, configuration, new DefaultGraphicsDimensions(480 * 2, 272, maxParameterValue), "NI Device Display", true, 0.7);
        this.executor = Executors.newSingleThreadScheduledExecutor();

        // Create a connection to the NIHostIntegrationAgent, which actually performs the display scan-out.
        // Note: this NIConnection is made without an event handler; and so we try to enable MIDI.
        this.niConnection = AbstractNIHostInterop.createInterop(deviceId, deviceSerial, null, host, true);
        this.fillInHeaderAndFooter();
    }


    /**
     * Constructor. A virtual LCD display of 960x272 pixels spread across two actual 480-pixel-wide displays.
     *
     * @param host              The host
     * @param maxParameterValue The maximum parameter value (upper bound)
     * @param configuration     The Maschine configuration
     * @param interop           The NIHIA interop used for communication with the device.
     */
    public NIGraphicDisplay(final IHost host, final int maxParameterValue, final IGraphicsConfiguration configuration, AbstractNIHostInterop interop) {
        super(host, configuration, new DefaultGraphicsDimensions(480 * 2, 272, maxParameterValue), "NI Device Display", true, 0.7);
        this.executor = Executors.newSingleThreadScheduledExecutor();

        // Create a connection to the NIHostIntegrationAgent, which actually performs the display scanout.
        this.niConnection = interop;
        this.fillInHeaderAndFooter();
    }

    /**
     * Converts a raw set of 32-bit RGB888 pixels into an RGB565 / RGB16 pixel.
     *
     * @param red   The 32-bit red intensity.
     * @param green The 32-bit green intensity.
     * @param blue  The 32-bit blue intensity.
     * @return The closes RGB565 color.
     */
    private static int sPixelFromRGB(final int red, final int green, final int blue) {
        int pixel = (red & 0xF8) >> 3;
        pixel <<= 6;
        pixel += (green & 0xFC) >> 2;
        pixel <<= 5;
        pixel += (blue & 0xF8) >> 3;
        return pixel;
    }

    /**
     * Fills in the header and footer bytes for our raw messages.
     */
    private void fillInHeaderAndFooter() {

        // Fill in the headers at the beginning of our buffers...
        for (int i = 0; i < DISPLAY_HEADER_LEFT.length; ++i) {
            this.byteStoreLeft[i] = DISPLAY_HEADER_LEFT[i];
            this.byteStoreRight[i] = DISPLAY_HEADER_RIGHT[i];
        }

        // ... and the footers at the end.
        for (int i = 0; i < DISPLAY_FOOTER.length; ++i) {
            final int footerOffset = this.byteStoreLeft.length - DISPLAY_FOOTER.length;
            this.byteStoreLeft[footerOffset + i] = DISPLAY_FOOTER[i];
            this.byteStoreRight[footerOffset + i] = DISPLAY_FOOTER[i];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(final String message) {
        if (message == null)
            return;

        this.host.showNotification(message);
        this.setNotificationMessage(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        this.send();
        this.isShutdown = true;

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            super.shutdown();
        });
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (final InterruptedException ex) {
            this.host.error("Display shutdown interrupted.", ex);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Send the buffered image to the screen.
     *
     * @param image An image of size 960 x 272 pixels.
     */
    public void send(final IBitmap image) {
        if (this.niConnection == null) {
            return;
        }

        // Copy to the buffer
        synchronized (this.screenBufferUpdateLock) {
            image.encode((imageBuffer, width, height) -> {

                // Start filling our buffers right after the display header.
                int leftIndex = DISPLAY_HEADER_LEFT.length;
                int rightIndex = DISPLAY_HEADER_RIGHT.length;

                final int screenSplitBoundary = width / 2;

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        final int blue = imageBuffer.get();
                        final int green = imageBuffer.get();
                        final int red = imageBuffer.get();
                        imageBuffer.get(); // Drop unused Alpha

                        final int pixel = sPixelFromRGB(red, green, blue);

                        // If this is on the left half of our final image, stick the pixels
                        // in the left byte store...
                        if (x < screenSplitBoundary) {
                            this.byteStoreLeft[leftIndex + 1] = (byte) (pixel & 0x00FF);
                            this.byteStoreLeft[leftIndex] = (byte) ((pixel & 0xFF00) >> 8);
                            leftIndex += 2;
                        }
                        // Otherwise, we'll render into the buffer for the right screen.
                        else {
                            this.byteStoreRight[rightIndex + 1] = (byte) (pixel & 0x00FF);
                            this.byteStoreRight[rightIndex] = (byte) ((pixel & 0xFF00) >> 8);
                            rightIndex += 2;
                        }
                    }
                }

                imageBuffer.rewind();
            });

            this.executor.submit(this::sendDisplayData);
        }
    }

    private void sendDisplayData() {
        if (this.niConnection == null) {
            return;
        }

        synchronized (this.screenBufferUpdateLock) {
            // Update our displays.
            this.niConnection.pushRequest(this.byteStoreLeft);
            this.niConnection.pushRequest(this.byteStoreRight);
            this.niConnection.requestFocus();
        }
    }
}
