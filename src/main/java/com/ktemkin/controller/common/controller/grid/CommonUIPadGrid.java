package com.ktemkin.controller.common.controller.grid;

import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.grid.BlinkingPadGrid;
import de.mossgrabers.framework.daw.midi.IMidiOutput;

public class CommonUIPadGrid extends BlinkingPadGrid
{
    public CommonUIPadGrid(ColorManager colorManager, IMidiOutput output, int rows, int cols, int startNote)
    {
        super(colorManager, output, rows, cols, startNote);
    }

}
