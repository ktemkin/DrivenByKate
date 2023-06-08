// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.ktemkin.controller.ni.maschine.core.controller;

import com.ktemkin.controller.ni.maschine.controller.MaschineControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.grid.BlinkingPadGrid;
import de.mossgrabers.framework.daw.midi.IMidiOutput;


/**
 * Implementation of the Maschine grid of pads.
 *
 * @author Kate Temkin
 */
public class MaschinePadGrid extends BlinkingPadGrid
{
    /**
     * The note at which our pad grid's "midi" starts.
     */
    public static final int START_NOTE = 36;


    /**
     * Matrix that converts a grid position to a midi value.
     */
    public final static byte[] GRID_TO_MIDI = {
            48, 49, 50, 51,
            44, 45, 46, 47,
            40, 41, 42, 43,
            36, 37, 38, 39,
    };

    /**
     * Matrix that converts a MIDI note to a pad position.
     * One must subtract 36 before using this lookup table.
     */
    public final static byte[] MIDI_TO_GRID = {
            12, 13, 14, 15, // The first note is in the bottom row.
            8, 9, 10, 11,
            4, 5, 6, 7,
            0, 1, 2, 3,

    };

    /**
     * The surface associated with this pad.
     */
    protected MaschineControlSurface surface;


    /**
     * Constructor. A 4x4 grid.
     *
     * @param colorManager The color manager for accessing specific colors to use
     * @param output       The MIDI output which can address the pad states
     */
    public MaschinePadGrid(final ColorManager colorManager, final IMidiOutput output)
    {
        super(colorManager, output, 4, 4, START_NOTE);
    }


    /**
     * Constructor.
     *
     * @param colorManager The color manager for accessing specific colors to use
     * @param output       The MIDI output which can address the pad states
     * @param rows         The number of rows of the grid
     * @param cols         The number of columns of the grid
     */
    public MaschinePadGrid(final ColorManager colorManager, final IMidiOutput output, final int rows, final int cols, final MaschineControlSurface surface)
    {
        super(colorManager, output, rows, cols, 36);
        this.surface = surface;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLight(int note, int color, int blinkColor, boolean fast)
    {
        super.setLight(note, color, blinkColor, fast);
        if (this.surface == null) {
            return;
        }

        // Find the note relative to 0.
        int relativeNote = note - this.getStartNote();

        // Ignore pads we don't have.
        if ((relativeNote < 0) || (relativeNote >= MIDI_TO_GRID.length)) {
            this.surface.getHost().println(String.format("REJECT NOTE %d", note));
            return;
        }

        // Fetch the pad number...
        int pad = MIDI_TO_GRID[relativeNote];

        // ... convert it to a button ID...
        ButtonID button = ButtonID.get(ButtonID.PAD1, pad);

        // ... and set its color.
        this.surface.setButtonColor(button, this.colorManager.getColor(color, button));
    }


    public void setSurface(MaschineControlSurface maschineControlSurface)
    {
        this.surface = maschineControlSurface;
    }
}