// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;
//MARKER The strings in this file shall not be translated

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class ColorSeriesHue implements ColorSeries {
    static final int slices = 24;
    private static final long serialVersionUID = 7L;

    public Color getColorAt(int i, int max) {
        return HSVtoRGB(i * 360 / max, 0.8f, 230f);
    }

    /**
     * Converts a value from HSV to RGB colorspace.
     * R,G,B all (0..255), H (0..360) (or -1 when S == 0), S (0..1), V (0..255)
     *
     * @param h hue
     * @param s stauration
     * @param v value
     * @return the color value
     */
    public static Color HSVtoRGB(float h, float s, float v) {
        float[] rgb = new float[3];
        if (s == 0.0) {
            if (h == -1.0) {
//				System.out.println("(" + h + "," + s + "," + hsv[2] + ") is a valid hsv value");
                rgb[0] = v;
                rgb[1] = v;
                rgb[2] = v;
            } else {
//				System.out.println("invalid HSV values are: (" + h + "," + s + "," + v + ")");
                return null;
            }
        } else {
            if (h == 360.0)
                h = 0;
            h /= 60.0;
            int i = (int) Math.floor(h);
            float f = h - (float) i;
            float p = (float) (v * (1.0 - s));
            float q = (float) (v * (1.0 - (s * f)));
            float t = (float) (v * (1.0 - (s * (1.0 - f))));
            switch (i) {
                case 0:
                    rgb[0] = v;
                    rgb[1] = t;
                    rgb[2] = p;
                    break;
                case 1:
                    rgb[0] = q;
                    rgb[1] = v;
                    rgb[2] = p;
                    break;
                case 2:
                    rgb[0] = p;
                    rgb[1] = v;
                    rgb[2] = t;
                    break;
                case 3:
                    rgb[0] = p;
                    rgb[1] = q;
                    rgb[2] = v;
                    break;
                case 4:
                    rgb[0] = t;
                    rgb[1] = p;
                    rgb[2] = v;
                    break;
                case 5:
                    rgb[0] = v;
                    rgb[1] = p;
                    rgb[2] = q;
                    break;
            }
        }
        return new Color(rgb[0] / 255, rgb[1] / 255, rgb[2] / 255);
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        final Rectangle bounds = g2.getClipBounds();
        final float width = bounds.width;
        float step = width / slices;
        float at = 0;
        for (int i = 0; i < slices; i++) {
            Color c = getColorAt(i, slices);
			/*
			Paint pp = new GradientPaint(at,0,darkColors[i],at+step/2,0,colors[i]);
			g2.setPaint(pp);
			*/
            g2.setColor(c);
            g2.fill(new Rectangle2D.Float(at + 2, 0, step - 2, 64));
            at += step;
        }
    }

    public Color getColorAt(int i, Color[] colors, int count) {
        return getColorAt(i, count);
    }

    public Color getDefaultColor() {
        return getColorAt(0, 1);
    }
}
