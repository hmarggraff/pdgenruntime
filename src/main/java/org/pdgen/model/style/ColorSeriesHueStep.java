// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class ColorSeriesHueStep extends ColorSeriesHue {
    private static final long serialVersionUID = 7L;
    final int steps;

    public ColorSeriesHueStep(int steps) {
        this.steps = steps;
    }

    public Color getColorAt(int i, int max) {
        if (max < steps)
            max = steps;
        int off = max / steps * (i % steps);
        int mod = i / steps;
        return HSVtoRGB((off + mod) * 360 / max, 0.8f, 220f);
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Rectangle bounds = g2.getClipBounds();
        final float width = bounds.width;
        final int height = bounds.height;
        float step = width / slices;
        float at = 0;
        for (int i = 0; i < slices; i++) {
            g2.setColor(getColorAt(i, slices));
            g2.fill(new Rectangle2D.Float(at + 2, 0, step - 2, height));
            at += step;
        }
    }

    public int getSteps() {
        return steps;
    }
}
