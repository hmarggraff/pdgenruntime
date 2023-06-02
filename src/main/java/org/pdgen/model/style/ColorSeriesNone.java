package org.pdgen.model.style;

import java.awt.*;

public class ColorSeriesNone extends ColorSeriesSingle {
    private static final long serialVersionUID = 7L;

    public static ColorSeries instance = new ColorSeriesNone();

    private ColorSeriesNone() {
        super(Color.WHITE);
    }
}
